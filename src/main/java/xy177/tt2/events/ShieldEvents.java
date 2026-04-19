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
import xy177.tt2.tools.TravelerShield;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ShieldEvents {

    /** 上一tick是否正在举盾，用于检测举盾开始时刻 */
    private final Map<UUID, Boolean> wasBlocking = new HashMap<>();

    /**
     * 暂存格挡原始伤害量。
     *
     * 背景：vanilla 在 EntityLivingBase.attackEntityFrom 中，
     * 当 isActiveItemStackBlocking() 为 true 且攻击来自正面时，
     * 会在触发 LivingHurtEvent 之前将 f 置为 0。
     * 因此 LivingHurtEvent.getAmount() 对于被正面格挡的攻击始终为 0，
     * 无法用它来累积招架值。
     *
     * LivingAttackEvent 在 f=0 赋值之前触发（是 attackEntityFrom 里的第一个 Forge 钩子），
     * 可以在此捕获原始伤害值，供后续 LivingHurtEvent 使用。
     * 两个事件均仅在服务端触发（attackEntityFrom 有 world.isRemote 前置判断）。
     */
    private final Map<UUID, Float> pendingParryDamage = new HashMap<>();

    /** 防止精准格挡反击时再次进入 onLivingHurt 造成死循环 */
    private final Set<UUID> counterAttacking = new HashSet<>();

    // -----------------------------------------------------------------------
    // 玩家Tick：检测举盾开始，给予窗口期药水（仅服务端）
    // -----------------------------------------------------------------------

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        // 只在服务端处理药水逻辑：客户端的 updatePotionEffects 因 world.isRemote==true
        // 不执行 iterator.remove()，若在客户端添加药水则永不过期。
        if (event.player.world.isRemote) return;

        EntityPlayer player = event.player;
        UUID id = player.getUniqueID();

        boolean currentlyBlocking = isBlockingWithShield(player);
        boolean prev = wasBlocking.getOrDefault(id, false);

        // 举盾开始（false→true 跳变）才给药水，长按右键不会反复给予
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
        if (player.isPotionActive(TT2Potions.PARRY_WINDOW)) {
            player.removePotionEffect(TT2Potions.PARRY_WINDOW);
        }
        int windowTicks = TT2Config.perfectParryWindowTicks;
        player.addPotionEffect(new PotionEffect(
            TT2Potions.PARRY_WINDOW, windowTicks, 0, false, false
        ));
    }

    // -----------------------------------------------------------------------
    // LivingAttackEvent：在 vanilla 将伤害归零之前捕获原始伤害量
    // -----------------------------------------------------------------------

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        if (!isBlockingWithShield(player)) return;

        ItemStack shieldStack = player.getActiveItemStack();
        if (player.getCooldownTracker().hasCooldown((TravelerShield) shieldStack.getItem())) return;
        if (event.getSource().isUnblockable()) return;

        // 角度检查：攻击来自正面（90° 弧内）
        Entity attacker = event.getSource().getTrueSource();
        if (!isInBlockingArc(player, attacker, TravelerShield.BLOCK_HALF_ANGLE_DEG)) return;

        // 暂存原始伤害量；LivingHurtEvent 中 event.getAmount() 已被 vanilla 归零
        pendingParryDamage.put(player.getUniqueID(), event.getAmount());
    }

    // -----------------------------------------------------------------------
    // LivingHurtEvent：处理格挡结果（perfect parry 或普通格挡）
    // -----------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();

        UUID id = player.getUniqueID();

        // 若没有暂存的伤害量，说明该次攻击未经过 onLivingAttack 的条件检查
        // （角度不在格挡弧内、盾牌冷却中、不可格挡伤害等）→ 不处理
        if (!pendingParryDamage.containsKey(id)) return;

        float originalDamage = pendingParryDamage.remove(id);

        if (!isBlockingWithShield(player)) return;

        ItemStack shieldStack = player.getActiveItemStack();
        TravelerShield shield = (TravelerShield) shieldStack.getItem();

        if (player.getCooldownTracker().hasCooldown(shield)) return;

        // 精准格挡判定（窗口期药水是否仍存在）
        boolean isPerfectParry = player.isPotionActive(TT2Potions.PARRY_WINDOW);

        // vanilla 对正面格挡攻击已将 event.getAmount() 归零，显式取消保持一致性
        event.setCanceled(true);

        if (isPerfectParry) {
            handlePerfectParry(player, shieldStack, event.getSource(), event.getSource().getTrueSource());
        } else {
            handleNormalBlock(player, shieldStack, originalDamage, shield);
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
    // 普通格挡：使用 originalDamage（来自 LivingAttackEvent，格挡前原始值）
    // -----------------------------------------------------------------------

    private void handleNormalBlock(EntityPlayer player, ItemStack shieldStack,
                                   float originalDamage, TravelerShield shield) {
        if (player.world.isRemote) return;

        player.world.playSound(null,
            player.posX, player.posY, player.posZ,
            SoundEvents.ITEM_SHIELD_BLOCK,
            SoundCategory.PLAYERS,
            1.0f, 0.8f + player.world.rand.nextFloat() * 0.4f
        );

        float current = TravelerShield.getParryAccum(shieldStack) + originalDamage;
        float max = TravelerShield.getParryMax(shieldStack);

        if (current >= max) {
            TravelerShield.setParryAccum(shieldStack, 0f);
            player.getCooldownTracker().setCooldown(shield, TravelerShield.getCooldownTicks(shieldStack));
            // ★ 强制中断举盾动作，否则玩家不放开右键时会继续处于 isHandActive 状态，
            // 导致后续攻击仍被判定为格挡
            player.stopActiveHand();
        } else {
            TravelerShield.setParryAccum(shieldStack, current);
        }
    }

    // -----------------------------------------------------------------------
    // 精准格挡反击
    // -----------------------------------------------------------------------

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
        UUID id = event.player.getUniqueID();
        wasBlocking.remove(id);
        pendingParryDamage.remove(id);
    }
}
