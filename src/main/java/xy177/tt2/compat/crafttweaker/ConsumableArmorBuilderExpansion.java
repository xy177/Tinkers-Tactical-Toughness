package xy177.tt2.compat.crafttweaker;

import c4.conarm.integrations.contenttweaker.traits.CoTArmorTraitBuilder;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import slimeknights.tconstruct.library.Util;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethod;
import xy177.tt2.api.consumable.ConsumableUses;

@ZenRegister
@ModOnly("contenttweaker")
@ZenExpansion("mods.contenttweaker.conarm.ArmorTraitBuilder")
public final class ConsumableArmorBuilderExpansion {
    private ConsumableArmorBuilderExpansion() {}

    @ZenMethod public static void setConsumable(CoTArmorTraitBuilder builder, int maximum) {
        setConsumable(builder, maximum, "material");
    }
    @ZenMethod public static void setConsumable(CoTArmorTraitBuilder builder, int maximum, String kind) {
        ConsumableUses.register(Util.sanitizeLocalizationString(builder.identifier), maximum, ZenConsumableUses.kind(kind));
    }
}
