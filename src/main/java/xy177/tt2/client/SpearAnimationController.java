package xy177.tt2.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.utils.ToolHelper;
import xy177.tt2.config.TT2Config;
import xy177.tt2.init.TT2Items;
import xy177.tt2.logic.SpearAttackTiming;
import xy177.tt2.network.PacketSpearStab;
import xy177.tt2.network.TT2Network;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public final class SpearAnimationController {

    private static final Map<Long, StabState> STABS = new HashMap<>();
    private static final Map<Long, Integer> LAST_SERVER_SEQUENCES =
        new LinkedHashMap<Long, Integer>(128, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, Integer> eldest) {
                return size() > 1024;
            }
        };
    private static final Map<Long, Deque<Integer>> LOCAL_PREDICTIONS = new HashMap<>();
    private static int nextSequence = 1;
    private static long lastRequestTick = Long.MIN_VALUE;
    private static long capturedCooldownTick = Long.MIN_VALUE;
    private static float capturedCooldownStrength;

    private SpearAnimationController() {
    }

    public static void captureInputState() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null) {
            capturedCooldownTick = Long.MIN_VALUE;
            return;
        }
        capturedCooldownTick = mc.world.getTotalWorldTime();
        capturedCooldownStrength = mc.player.getCooledAttackStrength(0.5F);
    }

    public static void requestLocalStab(EntityPlayer player, boolean cooldownAlreadyReset) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != player || mc.world == null || !TT2Config.enableSpear
            || player.isHandActive()) {
            return;
        }

        ItemStack stack = player.getHeldItemMainhand();
        if (!isSpear(stack) || ToolHelper.isBroken(stack)) {
            return;
        }

        long tick = mc.world.getTotalWorldTime();
        float cooldownStrength = cooldownAlreadyReset && capturedCooldownTick == tick
            ? capturedCooldownStrength : player.getCooledAttackStrength(0.5F);
        boolean predict = cooldownStrength + 1.0E-5F
            >= TT2Config.spearStabMinimumAttackRecharge;
        if (lastRequestTick == tick) {
            return;
        }
        lastRequestTick = tick;

        int sequence = nextSequence++;
        if (nextSequence == 0) {
            nextSequence = 1;
        }
        if (predict) {
            startLocal(player.getEntityId(), EnumHand.MAIN_HAND, sequence,
                SpearAttackTiming.animationDurationTicks(player));
        }
        TT2Network.CHANNEL.sendToServer(new PacketSpearStab(sequence));
        if (!cooldownAlreadyReset) {
            player.resetCooldown();
        }
    }

    public static void acceptServerAnimation(int entityId, EnumHand hand,
                                             int requestSequence, int animationSequence,
                                             int durationTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || !TT2Config.enableSpear || hand == null) {
            return;
        }
        long key = key(entityId, hand);
        Integer previousServerSequence = LAST_SERVER_SEQUENCES.get(key);
        if (previousServerSequence != null && previousServerSequence == animationSequence) {
            return;
        }
        if (mc.player != null && entityId == mc.player.getEntityId()) {
            Deque<Integer> predictions = LOCAL_PREDICTIONS.get(key);
            if (predictions != null && predictions.removeFirstOccurrence(requestSequence)) {
                LAST_SERVER_SEQUENCES.put(key, animationSequence);
                return;
            }
        }
        Entity entity = mc.world.getEntityByID(entityId);
        boolean sawSpear = entity instanceof EntityLivingBase
            && isSpear(((EntityLivingBase) entity).getHeldItem(hand));
        startServer(entityId, hand, animationSequence, durationTicks, sawSpear);
    }

    public static float attackTime(EntityLivingBase living, EnumHand hand, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || living.world != mc.world || !isSpear(living.getHeldItem(hand))) {
            return -1.0F;
        }
        StabState state = STABS.get(key(living.getEntityId(), hand));
        if (state == null) {
            return -1.0F;
        }
        float elapsed = (float) (mc.world.getTotalWorldTime() - state.startedAt) + partialTicks;
        if (elapsed < 0.0F || elapsed > state.durationTicks) {
            return -1.0F;
        }
        return Math.min(1.0F, elapsed / state.durationTicks);
    }

    public static void clientTick() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null) {
            clear();
            return;
        }
        long now = mc.world.getTotalWorldTime();
        Iterator<Map.Entry<Long, StabState>> iterator = STABS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, StabState> entry = iterator.next();
            StabState state = entry.getValue();
            Entity entity = mc.world.getEntityByID(state.entityId);
            long age = now - state.startedAt;
            if (age > state.durationTicks + 1L) {
                iterator.remove();
                continue;
            }
            if (entity instanceof EntityLivingBase) {
                if (isSpear(((EntityLivingBase) entity).getHeldItem(state.hand))) {
                    state.sawSpear = true;
                } else if (state.sawSpear) {
                    iterator.remove();
                }
            } else if (age > 5L) {
                iterator.remove();
                LAST_SERVER_SEQUENCES.remove(entry.getKey());
            }
        }
    }

    public static void clear() {
        STABS.clear();
        LAST_SERVER_SEQUENCES.clear();
        LOCAL_PREDICTIONS.clear();
        lastRequestTick = Long.MIN_VALUE;
        capturedCooldownTick = Long.MIN_VALUE;
    }

    private static void startLocal(int entityId, EnumHand hand, int requestSequence,
                                   int durationTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null) {
            return;
        }
        long key = key(entityId, hand);
        Deque<Integer> predictions = LOCAL_PREDICTIONS.computeIfAbsent(
            key, ignored -> new ArrayDeque<>()
        );
        predictions.addLast(requestSequence);
        while (predictions.size() > 64) {
            predictions.removeFirst();
        }
        STABS.put(key, new StabState(entityId, hand, mc.world.getTotalWorldTime(),
            sanitizeDuration(durationTicks), true));
    }

    private static void startServer(int entityId, EnumHand hand, int animationSequence,
                                    int durationTicks, boolean sawSpear) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null) {
            return;
        }
        long key = key(entityId, hand);
        Integer previousSequence = LAST_SERVER_SEQUENCES.get(key);
        if (previousSequence != null && previousSequence == animationSequence) {
            return;
        }
        LAST_SERVER_SEQUENCES.put(key, animationSequence);
        STABS.put(key, new StabState(entityId, hand, mc.world.getTotalWorldTime(),
            sanitizeDuration(durationTicks), sawSpear));
    }

    private static int sanitizeDuration(int durationTicks) {
        return Math.max(1, Math.min(72000, durationTicks));
    }

    private static long key(int entityId, EnumHand hand) {
        return ((long) entityId << 1) | (hand == EnumHand.OFF_HAND ? 1L : 0L);
    }

    private static boolean isSpear(ItemStack stack) {
        return !stack.isEmpty() && TT2Items.SPEAR != null && stack.getItem() == TT2Items.SPEAR;
    }

    private static final class StabState {
        private final int entityId;
        private final EnumHand hand;
        private final long startedAt;
        private final int durationTicks;
        private boolean sawSpear;

        private StabState(int entityId, EnumHand hand, long startedAt,
                          int durationTicks, boolean sawSpear) {
            this.entityId = entityId;
            this.hand = hand;
            this.startedAt = startedAt;
            this.durationTicks = durationTicks;
            this.sawSpear = sawSpear;
        }
    }
}
