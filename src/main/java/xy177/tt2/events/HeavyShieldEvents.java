package xy177.tt2.events;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xy177.tt2.config.TT2Config;
import xy177.tt2.potion.TT2Potions;
import xy177.tt2.tools.HeavyShield;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HeavyShieldEvents {

    private static final UUID BLOCKING_SPEED_UUID =
        UUID.fromString("b1e2f3a4-c5d6-7e8f-90a1-b2c3d4e5f601");

    private static final UUID IMBALANCE_SPEED_UUID =
        UUID.fromString("9a1b2c3d-4e5f-6070-8091-a2b3c4d5e6f7");

    private final Set<UUID> pendingBlock = new HashSet<>();
    private final Set<UUID> glowingImmunity = new HashSet<>();

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (player.world.isRemote) return;
        if (!isBlockingWithHeavyShield(player)) return;
        if (event.getSource().isUnblockable()) return;

        net.minecraft.entity.Entity attacker = event.getSource().getTrueSource();
        if (!isInBlockingArc(player, attacker, HeavyShield.BLOCK_HALF_ANGLE_DEG)) return;

        pendingBlock.add(player.getUniqueID());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase victim = event.getEntityLiving();

        if (victim instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) victim;
            UUID id = player.getUniqueID();

            if (pendingBlock.remove(id)) {
                if (!player.world.isRemote) {
                    player.addPotionEffect(new PotionEffect(
                        TT2Potions.OPPORTUNITY, HeavyShield.OPPORTUNITY_TICKS, 0, false, false
                    ));
                }
                event.setCanceled(true);
                return;
            }
        }

        if (event.isCanceled()) return;

        net.minecraft.entity.Entity attackerEntity = event.getSource().getTrueSource();
        if (attackerEntity instanceof EntityLivingBase) {
            EntityLivingBase attacker = (EntityLivingBase) attackerEntity;
            if (attacker.isPotionActive(TT2Potions.IMBALANCE)) {
                boolean isBoss = !attacker.isNonBoss();
                float reduction = isBoss
                    ? (float) TT2Config.imbalanceBossDamageReduction
                    : (float) TT2Config.imbalanceDamageReduction;
                event.setAmount(event.getAmount() * (1f - reduction));
            }
        }

        if (attackerEntity instanceof EntityPlayer) {
            EntityPlayer attacker = (EntityPlayer) attackerEntity;
            if (HeavyShield.isHeldByPlayer(attacker)
                    && !attacker.isPotionActive(TT2Potions.OPPORTUNITY)) {
                event.setAmount(event.getAmount() * (1f - TT2Config.plateShieldDamageReduction));
            }
        }

        if (victim.isPotionActive(TT2Potions.IMBALANCE)) {
            event.setAmount(event.getAmount() * (1f + (float) TT2Config.imbalanceDamageTakenIncrease));
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (event.player.world.isRemote) return;

        EntityPlayer player = event.player;
        IAttributeInstance speedAttr =
            player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (speedAttr == null) return;

        boolean isBlockingNow = isBlockingWithHeavyShield(player);
        boolean hasModifier = speedAttr.getModifier(BLOCKING_SPEED_UUID) != null;

        if (isBlockingNow && !hasModifier) {
            speedAttr.applyModifier(new AttributeModifier(
                BLOCKING_SPEED_UUID, "tt2_plate_blocking", 1.5, 1
            ));
        } else if (!isBlockingNow && hasModifier) {
            speedAttr.removeModifier(BLOCKING_SPEED_UUID);
        }
    }

    @SubscribeEvent
    public void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        if (isBlockingWithHeavyShield(player)) {
            player.motionY *= 0.5;
        }
    }

    @SubscribeEvent
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote) return;

        UUID id = entity.getUniqueID();
        boolean hasImbalance = entity.isPotionActive(TT2Potions.IMBALANCE);
        boolean hasImmunity = entity.isPotionActive(TT2Potions.IMBALANCE_IMMUNITY);
        boolean isBoss = !entity.isNonBoss();

        IAttributeInstance speedAttr =
            entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            boolean hasSpeedMod = speedAttr.getModifier(IMBALANCE_SPEED_UUID) != null;
            if (hasImbalance && !isBoss && !hasSpeedMod) {
                speedAttr.applyModifier(new AttributeModifier(
                    IMBALANCE_SPEED_UUID, "tt2_imbalance_speed",
                    -TT2Config.imbalanceSpeedReduction, 2
                ));
            } else if ((!hasImbalance || isBoss) && hasSpeedMod) {
                speedAttr.removeModifier(IMBALANCE_SPEED_UUID);
            }
        }

        boolean shouldGlow;
        switch (xy177.tt2.config.TT2Config.imbalanceGlowMode) {
            case 1:  shouldGlow = hasImbalance; break;
            case 2:  shouldGlow = hasImbalance || hasImmunity; break;
            default: shouldGlow = hasImmunity && !hasImbalance; break;
        }
        boolean wasImmunity = glowingImmunity.contains(id);
        if (shouldGlow && !wasImmunity) {
            glowingImmunity.add(id);
            entity.setGlowing(true);
        } else if (!shouldGlow && wasImmunity) {
            glowingImmunity.remove(id);
            entity.setGlowing(false);
        }
    }

    @SubscribeEvent
    public void onKnockback(LivingKnockBackEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!entity.isPotionActive(TT2Potions.IMBALANCE)) return;

        boolean isBoss = !entity.isNonBoss();
        float reduction = isBoss
            ? (float) TT2Config.imbalanceBossKnockbackReduction
            : (float) TT2Config.imbalanceKnockbackReduction;
        event.setStrength(event.getStrength() * (1f - reduction));
    }

    private boolean isBlockingWithHeavyShield(EntityPlayer player) {
        return player.isHandActive()
            && !player.getActiveItemStack().isEmpty()
            && player.getActiveItemStack().getItem() instanceof HeavyShield;
    }

    private boolean isInBlockingArc(EntityPlayer player,
                                    net.minecraft.entity.Entity attacker,
                                    double halfAngleDeg) {
        if (attacker == null) return true;
        double dx = attacker.posX - player.posX;
        double dz = attacker.posZ - player.posZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 0.01) return true;
        dx /= dist;
        dz /= dist;
        float yawRad = player.rotationYaw * ((float) Math.PI / 180.0f);
        double facingX = -Math.sin(yawRad);
        double facingZ = Math.cos(yawRad);
        return (facingX * dx + facingZ * dz) >= Math.cos(Math.toRadians(halfAngleDeg));
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        UUID id = event.player.getUniqueID();
        pendingBlock.remove(id);
        glowingImmunity.remove(id);
        IAttributeInstance speedAttr =
            event.player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (speedAttr != null && speedAttr.getModifier(BLOCKING_SPEED_UUID) != null) {
            speedAttr.removeModifier(BLOCKING_SPEED_UUID);
        }
    }
}
