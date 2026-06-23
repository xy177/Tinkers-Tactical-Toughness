package xy177.tt2.logic;

import c4.conarm.lib.armor.ArmorCore;
import c4.conarm.lib.ArmoryRegistry;
import c4.conarm.lib.traits.IArmorTrait;
import net.minecraft.init.Items;
import net.minecraft.init.Blocks;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import slimeknights.mantle.util.RecipeMatch;
import slimeknights.mantle.util.RecipeMatchRegistry;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.modifiers.ModifierTrait;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.TinkerFluids;
import xy177.tt2.init.TT2Items;
import xy177.tt2.item.ItemExperienceBottle;
import xy177.tt2.item.ItemModifierCrystal;
import xy177.tt2.modifiers.ModExperienceTransfer;
import xy177.tt2.tile.TileModifierWorktable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ModifierWorktableLogic {

    public static final String TAG_TT2 = "TT2ModifierWorktable";
    public static final String TAG_HIDDEN = "HiddenModifiers";
    public static final int TYPE_REMOVE = 1;
    public static final int TYPE_HIDE = 2;
    public static final int TYPE_UNHIDE = 3;
    public static final int TYPE_SORT = 4;
    public static final int TYPE_EXTRACT = 5;
    public static final int TYPE_EXTRACT_FORTIFY = 6;
    public static final int TYPE_EXTRACT_EMBOSS = 7;
    public static final int TYPE_EXTRACT_EXPERIENCE = 8;

    private ModifierWorktableLogic() {
    }

    public static boolean isTinkerItem(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof ToolCore || item instanceof ArmorCore;
    }

    public static List<String> getSelectableModifiers(ItemStack tool) {
        List<String> ids = new ArrayList<>();
        if (!isTinkerItem(tool)) {
            return ids;
        }
        NBTTagList modifiers = TagUtil.getModifiersTagList(tool);
        for (int i = 0; i < modifiers.tagCount(); i++) {
            String id = ModifierNBT.readTag(modifiers.getCompoundTagAt(i)).identifier;
            if (getModifier(id) != null && !ids.contains(id)) {
                ids.add(id);
            }
        }
        return ids;
    }

    public static List<String> getSelectableModifiers(TileModifierWorktable tile) {
        return getSelectableModifiers(tile, 0);
    }

    public static List<String> getSelectableModifiers(TileModifierWorktable tile, int selectedAction) {
        ItemStack tool = tile.getStackInSlot(TileModifierWorktable.SLOT_TOOL);
        List<String> ids = new ArrayList<>();
        for (String id : getSelectableModifiers(tool)) {
            int action = selectedAction == 0 ? action(tile, tool, id) : selectedAction;
            if (action != 0 && canApplyAction(tool, id, action)) {
                ids.add(id);
            }
        }
        return ids;
    }

    public static List<Integer> getSelectableActions(TileModifierWorktable tile) {
        ItemStack tool = tile.getStackInSlot(TileModifierWorktable.SLOT_TOOL);
        List<Integer> actions = new ArrayList<>();
        if (!isTinkerItem(tool)) {
            return actions;
        }
        int[] order = { TYPE_EXTRACT, TYPE_EXTRACT_EMBOSS, TYPE_EXTRACT_FORTIFY, TYPE_REMOVE, TYPE_HIDE, TYPE_UNHIDE, TYPE_SORT, TYPE_EXTRACT_EXPERIENCE };
        for (int action : order) {
            actions.add(action);
        }
        return actions;
    }

    public static IModifier getModifier(String id) {
        IModifier modifier = TinkerRegistry.getModifier(id);
        return modifier == null ? ArmoryRegistry.getArmorModifier(id) : modifier;
    }

    public static String getActionKey(TileModifierWorktable tile, String modifier) {
        return getActionKey(tile, modifier, 0);
    }

    public static String getActionKey(TileModifierWorktable tile, String modifier, int selectedAction) {
        if (modifier.isEmpty()) {
            return "";
        }
        ItemStack tool = tile.getStackInSlot(TileModifierWorktable.SLOT_TOOL);
        if (!isTinkerItem(tool) || !hasModifier(tool, modifier)) {
            return "";
        }
        int action = selectedAction == 0 ? action(tile, tool, modifier) : selectedAction;
        if (!canApplyAction(tool, modifier, action)) {
            return "";
        }
        return actionKey(action);
    }

    public static String actionKey(int action) {
        if (action == TYPE_REMOVE) return "remove";
        if (action == TYPE_HIDE) return "hide";
        if (action == TYPE_UNHIDE) return "unhide";
        if (action == TYPE_SORT) return "sort";
        if (action == TYPE_EXTRACT) return "extract";
        if (action == TYPE_EXTRACT_EXPERIENCE) return "extract_experience";
        if (action == TYPE_EXTRACT_EMBOSS) return "extract_emboss";
        if (action == TYPE_EXTRACT_FORTIFY) return "extract_fortify";
        return "";
    }

    public static ItemStack getResult(TileModifierWorktable tile, String modifier) {
        return getResult(tile, modifier, 0);
    }

    public static ItemStack getResult(TileModifierWorktable tile, String modifier, int selectedAction) {
        if (modifier.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack tool = tile.getStackInSlot(TileModifierWorktable.SLOT_TOOL);
        if (!isTinkerItem(tool) || !hasModifier(tool, modifier)) {
            return ItemStack.EMPTY;
        }
        int action = action(tile, tool, modifier);
        if (selectedAction != 0 && action == selectedAction) {
            action = selectedAction;
        } else if (selectedAction != 0) {
            return ItemStack.EMPTY;
        }
        if (action == 0) {
            return ItemStack.EMPTY;
        }
        if (isExtraction(action)) {
            if (action == TYPE_EXTRACT_EXPERIENCE) {
                int experience = ModExperienceTransfer.totalExperience(tool, modifier);
                return experience > 0 ? ItemExperienceBottle.withExperience(experience) : ItemStack.EMPTY;
            }
            ModifierNBT data = modifierData(tool, modifier);
            boolean emboss = action == TYPE_EXTRACT_EMBOSS;
            int value = action == TYPE_EXTRACT ? extractValue(tool, modifier) : 1;
            int maxValue = action == TYPE_EXTRACT ? extractMaxValue(tool, modifier) : 0;
            return ItemModifierCrystal.withModifier(modifier, 1, data.color, emboss, value, maxValue);
        }
        ItemStack result = tool.copy();
        result.setCount(1);
        applyToTool(result, modifier, action);
        repairCurrentHiddenModifiers(result, tool);
        return result;
    }

    public static void apply(TileModifierWorktable tile, String modifier) {
        apply(tile, modifier, 0);
    }

    public static void apply(TileModifierWorktable tile, String modifier, int selectedAction) {
        ItemStack result = getResult(tile, modifier, selectedAction);
        if (result.isEmpty()) {
            return;
        }
        ItemStack tool = tile.getStackInSlot(TileModifierWorktable.SLOT_TOOL);
        int action = action(tile, tool, modifier);
        if (selectedAction != 0) {
            if (action != selectedAction) {
                return;
            }
            action = selectedAction;
        }
        ItemStack input1 = tile.getStackInSlot(TileModifierWorktable.SLOT_INPUT_1);
        ItemStack input2 = tile.getStackInSlot(TileModifierWorktable.SLOT_INPUT_2);

        if (action == TYPE_HIDE) {
            consumeOne(input1);
            tile.setInventorySlotContents(TileModifierWorktable.SLOT_INPUT_1, new ItemStack(Items.GLASS_BOTTLE));
        } else if (action == TYPE_UNHIDE) {
            consumeOne(input1);
            tile.setInventorySlotContents(TileModifierWorktable.SLOT_INPUT_1, new ItemStack(Items.BUCKET));
        } else if (action == TYPE_SORT) {
        } else if (action == TYPE_REMOVE || action == TYPE_EXTRACT || action == TYPE_EXTRACT_EXPERIENCE || action == TYPE_EXTRACT_FORTIFY
            || action == TYPE_EXTRACT_EMBOSS) {
            if (action == TYPE_REMOVE) {
                consumeRemovalInput(tile, input1);
            } else {
                ItemStack originalTool = tool.copy();
                applyToTool(tool, modifier, action);
                repairCurrentHiddenModifiers(tool, originalTool);
                tile.setInventorySlotContents(TileModifierWorktable.SLOT_TOOL, tool);
                consumeOne(input1);
                consumeExtractionSecond(input2, action);
                return;
            }
        }
        tile.setInventorySlotContents(TileModifierWorktable.SLOT_TOOL, ItemStack.EMPTY);
    }

    public static ItemStack actionIcon(int action) {
        if (action == TYPE_EXTRACT) return TinkerCommons.matSlimeCrystalGreen.copy();
        if (action == TYPE_EXTRACT_EXPERIENCE) return new ItemStack(TT2Items.EXPERIENCE_BOTTLE);
        if (action == TYPE_EXTRACT_EMBOSS) return TinkerCommons.matSlimeCrystalMagma.copy();
        if (action == TYPE_EXTRACT_FORTIFY) return TinkerCommons.matSlimeCrystalBlue.copy();
        if (action == TYPE_REMOVE) return new ItemStack(Blocks.SPONGE, 1, 1);
        if (action == TYPE_HIDE) return new ItemStack(Items.POTIONITEM);
        if (action == TYPE_UNHIDE) return new ItemStack(Items.MILK_BUCKET);
        if (action == TYPE_SORT) return new ItemStack(Items.COMPASS);
        return ItemStack.EMPTY;
    }

    public static boolean isHidden(ItemStack stack, String modifier) {
        NBTTagList hidden = hiddenList(stack);
        for (int i = 0; i < hidden.tagCount(); i++) {
            if (modifier.equals(hidden.getStringTagAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static int action(TileModifierWorktable tile, ItemStack tool, String modifier) {
        ItemStack input1 = tile.getStackInSlot(TileModifierWorktable.SLOT_INPUT_1);
        ItemStack input2 = tile.getStackInSlot(TileModifierWorktable.SLOT_INPUT_2);
        if (isRemovalInput(input1)) return TYPE_REMOVE;
        if (isInvisibilityPotion(input1)) return TYPE_HIDE;
        if (input1.getItem() == Items.MILK_BUCKET && isHidden(tool, modifier)) return TYPE_UNHIDE;
        if (input1.getItem() == Items.COMPASS) return TYPE_SORT;
        if (sameItem(input1, TinkerCommons.matSlimeCrystalGreen) && input2.getItem() == Items.DYE
            && input2.getMetadata() == 4 && input2.getCount() >= 3 && isRegularExtractable(modifier)) return TYPE_EXTRACT;
        if (sameItem(input1, TinkerCommons.matMendingMoss) && input2.getItem() == Items.EXPERIENCE_BOTTLE
            && isLevelingModifier(modifier)) return TYPE_EXTRACT_EXPERIENCE;
        if (sameItem(input1, TinkerCommons.matSlimeCrystalBlue) && input2.getItem() == Items.GOLD_INGOT
            && isFortifyOrPolished(modifier)) return TYPE_EXTRACT_FORTIFY;
        if (sameItem(input1, TinkerCommons.matSlimeCrystalMagma) && input2.getItem() == Items.DIAMOND
            && isEmboss(modifier)) return TYPE_EXTRACT_EMBOSS;
        return 0;
    }

    public static boolean canApplyAction(ItemStack tool, String modifier, int action) {
        if (!isTinkerItem(tool) || !hasModifier(tool, modifier)) {
            return false;
        }
        if (action == TYPE_REMOVE) return isAdjustableModifier(modifier);
        if (action == TYPE_HIDE) return isAdjustableModifier(modifier) && !isHidden(tool, modifier);
        if (action == TYPE_UNHIDE) return isHidden(tool, modifier) && !isEmboss(modifier);
        if (action == TYPE_SORT) return canSortModifier(tool, modifier);
        if (action == TYPE_EXTRACT) return isRegularExtractable(modifier);
        if (action == TYPE_EXTRACT_EXPERIENCE) return isLevelingModifier(modifier)
            && ModExperienceTransfer.totalExperience(tool, modifier) > 0;
        if (action == TYPE_EXTRACT_FORTIFY) return isFortifyOrPolished(modifier);
        if (action == TYPE_EXTRACT_EMBOSS) return isEmboss(modifier);
        return false;
    }

    private static boolean canSortModifier(ItemStack tool, String modifier) {
        if (!isAdjustableModifier(modifier)) {
            return false;
        }
        List<String> visual = visualModifiers(tool);
        int index = visual.indexOf(modifier);
        return index >= 0 && index < visual.size() - 1;
    }

    private static void removeModifier(ItemStack tool, String modifier) {
        Set<String> linkedTraits = isEmboss(modifier) ? removableEmbossTraits(tool, modifier) : new HashSet<>();
        Set<String> removeIds = new HashSet<>();
        removeIds.add(modifier);
        removeIds.addAll(linkedTraits);

        NBTTagList base = TagUtil.getBaseModifiersTagList(tool);
        NBTTagList newBase = new NBTTagList();
        for (int i = 0; i < base.tagCount(); i++) {
            String id = base.getStringTagAt(i);
            if (!removeIds.contains(id)) {
                newBase.appendTag(new NBTTagString(id));
            }
        }
        TagUtil.setBaseModifiersTagList(tool, newBase);

        NBTTagList modifiers = TagUtil.getModifiersTagList(tool);
        NBTTagList newModifiers = new NBTTagList();
        for (int i = 0; i < modifiers.tagCount(); i++) {
            NBTTagCompound tag = modifiers.getCompoundTagAt(i);
            if (!removeIds.contains(ModifierNBT.readTag(tag).identifier)) {
                newModifiers.appendTag(tag.copy());
            }
        }
        TagUtil.setModifiersTagList(tool, newModifiers);

        NBTTagList traits = TagUtil.getTraitsTagList(tool);
        NBTTagList newTraits = new NBTTagList();
        for (int i = 0; i < traits.tagCount(); i++) {
            String id = traits.getStringTagAt(i);
            if (!removeIds.contains(id)) {
                newTraits.appendTag(new NBTTagString(id));
            }
        }
        TagUtil.setTraitsTagList(tool, newTraits);

        clearHidden(tool, modifier);
        returnSlot(tool);
        rebuild(tool);
        if (isEmboss(modifier)) {
            purgeIds(tool, removeIds);
        }
    }

    private static Set<String> removableEmbossTraits(ItemStack tool, String modifier) {
        Set<String> traits = embossTraitIds(modifier);
        traits.removeAll(baseMaterialTraits(tool));
        traits.removeAll(otherEmbossTraits(tool, modifier));
        return traits;
    }

    private static Set<String> otherEmbossTraits(ItemStack tool, String removedModifier) {
        Set<String> traits = new HashSet<>();
        NBTTagList base = TagUtil.getBaseModifiersTagList(tool);
        for (int i = 0; i < base.tagCount(); i++) {
            String id = base.getStringTagAt(i);
            if (isEmboss(id) && !removedModifier.equals(id)) {
                traits.addAll(embossTraitIds(id));
            }
        }
        return traits;
    }

    private static Set<String> baseMaterialTraits(ItemStack tool) {
        ItemStack copy = tool.copy();
        if (!copy.hasTagCompound()) {
            return new HashSet<>();
        }
        TagUtil.setBaseModifiersTagList(copy, new NBTTagList());
        TagUtil.setModifiersTagList(copy, new NBTTagList());
        TagUtil.setTraitsTagList(copy, new NBTTagList());
        TagUtil.setBaseModifiersUsed(copy.getTagCompound(), 0);
        rebuild(copy);
        Set<String> traits = stringSet(TagUtil.getTraitsTagList(copy));
        traits.addAll(stringSet(TagUtil.getBaseModifiersTagList(copy)));
        NBTTagList modifiers = TagUtil.getModifiersTagList(copy);
        for (int i = 0; i < modifiers.tagCount(); i++) {
            traits.add(ModifierNBT.readTag(modifiers.getCompoundTagAt(i)).identifier);
        }
        return traits;
    }

    private static Set<String> embossTraitIds(String modifier) {
        Set<String> traits = new HashSet<>();
        IModifier mod = getModifier(modifier);
        if (mod == null) {
            return traits;
        }
        Object value = fieldValue(mod, "traits");
        if (!(value instanceof Collection)) {
            return traits;
        }
        for (Object entry : (Collection<?>) value) {
            if (entry instanceof ITrait) {
                traits.add(((ITrait) entry).getIdentifier());
            }
        }
        return traits;
    }

    private static Object fieldValue(Object object, String name) {
        Class<?> type = object.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(object);
            } catch (ReflectiveOperationException ignored) {
                type = type.getSuperclass();
            }
        }
        return null;
    }

    private static Set<String> stringSet(NBTTagList list) {
        Set<String> values = new HashSet<>();
        for (int i = 0; i < list.tagCount(); i++) {
            values.add(list.getStringTagAt(i));
        }
        return values;
    }

    private static void purgeIds(ItemStack tool, Set<String> ids) {
        NBTTagList base = TagUtil.getBaseModifiersTagList(tool);
        NBTTagList newBase = new NBTTagList();
        for (int i = 0; i < base.tagCount(); i++) {
            String id = base.getStringTagAt(i);
            if (!ids.contains(id)) {
                newBase.appendTag(new NBTTagString(id));
            }
        }
        TagUtil.setBaseModifiersTagList(tool, newBase);

        NBTTagList modifiers = TagUtil.getModifiersTagList(tool);
        NBTTagList newModifiers = new NBTTagList();
        for (int i = 0; i < modifiers.tagCount(); i++) {
            NBTTagCompound tag = modifiers.getCompoundTagAt(i);
            if (!ids.contains(ModifierNBT.readTag(tag).identifier)) {
                newModifiers.appendTag(tag.copy());
            }
        }
        TagUtil.setModifiersTagList(tool, newModifiers);

        NBTTagList traits = TagUtil.getTraitsTagList(tool);
        NBTTagList newTraits = new NBTTagList();
        for (int i = 0; i < traits.tagCount(); i++) {
            String id = traits.getStringTagAt(i);
            if (!ids.contains(id)) {
                newTraits.appendTag(new NBTTagString(id));
            }
        }
        TagUtil.setTraitsTagList(tool, newTraits);
    }

    private static void reduceModifier(ItemStack tool, String modifier, int amount) {
        NBTTagList modifiers = TagUtil.getModifiersTagList(tool);
        int index = TinkerUtil.getIndexInCompoundList(modifiers, modifier);
        if (index < 0) {
            removeModifier(tool, modifier);
            return;
        }
        NBTTagCompound tag = modifiers.getCompoundTagAt(index);
        ModifierNBT data = ModifierNBT.readTag(tag);
        if (tag.hasKey("current") && tag.hasKey("max") && tag.getInteger("current") > amount) {
            ModifierNBT.IntegerNBT valueData = ModifierNBT.readInteger(tag);
            int oldLevel = data.level;
            int current = tag.getInteger("current") - amount;
            valueData.level = extractLevelForCurrent(modifier, current);
            valueData.write(tag);
            tag.setInteger("current", current);
            tag.setInteger("max", getMaxForLevel(modifier, Math.max(1, valueData.level)));
            modifiers.set(index, tag);
            TagUtil.setModifiersTagList(tool, modifiers);
            returnSlots(tool, oldLevel - valueData.level);
            rebuild(tool);
        } else if (data.level > 1) {
            data.level--;
            data.write(tag);
            modifiers.set(index, tag);
            TagUtil.setModifiersTagList(tool, modifiers);
            returnSlot(tool);
            rebuild(tool);
        } else {
            removeModifier(tool, modifier);
        }
    }

    private static void applyToTool(ItemStack tool, String modifier, int action) {
        if (action == TYPE_HIDE) {
            setHidden(tool, modifier, true);
        } else if (action == TYPE_UNHIDE) {
            setHidden(tool, modifier, false);
        } else if (action == TYPE_SORT) {
            sortVisualModifier(tool, modifier);
        } else if (action == TYPE_EXTRACT) {
            reduceModifier(tool, modifier, extractValue(tool, modifier));
        } else if (action == TYPE_EXTRACT_EXPERIENCE) {
            ModExperienceTransfer.extractExperience(tool, modifier, ModExperienceTransfer.totalExperience(tool, modifier));
        } else {
            removeModifier(tool, modifier);
        }
    }

    private static boolean isExtraction(int action) {
        return action == TYPE_EXTRACT || action == TYPE_EXTRACT_EXPERIENCE || action == TYPE_EXTRACT_FORTIFY || action == TYPE_EXTRACT_EMBOSS;
    }

    private static void sortVisualModifier(ItemStack tool, String modifier) {
        List<String> visual = visualModifiers(tool);
        int index = visual.indexOf(modifier);
        if (index < 0 || index >= visual.size() - 1) {
            return;
        }
        String next = visual.get(index + 1);
        visual.set(index + 1, modifier);
        visual.set(index, next);
        NBTTagList sorted = new NBTTagList();
        for (String id : visual) {
            sorted.appendTag(new NBTTagString(id));
        }
        TagUtil.setBaseModifiersTagList(tool, sorted);
    }

    private static List<String> visualModifiers(ItemStack tool) {
        List<String> ids = new ArrayList<>();
        NBTTagList base = TagUtil.getBaseModifiersTagList(tool);
        for (int i = 0; i < base.tagCount(); i++) {
            ids.add(base.getStringTagAt(i));
        }
        for (String id : getSelectableModifiers(tool)) {
            if (!ids.contains(id) && isAdjustableModifier(id)) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static void returnSlot(ItemStack tool) {
        NBTTagCompound tag = tool.getTagCompound();
        if (tag == null) {
            return;
        }
        int used = TagUtil.getBaseModifiersUsed(tag);
        TagUtil.setBaseModifiersUsed(tag, Math.max(0, used - 1));
        tool.setTagCompound(tag);
    }

    private static void returnSlots(ItemStack tool, int count) {
        for (int i = 0; i < count; i++) {
            returnSlot(tool);
        }
    }

    private static void rebuild(ItemStack tool) {
        try {
            if (tool.getItem() instanceof ToolCore) {
                slimeknights.tconstruct.library.utils.ToolBuilder.rebuildTool(tool.getTagCompound(), (ToolCore) tool.getItem());
            } else if (tool.getItem() instanceof c4.conarm.lib.tinkering.TinkersArmor) {
                c4.conarm.lib.tinkering.ArmorBuilder.rebuildArmor(tool.getTagCompound(),
                    (c4.conarm.lib.tinkering.TinkersArmor) tool.getItem());
            }
            applyHiddenVisuals(tool);
        } catch (Exception ignored) {
        }
    }

    private static void applyHiddenVisuals(ItemStack stack) {
        NBTTagList hidden = hiddenList(stack);
        for (int i = 0; i < hidden.tagCount(); i++) {
            removeVisualModifier(stack, hidden.getStringTagAt(i));
        }
    }

    private static void setHidden(ItemStack stack, String modifier, boolean hide) {
        NBTTagCompound extra = TagUtil.getExtraTag(stack);
        boolean found = writeHidden(extra, modifier, hide);
        TagUtil.setExtraTag(stack, extra);
        if (hide) {
            removeVisualModifier(stack, modifier);
        } else if (found) {
            restoreVisualModifier(stack, modifier);
        }
    }

    private static void clearHidden(ItemStack stack, String modifier) {
        NBTTagCompound extra = TagUtil.getExtraTag(stack);
        writeHidden(extra, modifier, false);
        TagUtil.setExtraTag(stack, extra);
    }

    private static boolean writeHidden(NBTTagCompound extra, String modifier, boolean hide) {
        NBTTagCompound tt2 = extra.getCompoundTag(TAG_TT2);
        NBTTagList hidden = tt2.getTagList(TAG_HIDDEN, TagUtil.TAG_TYPE_STRING);
        NBTTagList next = new NBTTagList();
        boolean found = false;
        for (int i = 0; i < hidden.tagCount(); i++) {
            String id = hidden.getStringTagAt(i);
            if (modifier.equals(id)) {
                found = true;
                if (!hide) {
                    continue;
                }
            }
            next.appendTag(new NBTTagString(id));
        }
        if (hide && !found) {
            next.appendTag(new NBTTagString(modifier));
        }
        tt2.setTag(TAG_HIDDEN, next);
        extra.setTag(TAG_TT2, tt2);
        return found;
    }

    private static void removeVisualModifier(ItemStack stack, String modifier) {
        NBTTagList base = TagUtil.getBaseModifiersTagList(stack);
        NBTTagList next = new NBTTagList();
        for (int i = 0; i < base.tagCount(); i++) {
            String id = base.getStringTagAt(i);
            if (!modifier.equals(id)) {
                next.appendTag(new NBTTagString(id));
            }
        }
        TagUtil.setBaseModifiersTagList(stack, next);
    }

    private static void restoreVisualModifier(ItemStack stack, String modifier) {
        NBTTagList base = TagUtil.getBaseModifiersTagList(stack);
        if (TinkerUtil.getIndexInList(base, modifier) >= 0) {
            return;
        }
        base.appendTag(new NBTTagString(modifier));
        TagUtil.setBaseModifiersTagList(stack, base);
    }

    private static boolean restoreVisualModifierForRebuild(ItemStack stack, String modifier) {
        NBTTagList base = TagUtil.getBaseModifiersTagList(stack);
        if (TinkerUtil.getIndexInList(base, modifier) >= 0) {
            return false;
        }
        base.appendTag(new NBTTagString(modifier));
        TagUtil.setBaseModifiersTagList(stack, base);
        return true;
    }

    private static boolean hasModifier(ItemStack stack, String modifier) {
        return TinkerUtil.getIndexInCompoundList(TagUtil.getModifiersTagList(stack), modifier) >= 0;
    }

    private static ModifierNBT modifierData(ItemStack stack, String modifier) {
        NBTTagCompound tag = TinkerUtil.getModifierTag(stack, modifier);
        return ModifierNBT.readTag(tag);
    }

    private static int extractValue(ItemStack tool, String modifier) {
        NBTTagCompound tag = TinkerUtil.getModifierTag(tool, modifier);
        if (!tag.hasKey("current") || !tag.hasKey("max")) {
            return 1;
        }
        int current = tag.getInteger("current");
        for (int level = 1; level <= Math.max(1, ModifierNBT.readInteger(tag).level); level++) {
            if (current >= getMaxForLevel(modifier, level)) {
                return extractSegmentMax(modifier, level);
            }
        }
        return Math.max(1, current);
    }

    private static int extractMaxValue(ItemStack tool, String modifier) {
        NBTTagCompound tag = TinkerUtil.getModifierTag(tool, modifier);
        if (!tag.hasKey("current") || !tag.hasKey("max")) {
            return 0;
        }
        int current = tag.getInteger("current");
        ModifierNBT.IntegerNBT data = ModifierNBT.readInteger(tag);
        for (int level = 1; level <= Math.max(1, data.level); level++) {
            if (current >= getMaxForLevel(modifier, level)) {
                return extractSegmentMax(modifier, level);
            }
        }
        return extractSegmentMax(modifier, Math.max(1, data.level));
    }

    private static int extractSegmentMax(String modifier, int level) {
        int segment = getMaxForLevel(modifier, level) - getMaxForLevel(modifier, level - 1);
        return Math.max(1, segment);
    }

    private static int extractLevelForCurrent(String modifier, int current) {
        int level = 0;
        while (current > getMaxForLevel(modifier, level)) {
            level++;
        }
        return level;
    }

    private static int getMaxForLevel(String modifier, int level) {
        if (level <= 0) {
            return 0;
        }
        IModifier mod = getModifier(modifier);
        if (mod == null) {
            return level;
        }
        try {
            for (Object aspect : modifierAspects(mod)) {
                if (isMultiAspect(aspect)) {
                    Method method = getDeclaredMethod(aspect.getClass(), "getMaxForLevel");
                    method.setAccessible(true);
                    return (Integer) method.invoke(aspect, level);
                }
            }
        } catch (Exception ignored) {
        }
        return level;
    }

    private static boolean isMultiAspect(Object aspect) {
        Class<?> type = aspect.getClass();
        while (type != null) {
            if ("slimeknights.tconstruct.library.modifiers.ModifierAspect$MultiAspect".equals(type.getName())) {
                return true;
            }
            type = type.getSuperclass();
        }
        return false;
    }

    private static Method getDeclaredMethod(Class<?> type, String name) throws NoSuchMethodException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredMethod(name, int.class);
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchMethodException(name);
    }

    private static List<?> modifierAspects(IModifier modifier) throws ReflectiveOperationException {
        Class<?> type = modifier.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField("aspects");
                field.setAccessible(true);
                return (List<?>) field.get(modifier);
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }
        return new ArrayList<>();
    }

    private static NBTTagList hiddenList(ItemStack stack) {
        return TagUtil.getExtraTag(stack).getCompoundTag(TAG_TT2).getTagList(TAG_HIDDEN, TagUtil.TAG_TYPE_STRING);
    }

    public static void preserveHiddenModifiersAfterRebuild(ItemStack result, ItemStack original) {
        if (!isTinkerItem(result) || !isTinkerItem(original)) {
            return;
        }
        NBTTagList hidden = hiddenList(original);
        if (hidden.tagCount() == 0) {
            return;
        }

        boolean changed = copyHiddenState(result, hidden);
        changed |= restoreHiddenModifierTags(result, original, hidden);

        if (changed) {
            rebuild(result);
        } else {
            applyHiddenVisuals(result);
        }
    }

    public static void repairCurrentHiddenModifiers(ItemStack result, ItemStack original) {
        if (!isTinkerItem(result) || !isTinkerItem(original)) {
            return;
        }
        NBTTagList hidden = hiddenList(result);
        if (hidden.tagCount() == 0) {
            return;
        }

        boolean changed = restoreHiddenModifierTags(result, original, hidden);
        if (changed) {
            rebuild(result);
        } else {
            applyHiddenVisuals(result);
        }
    }

    private static boolean restoreHiddenModifierTags(ItemStack result, ItemStack original, NBTTagList hidden) {
        boolean changed = false;
        for (int i = 0; i < hidden.tagCount(); i++) {
            String modifier = hidden.getStringTagAt(i);
            NBTTagCompound source = TinkerUtil.getModifierTag(original, modifier);
            if (source.getKeySet().isEmpty()) {
                continue;
            }
            if (!hasModifier(result, modifier)) {
                NBTTagList modifiers = TagUtil.getModifiersTagList(result);
                modifiers.appendTag(source.copy());
                TagUtil.setModifiersTagList(result, modifiers);
                changed = true;
            }
            if (restoreVisualModifierForRebuild(result, modifier)) {
                changed = true;
            }
        }
        return changed;
    }

    private static boolean copyHiddenState(ItemStack stack, NBTTagList hidden) {
        NBTTagCompound extra = TagUtil.getExtraTag(stack);
        NBTTagCompound tt2 = extra.getCompoundTag(TAG_TT2);
        NBTTagList current = tt2.getTagList(TAG_HIDDEN, TagUtil.TAG_TYPE_STRING);
        NBTTagList next = new NBTTagList();
        boolean changed = current.tagCount() != hidden.tagCount();

        for (int i = 0; i < current.tagCount(); i++) {
            String id = current.getStringTagAt(i);
            if (!containsString(next, id)) {
                next.appendTag(new NBTTagString(id));
            }
        }
        for (int i = 0; i < hidden.tagCount(); i++) {
            String id = hidden.getStringTagAt(i);
            if (!containsString(next, id)) {
                next.appendTag(new NBTTagString(id));
                changed = true;
            }
        }
        if (changed) {
            tt2.setTag(TAG_HIDDEN, next);
            extra.setTag(TAG_TT2, tt2);
            TagUtil.setExtraTag(stack, extra);
        }
        return changed;
    }

    private static boolean containsString(NBTTagList list, String value) {
        for (int i = 0; i < list.tagCount(); i++) {
            if (value.equals(list.getStringTagAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRemovalInput(ItemStack stack) {
        return stack.getItem() == Item.getItemFromBlock(net.minecraft.init.Blocks.SPONGE) && stack.getMetadata() == 1
            || isFluidContainer(stack, TinkerFluids.venom);
    }

    private static boolean isFluidContainer(ItemStack stack, net.minecraftforge.fluids.Fluid fluid) {
        FluidStack contained = FluidUtil.getFluidContained(stack);
        return contained != null && contained.getFluid() == fluid;
    }

    private static boolean isInvisibilityPotion(ItemStack stack) {
        return stack.getItem() instanceof ItemPotion && PotionUtils.getPotionFromItem(stack) == PotionTypes.INVISIBILITY;
    }

    private static boolean sameItem(ItemStack stack, ItemStack target) {
        return !stack.isEmpty() && !target.isEmpty()
            && stack.getItem() == target.getItem()
            && (target.getMetadata() == net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE
            || stack.getMetadata() == target.getMetadata());
    }

    private static boolean isCreative(String modifier) {
        IModifier mod = getModifier(modifier);
        return mod != null && mod.getClass().getName().endsWith("ModCreative");
    }

    public static boolean isCrystalExtractable(IModifier modifier) {
        return isExtractableModifierCandidate(modifier) && hasBoundModifierItem(modifier);
    }

    private static boolean isExtractableModifierCandidate(IModifier modifier) {
        return modifier != null
            && !ModExperienceTransfer.ID.equals(modifier.getIdentifier())
            && !isLevelingModifier(modifier.getIdentifier())
            && (!(modifier instanceof ITrait) || modifier instanceof ModifierTrait)
            && !(modifier instanceof IArmorTrait)
            && !modifier.getClass().getName().endsWith("ModCreative");
    }

    private static boolean hasBoundModifierItem(IModifier modifier) {
        if (!(modifier instanceof RecipeMatchRegistry)) {
            return false;
        }
        try {
            Field field = RecipeMatchRegistry.class.getDeclaredField("items");
            field.setAccessible(true);
            Object value = field.get(modifier);
            if (!(value instanceof Collection)) {
                return false;
            }
            for (Object match : (Collection<?>) value) {
                if (isNativeModifierRecipe(match)) {
                    return true;
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return false;
    }

    private static boolean isNativeModifierRecipe(Object match) {
        if (!(match instanceof RecipeMatch) || match instanceof ModifierCrystalRecipeMatch) {
            return false;
        }
        List<ItemStack> inputs = ((RecipeMatch) match).getInputs();
        return inputs != null && !inputs.isEmpty();
    }

    private static boolean isAdjustableModifier(String modifier) {
        return !isLevelingModifier(modifier)
            && !isEmboss(modifier)
            && (isExtractableModifierCandidate(getModifier(modifier)) || isFortifyOrPolished(modifier));
    }

    private static boolean isRegularExtractable(String modifier) {
        IModifier mod = getModifier(modifier);
        return isCrystalExtractable(mod) && !isFortifyOrPolished(modifier) && !isEmboss(modifier);
    }

    private static boolean isFortifyOrPolished(String modifier) {
        return modifier.startsWith("fortify") || modifier.startsWith("polished_armor");
    }

    private static boolean isEmboss(String modifier) {
        return modifier.startsWith("extratrait") || modifier.startsWith("moretcon.extratrait2");
    }

    private static boolean isLevelingModifier(String modifier) {
        return ModExperienceTransfer.isLevelingModifier(modifier);
    }

    private static void consumeRemovalInput(TileModifierWorktable tile, ItemStack input) {
        if (input.getItem() == Item.getItemFromBlock(net.minecraft.init.Blocks.SPONGE)) {
            consumeOne(input);
            tile.setInventorySlotContents(TileModifierWorktable.SLOT_INPUT_1,
                new ItemStack(net.minecraft.init.Blocks.SPONGE));
            return;
        }
        if (input.getItem() == Items.GLASS_BOTTLE || input.getItem() == Items.POTIONITEM) {
            consumeOne(input);
            tile.setInventorySlotContents(TileModifierWorktable.SLOT_INPUT_1, new ItemStack(Items.GLASS_BOTTLE));
        } else {
            consumeOne(input);
            tile.setInventorySlotContents(TileModifierWorktable.SLOT_INPUT_1, new ItemStack(Items.BUCKET));
        }
    }

    private static void consumeExtractionSecond(ItemStack input, int action) {
        int amount = action == TYPE_EXTRACT ? 3 : 1;
        input.shrink(amount);
    }

    private static void consumeOne(ItemStack stack) {
        stack.shrink(1);
    }
}
