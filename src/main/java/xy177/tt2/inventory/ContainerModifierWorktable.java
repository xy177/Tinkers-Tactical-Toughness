package xy177.tt2.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import slimeknights.mantle.inventory.ContainerMultiModule;
import xy177.tt2.logic.ModifierWorktableLogic;
import xy177.tt2.tile.TileModifierWorktable;

import java.util.ArrayList;
import java.util.List;

public class ContainerModifierWorktable extends ContainerMultiModule<TileModifierWorktable> {

    private final TileModifierWorktable tile;
    private int selectedIndex;
    private int selectedAction;
    private boolean selectingModifier;
    private int cachedSelection = -1;
    private int cachedAction = -1;
    private int cachedMode = -1;
    private String lastSignature = "";
    private final List<String> modifiers = new ArrayList<>();
    private final List<Integer> actions = new ArrayList<>();
    private static final int WORKTABLE_SLOTS = 4;
    private static final int PLAYER_SLOT_START = 9;

    public ContainerModifierWorktable(InventoryPlayer playerInventory, TileModifierWorktable tile) {
        super(tile);
        this.tile = tile;

        addSlotToContainer(new SlotModifierTool(tile, TileModifierWorktable.SLOT_TOOL, 8, 21));
        addSlotToContainer(new Slot(tile, TileModifierWorktable.SLOT_INPUT_1, 8, 45));
        addSlotToContainer(new Slot(tile, TileModifierWorktable.SLOT_INPUT_2, 8, 67));
        addSlotToContainer(new SlotModifierOutput(this, tile, TileModifierWorktable.SLOT_OUTPUT, 125, 42));

        EntityEquipmentSlot[] armorSlots = {
            EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET
        };
        for (int row = 0; row < armorSlots.length; row++) {
            addSlotToContainer(new SlotArmor(playerInventory, 39 - row, 152, 16 + row * 18, armorSlots[row]));
        }
        addSlotToContainer(new SlotOffhand(playerInventory, 40, 132, 70));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 102 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInventory, col, 8 + col * 18, 160));
        }
        refresh();
    }

    public List<String> getModifiers() {
        return modifiers;
    }

    public List<Integer> getActions() {
        return actions;
    }

    public boolean isSelectingModifier() {
        return selectingModifier;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        refresh();
    }

    public void onOutputTaken(EntityPlayer player, ItemStack stack) {
        ModifierWorktableLogic.apply(tile, selectedModifier(), selectedAction);
    }

    public String getSelectedModifier() {
        return selectedModifier();
    }

    public int getSelectedAction() {
        return selectedAction;
    }

    @Override
    public boolean enchantItem(EntityPlayer player, int id) {
        if (selectingModifier) {
            if (id == 0) {
                selectingModifier = false;
                selectedIndex = 0;
            } else {
                selectedIndex = id - 1;
            }
        } else if (id >= 0 && id < actions.size()) {
            selectedAction = actions.get(id);
            selectingModifier = true;
            selectedIndex = 0;
        }
        refresh();
        return true;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, selectedIndex);
        listener.sendWindowProperty(this, 1, selectedAction);
        listener.sendWindowProperty(this, 2, selectingModifier ? 1 : 0);
    }

    @Override
    public void detectAndSendChanges() {
        refresh();
        super.detectAndSendChanges();
        if (cachedSelection != selectedIndex || cachedAction != selectedAction || cachedMode != (selectingModifier ? 1 : 0)) {
            cachedSelection = selectedIndex;
            cachedAction = selectedAction;
            cachedMode = selectingModifier ? 1 : 0;
            for (IContainerListener listener : listeners) {
                listener.sendWindowProperty(this, 0, selectedIndex);
                listener.sendWindowProperty(this, 1, selectedAction);
                listener.sendWindowProperty(this, 2, cachedMode);
            }
        }
    }

    @Override
    public void updateProgressBar(int id, int data) {
        if (id == 0) {
            selectedIndex = data;
        } else if (id == 1) {
            selectedAction = data;
        } else if (id == 2) {
            selectingModifier = data != 0;
        }
        refresh();
    }

    @Override
    public void onCraftMatrixChanged(net.minecraft.inventory.IInventory inventoryIn) {
        refresh();
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        ItemStack stack = super.slotClick(slotId, dragType, clickTypeIn, player);
        refresh();
        return stack;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUsableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            copy = stack.copy();
            if (index == TileModifierWorktable.SLOT_OUTPUT) {
                if (!mergeItemStack(stack, PLAYER_SLOT_START, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onTake(player, stack);
            } else if (index >= WORKTABLE_SLOTS) {
                if (ModifierWorktableLogic.isTinkerItem(stack)) {
                    if (!mergeItemStack(stack, TileModifierWorktable.SLOT_TOOL, TileModifierWorktable.SLOT_TOOL + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!mergeItemStack(stack, TileModifierWorktable.SLOT_INPUT_1, TileModifierWorktable.SLOT_OUTPUT, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(stack, PLAYER_SLOT_START, inventorySlots.size(), false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }
        refresh();
        return copy;
    }

    private String selectedModifier() {
        if (selectedIndex < 0 || selectedIndex >= modifiers.size()) {
            return "";
        }
        return modifiers.get(selectedIndex);
    }

    private void refresh() {
        actions.clear();
        actions.addAll(ModifierWorktableLogic.getSelectableActions(tile));
        if (actions.isEmpty()) {
            selectedAction = 0;
            selectingModifier = false;
        } else if (!actions.contains(selectedAction)) {
            selectedAction = actions.get(0);
            selectingModifier = false;
            selectedIndex = 0;
        }
        modifiers.clear();
        modifiers.addAll(ModifierWorktableLogic.getSelectableModifiers(tile, selectedAction));
        if (selectedIndex >= modifiers.size()) {
            selectedIndex = Math.max(0, modifiers.size() - 1);
        }
        String signature = selectedIndex + "|"
            + selectedAction + "|"
            + selectingModifier + "|"
            + tile.getStackInSlot(TileModifierWorktable.SLOT_TOOL).serializeNBT()
            + tile.getStackInSlot(TileModifierWorktable.SLOT_INPUT_1).serializeNBT()
            + tile.getStackInSlot(TileModifierWorktable.SLOT_INPUT_2).serializeNBT();
        if (!signature.equals(lastSignature) || tile.getStackInSlot(TileModifierWorktable.SLOT_OUTPUT).isEmpty()) {
            lastSignature = signature;
            tile.setInventorySlotContents(TileModifierWorktable.SLOT_OUTPUT,
            selectingModifier ? ModifierWorktableLogic.getResult(tile, selectedModifier(), selectedAction) : ItemStack.EMPTY);
        }
    }

    private static class SlotArmor extends Slot {

        private final EntityEquipmentSlot armorType;

        SlotArmor(InventoryPlayer inventory, int index, int x, int y, EntityEquipmentSlot armorType) {
            super(inventory, index, x, y);
            this.armorType = armorType;
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return stack.getItem().isValidArmor(stack, armorType, ((InventoryPlayer) inventory).player);
        }

        @Override
        public String getSlotTexture() {
            return ItemArmor.EMPTY_SLOT_NAMES[armorType.getIndex()];
        }
    }

    private static class SlotOffhand extends Slot {

        SlotOffhand(InventoryPlayer inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }

        @Override
        public String getSlotTexture() {
            return "minecraft:items/empty_armor_slot_shield";
        }
    }
}
