package xy177.tt2.events;

import c4.conarm.lib.traits.IArmorTrait;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import slimeknights.tconstruct.library.utils.ToolHelper;
import xy177.tt2.config.TT2Config;
import xy177.tt2.potion.TT2Potions;
import xy177.tt2.tools.Doppelhander;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DoppelhanderEvents {

    private static final UUID DOPPELHANDER_ARMOR_UUID =
        UUID.fromString("8f1a0c4b-3515-4e07-9f23-5d0a4f7d9a01");
    private static final UUID DOPPELHANDER_TOUGHNESS_UUID =
        UUID.fromString("56d07f4c-79f2-4c6c-90a8-6ba615feab32");

    private final Map<UUID, ItemStack> pendingArmorTraitHits = new HashMap<>();
    private final Set<UUID> internalDamage = new HashSet<>();
    private final Set<UUID> stanceActive = new HashSet<>();

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (internalDamage.contains(player.getUniqueID())) return;
        if (!shouldBlockDamage(player, event.getSource())) return;

        ItemStack stack = player.getActiveItemStack();
        boolean inStance = player.isPotionActive(TT2Potions.DEFENSIVE_STANCE);
        float blockReduction = inStance
            ? 1.0f
            : Math.min(1.0f, (float) TT2Config.doppelhanderBaseBlockReduction + Doppelhander.getBlockBonus(stack));
        float remainingDamage = event.getAmount() * (1.0f - blockReduction);
        double oldMotionX = player.motionX;
        double oldMotionY = player.motionY;
        double oldMotionZ = player.motionZ;

        event.setCanceled(true);

        EnumHand activeHand = player.getActiveHand();
        if (remainingDamage > 0.0f) {
            player.resetActiveHand();
            pendingArmorTraitHits.put(player.getUniqueID(), stack.copy());
            internalDamage.add(player.getUniqueID());
            try {
                player.attackEntityFrom(event.getSource(), remainingDamage);
            } finally {
                internalDamage.remove(player.getUniqueID());
                pendingArmorTraitHits.remove(player.getUniqueID());
                player.motionX = oldMotionX;
                player.motionY = oldMotionY;
                player.motionZ = oldMotionZ;
                player.velocityChanged = false;
                if (!ToolHelper.isBroken(stack) && !player.isHandActive()) {
                    player.setActiveHand(activeHand);
                }
            }
        } else {
            player.motionX = oldMotionX;
            player.motionY = oldMotionY;
            player.motionZ = oldMotionZ;
            player.velocityChanged = false;
        }

        if (!inStance) {
            float nextBonus = Doppelhander.getBlockBonus(stack) + Doppelhander.getBlockGain(stack);
            if (TT2Config.doppelhanderBaseBlockReduction + nextBonus >= 1.0) {
                Doppelhander.setBlockBonus(stack, 0.0f);
                player.addPotionEffect(new PotionEffect(
                    TT2Potions.DEFENSIVE_STANCE,
                    TT2Config.doppelhanderDefensiveStanceDurationSeconds * 20,
                    0,
                    false,
                    false
                ));
                sendGuardStatus(player, 100);
                sendStanceStatus(player);
                playStatusSound(player, net.minecraft.init.SoundEvents.ENTITY_PLAYER_LEVELUP, 0.5f, 1.2f);
            } else {
                Doppelhander.setBlockBonus(stack, nextBonus);
                int percent = (int) Math.round((TT2Config.doppelhanderBaseBlockReduction + nextBonus) * 100.0);
                sendGuardStatus(player, percent);
                playStatusSound(player, net.minecraft.init.SoundEvents.ITEM_SHIELD_BLOCK, 0.7f, 1.1f);
            }
        } else {
            sendStanceStatus(player);
            playStatusSound(player, net.minecraft.init.SoundEvents.ITEM_SHIELD_BLOCK, 0.8f, 0.9f);
        }

        if (player.isPotionActive(TT2Potions.DEFENSIVE_STANCE)) {
            player.heal((float) (player.getMaxHealth() * TT2Config.doppelhanderBlockHealPercent));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        ItemStack stack = pendingArmorTraitHits.get(player.getUniqueID());
        if (stack == null || stack.isEmpty()) return;

        float newDamage = event.getAmount();
        List<IArmorTrait> armorTraits = Doppelhander.getPlateArmorTraits(stack);
        for (IArmorTrait trait : armorTraits) {
            newDamage = trait.onHurt(stack, player, event.getSource(), event.getAmount(), newDamage, event);
        }

        event.setAmount(Math.max(0.0f, newDamage));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        ItemStack stack = pendingArmorTraitHits.get(player.getUniqueID());
        if (stack == null || stack.isEmpty()) return;

        float newDamage = event.getAmount();
        List<IArmorTrait> armorTraits = Doppelhander.getPlateArmorTraits(stack);
        for (IArmorTrait trait : armorTraits) {
            newDamage = trait.onDamaged(stack, player, event.getSource(), event.getAmount(), newDamage, event);
        }

        event.setAmount(Math.max(0.0f, newDamage));
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (event.player.world.isRemote) return;

        EntityPlayer player = event.player;
        UUID playerId = player.getUniqueID();
        ItemStack heldDoppelhander = getHeldDoppelhander(player);

        updateArmorModifiers(player, heldDoppelhander);

        boolean nowInStance = player.isPotionActive(TT2Potions.DEFENSIVE_STANCE);
        boolean wasInStance = stanceActive.contains(playerId);
        if (nowInStance) {
            stanceActive.add(playerId);
        } else if (wasInStance) {
            stanceActive.remove(playerId);
            player.sendStatusMessage(new TextComponentTranslation("tt2.doppelhander.defensive_stance_ended"), true);
        }

        if (!heldDoppelhander.isEmpty()
                && nowInStance
                && player.isHandActive()
                && player.getActiveItemStack().getItem() instanceof Doppelhander) {
            redirectNearbyAggro(player);
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        UUID id = event.player.getUniqueID();
        pendingArmorTraitHits.remove(id);
        internalDamage.remove(id);
        stanceActive.remove(id);
        removeModifier(event.player.getEntityAttribute(SharedMonsterAttributes.ARMOR), DOPPELHANDER_ARMOR_UUID);
        removeModifier(event.player.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS), DOPPELHANDER_TOUGHNESS_UUID);
    }

    private boolean shouldBlockDamage(EntityPlayer player, DamageSource source) {
        if (source.isUnblockable()) {
            return false;
        }
        if (!player.isActiveItemStackBlocking()) {
            return false;
        }

        ItemStack stack = player.getActiveItemStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof Doppelhander)) {
            return false;
        }
        if (ToolHelper.isBroken(stack)) {
            return false;
        }

        EntityLivingBase attacker = source.getTrueSource() instanceof EntityLivingBase
            ? (EntityLivingBase) source.getTrueSource()
            : null;
        return attacker == null || Doppelhander.isWithinArc(player, attacker, Doppelhander.BLOCK_HALF_ANGLE_DEG);
    }

    private ItemStack getHeldDoppelhander(EntityPlayer player) {
        ItemStack mainhand = player.getHeldItemMainhand();
        if (mainhand.getItem() instanceof Doppelhander && !ToolHelper.isBroken(mainhand)) {
            return mainhand;
        }

        ItemStack offhand = player.getHeldItemOffhand();
        if (offhand.getItem() instanceof Doppelhander && !ToolHelper.isBroken(offhand)) {
            return offhand;
        }

        return ItemStack.EMPTY;
    }

    private void updateArmorModifiers(EntityPlayer player, ItemStack heldDoppelhander) {
        float armor = 0.0f;
        float toughness = 0.0f;

        if (!heldDoppelhander.isEmpty()) {
            boolean defensiveStance = player.isPotionActive(TT2Potions.DEFENSIVE_STANCE);
            armor = Doppelhander.getProvidedArmor(heldDoppelhander, defensiveStance);
            toughness = Doppelhander.getProvidedToughness(heldDoppelhander, defensiveStance);
        }

        updateModifier(
            player.getEntityAttribute(SharedMonsterAttributes.ARMOR),
            DOPPELHANDER_ARMOR_UUID,
            "tt2_doppelhander_armor",
            armor
        );
        updateModifier(
            player.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS),
            DOPPELHANDER_TOUGHNESS_UUID,
            "tt2_doppelhander_toughness",
            toughness
        );
    }

    private void updateModifier(IAttributeInstance attribute, UUID uuid, String name, double amount) {
        if (attribute == null) return;

        AttributeModifier existing = attribute.getModifier(uuid);
        if (existing != null) {
            if (existing.getAmount() == amount) {
                return;
            }
            attribute.removeModifier(existing);
        }

        if (amount != 0.0) {
            attribute.applyModifier(new AttributeModifier(uuid, name, amount, 0));
        }
    }

    private void removeModifier(IAttributeInstance attribute, UUID uuid) {
        if (attribute == null) return;
        AttributeModifier existing = attribute.getModifier(uuid);
        if (existing != null) {
            attribute.removeModifier(existing);
        }
    }

    private void redirectNearbyAggro(EntityPlayer player) {
        for (EntityLiving mob : player.world.getEntitiesWithinAABB(
            EntityLiving.class,
            player.getEntityBoundingBox().grow(10.0D, 4.0D, 10.0D)
        )) {
            if (!(mob instanceof IMob)) {
                continue;
            }
            if (mob.getAttackTarget() == null || mob.getAttackTarget() == player) {
                continue;
            }
            mob.setAttackTarget(player);
            mob.setRevengeTarget(player);
        }
    }

    private void sendGuardStatus(EntityPlayer player, int percent) {
        player.sendStatusMessage(new TextComponentTranslation("tt2.doppelhander.guard_performance", percent), true);
    }

    private void sendStanceStatus(EntityPlayer player) {
        player.sendStatusMessage(new TextComponentTranslation("tt2.doppelhander.defensive_stance_entered"), true);
    }

    private void playStatusSound(EntityPlayer player, SoundEvent sound, float volume, float pitch) {
        player.world.playSound(null, player.posX, player.posY, player.posZ, sound, SoundCategory.PLAYERS, volume, pitch);
    }
}
