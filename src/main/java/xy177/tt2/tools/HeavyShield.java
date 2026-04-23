package xy177.tt2.tools;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.ExtraMaterialStats;
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
import xy177.tt2.config.TT2Config;
import xy177.tt2.potion.TT2Potions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class HeavyShield extends TinkerToolCore {

    public static final float ATTACK_COEFF = 0.65f;
    public static final double BLOCK_HALF_ANGLE_DEG = 72.5;
    public static final int OPPORTUNITY_TICKS = 100;

    public HeavyShield() {
        super(
            PartMaterialType.head(TinkerTools.signHead),
            PartMaterialType.extra(TinkerTools.largePlate),
            PartMaterialType.handle(TinkerTools.toughToolRod)
        );
        this.setTranslationKey("tt2.heavy_shield");
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
        ExtraMaterialStats extra = materials.get(1).getStatsOrUnknown(MaterialTypes.EXTRA);
        HandleMaterialStats handle = materials.get(2).getStatsOrUnknown(MaterialTypes.HANDLE);

        data.head(head);
        data.extra(extra);
        data.handle(handle);

        data.modifiers = DEFAULT_MODIFIERS;
        return data;
    }

    @Override
    public int[] getRepairParts() {
        return new int[]{0};
    }

    @Override
    public float damagePotential() {
        return ATTACK_COEFF;
    }

    @Override
    public double attackSpeed() {
        return 1.0d;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
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

    @Override
    public boolean dealDamage(ItemStack stack, EntityLivingBase attacker, Entity target, float damage) {
        boolean hit = super.dealDamage(stack, attacker, target, damage);

        if (hit && !attacker.world.isRemote && target instanceof EntityLivingBase) {
            EntityLivingBase targetLiving = (EntityLivingBase) target;

            if (!targetLiving.isPotionActive(TT2Potions.IMBALANCE_IMMUNITY)) {
                int imbalanceTicks = (int) (damage * TT2Config.imbalanceDurationMultiplier * 20.0);
                if (imbalanceTicks > 0) {
                    targetLiving.addPotionEffect(new PotionEffect(
                        TT2Potions.IMBALANCE, imbalanceTicks, 0, false, false
                    ));

                    int immunityTicks = calculateImmunityTicks(stack, imbalanceTicks);
                    targetLiving.addPotionEffect(new PotionEffect(
                        TT2Potions.IMBALANCE_IMMUNITY, immunityTicks, 0, false, false
                    ));
                }
            }
        }

        return hit;
    }

    public static int calculateImmunityTicks(ItemStack shieldStack, int imbalanceTicks) {
        NBTTagList matList = TagUtil.getBaseMaterialsTagList(shieldStack);
        if (matList == null || matList.tagCount() < 3) return imbalanceTicks * 3;

        Material handleMat = TinkerRegistry.getMaterial(matList.getStringTagAt(2));
        if (handleMat == null) return imbalanceTicks * 3;

        HandleMaterialStats handleStats = handleMat.getStats(MaterialTypes.HANDLE);
        if (handleStats == null || handleStats.modifier <= 0) return imbalanceTicks * 3;

        double multiplier = Math.max(3.0, 4.0 / handleStats.modifier);
        return (int) (imbalanceTicks * multiplier);
    }

    public static boolean isHeldByPlayer(EntityPlayer player) {
        return player.getHeldItemMainhand().getItem() instanceof HeavyShield
            || player.getHeldItemOffhand().getItem() instanceof HeavyShield;
    }
}
