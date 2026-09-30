package xy177.tt2.compat.crafttweaker;

import com.teamacronymcoders.contenttweaker.modules.tinkers.traits.CoTTraitBuilder;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import slimeknights.tconstruct.library.Util;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethod;
import xy177.tt2.api.consumable.ConsumableUses;

@ZenRegister
@ModOnly("contenttweaker")
@ZenExpansion("mods.contenttweaker.tconstruct.TraitBuilder")
public final class ConsumableBuilderExpansion {
    private ConsumableBuilderExpansion() {}

    @ZenMethod public static void setConsumable(CoTTraitBuilder builder, int maximum) {
        setConsumable(builder, maximum, "material");
    }
    @ZenMethod public static void setConsumable(CoTTraitBuilder builder, int maximum, String kind) {
        ConsumableUses.register(Util.sanitizeLocalizationString(builder.identifier), maximum, ZenConsumableUses.kind(kind));
    }
}
