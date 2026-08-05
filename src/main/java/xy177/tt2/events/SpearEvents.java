package xy177.tt2.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;
import xy177.tt2.TT2;
import xy177.tt2.config.TT2Config;
import xy177.tt2.init.TT2Items;
import xy177.tt2.init.TT2Sounds;
import xy177.tt2.logic.SpearAttackTiming;
import xy177.tt2.logic.SpearChargeStats;
import xy177.tt2.modifiers.ModSpearLunge;
import xy177.tt2.network.PacketSpearAnimation;
import xy177.tt2.network.TT2Network;
import xy177.tt2.tools.Spear;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SpearEvents {

    private static final UUID CHARGE_DAMAGE_UUID = UUID.fromString("7f535a31-4d1f-4778-a20b-395aa09c743a");
    private static final UUID CHARGE_DAMAGE_PROBE_UUID = UUID.fromString("e36f1328-d347-4b53-b97c-897f70d3a805");
    private static final Map<UUID, Long> LAST_STAB_TICK = new HashMap<>();
    private static final Map<UUID, Integer> STAB_ANIMATION_SEQUENCES = new HashMap<>();
    private static final Map<UUID, ChargeState> CHARGE_STATES = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAttackEntity(AttackEntityEvent event) {
        if (isMainhandSpear(event.getEntityPlayer())) {
            requestClientStab(event.getEntityPlayer(), false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlaySoundAtEntity(PlaySoundAtEntityEvent event) {
        if (Spear.isCustomAttackInProgress() && isLegacyPlayerAttackSound(event.getSound())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!isMainhandSpear(event.getEntityPlayer())) {
            return;
        }
        requestClientStab(event.getEntityPlayer(), false);
        event.setCanceled(true);
        event.setUseBlock(Event.Result.DENY);
        event.setUseItem(Event.Result.DENY);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (isMainhandSpear(event.getEntityPlayer())) {
            requestClientStab(event.getEntityPlayer(), true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBreakBlock(BlockEvent.BreakEvent event) {
        if (isMainhandSpear(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote) {
            return;
        }
        ItemStack active = event.player.getActiveItemStack();
        if (!event.player.isHandActive() || !isSpear(active)) {
            CHARGE_STATES.remove(event.player.getUniqueID());
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.player.getUniqueID();
        LAST_STAB_TICK.remove(id);
        STAB_ANIMATION_SEQUENCES.remove(id);
        CHARGE_STATES.remove(id);
    }

    public static void handleStabRequest(EntityPlayerMP player, int sequence) {
        ItemStack stack = player.getHeldItemMainhand();
        if (!TT2Config.enableSpear || player.isHandActive() || !isSpear(stack)
            || ToolHelper.isBroken(stack)) {
            return;
        }
        long tick = player.world.getTotalWorldTime();
        Long previous = LAST_STAB_TICK.get(player.getUniqueID());
        if (previous != null && previous == tick) {
            return;
        }
        LAST_STAB_TICK.put(player.getUniqueID(), tick);

        if (player.getCooledAttackStrength(0.5F) + 1.0E-5F
            < TT2Config.spearStabMinimumAttackRecharge) {
            player.resetCooldown();
            return;
        }

        int durationTicks = SpearAttackTiming.animationDurationTicks(player);
        boolean hit = performStab(player, stack);
        if (hit) {
            playSpearSound(player, TT2Sounds.spearHit(stack));
        }
        playSpearSound(player, TT2Sounds.spearAttack(stack));
        int animationSequence = STAB_ANIMATION_SEQUENCES.compute(
            player.getUniqueID(), (id, previousSequence) -> nextSequence(previousSequence)
        );
        PacketSpearAnimation animation = new PacketSpearAnimation(
            player.getEntityId(), EnumHand.MAIN_HAND, sequence, animationSequence, durationTicks
        );
        TT2Network.CHANNEL.sendTo(animation, player);
        TT2Network.CHANNEL.sendToAllTracking(
            new PacketSpearAnimation(player.getEntityId(), EnumHand.MAIN_HAND,
                sequence, animationSequence, durationTicks),
            player
        );
    }

    private static void requestClientStab(EntityPlayer player, boolean cooldownAlreadyReset) {
        if (player.world.isRemote) {
            TT2.proxy.requestSpearStab(player, cooldownAlreadyReset);
        }
    }

    private static int nextSequence(Integer previousSequence) {
        int sequence = previousSequence == null ? 1 : previousSequence + 1;
        return sequence == 0 ? 1 : sequence;
    }

    public static void onSpearUsingTick(EntityLivingBase living, ItemStack stack) {
        if (!(living instanceof EntityPlayer) || living.world.isRemote || !isSpear(stack)
            || ToolHelper.isBroken(stack)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) living;
        if (!player.isHandActive() || player.getActiveItemStack() != stack) {
            return;
        }

        EnumHand hand = player.getActiveHand();
        long now = player.world.getTotalWorldTime();
        ChargeState state = CHARGE_STATES.get(player.getUniqueID());
        if (state == null || state.hand != hand) {
            state = new ChargeState(hand, now, player);
            CHARGE_STATES.put(player.getUniqueID(), state);
        }
        performChargeTick(player, stack, state, now);
    }

    public static void onSpearStoppedUsing(EntityLivingBase living) {
        if (living instanceof EntityPlayer && !living.world.isRemote) {
            CHARGE_STATES.remove(living.getUniqueID());
        }
    }

    private static boolean performStab(EntityPlayer player, ItemStack stack) {
        List<SpearTarget> targets = findTargets(
            player,
            TT2Config.spearStabMinReach,
            stabMaxReach(player),
            TT2Config.spearStabAimTolerance,
            0.0,
            0
        );

        int hitTargets = 0;
        for (SpearTarget spearTarget : targets) {
            Entity target = spearTarget.entity;
            if (TT2Config.spearStabMaxTargets > 0 && hitTargets >= TT2Config.spearStabMaxTargets) {
                break;
            }
            boolean hit = performTinkerAttack(
                player,
                stack,
                target,
                spearTarget.damageTarget,
                TT2Config.spearStabDurabilityPerTarget,
                TT2Config.spearStabExhaustionPerTarget,
                null,
                false
            );
            if (hit && TT2Config.spearStabBasePushback > 0.0) {
                applyPushback(player, target, TT2Config.spearStabBasePushback);
            }
            if (hit) {
                hitTargets++;
            }
            if (ToolHelper.isBroken(stack)) {
                break;
            }
        }

        player.resetCooldown();
        ModSpearLunge.applyLunge(player, stack);
        return hitTargets > 0;
    }

    private static void performChargeTick(EntityPlayer player, ItemStack stack, ChargeState state, long now) {
        SpearChargeStats stats = SpearChargeStats.resolve(stack);
        Vec3d look = player.getLookVec().normalize();
        double wielderFacingSpeed = state.sampleWielderForwardSpeed(player, look, now);
        long elapsedAfterDelay = now - state.startedAt - stats.braceDelayTicks;
        if (elapsedAfterDelay < 0L) {
            return;
        }

        boolean canKnockOffMountNow = elapsedAfterDelay <= stats.knockOffMountDurationTicks;
        boolean canPushBackNow = elapsedAfterDelay <= stats.pushbackDurationTicks;
        boolean canDealDamageNow = elapsedAfterDelay <= stats.damageDurationTicks;
        if (!canKnockOffMountNow && !canPushBackNow && !canDealDamageNow) {
            return;
        }

        double forwardExtension = Math.max(0.0, wielderFacingSpeed / 20.0);
        List<SpearTarget> targets = findTargets(
            player,
            TT2Config.spearStabMinReach,
            stabMaxReach(player),
            TT2Config.spearStabAimTolerance,
            forwardExtension,
            0
        );

        boolean affectedAnyTarget = false;
        for (SpearTarget spearTarget : targets) {
            Entity target = spearTarget.entity;
            UUID targetId = target.getUniqueID();
            double targetFacingSpeed = state.sampleTargetForwardSpeed(target, look, now);
            Long lastContact = state.contacts.get(targetId);
            if (lastContact != null
                && now - lastContact < TT2Config.spearChargeSameTargetDelayTicks) {
                continue;
            }

            double closingSpeed = Math.max(0.0, wielderFacingSpeed - targetFacingSpeed);
            boolean canDamage = canDealDamageNow
                && closingSpeed >= TT2Config.spearChargeMinimumClosingSpeedForDamage;
            boolean canPushBack = canPushBackNow
                && wielderFacingSpeed >= TT2Config.spearChargeMinimumWielderSpeedForPushback;
            boolean canKnockOffMount = TT2Config.spearChargeCanKnockTargetsOffMounts
                && canKnockOffMountNow
                && wielderFacingSpeed >= stats.knockOffMountSpeed
                && target.isRiding();
            boolean willPushBack = canPushBack && TT2Config.spearChargeBasePushback > 0.0;
            boolean canAffect = canDamage || canKnockOffMount || willPushBack;

            if (canAffect
                && TT2Config.spearChargeMaxTargets > 0
                && !state.affectedTargets.contains(targetId)
                && state.affectedTargets.size() >= TT2Config.spearChargeMaxTargets) {
                continue;
            }

            if (TT2Config.spearChargeBrieflyIgnoreSlowTouches || canAffect) {
                state.contacts.put(targetId, now);
            }

            boolean damaged = false;
            if (canDamage) {
                double chargeDamage = chargeDamage(stack, closingSpeed, stats.damageMultiplier);
                damaged = performTinkerAttack(
                    player,
                    stack,
                    target,
                    spearTarget.damageTarget,
                    TT2Config.spearChargeDurabilityCost,
                    TT2Config.spearChargeExhaustion,
                    chargeDamage,
                    true
                );
            }
            if (canKnockOffMount) {
                target.dismountRidingEntity();
            }
            if (willPushBack) {
                applyPushback(player, target, TT2Config.spearChargeBasePushback);
            }
            if ((canKnockOffMount || willPushBack) && !damaged) {
                consumeChargeContact(player, stack, target);
            }
            if (damaged || canKnockOffMount || willPushBack) {
                state.affectedTargets.add(targetId);
                affectedAnyTarget = true;
            }
            if (ToolHelper.isBroken(stack)) {
                player.stopActiveHand();
                break;
            }
        }
        if (affectedAnyTarget) {
            playSpearSound(player, TT2Sounds.spearHit(stack));
        }
    }

    private static double chargeDamage(ItemStack stack, double closingSpeed, double multiplier) {
        double damage = TT2Config.spearChargeBaseDamage + Math.floor(closingSpeed * multiplier);
        try {
            ToolNBT original = TagUtil.getOriginalToolStats(stack);
            double originalAttack = original.attack * ((Spear) stack.getItem()).damagePotential();
            damage += ToolHelper.getActualAttack(stack) - originalAttack;
        } catch (RuntimeException ignored) {
        }
        if (TT2Config.spearChargeDamageCap > 0.0) {
            damage = Math.min(damage, TT2Config.spearChargeDamageCap);
        }
        return Math.max(0.0, damage);
    }

    private static boolean performTinkerAttack(EntityPlayer player, ItemStack stack, Entity target,
                                                Entity damageTarget,
                                                int durabilityCost, double exhaustion, Double exactDamage,
                                                boolean forceFullCooldown) {
        IAttributeInstance damageAttribute = player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        AttributeModifier damageModifier = null;
        float oldFallDistance = player.fallDistance;
        boolean oldSprinting = player.isSprinting();
        int oldTicksSinceLastSwing = player.ticksSinceLastSwing;
        float exhaustionBefore = player.getFoodStats().foodExhaustionLevel;

        Spear.beginCustomAttack(
            durabilityCost,
            target,
            damageTarget == target ? null : damageTarget
        );
        boolean hit;
        boolean damageAttempted;
        try {
            if (exactDamage != null) {
                damageModifier = applyExactAttributeValue(damageAttribute, exactDamage);
            }
            if (forceFullCooldown) {
                player.ticksSinceLastSwing = 1000000;
            }

            player.fallDistance = 0.0F;
            player.setSprinting(false);
            ToolHelper.attackEntity(stack, (Spear) stack.getItem(), player, target, null, false);
        } finally {
            damageAttempted = Spear.wasCustomDamageAttempted();
            hit = Spear.endCustomAttack();
            if (damageModifier != null) {
                damageAttribute.removeModifier(damageModifier);
            }
            player.ticksSinceLastSwing = oldTicksSinceLastSwing;
            player.fallDistance = oldFallDistance;
            player.setSprinting(oldSprinting);
        }

        boolean handled = hit || (!damageAttempted && target instanceof EntityHanging);
        if (hit && target instanceof EntityLivingBase && !player.capabilities.isCreativeMode) {
            float after = player.getFoodStats().foodExhaustionLevel;
            float expectedAfterTinker = Math.min(40.0F, exhaustionBefore + 0.3F);
            float otherExhaustion = after - expectedAfterTinker;
            player.getFoodStats().foodExhaustionLevel = MathHelper.clamp(
                exhaustionBefore + (float) exhaustion + otherExhaustion,
                0.0F,
                40.0F
            );
        } else if (handled) {
            if (exhaustion > 0.0) {
                player.addExhaustion((float) exhaustion);
            }
        }
        return handled;
    }

    private static void consumeChargeContact(EntityPlayer player, ItemStack stack, Entity target) {
        if (target instanceof EntityLivingBase && !player.capabilities.isCreativeMode
            && TT2Config.spearChargeDurabilityCost > 0) {
            ToolHelper.damageTool(stack, TT2Config.spearChargeDurabilityCost, player);
        }
        if (TT2Config.spearChargeExhaustion > 0.0) {
            player.addExhaustion((float) TT2Config.spearChargeExhaustion);
        }
    }

    private static AttributeModifier applyExactAttributeValue(IAttributeInstance attribute, double desiredValue) {
        removeModifier(attribute, CHARGE_DAMAGE_UUID);
        removeModifier(attribute, CHARGE_DAMAGE_PROBE_UUID);

        double before = attribute.getAttributeValue();
        AttributeModifier probe = new AttributeModifier(
            CHARGE_DAMAGE_PROBE_UUID,
            "tt2 spear charge damage probe",
            1.0,
            0
        );
        attribute.applyModifier(probe);
        double slope = attribute.getAttributeValue() - before;
        attribute.removeModifier(probe);
        if (Math.abs(slope) < 1.0E-8) {
            slope = 1.0;
        }

        AttributeModifier modifier = new AttributeModifier(
            CHARGE_DAMAGE_UUID,
            "tt2 spear charge damage",
            (desiredValue - before) / slope,
            0
        );
        attribute.applyModifier(modifier);
        return modifier;
    }

    private static void removeModifier(IAttributeInstance attribute, UUID id) {
        AttributeModifier existing = attribute.getModifier(id);
        if (existing != null) {
            attribute.removeModifier(existing);
        }
    }

    private static List<SpearTarget> findTargets(EntityPlayer player, double minReach, double maxReach,
                                                 double margin, double forwardExtension, int maxTargets) {
        Map<UUID, SpearTarget> hitsByTarget = new HashMap<>();
        if (maxReach <= minReach || maxReach <= 0.0) {
            return new ArrayList<>();
        }

        World world = player.world;
        Vec3d eyes = player.getPositionEyes(1.0F);
        Vec3d look = player.getLookVec().normalize();
        Vec3d start = eyes.add(look.scale(Math.max(0.0, minReach)));
        Vec3d intendedEnd = eyes.add(look.scale(Math.max(0.0, maxReach + forwardExtension)));
        RayTraceResult blockHit = world.rayTraceBlocks(eyes, intendedEnd, false, true, false);
        Vec3d end = blockHit != null && blockHit.typeOfHit == RayTraceResult.Type.BLOCK
            ? blockHit.hitVec : intendedEnd;
        if (eyes.squareDistanceTo(end) <= eyes.squareDistanceTo(start)) {
            return new ArrayList<>();
        }

        AxisAlignedBB search = new AxisAlignedBB(start, end).grow(Math.max(0.0, margin) + 1.0);
        for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(player, search)) {
            if (!entity.isEntityAlive() || !entity.canBeCollidedWith()
                || !entity.canBeAttackedWithItem()) {
                continue;
            }

            Entity target = normalizeTarget(entity);
            if (target == player || !target.isEntityAlive() || !target.canBeAttackedWithItem()
                || target.isRidingOrBeingRiddenBy(player)) {
                continue;
            }
            if (target instanceof EntityPlayer) {
                EntityPlayer targetPlayer = (EntityPlayer) target;
                if (targetPlayer.isSpectator() || targetPlayer.capabilities.disableDamage
                    || !player.canAttackPlayer(targetPlayer)) {
                    continue;
                }
            }

            AxisAlignedBB bounds = entity.getEntityBoundingBox().grow(Math.max(0.0, margin));
            RayTraceResult intercept = bounds.calculateIntercept(start, end);
            Vec3d hitPosition;
            if (bounds.contains(start)) {
                hitPosition = start;
            } else if (intercept != null) {
                hitPosition = intercept.hitVec;
            } else {
                continue;
            }
            double distanceSq = start.squareDistanceTo(hitPosition);
            SpearTarget previous = hitsByTarget.get(target.getUniqueID());
            boolean hitPart = entity instanceof MultiPartEntityPart && entity != target;
            boolean previousHitPart = previous != null
                && previous.damageTarget instanceof MultiPartEntityPart
                && previous.damageTarget != previous.entity;
            if (previous == null || (hitPart && !previousHitPart)
                || (hitPart == previousHitPart && distanceSq < previous.distanceSq)) {
                hitsByTarget.put(target.getUniqueID(), new SpearTarget(target, entity, distanceSq));
            }
        }

        List<SpearTarget> hits = new ArrayList<>(hitsByTarget.values());
        hits.sort(Comparator.comparingDouble(hit -> hit.distanceSq));
        int limit = maxTargets <= 0 ? hits.size() : Math.min(maxTargets, hits.size());
        List<SpearTarget> result = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            result.add(hits.get(i));
        }
        return result;
    }

    private static Entity normalizeTarget(Entity entity) {
        if (entity instanceof MultiPartEntityPart) {
            Object parent = ((MultiPartEntityPart) entity).parent;
            if (parent instanceof Entity) {
                return (Entity) parent;
            }
        }
        return entity;
    }

    private static double stabMaxReach(EntityPlayer player) {
        return player.capabilities.isCreativeMode
            ? TT2Config.spearStabCreativeMaxReach
            : TT2Config.spearStabMaxReach;
    }

    private static void applyPushback(EntityPlayer attacker, Entity target, double strength) {
        float yaw = attacker.rotationYaw * 0.017453292F;
        if (target instanceof EntityLivingBase) {
            ((EntityLivingBase) target).knockBack(
                attacker,
                (float) strength,
                MathHelper.sin(yaw),
                -MathHelper.cos(yaw)
            );
        } else {
            target.addVelocity(
                -MathHelper.sin(yaw) * strength,
                0.1,
                MathHelper.cos(yaw) * strength
            );
        }
        target.velocityChanged = true;
    }

    private static boolean isMainhandSpear(EntityPlayer player) {
        return player != null && isSpear(player.getHeldItemMainhand());
    }

    private static boolean isSpear(ItemStack stack) {
        return !stack.isEmpty() && TT2Items.SPEAR != null && stack.getItem() == TT2Items.SPEAR;
    }

    private static void playSpearSound(EntityPlayer player, SoundEvent sound) {
        player.world.playSound(null, player.posX, player.posY, player.posZ,
            sound, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    private static boolean isLegacyPlayerAttackSound(SoundEvent sound) {
        return sound == SoundEvents.ENTITY_PLAYER_ATTACK_CRIT
            || sound == SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK
            || sound == SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE
            || sound == SoundEvents.ENTITY_PLAYER_ATTACK_STRONG
            || sound == SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP
            || sound == SoundEvents.ENTITY_PLAYER_ATTACK_WEAK;
    }

    private static final class SpearTarget {
        private final Entity entity;
        private final Entity damageTarget;
        private final double distanceSq;

        private SpearTarget(Entity entity, Entity damageTarget, double distanceSq) {
            this.entity = entity;
            this.damageTarget = damageTarget;
            this.distanceSq = distanceSq;
        }
    }

    private static final class ChargeState {
        private final EnumHand hand;
        private final long startedAt;
        private final Map<UUID, Long> contacts = new HashMap<>();
        private final Set<UUID> affectedTargets = new HashSet<>();
        private final MovementSample wielderMovement;
        private final Map<UUID, MovementSample> targetMovements = new HashMap<>();

        private ChargeState(EnumHand hand, long startedAt, Entity wielder) {
            this.hand = hand;
            this.startedAt = startedAt;
            this.wielderMovement = new MovementSample(wielder, startedAt);
        }

        private double sampleWielderForwardSpeed(Entity wielder, Vec3d look, long now) {
            wielderMovement.sample(wielder, now);
            return wielderMovement.forwardSpeed(look);
        }

        private double sampleTargetForwardSpeed(Entity target, Vec3d look, long now) {
            MovementSample movement = targetMovements.get(target.getUniqueID());
            if (movement == null) {
                movement = new MovementSample(target, now);
                targetMovements.put(target.getUniqueID(), movement);
            } else {
                movement.sample(target, now);
            }
            return movement.forwardSpeed(look);
        }
    }

    private static final class MovementSample {
        private double lastX;
        private double lastY;
        private double lastZ;
        private long lastTick;
        private double velocityX;
        private double velocityY;
        private double velocityZ;

        private MovementSample(Entity entity, long now) {
            lastX = entity.posX;
            lastY = entity.posY;
            lastZ = entity.posZ;
            lastTick = now;
            velocityX = (entity.posX - entity.prevPosX) * 20.0;
            velocityY = (entity.posY - entity.prevPosY) * 20.0;
            velocityZ = (entity.posZ - entity.prevPosZ) * 20.0;
        }

        private void sample(Entity entity, long now) {
            long elapsedTicks = now - lastTick;
            if (elapsedTicks <= 0L) {
                return;
            }
            double ticksPerSecond = 20.0 / elapsedTicks;
            velocityX = (entity.posX - lastX) * ticksPerSecond;
            velocityY = (entity.posY - lastY) * ticksPerSecond;
            velocityZ = (entity.posZ - lastZ) * ticksPerSecond;
            lastX = entity.posX;
            lastY = entity.posY;
            lastZ = entity.posZ;
            lastTick = now;
        }

        private double forwardSpeed(Vec3d look) {
            return velocityX * look.x + velocityY * look.y + velocityZ * look.z;
        }
    }
}
