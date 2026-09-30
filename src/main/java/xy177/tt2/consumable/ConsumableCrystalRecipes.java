package xy177.tt2.consumable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.modifiers.TinkerGuiException;
import xy177.tt2.api.consumable.ConsumableUses;
import xy177.tt2.init.TT2Items;
import xy177.tt2.item.ItemModifierCrystal;
import xy177.tt2.logic.ModifierWorktableLogic;

/** Stateful crystals are transferred one at a time; normal crystals keep the original recipes. */
public final class ConsumableCrystalRecipes {
    private ConsumableCrystalRecipes() {}

    public static ItemStack apply(NonNullList<ItemStack> inputs, ItemStack original, boolean consume)
        throws TinkerGuiException {
        ItemStack crystal = null;
        int occupied = 0;
        for (ItemStack input : inputs) {
            if (input.isEmpty()) continue;
            occupied++;
            if (input.getItem() == TT2Items.MODIFIER_CRYSTAL && input.hasTagCompound()
                && input.getTagCompound().hasKey(ConsumableUses.TAG, 10)) crystal = input;
        }
        if (crystal == null) return null;
        if (occupied != 1) return ItemStack.EMPTY;
        NBTTagCompound data = crystal.getTagCompound();
        IModifier modifier = ModifierWorktableLogic.getModifier(data.getString(ItemModifierCrystal.TAG_MODIFIER));
        if (modifier == null) return ItemStack.EMPTY;
        ConsumableUses.Definition definition = ConsumableUses.definition(modifier.getIdentifier());
        if (definition != null && definition.kind == ConsumableUses.Kind.MODIFIER
            && data.getCompoundTag(ConsumableUses.TAG).getCompoundTag(definition.id)
                .getInteger(ConsumableUses.REMAINING) <= 0) return ItemStack.EMPTY;
        ItemStack result = original.copy();
        int amount = ItemModifierCrystal.getValue(crystal);
        for (int i = 0; i < amount; i++) {
            // A whole-pool crystal can contain several levels. Validate each as a normal
            // successive application, retaining native max-level and free-slot checks.
            if (!ConsumableCallbacks.canApply(modifier, result, result.copy())) return ItemStack.EMPTY;
            modifier.apply(result);
        }
        ConsumableUses.rebuild(result);
        ConsumableUses.mergeSnapshot(result, data.getCompoundTag(ConsumableUses.TAG));
        ModifierWorktableLogic.repairCurrentHiddenModifiers(result, original);
        if (consume) crystal.shrink(1);
        return result;
    }
}
