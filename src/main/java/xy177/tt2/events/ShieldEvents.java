package xy177.tt2.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;
import xy177.tt2.config.TT2Config;
import xy177.tt2.potion.TT2Potions;
import xy177.tt2.tools.SwiftShield;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ShieldEvents {

    private final Map<UUID, Boolean> wasBlocking = new HashMap<>();

    private final Map<UUID, Float> pendingParryDamage = new HashMap<>();

    private final Set<UUID> counterAttacking = new HashSet<>();


    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (event.player.world.isRemote) return;

        EntityPlayer player = event.player;
        UUID id = player.getUniqueID();

        boolean currentlyBlocking = isBlockingWithShield(player);
        boolean prev = wasBlocking.getOrDefault(id, false);

        if (currentlyBlocking && !prev) {
            applyParryWindowPotion(player);
        }

        wasBlocking.put(id, currentlyBlocking);
    }

    private boolean isBlockingWithShield(EntityPlayer player) {
        return player.isHandActive()
            && !player.getActiveItemStack().isEmpty()
            && player.getActiveItemStack().getItem() instanceof SwiftShield;
    }

    private void applyParryWindowPotion(EntityPlayer player) {
        if (player.isPotionActive(TT2Potions.PARRY_WINDOW)) {
            player.removePotionEffect(TT2Potions.PARRY_WINDOW);
        }
        int windowTicks = TT2Config.perfectParryWindowTicks;
        player.addPotionEffect(new PotionEffect(
            TT2Potions.PARRY_WINDOW, windowTicks, 0, false, false
        ));
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        if (!isBlockingWithShield(player)) return;

        ItemStack shieldStack = player.getActiveItemStack();
        if (player.getCooldownTracker().hasCooldown((SwiftShield) shieldStack.getItem())) return;
        if (event.getSource().isUnblockable()) return;

        Entity attacker = event.getSource().getTrueSource();
        if (!isInBlockingArc(player, attacker, SwiftShield.BLOCK_HALF_ANGLE_DEG)) return;

        pendingParryDamage.put(player.getUniqueID(), event.getAmount());
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        UUID id = player.getUniqueID();

        if (!pendingParryDamage.containsKey(id)) return;

        float originalDamage = pendingParryDamage.remove(id);

        if (!isBlockingWithShield(player)) return;

        ItemStack shieldStack = player.getActiveItemStack();
        SwiftShield shield = (SwiftShield) shieldStack.getItem();

        if (player.getCooldownTracker().hasCooldown(shield)) return;

        boolean isPerfectParry = player.isPotionActive(TT2Potions.PARRY_WINDOW);

        event.setCanceled(true);

        if (isPerfectParry) {
            handlePerfectParry(player, shieldStack, event.getSource(), event.getSource().getTrueSource());
        } else {
            handleNormalBlock(player, shieldStack, originalDamage, shield);
        }
    }

    private void handlePerfectParry(EntityPlayer player, ItemStack shieldStack,
                                    DamageSource source, Entity attacker) {
        if (player.world.isRemote) return;

        player.world.playSound(null,
            player.posX, player.posY, player.posZ,
            SoundEvents.BLOCK_ANVIL_LAND,
            SoundCategory.PLAYERS,
            0.6f, 1.6f + player.world.rand.nextFloat() * 0.3f
        );

        if (attacker instanceof EntityLivingBase
                && !(attacker instanceof FakePlayer)
                && attacker != player) {
            UUID pid = player.getUniqueID();
            if (!counterAttacking.contains(pid)) {
                counterAttacking.add(pid);
                try {
                    performCounterAttack(shieldStack, player, (EntityLivingBase) attacker);
                } finally {
                    counterAttacking.remove(pid);
                }
            }
        }
    }

    private void handleNormalBlock(EntityPlayer player, ItemStack shieldStack,
                                   float originalDamage, SwiftShield shield) {
        if (player.world.isRemote) return;

        player.world.playSound(null,
            player.posX, player.posY, player.posZ,
            SoundEvents.ITEM_SHIELD_BLOCK,
            SoundCategory.PLAYERS,
            1.0f, 0.8f + player.world.rand.nextFloat() * 0.4f
        );

        float current = SwiftShield.getParryAccum(shieldStack) + originalDamage;
        float max = SwiftShield.getParryMax(shieldStack);

        if (current >= max) {
            SwiftShield.setParryAccum(shieldStack, 0f);
            player.getCooldownTracker().setCooldown(shield, SwiftShield.getCooldownTicks(shieldStack));
            player.stopActiveHand();
        } else {
            SwiftShield.setParryAccum(shieldStack, current);
        }
    }

    private void performCounterAttack(ItemStack shieldStack, EntityPlayer player,
                                      EntityLivingBase attacker) {
        float baseDamage = ToolHelper.getActualAttack(shieldStack);
        if (baseDamage <= 0) return;

        NBTTagList traits = TagUtil.getTraitsTagList(shieldStack);
        float finalDamage = baseDamage;
        for (int i = 0; i < traits.tagCount(); i++) {
            ITrait trait = TinkerRegistry.getTrait(traits.getStringTagAt(i));
            if (trait != null) {
                finalDamage = trait.damage(shieldStack, player, attacker,
                    baseDamage, finalDamage, false);
            }
        }

        boolean wasHit = attacker.attackEntityFrom(
            DamageSource.causePlayerDamage(player), finalDamage
        );

        for (int i = 0; i < traits.tagCount(); i++) {
            ITrait trait = TinkerRegistry.getTrait(traits.getStringTagAt(i));
            if (trait != null) {
                trait.onHit(shieldStack, player, attacker, finalDamage, false);
                trait.afterHit(shieldStack, player, attacker, finalDamage, false, wasHit);
            }
        }
    }

    private boolean isInBlockingArc(EntityPlayer player, Entity attacker, double halfAngleDeg) {
        if (attacker == null) return true;

        double dx = attacker.posX - player.posX;
        double dz = attacker.posZ - player.posZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 0.01) return true;

        dx /= dist;
        dz /= dist;

        float yawRad = player.rotationYaw * ((float) Math.PI / 180.0f);
        double facingX = -Math.sin(yawRad);
        double facingZ =  Math.cos(yawRad);

        double cosAngle = facingX * dx + facingZ * dz;
        double cosHalf  = Math.cos(Math.toRadians(halfAngleDeg));
        return cosAngle >= cosHalf;
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        UUID id = event.player.getUniqueID();
        wasBlocking.remove(id);
        pendingParryDamage.remove(id);
    }
}
