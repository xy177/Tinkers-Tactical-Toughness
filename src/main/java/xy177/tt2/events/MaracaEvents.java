package xy177.tt2.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.util.EnumActionResult;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickEmpty;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import slimeknights.tconstruct.library.events.TinkerCraftingEvent;
import xy177.tt2.config.TT2Config;
import xy177.tt2.potion.TT2Potions;
import xy177.tt2.tools.Maraca;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MaracaEvents {

    private static final char QUICK = 'Q';
    private static final char SLOW = 'S';
    private static final char RESONANCE = 'R';

    private static final int PARTY_RESONANCE_COST = 200;

    private static final UUID SELF_ATTACK_UUID = UUID.fromString("762d5364-6aa7-4fd7-8e4a-a8572e58c3dd");
    private static final UUID ATTACK_UUID = UUID.fromString("4e463dbf-3cf0-43db-b12f-47b7637d51c0");
    private static final Map<UUID, String> ACTION_RECORDS = new HashMap<>();
    private static final Set<UUID> SLOW_ATTACKERS = new HashSet<>();
    private final Set<UUID> uiOpenPlayers = new HashSet<>();
    private final Map<UUID, EnumMap<Melody, MelodyState>> melodies = new HashMap<>();
    private final Set<UUID> retargeting = new HashSet<>();

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!event.getEntityLiving().world.isRemote) {
            updateMelodies(event.getEntityLiving());
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote) {
            return;
        }

        EntityPlayer player = event.player;
        Maraca.purgeTemporaryCopies(player);
        if (isUiOpen(player)) {
            Maraca.clearTemporaryOffhandIfNeeded(player);
            return;
        }

        Maraca.onMainhandUpdated(player);
    }

    @SubscribeEvent
    public void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntityLiving().world.isRemote || !(event.getEntityLiving() instanceof EntityPlayer)
                || event.getSlot() != EntityEquipmentSlot.MAINHAND) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        Maraca.purgeTemporaryCopies(player);
        ItemStack mainhand = player.getHeldItemMainhand();
        if (isUiOpen(player)) {
            Maraca.clearTemporaryOffhandIfNeeded(player);
            return;
        }
        if (mainhand.getItem() instanceof Maraca) {
            Maraca.onMainhandUpdated(player);
        }
    }

    @SubscribeEvent
    public void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getEntityPlayer().world.isRemote || !(event.getEntityPlayer() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getEntityPlayer();
        Maraca.purgeTemporaryCopies(player);
        Maraca.onMainhandUpdated(player);
    }

    @SubscribeEvent
    public void onContainerClose(PlayerContainerEvent.Close event) {
        if (event.getEntityPlayer().world.isRemote || !(event.getEntityPlayer() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getEntityPlayer();
        Maraca.purgeTemporaryCopies(player);
        Maraca.onMainhandUpdated(player);
    }

    private boolean hasOpenContainer(EntityPlayer player) {
        return player.openContainer != player.inventoryContainer;
    }

    public void setUiOpen(EntityPlayer player, boolean open) {
        UUID id = player.getUniqueID();
        if (open) {
            uiOpenPlayers.add(id);
            Maraca.clearTemporaryOffhandIfNeeded(player);
        } else {
            uiOpenPlayers.remove(id);
            Maraca.purgeTemporaryCopies(player);
        }
    }

    private boolean isUiOpen(EntityPlayer player) {
        return uiOpenPlayers.contains(player.getUniqueID()) || hasOpenContainer(player);
    }

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        ItemStack stack = event.getEntityItem().getItem();
        if (!(stack.getItem() instanceof Maraca)) {
            return;
        }

        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityItem) {
            ItemStack stack = ((EntityItem) entity).getItem();
            if (stack.getItem() instanceof Maraca && Maraca.isTemporaryCopy(stack)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent event) {
        ItemStack stack = event.getItem().getItem();
        if (stack.getItem() instanceof Maraca && Maraca.isTemporaryCopy(stack)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onToolModify(TinkerCraftingEvent.ToolModifyEvent event) {
        ItemStack stack = event.getToolBeforeModification();
        if (stack.getItem() instanceof Maraca && Maraca.isTemporaryCopy(stack)) {
            event.setCanceled("Maraca temporary copy cannot be modified.");
        }
    }

    @SubscribeEvent
    public void onToolPartReplacement(slimeknights.tconstruct.library.events.TinkerEvent.OnToolPartReplacement replacement) {
        ItemStack stack = replacement.toolStack;
        if (stack.getItem() instanceof Maraca && Maraca.isTemporaryCopy(stack)) {
            replacement.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRightClickEmpty(RightClickEmpty event) {
        if (isRestrictedUse(event.getEntityPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLeftClickBlock(LeftClickBlock event) {
        if (isRestrictedUse(event.getEntityPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEntityInteract(EntityInteract event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = getPrimaryMaraca(player);
        if (stack.isEmpty()) {
            return;
        }

        if (!canUseMaraca(player, stack)) {
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.FAIL);
        }
    }

    @SubscribeEvent
    public void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = getPrimaryMaraca(player);
        if (stack.isEmpty()) {
            return;
        }

        if (!canUseMaraca(player, stack)) {
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.FAIL);
        }
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof Maraca)) {
            return;
        }

        ItemStack mainhand = getPrimaryMaraca(event.getEntityPlayer());
        if (mainhand.isEmpty() || !canUseMaraca(event.getEntityPlayer(), mainhand)) {
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.FAIL);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAttackEntity(AttackEntityEvent event) {
        ItemStack stack = getPrimaryMaraca(event.getEntityPlayer());
        if (!stack.isEmpty() && !canUseMaraca(event.getEntityPlayer(), stack)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer attacker = (EntityPlayer) event.getSource().getTrueSource();
        if (isSlowAttack(attacker)) {
            return;
        }

        ItemStack stack = getPrimaryMaraca(attacker);
        if (!stack.isEmpty() && !canUseMaraca(attacker, stack)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLivingHurt(LivingHurtEvent event) {
        Entity trueSource = event.getSource().getTrueSource();
        if (trueSource instanceof EntityPlayer) {
            EntityPlayer attacker = (EntityPlayer) trueSource;
            float bonus = getAttackBonus(attacker);
            if (bonus > 0f) {
                event.setAmount(event.getAmount() * (1f + bonus));
            }
        }

        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            if (hasMelody(player, Melody.DEFENSE)) {
                event.setAmount(event.getAmount() * 0.60f);
            }
            if (hasMelody(player, Melody.PARTY)) {
                event.setAmount(event.getAmount() * 1.20f);
            }
        } else if (hasMelody(event.getEntityLiving(), Melody.DEFENSE)) {
            event.setAmount(event.getAmount() * 0.75f);
        }
    }

    @SubscribeEvent
    public void onKnockback(LivingKnockBackEvent event) {
        if (hasMelody(event.getEntityLiving(), Melody.KNOCKBACK_IMMUNITY)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLivingSetAttackTarget(LivingSetAttackTargetEvent event) {
        if (!(event.getEntityLiving() instanceof EntityLiving) || !(event.getTarget() instanceof EntityPlayer)) {
            return;
        }

        EntityLiving aggressor = (EntityLiving) event.getEntityLiving();
        EntityPlayer player = (EntityPlayer) event.getTarget();
        if (!hasMelody(player, Melody.PARTY) || retargeting.contains(aggressor.getUniqueID())) {
            return;
        }

        EntityLivingBase redirect = findRedirectTarget(aggressor, player, 8.0D);
        if (redirect == null || redirect == player) {
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
    public void onLogout(PlayerLoggedOutEvent event) {
        cleanupPlayer(event.player);
    }

    @SubscribeEvent
    public void onDimensionChange(PlayerChangedDimensionEvent event) {
        cleanupPlayer(event.player);
    }

    public void handleDropKey(EntityPlayerMP player) {
        ItemStack stack = getPrimaryMaraca(player);
        if (!stack.isEmpty() && canUseQuickOrMelody(player, stack)) {
            performMelody(player, stack);
        }
    }

    public void handleQuickKey(EntityPlayerMP player, int targetEntityId) {
        ItemStack stack = getPrimaryMaraca(player);
        if (!stack.isEmpty() && canUseQuickOrMelody(player, stack)) {
            recordQuick(player, stack);
            if (targetEntityId < 0) {
                player.swingArm(EnumHand.MAIN_HAND);
                player.resetCooldown();
            } else {
                syncTemporaryState(player, stack);
            }
        }
    }

    public void handleSlowKey(EntityPlayerMP player, int targetEntityId) {
        ItemStack stack = getPrimaryMaraca(player);
        if (stack.isEmpty() || !canUseTemporaryOffhand(player, stack)) {
            return;
        }

        EntityLivingBase target = null;
        Entity entity = player.world.getEntityByID(targetEntityId);
        if (entity instanceof EntityLivingBase && entity != player && canAttack(player, (EntityLivingBase) entity)
                && player.getDistanceSq(entity) <= 36.0D) {
            target = (EntityLivingBase) entity;
        }
        performSlow(player, stack, target);
    }

    public void handleSwapKey(EntityPlayerMP player) {
        ItemStack stack = getPrimaryMaraca(player);
        if (stack.isEmpty() || !canUseTemporaryOffhand(player, stack)) {
            return;
        }

        recordAction(player, stack, RESONANCE);
        playResonanceSound(player);
        performResonance(player, stack);
        setCooldown(player, stack, 1.2f);
        syncTemporaryState(player, stack);
    }

    public static void recordQuick(EntityPlayer player, ItemStack stack) {
        if (player.world.isRemote || !(stack.getItem() instanceof Maraca)) {
            return;
        }
        appendRecord(player, stack, QUICK);
        showRecord(player);
        playQuickSound(player);
    }

    public static boolean isSlowAttack(EntityPlayer player) {
        return SLOW_ATTACKERS.contains(player.getUniqueID());
    }

    public boolean performSlowFromItem(EntityPlayer player, ItemStack stack, EntityLivingBase target) {
        if (player.world.isRemote || stack.isEmpty() || !(stack.getItem() instanceof Maraca)
                || !canUseTemporaryOffhand(player, stack)) {
            return false;
        }

        performSlow(player, stack, target);
        syncTemporaryState(player, stack);
        return true;
    }

    private void performSlow(EntityPlayer player, ItemStack stack, EntityLivingBase presetTarget) {
        recordAction(player, stack, SLOW);
        swingOffhand(player);
        playSlowSound(player);

        EntityLivingBase target = presetTarget != null ? presetTarget : findLookTarget(player, 4.5D);
        if (target != null && canAttack(player, target)) {
            SLOW_ATTACKERS.add(player.getUniqueID());
            try {
                ((Maraca) stack.getItem()).performSpecialAttack(stack, player, target, 1.0f, false);
            } finally {
                SLOW_ATTACKERS.remove(player.getUniqueID());
            }
        }
        setCooldown(player, stack, 1.5f);
        syncTemporaryState(player, stack);
    }

    private void performResonance(EntityPlayer player, ItemStack stack) {
        EntityLivingBase primary = findLookTarget(player, 2.5D);
        List<EntityLivingBase> targets = getHostileTargets(player, 2.5D, 2.5D);
        if (primary == null) {
            primary = targets.stream().min(Comparator.comparingDouble(player::getDistanceSq)).orElse(null);
        }

        float multiplier = hasMelody(player, Melody.PARTY) ? 1.5f : 0.75f;
        for (EntityLivingBase target : targets) {
            ((Maraca) stack.getItem()).performSpecialAttack(stack, player, target, multiplier, false);
            if (target == primary && hasMelody(player, Melody.PARTY)) {
                target.addPotionEffect(new PotionEffect(TT2Potions.IMBALANCE, 100, 0, false, false));
                target.addPotionEffect(new PotionEffect(TT2Potions.IMBALANCE_IMMUNITY, 300, 0, false, false));
            }
        }

        if (hasMelody(player, Melody.PARTY)) {
            reduceMelody(player, Melody.PARTY, PARTY_RESONANCE_COST);
        }
    }

    private void performMelody(EntityPlayer player, ItemStack stack) {
        String record = getRecord(player, stack);
        if (record.isEmpty()) {
            return;
        }

        List<QueuedMelody> queue = collectQueuedMelodies(record);
        clearRecord(player, stack);
        if (queue.isEmpty()) {
            showRecord(player);
            syncTemporaryState(player, stack);
            return;
        }

        List<ITextComponent> triggered = new ArrayList<>();
        for (QueuedMelody queued : queue) {
            applyQueuedMelody(player, queued, triggered);
        }
        showTriggeredMelodies(player, triggered);
        syncTemporaryState(player, stack);
    }

    private void applyQueuedMelody(EntityPlayer player, QueuedMelody queued, List<ITextComponent> triggered) {
        switch (queued.melody) {
            case SELF_ATTACK:
                addOrRefreshMelody(player, player, Melody.SELF_ATTACK, 600, 0.15f, false);
                triggered.add(new TextComponentTranslation("tt2.maraca.melody.self_attack"));
                break;
            case ATTACK:
                for (EntityLivingBase ally : getFriendlyEntities(player)) {
                    addOrRefreshMelody(player, ally, Melody.ATTACK,
                        TT2Config.maracaAttackMelodyDurationTicks,
                        ally == player ? (float) TT2Config.maracaAttackMelodySelfBonus : (float) TT2Config.maracaAttackMelodyAllyBonus,
                        false);
                }
                triggered.add(new TextComponentTranslation("tt2.maraca.melody.attack"));
                break;
            case KNOCKBACK_IMMUNITY:
                for (EntityLivingBase ally : getFriendlyEntities(player)) {
                    addOrRefreshMelody(player, ally, Melody.KNOCKBACK_IMMUNITY, ally == player ? 3000 : 1800, 0f, false);
                }
                triggered.add(new TextComponentTranslation("tt2.maraca.melody.stability"));
                break;
            case DEFENSE:
                for (EntityLivingBase ally : getFriendlyEntities(player)) {
                    addOrRefreshMelody(player, ally, Melody.DEFENSE, 1200, ally == player ? 0.40f : 0.25f, false);
                }
                triggered.add(new TextComponentTranslation("tt2.maraca.melody.guard"));
                break;
            case PARTY:
                int duration = clearOtherMelodies(player);
                duration = (int) Math.round(duration * TT2Config.maracaPartyDurationCoefficient);
                if (duration > 0) {
                    addOrRefreshMelody(player, player, Melody.PARTY, duration, 0f, true);
                    triggered.add(new TextComponentTranslation("tt2.maraca.melody.party"));
                }
                break;
            case EXTEND:
                for (EntityLivingBase ally : getFriendlyEntities(player)) {
                    extendMelodies(ally);
                }
                triggered.add(new TextComponentTranslation("tt2.maraca.melody.extend"));
                break;
        }
    }

    private List<QueuedMelody> collectQueuedMelodies(String record) {
        List<QueuedMelody> matches = new ArrayList<>();
        addQueuedMelody(matches, record, "QQ", Melody.SELF_ATTACK);
        addQueuedMelody(matches, record, "QRRS", Melody.ATTACK);
        addQueuedMelody(matches, record, "SRSQ", Melody.KNOCKBACK_IMMUNITY);
        addQueuedMelody(matches, record, "SQRQ", Melody.DEFENSE);
        addQueuedMelody(matches, record, "QQQS", Melody.PARTY);
        addQueuedMelody(matches, record, "RSR", Melody.EXTEND);
        matches.sort(Comparator.comparingInt(match -> match.firstIndex));
        return matches;
    }

    private void addQueuedMelody(List<QueuedMelody> matches, String record, String pattern, Melody melody) {
        int index = record.indexOf(pattern);
        if (index >= 0) {
            matches.add(new QueuedMelody(index, melody));
        }
    }

    private boolean canUseMaraca(EntityPlayer player, ItemStack stack) {
        return Maraca.canUseAsPrimary(stack) && Maraca.canAct(player, stack);
    }

    private boolean canUseQuickOrMelody(EntityPlayer player, ItemStack stack) {
        return canUseMaraca(player, stack);
    }

    private boolean canUseTemporaryOffhand(EntityPlayer player, ItemStack stack) {
        return canUseMaraca(player, stack) && Maraca.hasTemporaryOffhandCopy(player);
    }

    private void syncTemporaryState(EntityPlayer player, ItemStack stack) {
        Maraca.syncTemporaryOffhand(player);
    }

    private void recordAction(EntityPlayer player, ItemStack stack, char action) {
        if (!player.world.isRemote) {
            appendRecord(player, stack, action);
            showRecord(player);
        }
    }

    private static void appendRecord(EntityPlayer player, ItemStack stack, char action) {
        Maraca.appendRecord(stack, action);
        ACTION_RECORDS.put(player.getUniqueID(), Maraca.getRecord(stack));
    }

    private static String getRecord(EntityPlayer player, ItemStack stack) {
        String record = ACTION_RECORDS.getOrDefault(player.getUniqueID(), "");
        if (record.isEmpty()) {
            record = Maraca.getRecord(stack);
            if (!record.isEmpty()) {
                ACTION_RECORDS.put(player.getUniqueID(), record);
            }
        }
        return record;
    }

    private static void clearRecord(EntityPlayer player, ItemStack stack) {
        ACTION_RECORDS.remove(player.getUniqueID());
        Maraca.clearRecord(stack);
    }

    private static void showRecord(EntityPlayer player) {
        String record = ACTION_RECORDS.getOrDefault(player.getUniqueID(), "");
        player.sendStatusMessage(new TextComponentTranslation(
            "tt2.maraca.record",
            record.isEmpty() ? new TextComponentString("-") : buildRecordComponent(record)
        ), true);
    }

    private static ITextComponent buildRecordComponent(String record) {
        ITextComponent text = new TextComponentString("");
        for (int i = 0; i < record.length(); i++) {
            if (i > 0) {
                text.appendText("-");
            }
            text.appendSibling(actionName(record.charAt(i)));
        }
        return text;
    }

    private static ITextComponent actionName(char action) {
        ITextComponent component;
        switch (action) {
            case QUICK:
                component = new TextComponentTranslation("tt2.maraca.action.quick");
                component.getStyle().setColor(TextFormatting.RED);
                return component;
            case SLOW:
                component = new TextComponentTranslation("tt2.maraca.action.slow");
                component.getStyle().setColor(TextFormatting.BLUE);
                return component;
            case RESONANCE:
                component = new TextComponentTranslation("tt2.maraca.action.resonance");
                component.getStyle().setColor(TextFormatting.GREEN);
                return component;
            default:
                return new TextComponentString("?");
        }
    }

    private void setCooldown(EntityPlayer player, ItemStack stack, float intervals) {
        int ticks = Math.max(1, Math.round((float) (20.0D / ((Maraca) stack.getItem()).attackSpeed() * intervals)));
        player.getCooldownTracker().setCooldown(stack.getItem(), ticks);
        player.resetCooldown();
    }

    private ItemStack getPrimaryMaraca(EntityPlayer player) {
        ItemStack mainhand = player.getHeldItemMainhand();
        if (mainhand.getItem() instanceof Maraca) {
            return mainhand;
        }
        return ItemStack.EMPTY;
    }

    private boolean isRestrictedUse(EntityPlayer player) {
        ItemStack stack = getPrimaryMaraca(player);
        return !stack.isEmpty() && !canUseMaraca(player, stack);
    }

    private EntityLivingBase findLookTarget(EntityPlayer player, double range) {
        Vec3d eyes = player.getPositionEyes(1.0f);
        Vec3d look = player.getLook(1.0f);
        Vec3d end = eyes.add(look.scale(range));
        EntityLivingBase best = null;
        double bestDist = range * range;

        for (EntityLivingBase target : player.world.getEntitiesWithinAABB(
                EntityLivingBase.class,
                player.getEntityBoundingBox().grow(range, range, range),
                entity -> entity != player && entity.isEntityAlive() && canAttack(player, entity))) {
            AxisAlignedBB box = target.getEntityBoundingBox().grow(target.getCollisionBorderSize());
            RayTraceResult hit = box.calculateIntercept(eyes, end);
            if (hit != null) {
                double dist = eyes.squareDistanceTo(hit.hitVec);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = target;
                }
            }
        }
        return best;
    }

    private List<EntityLivingBase> getHostileTargets(EntityPlayer player, double horizontal, double vertical) {
        return player.world.getEntitiesWithinAABB(
            EntityLivingBase.class,
            player.getEntityBoundingBox().grow(horizontal, vertical, horizontal),
            entity -> entity != player && entity.isEntityAlive() && canAttack(player, entity)
        );
    }

    private boolean canAttack(EntityPlayer player, EntityLivingBase target) {
        return target.canBeAttackedWithItem() && !player.isOnSameTeam(target);
    }

    private List<EntityLivingBase> getFriendlyEntities(EntityPlayer player) {
        return player.world.getEntitiesWithinAABB(
            EntityLivingBase.class,
            player.getEntityBoundingBox().grow(3.5D, 2.5D, 3.5D),
            entity -> entity.isEntityAlive()
                && (entity == player
                    || entity instanceof EntityPlayer
                    || entity instanceof EntityIronGolem
                    || entity instanceof EntitySnowman
                    || isOwnedBy(entity, player))
        );
    }

    private void addOrRefreshMelody(EntityPlayer caster, EntityLivingBase target, Melody melody, int duration,
                                    float value, boolean locked) {
        EnumMap<Melody, MelodyState> map = melodies.computeIfAbsent(target.getUniqueID(), id -> new EnumMap<>(Melody.class));
        if (melody != Melody.PARTY && map.containsKey(Melody.PARTY)) {
            return;
        }

        MelodyState existing = map.get(melody);
        if (existing != null && existing.locked) {
            return;
        }

        map.put(melody, new MelodyState(caster.getUniqueID(), duration, duration, value, locked));
        applyMelodyIndicator(target, melody, duration);
        updateAttackModifiers(target);
    }

    private void updateMelodies(EntityLivingBase entity) {
        EnumMap<Melody, MelodyState> map = melodies.get(entity.getUniqueID());
        if (map == null || map.isEmpty()) {
            updateAttackModifiers(entity);
            return;
        }

        List<Melody> expired = new ArrayList<>();
        for (Map.Entry<Melody, MelodyState> entry : map.entrySet()) {
            entry.getValue().remainingTicks--;
            if (entry.getValue().remainingTicks <= 0) {
                expired.add(entry.getKey());
            }
        }

        for (Melody melody : expired) {
            clearMelodyIndicator(entity, melody);
            map.remove(melody);
        }

        if (map.isEmpty()) {
            melodies.remove(entity.getUniqueID());
        }
        updateAttackModifiers(entity);
    }

    private float getAttackBonus(EntityLivingBase entity) {
        EnumMap<Melody, MelodyState> map = melodies.get(entity.getUniqueID());
        if (map == null) {
            return 0f;
        }

        float bonus = 0f;
        MelodyState self = map.get(Melody.SELF_ATTACK);
        MelodyState attack = map.get(Melody.ATTACK);
        if (self != null) {
            bonus += self.value;
        }
        if (attack != null) {
            bonus += attack.value;
        }
        return bonus;
    }

    private boolean hasMelody(EntityLivingBase entity, Melody melody) {
        EnumMap<Melody, MelodyState> map = melodies.get(entity.getUniqueID());
        return map != null && map.containsKey(melody);
    }

    private void reduceMelody(EntityPlayer player, Melody melody, int ticks) {
        EnumMap<Melody, MelodyState> map = melodies.get(player.getUniqueID());
        if (map == null || !map.containsKey(melody)) {
            return;
        }

        MelodyState state = map.get(melody);
        state.remainingTicks -= ticks;
        if (state.remainingTicks <= 0) {
            clearMelodyIndicator(player, melody);
            map.remove(melody);
        } else {
            applyMelodyIndicator(player, melody, state.remainingTicks);
        }
    }

    private int clearOtherMelodies(EntityPlayer player) {
        EnumMap<Melody, MelodyState> map = melodies.computeIfAbsent(player.getUniqueID(), id -> new EnumMap<>(Melody.class));
        int total = 0;
        for (Melody melody : new ArrayList<>(map.keySet())) {
            if (melody != Melody.PARTY) {
                total += map.get(melody).remainingTicks;
                clearMelodyIndicator(player, melody);
                map.remove(melody);
            }
        }
        updateAttackModifiers(player);
        return total;
    }

    private void extendMelodies(EntityLivingBase entity) {
        EnumMap<Melody, MelodyState> map = melodies.get(entity.getUniqueID());
        if (map == null) {
            return;
        }

        for (Map.Entry<Melody, MelodyState> entry : map.entrySet()) {
            MelodyState state = entry.getValue();
            if (!state.locked) {
                int cap = Math.round(state.baseTicks * 1.5f);
                state.remainingTicks = Math.min(cap, state.remainingTicks + Math.round(state.baseTicks * 0.5f));
                applyMelodyIndicator(entity, entry.getKey(), state.remainingTicks);
            }
        }
    }

    private void updateAttackModifiers(EntityLivingBase entity) {
        IAttributeInstance attr = entity.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        if (attr == null) {
            return;
        }

        removeModifier(attr, SELF_ATTACK_UUID);
        removeModifier(attr, ATTACK_UUID);

        if (entity instanceof EntityPlayer) {
            return;
        }

        EnumMap<Melody, MelodyState> map = melodies.get(entity.getUniqueID());
        if (map == null) {
            return;
        }

        applyAttackModifier(attr, SELF_ATTACK_UUID, "tt2_maraca_self_attack", map.get(Melody.SELF_ATTACK));
        applyAttackModifier(attr, ATTACK_UUID, "tt2_maraca_attack", map.get(Melody.ATTACK));
    }

    private void applyAttackModifier(IAttributeInstance attr, UUID uuid, String name, MelodyState state) {
        if (state != null && state.value > 0f) {
            attr.applyModifier(new AttributeModifier(uuid, name, state.value, 2));
        }
    }

    private void removeModifier(IAttributeInstance attr, UUID uuid) {
        AttributeModifier existing = attr.getModifier(uuid);
        if (existing != null) {
            attr.removeModifier(existing);
        }
    }

    private EntityLivingBase findRedirectTarget(EntityLiving aggressor, EntityPlayer player, double range) {
        boolean boss = !aggressor.isNonBoss();
        EntityLivingBase redirect = nearest(player, player.world.getEntitiesWithinAABB(
            EntityPlayer.class,
            player.getEntityBoundingBox().grow(range, 8.0D, range),
            entity -> entity != player && entity.isEntityAlive()
        ));
        if (redirect != null) {
            return redirect;
        }

        List<EntityLivingBase> owned = player.world.getEntitiesWithinAABB(
            EntityLivingBase.class,
            player.getEntityBoundingBox().grow(range, 8.0D, range),
            entity -> entity != player && entity.isEntityAlive() && isOwnedBy(entity, player)
        );

        if (!boss) {
            redirect = nearest(player, new ArrayList<>(player.world.getEntitiesWithinAABB(
                EntityIronGolem.class,
                player.getEntityBoundingBox().grow(range, 8.0D, range),
                EntityLivingBase::isEntityAlive
            )));
            if (redirect != null) {
                return redirect;
            }

            redirect = nearest(player, new ArrayList<>(player.world.getEntitiesWithinAABB(
                EntitySnowman.class,
                player.getEntityBoundingBox().grow(range, 8.0D, range),
                EntityLivingBase::isEntityAlive
            )));
            if (redirect != null) {
                return redirect;
            }
        }

        return nearest(player, owned);
    }

    private EntityLivingBase nearest(EntityPlayer player, List<? extends EntityLivingBase> candidates) {
        return candidates.stream().min(Comparator.comparingDouble(player::getDistanceSq)).orElse(null);
    }

    private boolean isOwnedBy(EntityLivingBase entity, EntityPlayer player) {
        if (entity instanceof EntityTameable) {
            EntityTameable tameable = (EntityTameable) entity;
            return tameable.isTamed() && player.getUniqueID().equals(tameable.getOwnerId());
        }
        if (entity instanceof IEntityOwnable) {
            UUID ownerId = ((IEntityOwnable) entity).getOwnerId();
            return ownerId != null && ownerId.equals(player.getUniqueID());
        }
        return false;
    }

    private static void playQuickSound(EntityPlayer player) {
        player.world.playSound(null, player.posX, player.posY, player.posZ,
            net.minecraft.init.SoundEvents.BLOCK_SAND_PLACE, SoundCategory.PLAYERS, 0.8f, 1.0f);
    }

    private void playSlowSound(EntityPlayer player) {
        player.world.playSound(null, player.posX, player.posY, player.posZ,
            net.minecraft.init.SoundEvents.BLOCK_SAND_STEP, SoundCategory.PLAYERS, 0.8f, 1.0f);
    }

    private void playResonanceSound(EntityPlayer player) {
        playQuickSound(player);
        playSlowSound(player);
    }

    private void showTriggeredMelodies(EntityPlayer player, List<ITextComponent> triggered) {
        if (triggered.isEmpty()) {
            return;
        }

        TextComponentTranslation message = new TextComponentTranslation("tt2.maraca.melody.triggered_prefix");
        for (int i = 0; i < triggered.size(); i++) {
            if (i > 0) {
                message.appendText("+");
            }
            message.appendSibling(triggered.get(i));
        }
        player.sendStatusMessage(message, true);
    }

    private void applyMelodyIndicator(EntityLivingBase entity, Melody melody, int duration) {
        clearMelodyIndicator(entity, melody);
        PotionEffect effect = createMelodyIndicator(melody, duration);
        if (effect != null) {
            entity.addPotionEffect(effect);
        }
    }

    private void clearMelodyIndicator(EntityLivingBase entity, Melody melody) {
        Potion potion = getIndicatorPotion(melody);
        if (potion != null) {
            entity.removePotionEffect(potion);
        }
    }

    private PotionEffect createMelodyIndicator(Melody melody, int duration) {
        Potion potion = getIndicatorPotion(melody);
        if (potion == null) {
            return null;
        }
        return new PotionEffect(potion, duration, 0, false, false);
    }

    private Potion getIndicatorPotion(Melody melody) {
        switch (melody) {
            case SELF_ATTACK:
                return TT2Potions.MARACA_SELF_ATTACK;
            case ATTACK:
                return TT2Potions.MARACA_ATTACK;
            case KNOCKBACK_IMMUNITY:
                return TT2Potions.MARACA_STABILITY;
            case DEFENSE:
                return TT2Potions.MARACA_GUARD;
            case PARTY:
                return TT2Potions.MARACA_PARTY;
            default:
                return null;
        }
    }

    private void swingOffhand(EntityPlayer player) {
        player.swingArm(EnumHand.OFF_HAND);
        if (player instanceof EntityPlayerMP) {
            SPacketAnimation packet = new SPacketAnimation(player, 3);
            for (EntityPlayerMP viewer : ((EntityPlayerMP) player).getServerWorld().getPlayers(
                EntityPlayerMP.class,
                other -> other.getDistanceSq(player) < 256.0D)) {
                viewer.connection.sendPacket(packet);
            }
        }
    }

    private void cleanupPlayer(EntityPlayer player) {
        EnumMap<Melody, MelodyState> removed = melodies.remove(player.getUniqueID());
        if (removed != null) {
            updateAttackModifiers(player);
        }
        ACTION_RECORDS.remove(player.getUniqueID());
        SLOW_ATTACKERS.remove(player.getUniqueID());
        retargeting.remove(player.getUniqueID());
    }

    private enum Melody {
        SELF_ATTACK,
        ATTACK,
        KNOCKBACK_IMMUNITY,
        DEFENSE,
        PARTY,
        EXTEND
    }

    private static class MelodyState {
        final UUID caster;
        final int baseTicks;
        final float value;
        final boolean locked;
        int remainingTicks;

        MelodyState(UUID caster, int baseTicks, int remainingTicks, float value, boolean locked) {
            this.caster = caster;
            this.baseTicks = baseTicks;
            this.remainingTicks = remainingTicks;
            this.value = value;
            this.locked = locked;
        }
    }

    private static class QueuedMelody {
        final int firstIndex;
        final Melody melody;

        QueuedMelody(int firstIndex, Melody melody) {
            this.firstIndex = firstIndex;
            this.melody = melody;
        }
    }

}



