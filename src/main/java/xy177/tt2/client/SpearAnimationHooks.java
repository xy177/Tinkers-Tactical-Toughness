package xy177.tt2.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.tt2.config.TT2Config;
import xy177.tt2.init.TT2Items;

@SideOnly(Side.CLIENT)
public final class SpearAnimationHooks {

    private static final float DEG_TO_RAD = (float) Math.PI / 180.0F;

    private SpearAnimationHooks() {
    }

    public static boolean renderFirstPerson(RenderSpecificHandEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        AbstractClientPlayer player = mc.player;
        if (player == null || !isSpear(event.getItemStack())) {
            return false;
        }

        float attackTime = SpearAnimationController.attackTime(
            player, event.getHand(), event.getPartialTicks()
        );
        boolean using = isUsingSpear(player, event.getHand());

        EnumHandSide side = handSide(player, event.getHand());
        int inverse = side == EnumHandSide.RIGHT ? 1 : -1;
        GlStateManager.pushMatrix();
        try {
            float equipProgress = using ? 0.0F : event.getEquipProgress();
            GlStateManager.translate(
                inverse * 0.56F,
                -0.52F - equipProgress * 0.6F,
                -0.72F
            );
            if (using) {
                applyFirstPersonUse(player, event.getHand(), event.getPartialTicks(), inverse);
            } else if (attackTime >= 0.0F) {
                applyFirstPersonAttack(SpearAnimationMath.attack(attackTime), inverse);
            }
            SpearRenderContext.begin();
            try {
                mc.getItemRenderer().renderItemSide(
                    player,
                    event.getItemStack(),
                    side == EnumHandSide.RIGHT
                        ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
                        : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
                    side == EnumHandSide.LEFT
                );
            } finally {
                SpearRenderContext.end();
            }
        } finally {
            GlStateManager.popMatrix();
        }
        event.setCanceled(true);
        return true;
    }

    public static void applyThirdPersonPose(ModelBiped model, Entity entity, float ageInTicks) {
        if (!(entity instanceof EntityPlayer) || TT2Items.SPEAR == null) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        if (player.isHandActive() && !isSpear(player.getActiveItemStack())
            && player.getActiveItemStack().getItemUseAction() == EnumAction.BOW) {
            return;
        }
        float partialTicks = MathHelper.clamp(ageInTicks - entity.ticksExisted, 0.0F, 1.0F);
        float attackTime = SpearAnimationController.attackTime(
            player, EnumHand.MAIN_HAND, partialTicks
        );
        if (attackTime >= 0.0F) {
            applyThirdPersonAttackBody(model, player, attackTime);
        }
        applyThirdPersonArm(model, player, EnumHandSide.RIGHT, partialTicks);
        applyThirdPersonArm(model, player, EnumHandSide.LEFT, partialTicks);
    }

    public static void applyThirdPersonItem(EntityLivingBase living, ItemStack stack,
                                            EnumHandSide side, float partialTicks) {
        if (!isSpear(stack)) {
            return;
        }
        EnumHand hand = handForSide(living, side);
        int inverse = side == EnumHandSide.RIGHT ? 1 : -1;
        float attackTime = SpearAnimationController.attackTime(living, hand, partialTicks);
        SpearAnimationMath.AttackPose attack = attackTime >= 0.0F
            ? SpearAnimationMath.attack(attackTime)
            : SpearAnimationMath.attack(0.0F);
        float stabStrength = (float) TT2Config.spearStabAnimationStrength;

        if (attackTime >= 0.0F) {
            float movement = (attack.attack - attack.retract) * stabStrength;
            rotateAround(70.0F * movement, -1.0F, 0.0F, 0.0F,
                0.0F, -0.125F, 0.125F);
            GlStateManager.translate(0.0F, 0.38F * movement, 0.0F);
        }

        if (isUsingSpear(living, hand)) {
            float braceStrength = (float) TT2Config.spearBraceAnimationStrength;
            SpearAnimationMath.UsePose use = SpearAnimationMath.use(
                living.getActiveItemStack(),
                living.getItemInUseMaxCount() + partialTicks
            );
            GlStateManager.translate(0.0F, 0.0F,
                -0.38F * (use.raiseMovement - use.raiseBack) * braceStrength);
            rotateAround(
                70.0F * (use.raise - use.raiseBack) * braceStrength
                    - 40.0F * (attack.attack - attack.retract) * stabStrength,
                -1.0F, 0.0F, 0.0F,
                0.0F, -0.03125F, 0.125F
            );
            rotateAround(
                inverse * 90.0F * ((use.raise - use.sway) * braceStrength
                    + (3.0F * attack.retract + attack.attack) * stabStrength),
                0.0F, 1.0F, 0.0F,
                0.0F, 0.0F, 0.125F
            );
        }
    }

