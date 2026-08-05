package xy177.tt2.modifiers;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import slimeknights.tconstruct.library.modifiers.IModifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class SpearModifierRecipeHooks {

    private static final String KNOCKBACK_ID = "knockback";

    private SpearModifierRecipeHooks() {
    }

    public static Collection<IModifier> filterModifiers(Collection<IModifier> modifiers,
                                                        NonNullList<ItemStack> inputs,
                                                        ItemStack tool) {
        if (modifiers == null || inputs == null || !ModSpearLunge.hasRecipeIngredients(inputs)) {
            return modifiers;
        }

        String excludedId = ModSpearLunge.isSpear(tool) ? KNOCKBACK_ID : ModSpearLunge.ID;
        List<IModifier> filtered = new ArrayList<>(modifiers.size());
        for (IModifier modifier : modifiers) {
            if (modifier == null || !excludedId.equals(modifier.getIdentifier())) {
                filtered.add(modifier);
            }
        }
        return filtered.size() == modifiers.size() ? modifiers : filtered;
    }
}
