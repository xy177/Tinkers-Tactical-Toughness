package xy177.tt2.init;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import slimeknights.mantle.util.RecipeMatch;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.smeltery.CastingRecipe;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.TinkerFluids;
import xy177.tt2.TT2;

@Mod.EventBusSubscriber(modid = TT2.MOD_ID)
public final class TT2Recipes {

    private static final int FORGING_TEMPLATE_STONE_AMOUNT = 7 * 72;

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

    public static void registerCastingRecipes() {
        registerTemplateCasting(TT2Items.FORGING_TEMPLATE_FARMING, TinkerCommons.matSlimeBallMagma);
        registerTemplateCasting(TT2Items.FORGING_TEMPLATE_COMBAT, new ItemStack(Items.IRON_INGOT));
        registerTemplateCasting(TT2Items.FORGING_TEMPLATE_MINING, new ItemStack(Items.GOLD_INGOT));
        registerTemplateCasting(TT2Items.FORGING_TEMPLATE_EXCAVATION, TinkerCommons.ingotCobalt);
        registerTemplateCasting(TT2Items.FORGING_TEMPLATE_FELLING, TinkerCommons.matSlimeBallBlue);
        registerTemplateCasting(TT2Items.FORGING_TEMPLATE_INSIGHT, TinkerCommons.ingotManyullyn);
        registerTemplateCasting(TT2Items.FORGING_TEMPLATE_NATURE, new ItemStack(Items.EMERALD));
        registerTemplateCasting(TT2Items.FORGING_TEMPLATE_SHEARING, new ItemStack(Items.DIAMOND));
        registerTemplateCasting(TT2Items.FORGING_TEMPLATE_RESEARCH, TinkerCommons.ingotArdite);
    }

    private static void registerTemplateCasting(Item output, ItemStack base) {
        if (output == null || base == null || base.isEmpty() || TinkerFluids.searedStone == null) {
            return;
        }
        TinkerRegistry.registerTableCasting(new CastingRecipe(
            new ItemStack(output),
            RecipeMatch.ofNBT(base),
            TinkerFluids.searedStone,
            FORGING_TEMPLATE_STONE_AMOUNT,
            true,
            false
        ));
    }
}