    private static void applyFirstPersonAttack(SpearAnimationMath.AttackPose attack, int inverse) {
        float strength = (float) TT2Config.spearStabAnimationStrength;
        GlStateManager.translate(
            inverse * 0.1F * (attack.prepare - attack.thrust) * strength,
            -0.075F * (attack.prepare - attack.retract) * strength,
            0.65F * (attack.prepare - attack.thrust) * strength
        );
        GlStateManager.rotate(-70.0F * (attack.prepare - attack.retract) * strength,
            1.0F, 0.0F, 0.0F);
        GlStateManager.translate(0.0F, 0.0F,
            -0.25F * (attack.retract - attack.thrust) * strength);
    }

    private static void applyFirstPersonUse(EntityLivingBase living, EnumHand hand,
                                            float partialTicks, int inverse) {
        SpearAnimationMath.UsePose use = SpearAnimationMath.use(
            living.getHeldItem(hand),
            living.getItemInUseMaxCount() + partialTicks
        );
        float braceStrength = (float) TT2Config.spearBraceAnimationStrength;
        float fatigueStrength = (float) TT2Config.spearFatigueAnimationStrength;
        GlStateManager.translate(
            inverse * ((0.15F * use.raise - 0.05F * use.raiseEnd
                - 0.1F * use.sway) * braceStrength + 0.005F * use.slow * fatigueStrength),
            (-0.075F * use.raise + 0.075F * use.raiseMiddle) * braceStrength
                + 0.01F * use.fast * fatigueStrength,
            (0.05F * use.raiseStart - 0.05F * use.raiseEnd) * braceStrength
                + 0.005F * use.slow * fatigueStrength
        );
        rotateAround(
            (-65.0F * SpearAnimationMath.inOutBack(use.raise)
                + 100.0F * use.raiseBack) * braceStrength
                + (-35.0F * use.lower - 0.5F * use.fast) * fatigueStrength,
            1.0F, 0.0F, 0.0F,
            0.0F, 0.1F, 0.0F
        );
        rotateAround(
            inverse * ((-90.0F * SpearAnimationMath.progress(use.raise, 0.5F, 0.55F)
                + 90.0F * use.sway) * braceStrength + 2.0F * use.slow * fatigueStrength),
            0.0F, -1.0F, 0.0F,
            inverse * 0.15F, 0.0F, 0.0F
        );
    }

