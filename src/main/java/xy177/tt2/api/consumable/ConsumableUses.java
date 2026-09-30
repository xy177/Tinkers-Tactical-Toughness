package xy177.tt2.api.consumable;

import c4.conarm.lib.tinkering.ArmorBuilder;
import c4.conarm.lib.tinkering.TinkersArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.modifiers.TinkerGuiException;
import slimeknights.tconstruct.library.tinkering.TinkersItem;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.library.utils.ToolBuilder;
import xy177.tt2.logic.ModifierWorktableLogic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/** Public, opt-in API. Mutations must run on the logical server's main thread. */
public final class ConsumableUses {
    public enum Kind { MATERIAL, MODIFIER }

    public static final String TAG = "TT2ConsumableUses";
    public static final String REMAINING = "Remaining";
    public static final String SLOTS = "SlotsPaid";
    private static final String IDENTITY = "TT2ConsumableIdentity";
    private static final Map<String, Definition> DEFINITIONS = new LinkedHashMap<>();
    private static final ThreadLocal<IdentityHashMap<ItemStack, Action>> ACTIONS =
        ThreadLocal.withInitial(IdentityHashMap::new);

    private ConsumableUses() {}

    public static final class Definition {
        public final String id;
        public final int maximum;
        public final Kind kind;

        private Definition(String id, int maximum, Kind kind) {
            this.id = id;
            this.maximum = maximum;
            this.kind = kind;
        }
    }

    public static synchronized void register(String id, int maximum, Kind kind) {
        if (id == null || id.isEmpty() || maximum < 1 || kind == null) {
            throw new IllegalArgumentException("Consumable uses require an ID, a positive maximum and a kind");
        }
        Definition old = DEFINITIONS.get(id);
        if (old != null && (old.maximum != maximum || old.kind != kind)) {
            throw new IllegalArgumentException("Conflicting consumable definition: " + id);
        }
        DEFINITIONS.put(id, new Definition(id, maximum, kind));
    }

    public static Definition definition(String id) { return DEFINITIONS.get(id); }

    public static boolean hasConsumables(ItemStack stack) {
        for (Definition def : DEFINITIONS.values()) if (isPresent(stack, def.id)) return true;
        return false;
    }

    /** Mark the original before a projectile or spell stores a copy of its tool. */
    public static void prepareSnapshot(ItemStack stack) {
        if (stack.isEmpty()) return;
        for (Definition def : DEFINITIONS.values()) {
            if (isPresent(stack, def.id)) {
                NBTTagCompound root = TagUtil.getTagSafe(stack);
                if (!root.hasUniqueId(IDENTITY)) root.setUniqueId(IDENTITY, UUID.randomUUID());
                stack.setTagCompound(root);
                return;
            }
        }
    }

