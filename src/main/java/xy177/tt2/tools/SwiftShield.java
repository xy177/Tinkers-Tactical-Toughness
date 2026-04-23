package xy177.tt2.tools;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
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

public class SwiftShield extends TinkerToolCore {

    public static final float ATTACK_COEFF = 0.4f;
    public static final String TAG_PARRY_ACCUM = "parry_accum";
    public static final double BLOCK_HALF_ANGLE_DEG = 45.0;

    public SwiftShield() {
        super(
            PartMaterialType.head(TinkerTools.panHead),
            PartMaterialType.handle(TinkerTools.largePlate)
        );
        this.setTranslationKey("tt2.swift_shield");
        addCategory(Category.WEAPON);

        this.addPropertyOverride(new ResourceLocation("blocking"), new IItemPropertyGetter() {
            @Override
            @SideOnly(Side.CLIENT)
            public float apply(@Nonnull ItemStack stack, @Nullable World worldIn,
                               @Nullable EntityLivingBase entityIn) {
                return entityIn != null
                    && entityIn.isHandActive()
                    && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
            }
        });
    }

    @Override
    protected ToolNBT buildTagData(List<Material> materials) {
        ToolNBT data = new ToolNBT();

        HeadMaterialStats head = materials.get(0).getStatsOrUnknown(MaterialTypes.HEAD);
        HandleMaterialStats handle = materials.get(1).getStatsOrUnknown(MaterialTypes.HANDLE);

        data.head(head);
        data.handle(handle);

        data.modifiers = DEFAULT_MODIFIERS;
        return data;
    }

    @Override
    public float damagePotential() {
        return ATTACK_COEFF;
    }

    @Override
    public double attackSpeed() {
        return 1.2d;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
        preventSlowDown(entity, 1.0f);
        super.onUpdate(stack, world, entity, slot, isSelected);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.getCooldownTracker().hasCooldown(this)) {
            player.setActiveHand(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }

    @Nonnull
    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BLOCK;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean canDestroyBlockInCreative(World world, net.minecraft.util.math.BlockPos pos,
                                             ItemStack stack, EntityPlayer player) {
        return false;
    }

    public static float getParryAccum(ItemStack stack) {
        NBTTagCompound extra = TagUtil.getExtraTag(stack);
        return extra.getFloat(TAG_PARRY_ACCUM);
    }

    public static void setParryAccum(ItemStack stack, float value) {
        NBTTagCompound extra = TagUtil.getExtraTag(stack);
        extra.setFloat(TAG_PARRY_ACCUM, Math.max(0, value));
        TagUtil.setExtraTag(stack, extra);
    }

    public static float getParryMax(ItemStack stack) {
        net.minecraft.nbt.NBTTagList matList = TagUtil.getBaseMaterialsTagList(stack);
        if (matList == null || matList.tagCount() < 2) return 20f;

        Material headMat = TinkerRegistry.getMaterial(matList.getStringTagAt(0));
        Material handleMat = TinkerRegistry.getMaterial(matList.getStringTagAt(1));
        if (headMat == null || handleMat == null) return 20f;

        HeadMaterialStats headStats = headMat.getStats(MaterialTypes.HEAD);
        HandleMaterialStats handleStats = handleMat.getStats(MaterialTypes.HANDLE);
        if (headStats == null || handleStats == null) return 20f;

        return headStats.attack
            * handleStats.modifier
            * (float) (xy177.tt2.config.TT2Config.parryThresholdPercent / 100.0);
    }

    public static int getCooldownTicks(ItemStack stack) {
        net.minecraft.nbt.NBTTagList matList = TagUtil.getBaseMaterialsTagList(stack);
        if (matList == null || matList.tagCount() < 2) return 30;

        Material handleMat = TinkerRegistry.getMaterial(matList.getStringTagAt(1));
        if (handleMat == null) return 30;

        HandleMaterialStats handleStats = handleMat.getStats(MaterialTypes.HANDLE);
        if (handleStats == null || handleStats.modifier <= 0) return 30;

        int ticks = (int) Math.ceil(
            (5.0 / handleStats.modifier)
            * xy177.tt2.config.TT2Config.cooldownCoefficient
            * 20.0
        );
        return Math.max(30, ticks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world,
                               List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);

        if (stack.hasTagCompound()) {
            float parryAccum = getParryAccum(stack);
            float parryMax = getParryMax(stack);
            String label = I18n.format("tooltip.tt2.parry_value");
            tooltip.add(String.format("%s: %.1f / %.1f", label, parryAccum, parryMax));
        }
    }
}