    private static void applyThirdPersonArm(ModelBiped model, EntityPlayer player,
                                            EnumHandSide side, float partialTicks) {
        EnumHand hand = handForSide(player, side);
        ItemStack stack = player.getHeldItem(hand);
        if (!isSpear(stack)) {
            return;
        }

        int inverse = side == EnumHandSide.RIGHT ? 1 : -1;
        ModelRenderer arm = side == EnumHandSide.RIGHT
            ? model.bipedRightArm : model.bipedLeftArm;
        float armX = -((float) Math.PI / 2.0F) + model.bipedHead.rotateAngleX + 0.8F;
        if (player.isElytraFlying()) {
            armX -= 55.0F * DEG_TO_RAD;
        }
        arm.rotateAngleX = MathHelper.clamp(armX, -120.0F * DEG_TO_RAD, 30.0F * DEG_TO_RAD);
        arm.rotateAngleY = MathHelper.clamp(
            model.bipedHead.rotateAngleY - 0.1F * inverse,
            -60.0F * DEG_TO_RAD,
            60.0F * DEG_TO_RAD
        );

        if (isUsingSpear(player, hand)) {
            SpearAnimationMath.UsePose use = SpearAnimationMath.use(
                player.getActiveItemStack(),
                player.getItemInUseMaxCount() + partialTicks
            );
            float braceStrength = (float) TT2Config.spearBraceAnimationStrength;
            float fatigueStrength = (float) TT2Config.spearFatigueAnimationStrength;
            arm.rotateAngleY += -inverse * use.fast * use.intensity
                * fatigueStrength * DEG_TO_RAD;
            arm.rotateAngleZ += -0.5F * inverse * use.slow * use.intensity
                * fatigueStrength * DEG_TO_RAD;
            arm.rotateAngleX += ((-40.0F * use.raiseStart + 30.0F * use.raiseMiddle
                - 20.0F * use.raiseEnd + 10.0F * use.raiseBack) * braceStrength
                + (20.0F * use.lower + 0.6F * use.slow * use.intensity)
                * fatigueStrength) * DEG_TO_RAD;
        }

        float attackTime = SpearAnimationController.attackTime(player, hand, partialTicks);
        if (attackTime >= 0.0F) {
            SpearAnimationMath.AttackPose attack = SpearAnimationMath.attack(attackTime);
            arm.rotateAngleX += (90.0F * attack.prepare - 120.0F * attack.attack
                + 30.0F * attack.retract) * TT2Config.spearStabAnimationStrength * DEG_TO_RAD;
        }
    }

    private static void applyThirdPersonAttackBody(ModelBiped model, EntityPlayer player,
                                                   float attackTime) {
        float bodyYaw = MathHelper.sin(MathHelper.sqrt(attackTime)
            * ((float) Math.PI * 2.0F)) * 0.2F
            * (float) TT2Config.spearStabAnimationStrength;
        if (player.getPrimaryHand() == EnumHandSide.LEFT) {
            bodyYaw *= -1.0F;
        }
        model.bipedBody.rotateAngleY = bodyYaw;
        model.bipedRightArm.rotationPointZ = MathHelper.sin(bodyYaw) * 5.0F;
        model.bipedRightArm.rotationPointX = -MathHelper.cos(bodyYaw) * 5.0F;
        model.bipedLeftArm.rotationPointZ = -MathHelper.sin(bodyYaw) * 5.0F;
        model.bipedLeftArm.rotationPointX = MathHelper.cos(bodyYaw) * 5.0F;
    }

    private static void rotateAround(float angle, float axisX, float axisY, float axisZ,
                                     float pivotX, float pivotY, float pivotZ) {
        GlStateManager.translate(pivotX, pivotY, pivotZ);
        GlStateManager.rotate(angle, axisX, axisY, axisZ);
        GlStateManager.translate(-pivotX, -pivotY, -pivotZ);
    }

    private static boolean isUsingSpear(EntityLivingBase living, EnumHand hand) {
        return living.isHandActive() && living.getActiveHand() == hand
            && isSpear(living.getActiveItemStack());
    }

    private static EnumHandSide handSide(EntityLivingBase living, EnumHand hand) {
        return hand == EnumHand.MAIN_HAND
            ? living.getPrimaryHand() : living.getPrimaryHand().opposite();
    }

    private static EnumHand handForSide(EntityLivingBase living, EnumHandSide side) {
        return living.getPrimaryHand() == side ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
    }

    public static boolean isSpear(ItemStack stack) {
        return !stack.isEmpty() && TT2Items.SPEAR != null && stack.getItem() == TT2Items.SPEAR;
    }
}
