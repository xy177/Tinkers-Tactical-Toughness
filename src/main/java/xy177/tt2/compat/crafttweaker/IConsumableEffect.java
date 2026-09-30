package xy177.tt2.compat.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.tt2.IConsumableEffect")
@FunctionalInterface
public interface IConsumableEffect {
    @ZenMethod boolean execute(IItemStack tool);
}
