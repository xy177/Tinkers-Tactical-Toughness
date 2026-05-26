package xy177.tt2.init;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import xy177.tt2.TT2;

@Mod.EventBusSubscriber(modid = TT2.MOD_ID)
public final class TT2Recipes {

    private TT2Recipes() {
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        if (TT2Blocks.MODIFIER_WORKTABLE == null) {
            return;
        }
        IRecipe recipe = new ShapedOreRecipe(
            new ResourceLocation(TT2.MOD_ID, "modifier_worktable"),
            new ItemStack(TT2Blocks.MODIFIER_WORKTABLE),
            "SSS",
            "R R",
            "R R",
            'S', "blockSeared",
            'R', Ingredient.fromStacks(new ItemStack(Blocks.STONE))
        ).setRegistryName(TT2.MOD_ID, "modifier_worktable");
        event.getRegistry().register(recipe);
    }
}
