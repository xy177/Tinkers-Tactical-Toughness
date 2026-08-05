package xy177.tt2.init;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import xy177.tt2.TT2;
import xy177.tt2.block.BlockModifierWorktable;
import xy177.tt2.tile.TileModifierWorktable;

@Mod.EventBusSubscriber(modid = TT2.MOD_ID)
public final class TT2Blocks {

    public static final int GUI_MODIFIER_WORKTABLE = 1;
    public static final int GUI_CRAFTSMAN_STAFF_ALYZER = 2;
    public static final int GUI_CRAFTSMAN_STAFF_AE_TOOL = 3;
    public static BlockModifierWorktable MODIFIER_WORKTABLE;

    private TT2Blocks() {
    }

    public static void preInit() {
        GameRegistry.registerTileEntity(TileModifierWorktable.class,
            new ResourceLocation(TT2.MOD_ID, "modifier_worktable"));
        NetworkRegistry.INSTANCE.registerGuiHandler(TT2.instance, new TT2GuiHandler());
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        MODIFIER_WORKTABLE = new BlockModifierWorktable();
        MODIFIER_WORKTABLE.setRegistryName(TT2.MOD_ID, "modifier_worktable");
        event.getRegistry().register(MODIFIER_WORKTABLE);
    }

    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        if (MODIFIER_WORKTABLE != null) {
            event.getRegistry().register(new ItemBlock(MODIFIER_WORKTABLE)
                .setRegistryName(MODIFIER_WORKTABLE.getRegistryName()));
        }
    }
}
