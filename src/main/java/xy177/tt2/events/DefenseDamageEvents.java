package xy177.tt2.events;

import c4.conarm.common.ConstructsRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xy177.tt2.config.TT2Config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DefenseDamageEvents {

    public static final String DODGED_DAMAGE_TAG = "tt2_scout_dodged_damage";

    private static final UUID ARMOR_UUID = UUID.fromString("3bcbf8c9-fbd6-4dd5-b515-34849da511b4");
    private static final UUID TOUGHNESS_UUID = UUID.fromString("f618494d-7f4a-48d0-bb36-67d4185100fa");

    private final Map<UUID, Float> damage = new HashMap<>();
    private final Map<UUID, Long> lastHitTick = new HashMap<>();
    private final Map<UUID, Long> lastTriggerTick = new HashMap<>();
    private final Map<UUID, Long> nextRecoveryTick = new HashMap<>();
    private final Map<UUID, Long> lastBookCooldownEndTick = new HashMap<>();
    private final Map<UUID, Float> preTraitDamage = new HashMap<>();

    private static final int ARMORY_BOOK_BAR_MAX = 100;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingHurtEarly(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }
        if (!isEnemyDamage(event.getSource())) {
            return;
        }
        preTraitDamage.put(event.getEntityLiving().getUniqueID(), event.getAmount());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingHurtLate(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        Float beforeTraits = preTraitDamage.remove(player.getUniqueID());
        if (beforeTraits == null || beforeTraits <= 0f) {
            return;
        }

        float efficiency = getEfficiency(player);
        if (efficiency >= 1f) {
            return;
        }

        float prevented = beforeTraits - event.getAmount();
        if (prevented > 0f) {
            event.setAmount(beforeTraits - prevented * efficiency);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (player.world.isRemote || event.getAmount() <= 0f) {
            return;
        }
        if (!isEnemyDamage(event.getSource()) || isBlocking(player, event.getSource())
                || event.getSource().damageType.equals(DODGED_DAMAGE_TAG)) {
            return;
        }

        lastHitTick.put(player.getUniqueID(), player.world.getTotalWorldTime());

        EntityLivingBase attacker = (EntityLivingBase) event.getSource().getTrueSource();
        float gain = attacker.isNonBoss()
            ? (float) TT2Config.defenseDamageNormalHitPercent
            : (float) TT2Config.defenseDamageBossHitPercent;
        tryAddDefenseDamage(player, gain);
        syncArmoryBookDisplay(player);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.player.world.isRemote) {
            return;
        }

        EntityPlayer player = event.player;
        UUID id = player.getUniqueID();
        float current = damage.getOrDefault(id, 0f);
        if (current <= 0f) {
            damage.remove(id);
            lastHitTick.remove(id);
            lastTriggerTick.remove(id);
            nextRecoveryTick.remove(id);
            lastBookCooldownEndTick.remove(id);
            updateArmorPenalty(player, 1f);
            clearArmoryBookDisplay(player);
            return;
        }

        long now = player.world.getTotalWorldTime();
        long lastHit = lastHitTick.getOrDefault(id, now);
        if (now - lastHit >= TT2Config.defenseDamageRecoveryDelayTicks) {
            long next = nextRecoveryTick.getOrDefault(id, now);
            if (now >= next) {
                current = Math.max(0f, current - (float) TT2Config.defenseDamageRecoveryPercent);
                damage.put(id, current);
                nextRecoveryTick.put(id, now + TT2Config.defenseDamageRecoveryIntervalTicks);
            }
        }

        updateArmorPenalty(player, getEfficiency(current));
        syncArmoryBookDisplay(player);
    }

    @SubscribeEvent
    public void onLogout(PlayerLoggedOutEvent event) {
        UUID id = event.player.getUniqueID();
        damage.remove(id);
        lastHitTick.remove(id);
        lastTriggerTick.remove(id);
        nextRecoveryTick.remove(id);
        lastBookCooldownEndTick.remove(id);
        preTraitDamage.remove(id);
        removeModifier(event.player.getEntityAttribute(SharedMonsterAttributes.ARMOR), ARMOR_UUID);
        removeModifier(event.player.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS), TOUGHNESS_UUID);
    }

    public float getEfficiency(EntityPlayer player) {
        return getEfficiency(damage.getOrDefault(player.getUniqueID(), 0f));
    }

    private float getEfficiency(float defenseDamage) {
        float min = (float) TT2Config.defenseDamageMinimumEfficiency;
        return Math.max(min, 1f - Math.min(1f, defenseDamage));
    }

    private void tryAddDefenseDamage(EntityPlayer player, float amount) {
        UUID id = player.getUniqueID();
        long now = player.world.getTotalWorldTime();
        long lastTrigger = lastTriggerTick.getOrDefault(id, Long.MIN_VALUE / 4);
        if (now - lastTrigger < TT2Config.defenseDamageTriggerIntervalTicks) {
            return;
        }

        lastTriggerTick.put(id, now);
        addDefenseDamage(player, amount);
    }

    private void addDefenseDamage(EntityPlayer player, float amount) {
        UUID id = player.getUniqueID();
        float maxDamage = 1f - (float) TT2Config.defenseDamageMinimumEfficiency;
        float next = Math.min(maxDamage, damage.getOrDefault(id, 0f) + amount);
        damage.put(id, next);
        nextRecoveryTick.put(id, player.world.getTotalWorldTime() + TT2Config.defenseDamageRecoveryDelayTicks);
        updateArmorPenalty(player, getEfficiency(next));
    }

    private void syncArmoryBookDisplay(EntityPlayer player) {
        Item armoryBook = ConstructsRegistry.book;
        if (armoryBook == null) {
            return;
        }

        boolean hasBook = updateArmoryBookBar(player, armoryBook, getEfficiency(player));
        if (!hasBook) {
            return;
        }

        UUID id = player.getUniqueID();
        if (damage.getOrDefault(id, 0f) <= 0f) {
            player.getCooldownTracker().removeCooldown(armoryBook);
            lastBookCooldownEndTick.remove(id);
            return;
        }

        long now = player.world.getTotalWorldTime();
        long lastHit = lastHitTick.getOrDefault(id, now);
        long cooldownEnd;
        int cooldownTicks;
        if (now - lastHit < TT2Config.defenseDamageRecoveryDelayTicks) {
            cooldownEnd = lastHit + TT2Config.defenseDamageRecoveryDelayTicks;
            cooldownTicks = (int) Math.max(1, cooldownEnd - now);
        } else {
            cooldownEnd = nextRecoveryTick.getOrDefault(id, now);
            cooldownTicks = (int) Math.max(0, cooldownEnd - now);
        }

        Long lastAppliedEnd = lastBookCooldownEndTick.get(id);
        if (cooldownTicks <= 0) {
            player.getCooldownTracker().removeCooldown(armoryBook);
            lastBookCooldownEndTick.remove(id);
        } else if (lastAppliedEnd == null || lastAppliedEnd != cooldownEnd) {
            player.getCooldownTracker().setCooldown(armoryBook, cooldownTicks);
            lastBookCooldownEndTick.put(id, cooldownEnd);
        }
    }

    private void clearArmoryBookDisplay(EntityPlayer player) {
        Item armoryBook = ConstructsRegistry.book;
        if (armoryBook == null) {
            return;
        }

        if (updateArmoryBookBar(player, armoryBook, 1f)) {
            player.getCooldownTracker().removeCooldown(armoryBook);
        }
    }

    private boolean updateArmoryBookBar(EntityPlayer player, Item armoryBook, float efficiency) {
        InventoryPlayer inventory = player.inventory;
        int displayDamage = Math.max(0, Math.min(
            ARMORY_BOOK_BAR_MAX,
            Math.round((1f - efficiency) * ARMORY_BOOK_BAR_MAX)
        ));
        boolean found = false;

        for (ItemStack stack : inventory.mainInventory) {
            if (stack.getItem() == armoryBook) {
                stack.setItemDamage(displayDamage);
                found = true;
            }
        }
        for (ItemStack stack : inventory.offHandInventory) {
            if (stack.getItem() == armoryBook) {
                stack.setItemDamage(displayDamage);
                found = true;
            }
        }
        return found;
    }

    private void updateArmorPenalty(EntityPlayer player, float efficiency) {
        updatePenalty(
            player.getEntityAttribute(SharedMonsterAttributes.ARMOR),
            ARMOR_UUID,
            "tt2_defense_damage_armor",
            1f - efficiency
        );
        updatePenalty(
            player.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS),
            TOUGHNESS_UUID,
            "tt2_defense_damage_toughness",
            1f - efficiency
        );
    }

    private void updatePenalty(IAttributeInstance attr, UUID uuid, String name, float amount) {
        if (attr == null) {
            return;
        }
        removeModifier(attr, uuid);
        if (amount > 0f) {
            attr.applyModifier(new AttributeModifier(uuid, name, -amount, 2));
        }
    }

    private void removeModifier(IAttributeInstance attr, UUID uuid) {
        if (attr != null && attr.getModifier(uuid) != null) {
            attr.removeModifier(uuid);
        }
    }

    private boolean isEnemyDamage(DamageSource source) {
        Entity trueSource = source.getTrueSource();
        return trueSource instanceof EntityLivingBase && !(trueSource instanceof EntityPlayer);
    }

    private boolean isBlocking(EntityPlayer player, DamageSource source) {
        if (source.isUnblockable() || !player.isActiveItemStackBlocking()) {
            return false;
        }
        ItemStack stack = player.getActiveItemStack();
        if (stack.isEmpty() || stack.getItemUseAction() != EnumAction.BLOCK) {
            return false;
        }
        Entity attacker = source.getTrueSource();
        return attacker == null || isFacing(player, attacker, 90.0D);
    }

    private boolean isFacing(EntityPlayer player, Entity attacker, double halfAngleDeg) {
        Vec3d look = player.getLookVec();
        Vec3d direction = new Vec3d(attacker.posX - player.posX, 0, attacker.posZ - player.posZ);
        if (direction.lengthSquared() < 0.0001D) {
            return true;
        }
        direction = direction.normalize();
        return look.x * direction.x + look.z * direction.z >= Math.cos(Math.toRadians(halfAngleDeg));
    }
}
