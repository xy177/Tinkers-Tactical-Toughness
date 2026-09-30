package xy177.tt2.compat.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import xy177.tt2.api.consumable.ConsumableUses;

@ZenRegister
@ZenClass("mods.tt2.ConsumableUses")
public final class ZenConsumableUses {
    private ZenConsumableUses() {}

    private static ItemStack liveStack(IItemStack tool) {
        // IItemStack conversion copies; mutable() exposes the callback's original item.
        return tool == null ? ItemStack.EMPTY : CraftTweakerMC.getItemStack(tool.mutable());
    }

    @ZenMethod public static void register(String identifier, int maximum, String kind) {
        ConsumableUses.register(identifier, maximum, kind(kind));
    }
    static ConsumableUses.Kind kind(String value) {
        if ("material".equalsIgnoreCase(value)) return ConsumableUses.Kind.MATERIAL;
        if ("modifier".equalsIgnoreCase(value)) return ConsumableUses.Kind.MODIFIER;
        throw new IllegalArgumentException("Consumable kind must be material or modifier");
    }
    @ZenMethod public static int getRemainingUses(IItemStack tool, String id) {
        return ConsumableUses.remaining(liveStack(tool), id);
    }
    @ZenMethod public static boolean canUse(IItemStack tool, String id, int amount) {
        return ConsumableUses.canUse(liveStack(tool), id, amount);
    }
    @ZenMethod public static boolean tryConsume(IItemStack tool, String id) { return tryConsume(tool, id, 1); }
    @ZenMethod public static boolean tryConsume(IItemStack tool, String id, int amount) {
        return ConsumableUses.tryConsume(liveStack(tool), id, amount);
    }
    @ZenMethod public static int restore(IItemStack tool, String id, int amount) {
        return ConsumableUses.restore(liveStack(tool), id, amount);
    }
    @ZenMethod public static boolean tryUse(IItemStack tool, String id, int amount, IConsumableEffect effect) {
        return ConsumableUses.tryUse(liveStack(tool), id, amount, () -> effect.execute(tool));
    }
}
