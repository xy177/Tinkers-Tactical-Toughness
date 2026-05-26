package xy177.tt2.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotModifierOutput extends Slot {

    private final ContainerModifierWorktable container;

    public SlotModifierOutput(ContainerModifierWorktable container, IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.container = container;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack onTake(EntityPlayer player, ItemStack stack) {
        container.onOutputTaken(player, stack);
        return super.onTake(player, stack);
    }
}
