package xy177.tt2.init;

import c4.conarm.lib.ArmoryRegistry;
import slimeknights.mantle.util.RecipeMatchRegistry;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.modifiers.Modifier;
import xy177.tt2.logic.ModifierCrystalRecipeMatch;
import xy177.tt2.logic.ModifierWorktableLogic;
import xy177.tt2.item.ItemModifierCrystal;
import xy177.tt2.modifiers.ModExperienceTransfer;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TT2ModifierCrystalRecipes {

    private TT2ModifierCrystalRecipes() {
    }

    public static void register() {
        registerExperienceTransfer();
        Set<String> registered = new HashSet<>();
        for (IModifier modifier : TinkerRegistry.getAllModifiers()) {
            addCrystalMatch(modifier, registered);
        }
        for (IModifier modifier : ArmoryRegistry.getAllArmorModifiers()) {
            addCrystalMatch(modifier, registered);
        }
    }

    private static void addCrystalMatch(IModifier modifier, Set<String> registered) {
        if (!(modifier instanceof RecipeMatchRegistry) || !ModifierWorktableLogic.isCrystalExtractable(modifier)) {
            return;
        }
        String id = modifier.getIdentifier();
        boolean emboss = ModifierCrystalRecipeMatch.isEmboss(id);
        int embossLevel = emboss ? ItemModifierCrystal.embossLevel(id) : 0;
        String key = id + "|" + emboss + "|" + embossLevel;
        if (registered.add(key)) {
            ((RecipeMatchRegistry) modifier).addRecipeMatch(new ModifierCrystalRecipeMatch(id, emboss, embossLevel));
            if (emboss && modifier instanceof Modifier) {
                addUniqueEmbossAspect((Modifier) modifier, embossLevel);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void addUniqueEmbossAspect(Modifier modifier, int embossLevel) {
        try {
            Field field = Modifier.class.getDeclaredField("aspects");
            field.setAccessible(true);
            ((List) field.get(modifier)).add(new ModifierCrystalRecipeMatch.UniqueEmbossAspect(embossLevel));
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static void registerExperienceTransfer() {
        ModExperienceTransfer modifier = xy177.tt2.proxy.CommonProxy.experienceTransfer;
        if (modifier == null) {
            return;
        }
        ArmoryRegistry.registerModifier(modifier);
    }
}
