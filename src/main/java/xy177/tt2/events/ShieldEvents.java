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
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.tools.ToolNBT;
import xy177.tt2.config.TT2Config;
import xy177.tt2.potion.TT2Potions;
import xy177.tt2.tools.TravelerShield;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ShieldEvents {

    /** 上一tick是否正在举盾，用于检测举盾开始时刻（false→true跳变） */
    private final Map<UUID, Boolean> wasBlocking = new HashMap<>();

    /** 防止精准格挡反击时再次进入 onLivingHurt 造成死循环 */
    private final Set<UUID> counterAttacking = new HashSet<>();

    // -----------------------------------------------------------------------
    // 玩家Tick：检测举盾开始，给予窗口期药水
    // -----------------------------------------------------------------------

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        EntityPlayer player = event.player;
        UUID id = player.getUniqueID();

        boolean currentlyBlocking = isBlockingWithShield(player);
        boolean prev = wasBlocking.getOrDefault(id, false);

        // 举盾开始（false → true 跳变）才给药水，长按右键不会反复给予
        if (currentlyBlocking && !prev) {
            applyParryWindowPotion(player);
        }

        wasBlocking.put(id, currentlyBlocking);
    }

    private boolean isBlockingWithShield(EntityPlayer player) {
        return player.isHandActive()
            && !player.getActiveItemStack().isEmpty()
            && player.getActiveItemStack().getItem() instanceof TravelerShield;
    }

    private void applyParryWindowPotion(EntityPlayer player) {
        // 若已存在则先移除再重新添加（松开后立刻举盾时重置计时）
        if (player.isPotionActive(TT2Potions.PARRY_WINDOW)) {
            player.removePotionEffect(TT2Potions.PARRY_WINDOW);
        }
        int windowTicks = TT2Config.perfectParryWindowTicks;
        // ambient=false, showParticles=false → 无粒子
        player.addPotionEffect(new PotionEffect(
            TT2Potions.PARRY_WINDOW, windowTicks, 0, false, false
        ));
    }

    // -----------------------------------------------------------------------
    // 核心：格挡逻辑
    // -----------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        if (!isBlockingWithShield(player)) return;

        ItemStack shieldStack = player.getActiveItemStack();
        TravelerShield shield = (TravelerShield) shieldStack.getItem();

        if (player.getCooldownTracker().hasCooldown(shield)) return;

        DamageSource source = event.getSource();
        if (source.isUnblockable()) return;

        Entity attacker = source.getTrueSource();
        if (!isInBlockingArc(player, attacker, TravelerShield.BLOCK_HALF_ANGLE_DEG)) return;

        // 精准格挡判定：检测药水效果是否仍然存在
        boolean isPerfectParry = player.isPotionActive(TT2Potions.PARRY_WINDOW);

        // 完全格挡，取消原始伤害
        event.setCanceled(true);

        if (isPerfectParry) {
            handlePerfectParry(player, shieldStack, source, attacker);
        } else {
            handleNormalBlock(player, shieldStack, event.getAmount(), shield);
        }
    }

    // -----------------------------------------------------------------------
    // 精准格挡
    // -----------------------------------------------------------------------

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

    // -----------------------------------------------------------------------
    // 普通格挡
    // -----------------------------------------------------------------------

    private void handleNormalBlock(EntityPlayer player, ItemStack shieldStack,
                                   float blockedDamage, TravelerShield shield) {
        if (player.world.isRemote) return;

        player.world.playSound(null,
            player.posX, player.posY, player.posZ,
            SoundEvents.ITEM_SHIELD_BLOCK,
            SoundCategory.PLAYERS,
            1.0f, 0.8f + player.world.rand.nextFloat() * 0.4f
        );

        float current = TravelerShield.getParryAccum(shieldStack) + blockedDamage;
        float max = TravelerShield.getParryMax(shieldStack);

        if (current >= max) {
            TravelerShield.setParryAccum(shieldStack, 0f);
            player.getCooldownTracker().setCooldown(shield, TravelerShield.getCooldownTicks(shieldStack));
        } else {
            TravelerShield.setParryAccum(shieldStack, current);
        }
    }

    // -----------------------------------------------------------------------
    // 精准格挡反击
    // -----------------------------------------------------------------------

    private void performCounterAttack(ItemStack shieldStack, EntityPlayer player,
                                      EntityLivingBase attacker) {
        ToolNBT toolData = new ToolNBT(TagUtil.getToolTag(shieldStack.getTagCompound()));
        float baseDamage = toolData.attack;
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

    // -----------------------------------------------------------------------
    // 格挡角度判断
    // -----------------------------------------------------------------------

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

    // -----------------------------------------------------------------------
    // 清理
    // -----------------------------------------------------------------------

    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        wasBlocking.remove(event.player.getUniqueID());
    }
}
