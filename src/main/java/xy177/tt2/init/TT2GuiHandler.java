package xy177.tt2.init;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import xy177.tt2.client.gui.GuiCraftsmanEye;
import xy177.tt2.client.gui.GuiModifierWorktable;
import xy177.tt2.compat.CraftsmanStaffCompat;
import xy177.tt2.inventory.ContainerCraftsmanEye;
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
        if (id == TT2Blocks.GUI_CRAFTSMAN_STAFF_ALYZER) {
            net.minecraft.item.ItemStack staff = player.getHeldItem(
                x == 0 ? net.minecraft.util.EnumHand.MAIN_HAND : net.minecraft.util.EnumHand.OFF_HAND);
            return new ContainerCraftsmanEye(player,
                x == 0 ? net.minecraft.util.EnumHand.MAIN_HAND : net.minecraft.util.EnumHand.OFF_HAND, staff);
        }
        if (id == TT2Blocks.GUI_CRAFTSMAN_STAFF_AE_TOOL) {
            net.minecraft.item.ItemStack staff = player.getHeldItem(
                x == 0 ? net.minecraft.util.EnumHand.MAIN_HAND : net.minecraft.util.EnumHand.OFF_HAND);
            return CraftsmanStaffCompat.createAeNetworkToolGui(player, staff, false);
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
        if (id == TT2Blocks.GUI_CRAFTSMAN_STAFF_ALYZER) {
            net.minecraft.item.ItemStack staff = player.getHeldItem(
                x == 0 ? net.minecraft.util.EnumHand.MAIN_HAND : net.minecraft.util.EnumHand.OFF_HAND);
            return new GuiCraftsmanEye(new ContainerCraftsmanEye(player,
                x == 0 ? net.minecraft.util.EnumHand.MAIN_HAND : net.minecraft.util.EnumHand.OFF_HAND, staff));
        }
        if (id == TT2Blocks.GUI_CRAFTSMAN_STAFF_AE_TOOL) {
            net.minecraft.item.ItemStack staff = player.getHeldItem(
                x == 0 ? net.minecraft.util.EnumHand.MAIN_HAND : net.minecraft.util.EnumHand.OFF_HAND);
            return CraftsmanStaffCompat.createAeNetworkToolGui(player, staff, true);
        }
        return null;
    }
}
