package xy177.tt2.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import xy177.tt2.logic.ModifierWorktableLogic;

public class SlotModifierTool extends Slot {

    public SlotModifierTool(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return ModifierWorktableLogic.isTinkerItem(stack);
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
