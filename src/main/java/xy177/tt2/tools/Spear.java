package xy177.tt2.tools;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.materials.ExtraMaterialStats;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.materials.MaterialTypes;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.SwordCore;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.tools.TinkerTools;
import xy177.tt2.TT2;
import xy177.tt2.config.TT2Config;
import xy177.tt2.events.SpearEvents;
import xy177.tt2.init.TT2Sounds;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class Spear extends SwordCore {

    private static final ThreadLocal<CustomAttackContext> CUSTOM_ATTACK = new ThreadLocal<>();

    public Spear() {
        super(
            PartMaterialType.head(TinkerTools.arrowHead),
            PartMaterialType.handle(TinkerTools.toughToolRod),
            PartMaterialType.extra(TinkerTools.handGuard)
        );
        setTranslationKey("tt2.spear");
        addCategory(Category.WEAPON);
        addPropertyOverride(new ResourceLocation(TT2.MOD_ID, "in_hand"),
            (stack, world, entity) -> TT2.proxy.isRenderingSpearInHand() ? 1.0F : 0.0F);

    }

    @Override
    public float damagePotential() {
        return (float) TT2Config.spearDamageCoefficient;
    }

    @Override
    public double attackSpeed() {
        return TT2Config.spearAttackSpeed;
    }

    @Override
    protected ToolNBT buildTagData(List<Material> materials) {
        HeadMaterialStats head = materials.get(0).getStatsOrUnknown(MaterialTypes.HEAD);
        HandleMaterialStats handle = materials.get(1).getStatsOrUnknown(MaterialTypes.HANDLE);
        ExtraMaterialStats guard = materials.get(2).getStatsOrUnknown(MaterialTypes.EXTRA);

        ToolNBT data = new ToolNBT();
        data.head(head);
        data.extra(guard);
        data.handle(handle);
        data.modifiers = DEFAULT_MODIFIERS;
        return data;
    }

    @Override
    public int[] getRepairParts() {
        return new int[]{0};
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        return entityLiving instanceof EntityPlayer;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        return true;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
        return player.getHeldItemMainhand() == stack;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (ToolHelper.isBroken(stack)) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        if (!world.isRemote) {
            world.playSound(null, player.posX, player.posY, player.posZ,
                TT2Sounds.spearUse(stack), SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Nonnull
    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.NONE;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase living, int count) {
        SpearEvents.onSpearUsingTick(living, stack);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase living, int timeLeft) {
        SpearEvents.onSpearStoppedUsing(living);
    }

    @Override
    public boolean dealDamage(ItemStack stack, EntityLivingBase attacker, Entity target, float damage) {
        CustomAttackContext context = CUSTOM_ATTACK.get();
        boolean customTarget = context != null && target == context.attackTarget;
        if (customTarget) {
            context.damageAttempted = true;
        }
        Entity damageTarget = customTarget && context.damageTarget != null
            ? context.damageTarget : target;
        boolean hit = super.dealDamage(stack, attacker, damageTarget, damage);
        if (customTarget) {
            context.hit |= hit;
        }
        return hit;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        boolean result = super.hitEntity(stack, target, attacker);
        CustomAttackContext context = CUSTOM_ATTACK.get();
        if (context != null) {
            context.customDurabilityPending = target == context.attackTarget;
        }
        return result;
    }

    @Override
    public void reduceDurabilityOnHit(ItemStack stack, EntityPlayer player, float damage) {
        CustomAttackContext context = CUSTOM_ATTACK.get();
        if (context == null || !context.customDurabilityPending) {
            super.reduceDurabilityOnHit(stack, player, damage);
        } else if (context.durabilityCost > 0) {
            context.customDurabilityPending = false;
            ToolHelper.damageTool(stack, context.durabilityCost, player);
        } else {
            context.customDurabilityPending = false;
        }
    }

    public static void beginCustomAttack(int durabilityCost, Entity attackTarget,
                                         @Nullable Entity damageTarget) {
        CUSTOM_ATTACK.set(new CustomAttackContext(
            Math.max(0, durabilityCost),
            attackTarget,
            damageTarget
        ));
    }

    public static boolean endCustomAttack() {
        CustomAttackContext context = CUSTOM_ATTACK.get();
        CUSTOM_ATTACK.remove();
        return context != null && context.hit;
    }

    public static boolean wasCustomDamageAttempted() {
        CustomAttackContext context = CUSTOM_ATTACK.get();
        return context != null && context.damageAttempted;
    }

    public static boolean isCustomAttackInProgress() {
        return CUSTOM_ATTACK.get() != null;
    }

    private static final class CustomAttackContext {
        private final int durabilityCost;
        private final Entity attackTarget;
        private final Entity damageTarget;
        private boolean damageAttempted;
        private boolean customDurabilityPending;
        private boolean hit;

        private CustomAttackContext(int durabilityCost, Entity attackTarget,
                                    @Nullable Entity damageTarget) {
            this.durabilityCost = durabilityCost;
            this.attackTarget = attackTarget;
            this.damageTarget = damageTarget;
        }
    }
}
