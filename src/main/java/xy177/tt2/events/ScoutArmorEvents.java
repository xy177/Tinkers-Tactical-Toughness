package xy177.tt2.events;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.util.math.MathHelper;
import slimeknights.tconstruct.library.utils.ToolHelper;
import xy177.tt2.armor.ScoutArmorCore;
import xy177.tt2.config.TT2Config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ScoutArmorEvents {

    private final Map<UUID, Long> dodgedTicks = new HashMap<>();
    private final Set<UUID> retargeting = java.util.Collections.newSetFromMap(new HashMap<>());
    private final Map<UUID, Integer> airJumpsUsed = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().isProjectile()
                && event.getSource().getTrueSource() instanceof EntityPlayer) {
            EntityPlayer attacker = (EntityPlayer) event.getSource().getTrueSource();
            float bonusPercent = getRangedDamageBonusPercent(attacker);
            if (bonusPercent > 0f) {
                event.setAmount(event.getAmount() * (1f + bonusPercent / 100f));
            }
        }

        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }
        if (event.getSource().damageType.equals(DefenseDamageEvents.DODGED_DAMAGE_TAG)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        applyEnvironmentalReduction(player, event);
        float dodgeChance = getTotalDodgeChance(player);
        if (dodgeChance <= 0f) {
            return;
        }

        if (player.getRNG().nextFloat() <= dodgeChance) {
            if (event.getSource().getTrueSource() != null) {
                event.setCanceled(true);
                player.attackEntityFrom(
                    new EntityDamageSource(DefenseDamageEvents.DODGED_DAMAGE_TAG, event.getSource().getTrueSource()),
                    event.getAmount() * (float) TT2Config.scoutDodgeDamageFactor
                );
            } else {
                event.setAmount(event.getAmount() * (float) TT2Config.scoutDodgeDamageFactor);
            }
            dodgedTicks.put(player.getUniqueID(), player.world.getTotalWorldTime());
            player.world.playSound(
                null,
                player.posX,
                player.posY,
                player.posZ,
                SoundEvents.ENTITY_ENDERMEN_TELEPORT,
                player.getSoundCategory(),
                0.8f,
                1.0f
            );
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        float reduction = getTotalFallDamageReduction(player);
        if (reduction > 0f) {
            event.setDamageMultiplier(event.getDamageMultiplier() * (1f - reduction));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingKnockBack(LivingKnockBackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        Long dodgedTick = dodgedTicks.get(player.getUniqueID());
        if (dodgedTick != null && dodgedTick == player.world.getTotalWorldTime()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLivingSetAttackTarget(LivingSetAttackTargetEvent event) {
        if (!(event.getEntityLiving() instanceof EntityLiving)) {
            return;
        }
        if (!(event.getTarget() instanceof EntityPlayer)) {
            return;
        }

        EntityLiving aggressor = (EntityLiving) event.getEntityLiving();
        EntityPlayer scoutPlayer = (EntityPlayer) event.getTarget();
        if (retargeting.contains(aggressor.getUniqueID())) {
            return;
        }

        double range = getAggroRedirectRange(scoutPlayer);
        if (range <= 0) {
            return;
        }

        EntityLivingBase redirect = findRedirectTarget(aggressor, scoutPlayer, range);
        if (redirect == null || redirect == scoutPlayer) {
            return;
        }

        retargeting.add(aggressor.getUniqueID());
        try {
            aggressor.setAttackTarget(redirect);
            aggressor.setRevengeTarget(redirect);
        } finally {
            retargeting.remove(aggressor.getUniqueID());
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        int pieces = getScoutPieceCount(player);
        if (event.phase == TickEvent.Phase.END) {
            player.stepHeight = pieces >= 3 ? 2.1f : 0.6f;
        }

        if (player.world.isRemote) {
            return;
        }

        UUID id = player.getUniqueID();
        if (event.phase == TickEvent.Phase.END && (player.onGround || pieces < 2)) {
            airJumpsUsed.remove(id);
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        UUID id = event.player.getUniqueID();
        dodgedTicks.remove(id);
        airJumpsUsed.remove(id);
        retargeting.remove(id);
    }

    public boolean handleExtraJumpRequest(EntityPlayerMP player) {
        UUID id = player.getUniqueID();
        int used = airJumpsUsed.getOrDefault(id, 0);
        if (!canUseExtraJump(player, used)) {
            return false;
        }

        airJumpsUsed.put(id, used + 1);
        performExtraJump(player);
        return true;
    }

    public static int getScoutPieceCount(EntityPlayer player) {
        int pieces = 0;
        for (ItemStack stack : player.getArmorInventoryList()) {
            if (stack.getItem() instanceof ScoutArmorCore) {
                pieces++;
            }
        }
        return pieces;
    }

    public static int getMaxExtraJumps(EntityPlayer player) {
        int pieces = getScoutPieceCount(player);
        if (pieces >= 4) {
            return 2;
        }
        if (pieces >= 2) {
            return 1;
        }
        return 0;
    }

    public static double getAggroRedirectRange(EntityPlayer player) {
        double total = 0;
        int pieces = 0;
        for (ItemStack stack : player.getArmorInventoryList()) {
            if (stack.getItem() instanceof ScoutArmorCore) {
                total += ((ScoutArmorCore) stack.getItem()).getAggroRangeContribution();
                pieces++;
            }
        }
        if (pieces == 4) {
            total += 4.0;
        }
        return total;
    }

    public static boolean canUseExtraJump(EntityPlayer player, int usedJumps) {
        return usedJumps < getMaxExtraJumps(player)
            && !player.onGround
            && !player.capabilities.isFlying
            && !player.isInWater()
            && !player.isInLava()
            && !player.isOnLadder()
            && !player.isRiding();
    }

    public static void performExtraJump(EntityPlayer player) {
        player.motionY = 0.42D;
        PotionEffect jumpBoost = player.getActivePotionEffect(MobEffects.JUMP_BOOST);
        if (jumpBoost != null) {
            player.motionY += (jumpBoost.getAmplifier() + 1) * 0.1D;
        }

        if (player.isSprinting()) {
            float yaw = player.rotationYaw * 0.017453292F;
            player.motionX -= MathHelper.sin(yaw) * 0.2F;
            player.motionZ += MathHelper.cos(yaw) * 0.2F;
        }

        player.isAirBorne = true;
        player.fallDistance = 0.0F;
        player.velocityChanged = true;
    }

    private float getRangedDamageBonusPercent(EntityPlayer player) {
        float total = 0f;
        for (ItemStack stack : player.getArmorInventoryList()) {
            if (stack.getItem() instanceof ScoutArmorCore && !ToolHelper.isBroken(stack)) {
                total += ((ScoutArmorCore) stack.getItem()).getRangedDamageBonusPercent(
                    stack,
                    TT2Config.scoutRangedDamageCoefficient
                );
            }
        }
        return total;
    }

    private float getTotalDodgeChance(EntityPlayer player) {
        float total = 0f;
        for (ItemStack stack : player.getArmorInventoryList()) {
            if (stack.getItem() instanceof ScoutArmorCore && !ToolHelper.isBroken(stack)) {
                total += ((ScoutArmorCore) stack.getItem()).getDodgeChanceContribution(
                    stack,
                    TT2Config.scoutDodgeChanceCoefficient,
                    TT2Config.scoutDodgeChanceCap
                );
            }
        }
        return Math.min(total, (float) TT2Config.scoutDodgeChanceCap);
    }

    private float getTotalFallDamageReduction(EntityPlayer player) {
        float total = 0f;
        for (ItemStack stack : player.getArmorInventoryList()) {
            if (stack.getItem() instanceof ScoutArmorCore && !ToolHelper.isBroken(stack)) {
                total += ((ScoutArmorCore) stack.getItem()).getFallDamageReductionContribution();
            }
        }
        return Math.min(total, (float) TT2Config.scoutFallDamageReduction);
    }

    private float getTotalEnvironmentalDamageReduction(EntityPlayer player) {
        float total = 0f;
        for (ItemStack stack : player.getArmorInventoryList()) {
            if (stack.getItem() instanceof ScoutArmorCore && !ToolHelper.isBroken(stack)) {
                total += ((ScoutArmorCore) stack.getItem()).getEnvironmentalDamageReductionContribution();
            }
        }
        return Math.min(total, (float) TT2Config.scoutEnvironmentalDamageReduction);
    }

    private void applyEnvironmentalReduction(EntityPlayer player, LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (source == null || source.damageType.equals(DefenseDamageEvents.DODGED_DAMAGE_TAG)) {
            return;
        }
        if (!isEnvironmentalDamage(source)) {
            return;
        }

        float reduction = getTotalEnvironmentalDamageReduction(player);
        if (reduction > 0f) {
            event.setAmount(event.getAmount() * (1f - reduction));
        }
    }

    private boolean isEnvironmentalDamage(DamageSource source) {
        if (source == DamageSource.FALL) {
            return false;
        }

        switch (source.damageType) {
            case "inFire":
            case "onFire":
            case "lava":
            case "hotFloor":
            case "inWall":
            case "cramming":
            case "drown":
            case "cactus":
            case "magic":
            case "thorns":
            case "anvil":
            case "fallingBlock":
            case "flyIntoWall":
                return true;
            default:
                return false;
        }
    }

    private EntityLivingBase findRedirectTarget(EntityLiving aggressor, EntityPlayer scoutPlayer, double range) {
        boolean boss = !aggressor.isNonBoss();

        List<EntityLivingBase> players = scoutPlayer.world.getEntitiesWithinAABB(
            EntityPlayer.class,
            scoutPlayer.getEntityBoundingBox().grow(range, 8.0D, range),
            entity -> entity != scoutPlayer && entity.isEntityAlive()
        );
        EntityLivingBase redirect = getNearestTarget(scoutPlayer, players);
        if (redirect != null) {
            return redirect;
        }

        List<EntityLivingBase> owned = scoutPlayer.world.getEntitiesWithinAABB(
            EntityLivingBase.class,
            scoutPlayer.getEntityBoundingBox().grow(range, 8.0D, range),
            entity -> entity != scoutPlayer && entity.isEntityAlive() && isOwnedBy(entity, scoutPlayer)
        );

        if (!boss) {
            List<EntityLivingBase> iron = new ArrayList<>(scoutPlayer.world.getEntitiesWithinAABB(
                EntityIronGolem.class,
                scoutPlayer.getEntityBoundingBox().grow(range, 8.0D, range),
                EntityLivingBase::isEntityAlive
            ));
            redirect = getNearestTarget(scoutPlayer, iron);
            if (redirect != null) {
                return redirect;
            }

            List<EntityLivingBase> snow = new ArrayList<>(scoutPlayer.world.getEntitiesWithinAABB(
                EntitySnowman.class,
                scoutPlayer.getEntityBoundingBox().grow(range, 8.0D, range),
                EntityLivingBase::isEntityAlive
            ));
            redirect = getNearestTarget(scoutPlayer, snow);
            if (redirect != null) {
                return redirect;
            }

            List<EntityLivingBase> villagers = new ArrayList<>(scoutPlayer.world.getEntitiesWithinAABB(
                EntityVillager.class,
                scoutPlayer.getEntityBoundingBox().grow(range, 8.0D, range),
                EntityLivingBase::isEntityAlive
            ));
            redirect = getNearestTarget(scoutPlayer, villagers);
            if (redirect != null) {
                return redirect;
            }
        }

        return getNearestTarget(scoutPlayer, owned);
    }

    private EntityLivingBase getNearestTarget(EntityPlayer player, List<? extends EntityLivingBase> candidates) {
        return candidates.stream()
            .sorted(Comparator.comparingDouble(player::getDistanceSq))
            .findFirst()
            .orElse(null);
    }

    private boolean isOwnedBy(EntityLivingBase entity, EntityPlayer player) {
        if (entity instanceof EntityTameable) {
            EntityTameable tameable = (EntityTameable) entity;
            return tameable.isTamed() && player.getUniqueID().equals(tameable.getOwnerId());
        }
        if (entity instanceof IEntityOwnable) {
            IEntityOwnable ownable = (IEntityOwnable) entity;
            UUID ownerId = ownable.getOwnerId();
            return ownerId != null && ownerId.equals(player.getUniqueID());
        }
        return false;
    }
}
