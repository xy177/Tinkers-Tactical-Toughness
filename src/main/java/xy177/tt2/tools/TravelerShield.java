package xy177.tt2.tools;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.materials.MaterialTypes;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.TinkerToolCore;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.tools.TinkerTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 旅行者盾牌 (Traveler's Shield)
 *
 * 部件：
 *   index 0 → 盘头（panHead）  → HeadMaterialStats（提供攻击力、耐久）
 *   index 1 → 大板（largePlate）→ HandleMaterialStats（提供耐久倍率）
 *
 * 特性：
 *   · 格挡角度 90°（前方 ±45°）
 *   · 举盾时移速不降低
 *   · 攻击力系数 0.4
 *   · 精准格挡（举盾后1.5秒内受击）→ 反击 + 铁砧音效
 *   · 普通格挡 → 累积招架值，满后进入破盾冷却
 *
 * 所有格挡/反击逻辑在 ShieldEvents 中处理。
 */
public class TravelerShield extends TinkerToolCore {

    // 攻击力系数（最终攻击 = 盘头攻击 * ATTACK_COEFF）
    public static final float ATTACK_COEFF = 0.4f;

    // Extra NBT 中储存招架累计值的 key
    public static final String TAG_PARRY_ACCUM = "parry_accum";

    // 格挡半角（度），总格挡弧度 = 2 * BLOCK_HALF_ANGLE_DEG = 90°
    public static final double BLOCK_HALF_ANGLE_DEG = 45.0;

    public TravelerShield() {
        super(
            PartMaterialType.head(TinkerTools.panHead),       // index 0：盘头，顶端
            PartMaterialType.handle(TinkerTools.largePlate)   // index 1：大板，手柄
        );
        this.setTranslationKey("tt2.traveler_shield");
        addCategory(Category.WEAPON);
    }

    // -----------------------------------------------------------------------
    // NBT 构建
    // -----------------------------------------------------------------------

    @Override
    protected ToolNBT buildTagData(List<Material> materials) {
        ToolNBT data = new ToolNBT();

        HeadMaterialStats head     = materials.get(0).getStatsOrUnknown(MaterialTypes.HEAD);
        HandleMaterialStats handle = materials.get(1).getStatsOrUnknown(MaterialTypes.HANDLE);

        // 先按 head 初始化基础属性（耐久、攻击、速度、采集等级）
        data.head(head);
        // 再按 handle 修正耐久
        data.handle(handle);

        // 应用攻击系数（盾牌攻击力降低）
        data.attack *= ATTACK_COEFF;

        data.modifiers = DEFAULT_MODIFIERS;
        return data;
    }

    // -----------------------------------------------------------------------
    // 招架值工具方法（供 ShieldEvents 使用）
    // -----------------------------------------------------------------------

    /**
     * 读取当前招架累计值
     */
    public static float getParryAccum(ItemStack stack) {
        NBTTagCompound extra = TagUtil.getExtraTag(stack);
        return extra.getFloat(TAG_PARRY_ACCUM);
    }

    /**
     * 写入招架累计值
     */
    public static void setParryAccum(ItemStack stack, float value) {
        NBTTagCompound extra = TagUtil.getExtraTag(stack);
        extra.setFloat(TAG_PARRY_ACCUM, Math.max(0, value));
        TagUtil.setExtraTag(stack, extra);
    }

    /**
     * 计算当前盾牌的招架值上限。
     * 公式：盘顶端攻击(原始) × 大板手柄耐久倍率 × (parryThresholdPercent / 100)
     * 注意：这里取材料的原始攻击值（不乘以ATTACK_COEFF），上限是基于材料属性的独立设计。
     */
    public static float getParryMax(ItemStack stack) {
        net.minecraft.nbt.NBTTagList matList = TagUtil.getBaseMaterialsTagList(stack);
        if (matList == null || matList.tagCount() < 2) return 20f;

        Material headMat   = TinkerRegistry.getMaterial(matList.getStringTagAt(0));
        Material handleMat = TinkerRegistry.getMaterial(matList.getStringTagAt(1));
        if (headMat == null || handleMat == null) return 20f;

        HeadMaterialStats headStats     = headMat.getStats(MaterialTypes.HEAD);
        HandleMaterialStats handleStats = handleMat.getStats(MaterialTypes.HANDLE);
        if (headStats == null || handleStats == null) return 20f;

        return headStats.attack
            * handleStats.modifier
            * (float)(xy177.tt2.config.TT2Config.parryThresholdPercent / 100.0);
    }

    /**
     * 计算破盾冷却tick数。
     * 公式：ceil(5 / (盘材料耐久 / 200) × cooldownCoefficient × 20)
     * 盘材料耐久越高 → 冷却越短（材料越好，回复越快）
     */
    public static int getCooldownTicks(ItemStack stack) {
        net.minecraft.nbt.NBTTagList matList = TagUtil.getBaseMaterialsTagList(stack);
        if (matList == null || matList.tagCount() < 1) return 100;

        Material headMat = TinkerRegistry.getMaterial(matList.getStringTagAt(0));
        if (headMat == null) return 100;

        HeadMaterialStats headStats = headMat.getStats(MaterialTypes.HEAD);
        if (headStats == null || headStats.durability <= 0) return 100;

        double durabCoeff = headStats.durability / 200.0;
        int ticks = (int) Math.ceil(
            5.0 / durabCoeff
            * xy177.tt2.config.TT2Config.cooldownCoefficient
            * 20.0
        );
        return Math.max(20, ticks);
    }

    // -----------------------------------------------------------------------
    // 物品行为
    // -----------------------------------------------------------------------

    /** 举盾时不降低移速（通过ToolCore的preventSlowDown代理实现） */
    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
        // 1.0f = 保持正常移速（FryPan用0.7f，默认盾牌用0.2f）
        preventSlowDown(entity, 1.0f);
        super.onUpdate(stack, world, entity, slot, isSelected);
    }

    /** 右键举盾 */
    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        // 冷却中无法举盾
        if (player.getCooldownTracker().hasCooldown(this)) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    /** 使用动画：格挡 */
    @Nonnull
    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BLOCK;
    }

    /** 格挡持续时间（足够长，手动松开才结束） */
    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    /** damagePotential：系数已在buildTagData中应用，此处返回0不再叠加 */
    @Override
    public float damagePotential() {
        return 0.0f;
    }

    /** 攻击速度（参考FryPan，盾牌攻击稍慢） */
    @Override
    public double attackSpeed() {
        return 1.2d;
    }

    // -----------------------------------------------------------------------
    // Tooltip
    // -----------------------------------------------------------------------

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world,
                               List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);

        if (stack.hasTagCompound()) {
            float parryAccum = getParryAccum(stack);
            float parryMax   = getParryMax(stack);

            // "招架值"支持多语言翻译，键名 tooltip.tt2.parry_value
            String label = I18n.format("tooltip.tt2.parry_value");
            tooltip.add(String.format("%s: %.1f / %.1f", label, parryAccum, parryMax));
        }
    }
}