    /** Resolve only opted-in tools. Never spend uses on a disposable spell snapshot. */
    public static ItemStack resolveLiveTool(ItemStack snapshot, EntityLivingBase owner) {
        if (!hasConsumables(snapshot)) return snapshot;
        NBTTagCompound tag = TagUtil.getTagSafe(snapshot);
        if (tag.hasUniqueId(IDENTITY)) {
            UUID id = tag.getUniqueId(IDENTITY);
            for (ItemStack held : owner.getEquipmentAndArmor()) {
                if (sameIdentity(held, snapshot, id)) return held;
            }
            if (owner instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) owner;
                for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                    ItemStack held = player.inventory.getStackInSlot(i);
                    if (sameIdentity(held, snapshot, id)) return held;
                }
            }
        }
        ItemStack inactive = snapshot.copy();
        for (Definition def : DEFINITIONS.values()) {
            if (isPresent(inactive, def.id)) writeRemaining(inactive, def.id, 0);
        }
        rebuild(inactive);
        return inactive;
    }

    private static boolean sameIdentity(ItemStack held, ItemStack snapshot, UUID id) {
        return !held.isEmpty() && held.getItem() == snapshot.getItem() && held.hasTagCompound()
            && held.getTagCompound().hasUniqueId(IDENTITY) && id.equals(held.getTagCompound().getUniqueId(IDENTITY));
    }

    public static boolean isPresent(ItemStack stack, String id) {
        return !stack.isEmpty() && (TinkerUtil.hasTrait(TagUtil.getTagSafe(stack), id)
            || TinkerUtil.hasModifier(TagUtil.getTagSafe(stack), id));
    }

    public static int remaining(ItemStack stack, String id) {
        Definition def = definition(id);
        return def == null || !isPresent(stack, id) ? 0 : remaining(TagUtil.getTagSafe(stack), def);
    }

    public static int remaining(NBTTagCompound root, Definition def) {
        NBTTagCompound state = root.getCompoundTag(TAG).getCompoundTag(def.id);
        return state.hasKey(REMAINING, 3)
            ? Math.max(0, Math.min(def.maximum, state.getInteger(REMAINING))) : def.maximum;
    }

    public static boolean canUse(ItemStack stack, String id, int amount) {
        return amount > 0 && remaining(stack, id) >= amount;
    }

    /** Allows the rest of an already-started action to finish its last use. */
    public static boolean isActive(ItemStack stack, String id) {
        if (definition(id) == null) return true;
        Action action = ACTIONS.get().get(stack);
        return action == null ? remaining(stack, id) > 0 : action.active.contains(id);
    }

    public static boolean tryConsume(ItemStack stack, String id, int amount) {
        if (!FMLCommonHandler.instance().getEffectiveSide().isServer() || !canUse(stack, id, amount)) return false;
        if (MinecraftForge.EVENT_BUS.post(new ConsumableUseEvent.Before(stack, id, amount))) return false;
        // A listener may have changed the tool while deciding whether consumption is allowed.
        if (!canUse(stack, id, amount)) return false;
        try (Action action = beginAction(stack)) {
            int before = remaining(stack, id);
            writeRemaining(stack, id, before - amount);
            action.changed.add(id);
            MinecraftForge.EVENT_BUS.post(new ConsumableUseEvent.Consumed(stack, id, amount));
        }
        return true;
    }

    /** Reserves the cost before running the effect; false/exception rolls back only this reservation. */
    public static boolean tryUse(ItemStack stack, String id, int amount, BooleanSupplier effect) {
        if (effect == null) throw new IllegalArgumentException("Missing effect");
        if (!FMLCommonHandler.instance().getEffectiveSide().isServer() || !canUse(stack, id, amount)) return false;
        if (MinecraftForge.EVENT_BUS.post(new ConsumableUseEvent.Before(stack, id, amount))
            || !canUse(stack, id, amount)) return false;
        try (Action action = beginAction(stack)) {
            writeRemaining(stack, id, remaining(stack, id) - amount);
            action.changed.add(id);
            boolean success = false;
            try {
                success = effect.getAsBoolean();
                if (success) MinecraftForge.EVENT_BUS.post(new ConsumableUseEvent.Consumed(stack, id, amount));
                return success;
            } finally {
                if (!success) writeRemaining(stack, id, Math.min(definition(id).maximum, remaining(stack, id) + amount));
            }
        }
    }

    /** Explicit restoration for other mods; normal durability healing never calls this. */
    public static int restore(ItemStack stack, String id, int amount) {
        Definition def = definition(id);
        if (def == null || amount <= 0 || !isPresent(stack, id)
            || !FMLCommonHandler.instance().getEffectiveSide().isServer()) return 0;
        int before = remaining(stack, id);
        int after = (int) Math.min(def.maximum, (long) before + amount);
        if (after == before) return 0;
        try (Action action = beginAction(stack)) {
            writeRemaining(stack, id, after);
            action.changed.add(id);
            MinecraftForge.EVENT_BUS.post(new ConsumableUseEvent.Restored(stack, id, after - before));
        }
        return after - before;
    }

    public static boolean needsRepair(ItemStack stack) {
        for (Definition def : DEFINITIONS.values()) {
            if (def.kind == Kind.MATERIAL && isPresent(stack, def.id) && remaining(stack, def.id) < def.maximum) return true;
        }
        return false;
    }

    /** Operates on the repair recipe's RESULT copy, including client recipe previews. */
    public static void restoreMaterialUsesOnRepair(ItemStack result) {
        boolean changed = false;
        for (Definition def : DEFINITIONS.values()) {
            if (def.kind == Kind.MATERIAL && isPresent(result, def.id) && remaining(result, def.id) < def.maximum) {
                writeRemaining(result, def.id, def.maximum);
                changed = true;
            }
        }
        if (changed) rebuild(result);
    }

    public static NBTTagCompound state(NBTTagCompound root, Definition def) {
        NBTTagCompound all = root.getCompoundTag(TAG);
        NBTTagCompound value = all.getCompoundTag(def.id);
        if (!value.hasKey(REMAINING, 3)) value.setInteger(REMAINING, def.maximum);
        value.setString("Kind", def.kind.name());
        all.setTag(def.id, value);
        root.setTag(TAG, all);
        return value;
    }

    private static void writeRemaining(ItemStack stack, String id, int amount) {
        NBTTagCompound root = TagUtil.getTagSafe(stack);
        state(root, definition(id)).setInteger(REMAINING, amount);
        stack.setTagCompound(root);
        refreshDisplay(root, id);
    }

    public static void refreshDisplay(NBTTagCompound root, String id) {
        Definition def = definition(id);
        if (def == null) return;
        NBTTagList modifiers = TagUtil.getModifiersTagList(root);
        for (int i = 0; i < modifiers.tagCount(); i++) {
            NBTTagCompound tag = modifiers.getCompoundTagAt(i);
            if (id.equals(tag.getString("identifier"))) tag.setInteger(TAG, remaining(root, def));
        }
    }

    public static void recordApplication(IModifier modifier, NBTTagCompound root, int slotsBefore) {
        Definition def = definition(modifier.getIdentifier());
        if (def == null) {
            if (ModifierWorktableLogic.hasConsumableEmboss(modifier.getIdentifier())) {
                NBTTagCompound all = root.getCompoundTag(TAG);
                NBTTagCompound value = all.getCompoundTag(modifier.getIdentifier());
                value.setInteger(SLOTS, value.getInteger(SLOTS) + Math.max(0, TagUtil.getBaseModifiersUsed(root) - slotsBefore));
                all.setTag(modifier.getIdentifier(), value);
                root.setTag(TAG, all);
            }
            return;
        }
        NBTTagCompound state = state(root, def);
        if (def.kind == Kind.MODIFIER) {
            int paid = Math.max(0, TagUtil.getBaseModifiersUsed(root) - slotsBefore);
            state.setInteger(SLOTS, state.getInteger(SLOTS) + paid);
        }
        refreshDisplay(root, def.id);
    }

    public static int paidSlots(ItemStack stack, String id) {
        return TagUtil.getTagSafe(stack).getCompoundTag(TAG).getCompoundTag(id).getInteger(SLOTS);
    }

    /** A crystal carries counts, never a fresh pool. Existing destination counts can only decrease. */
    public static NBTTagCompound snapshot(ItemStack tool, Iterable<String> ids) {
        NBTTagCompound result = new NBTTagCompound();
        for (String id : ids) {
            Definition def = definition(id);
            if (def != null && isPresent(tool, id)) {
                NBTTagCompound value = new NBTTagCompound();
                value.setInteger(REMAINING, remaining(tool, id));
                result.setTag(id, value);
            }
        }
        return result;
    }

    public static void mergeSnapshot(ItemStack tool, NBTTagCompound snapshot) {
        NBTTagCompound root = TagUtil.getTagSafe(tool);
        for (String id : snapshot.getKeySet()) {
            Definition def = definition(id);
            if (def == null || !isPresent(tool, id)) continue;
            int count = Math.max(0, snapshot.getCompoundTag(id).getInteger(REMAINING));
            state(root, def).setInteger(REMAINING, Math.min(remaining(root, def), count));
            refreshDisplay(root, id);
        }
        tool.setTagCompound(root);
        rebuild(tool);
    }

    /** Removes only an explicitly applied consumable modifier, never a material-provided trait. */
    public static boolean removeModifier(ItemStack stack, String id) {
        Definition def = definition(id);
        if (def == null || def.kind != Kind.MODIFIER) return false;
        NBTTagCompound root = TagUtil.getTagSafe(stack);
        NBTTagList base = TagUtil.getBaseModifiersTagList(root);
        boolean applied = false;
        for (int i = base.tagCount() - 1; i >= 0; i--) {
            if (id.equals(base.getStringTagAt(i))) { base.removeTag(i); applied = true; }
        }
        // Hidden modifiers are intentionally absent from BaseModifiers, but still have their data.
        if (!applied && !TinkerUtil.hasModifier(root, id)
            && !root.getCompoundTag(TAG).getCompoundTag(id).hasKey(SLOTS)) return false;
        int slots = Math.max(0, paidSlots(stack, id));
        TagUtil.setBaseModifiersUsed(root, Math.max(0, TagUtil.getBaseModifiersUsed(root) - slots));
        TagUtil.setBaseModifiersTagList(root, base);
        NBTTagList modifiers = TagUtil.getModifiersTagList(root);
        for (int i = modifiers.tagCount() - 1; i >= 0; i--) {
            if (id.equals(modifiers.getCompoundTagAt(i).getString("identifier"))) modifiers.removeTag(i);
        }
        TagUtil.setModifiersTagList(root, modifiers);
        NBTTagList traits = TagUtil.getTraitsTagList(root);
        for (int i = traits.tagCount() - 1; i >= 0; i--) if (id.equals(traits.getStringTagAt(i))) traits.removeTag(i);
        TagUtil.setTraitsTagList(root, traits);
        root.getCompoundTag(TAG).removeTag(id);
        NBTTagCompound extra = TagUtil.getExtraTag(root);
        NBTTagList hidden = extra.getCompoundTag("TT2ModifierWorktable").getTagList("HiddenModifiers", 8);
        for (int i = hidden.tagCount() - 1; i >= 0; i--) if (id.equals(hidden.getStringTagAt(i))) hidden.removeTag(i);
        stack.setTagCompound(root);
        rebuild(stack);
        return true;
    }

    public static void rebuild(ItemStack stack) {
        try {
            ItemStack before = stack.copy();
            if (stack.getItem() instanceof TinkersItem) ToolBuilder.rebuildTool(stack.getTagCompound(), (TinkersItem) stack.getItem());
            else if (stack.getItem() instanceof TinkersArmor) ArmorBuilder.rebuildArmor(stack.getTagCompound(), (TinkersArmor) stack.getItem());
            ModifierWorktableLogic.repairCurrentHiddenModifiers(stack, before);
        } catch (TinkerGuiException ex) {
            throw new IllegalStateException("Could not rebuild consumable tool", ex);
        }
    }

    public static Action beginAction(ItemStack stack) {
        IdentityHashMap<ItemStack, Action> actions = ACTIONS.get();
        Action action = actions.get(stack);
        if (action == null) {
            action = new Action(stack, null);
            actions.put(stack, action);
        }
        action.depth++;
        return action;
    }

    /** Starts a distinct target/action, even when invoked by another attack on the same tool. */
    public static Action beginAttack(ItemStack stack) {
        IdentityHashMap<ItemStack, Action> actions = ACTIONS.get();
        Action action = new Action(stack, actions.get(stack));
        action.depth = 1;
        actions.put(stack, action);
        return action;
    }

    public static final class Action implements AutoCloseable {
        private final ItemStack stack;
        private Action parent;
        private final Set<String> active = new HashSet<>();
        private final Set<String> changed = new HashSet<>();
        private final Map<String, Integer> before = new HashMap<>();
        private int depth;
        private boolean detached;
        private boolean resumed;

        private Action(ItemStack stack, Action parent) {
            this.stack = stack;
            this.parent = parent;
            for (Definition def : DEFINITIONS.values()) {
                int count = remaining(stack, def.id);
                before.put(def.id, count);
                if (count > 0) active.add(def.id);
            }
        }

        /** Detach between synchronous Forge hurt/damage events; caller must resume or close it. */
        public Action suspend() {
            if (depth != 1 || detached) throw new IllegalStateException("Only the outer action may be suspended");
            if (parent == null) ACTIONS.get().remove(stack);
            else ACTIONS.get().put(stack, parent);
            detached = true;
            return this;
        }

        public Action resume() {
            if (!detached || depth != 1) throw new IllegalStateException("Action is not suspended");
            parent = ACTIONS.get().get(stack);
            ACTIONS.get().put(stack, this);
            detached = false;
            resumed = true;
            return this;
        }

        @Override public void close() {
            if (--depth != 0) return;
            if (!detached && parent != null) {
                ACTIONS.get().put(stack, parent);
                if (!resumed) {
                    parent.changed.addAll(changed);
                    return;
                }
            } else if (!detached) {
                ACTIONS.get().remove(stack);
            }
            if (ACTIONS.get().isEmpty()) ACTIONS.remove();
            boolean rebuild = false;
            for (String id : changed) {
                int count = remaining(stack, id);
                if (before.getOrDefault(id, 0) > 0 && count == 0) {
                    Definition def = definition(id);
                    if (def.kind == Kind.MODIFIER) removeModifier(stack, id);
                    else rebuild = true;
                    MinecraftForge.EVENT_BUS.post(new ConsumableUseEvent.Exhausted(stack, id, 0));
                } else if (before.getOrDefault(id, 0) == 0 && count > 0) rebuild = true;
            }
            if (rebuild) rebuild(stack);
        }
    }
}
