package xy177.tt2.init;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import xy177.tt2.client.gui.GuiModifierWorktable;
import xy177.tt2.inventory.ContainerModifierWorktable;
import xy177.tt2.tile.TileModifierWorktable;

import javax.annotation.Nullable;

public class TT2GuiHandler implements IGuiHandler {

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == TT2Blocks.GUI_MODIFIER_WORKTABLE) {
            TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
            if (tile instanceof TileModifierWorktable) {
                return new ContainerModifierWorktable(player.inventory, (TileModifierWorktable) tile);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == TT2Blocks.GUI_MODIFIER_WORKTABLE) {
            TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
            if (tile instanceof TileModifierWorktable) {
                return new GuiModifierWorktable(player.inventory, (TileModifierWorktable) tile);
            }
        }
        return null;
    }
}
