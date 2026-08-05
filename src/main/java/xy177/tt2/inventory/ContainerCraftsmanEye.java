package xy177.tt2.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import slimeknights.tconstruct.library.utils.ToolHelper;
import xy177.tt2.compat.CraftsmanEyeGenetics;

/** TT2-owned portable analyzer container using Forestry only as a genetic-data provider. */
public final class ContainerCraftsmanEye extends Container {

    private static final int PLAYER_SLOT_X = 43;
    private static final int PLAYER_SLOT_Y = 156;
    private static final int INTERNAL_SLOT_COUNT = CraftsmanEyeInventory.SLOT_COUNT;

    private final EntityPlayer player;
    private final CraftsmanEyeInventory eyeInventory;
    private boolean processingInput;
    private boolean fullSyncRequired;

    public ContainerCraftsmanEye(EntityPlayer player, EnumHand hand, ItemStack staff) {
        this.player = player;
        this.eyeInventory = new CraftsmanEyeInventory(player, hand, staff);
        this.eyeInventory.setInputChangedListener(this::processInput);
        addSlotToContainer(new InputSlot(eyeInventory, CraftsmanEyeInventory.SLOT_INPUT, 223, 26, this));
        for (int page = CraftsmanEyeInventory.SLOT_PAGE_ONE;
             page <= CraftsmanEyeInventory.SLOT_PAGE_FIVE; page++) {
            addSlotToContainer(new PageSlot(eyeInventory, page, 223, 39 + page * 18, this));
        }
        addPlayerInventory();
    }

    public CraftsmanEyeInventory getEyeInventory() {
        return eyeInventory;
    }

    public ItemStack getStaff() {
        return eyeInventory.getStaff();
    }

    public ItemStack getActiveSpecimen() {
        for (int page = CraftsmanEyeInventory.SLOT_PAGE_ONE;
             page <= CraftsmanEyeInventory.SLOT_PAGE_FIVE; page++) {
            ItemStack stack = eyeInventory.getStackInSlot(page);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return eyeInventory.getStackInSlot(CraftsmanEyeInventory.SLOT_INPUT);
    }

    public int getActivePage() {
        for (int page = CraftsmanEyeInventory.SLOT_PAGE_ONE;
             page <= CraftsmanEyeInventory.SLOT_PAGE_FIVE; page++) {
            if (!eyeInventory.getStackInSlot(page).isEmpty()) {
                return page;
            }
        }
        return 0;
    }

    public boolean canAcceptInput(ItemStack stack) {
        if (stack == null || stack.isEmpty() || hasPageSpecimen()) {
            return false;
        }
        return CraftsmanEyeGenetics.isAnalyzable(CraftsmanEyeGenetics.normalize(stack));
    }

    public boolean canAcceptPage(int targetPage, ItemStack stack) {
        if (stack == null || stack.isEmpty()
            || !CraftsmanEyeGenetics.isAnalyzed(CraftsmanEyeGenetics.normalize(stack))) {
            return false;
        }
        for (int page = CraftsmanEyeInventory.SLOT_PAGE_ONE;
             page <= CraftsmanEyeInventory.SLOT_PAGE_FIVE; page++) {
            if (page != targetPage && !eyeInventory.getStackInSlot(page).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void detectAndSendChanges() {
        if (!player.world.isRemote) {
            processInput();
        }
        super.detectAndSendChanges();
        if (!player.world.isRemote && fullSyncRequired) {
            fullSyncRequired = false;
            for (IContainerListener listener : listeners) {
                listener.sendAllContents(this, getInventory());
            }
        }
    }

    /**
     * Slot changes can arrive through a normal click, a drag, or a quick-move
     * packet. Run the server-side fallback after vanilla has applied the click.
     */
    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer clicker) {
        ItemStack result = super.slotClick(slotId, dragType, clickType, clicker);
        if (!player.world.isRemote) {
            processInput();
        }
        return result;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return eyeInventory.isUsableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        if (index < 0 || index >= inventorySlots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        if (index < INTERNAL_SLOT_COUNT) {
            if (!mergeItemStack(stack, INTERNAL_SLOT_COUNT, inventorySlots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (canAcceptInput(stack)) {
            if (!mergeItemStack(stack, CraftsmanEyeInventory.SLOT_INPUT,
                CraftsmanEyeInventory.SLOT_INPUT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        slot.onTake(player, stack);
        if (!player.world.isRemote) {
            processInput();
        }
        return original;
    }

    private void addPlayerInventory() {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int slotIndex = column + row * 9 + 9;
                addSlotToContainer(new Slot(player.inventory, slotIndex,
                    PLAYER_SLOT_X + column * 18, PLAYER_SLOT_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            Slot slot = column == player.inventory.currentItem
                ? new LockedSlot(player.inventory, column, PLAYER_SLOT_X + column * 18, PLAYER_SLOT_Y + 58)
                : new Slot(player.inventory, column, PLAYER_SLOT_X + column * 18, PLAYER_SLOT_Y + 58);
            addSlotToContainer(slot);
        }
    }

    private void processInput() {
        if (player.world.isRemote) {
            return;
        }
        if (processingInput || hasPageSpecimen() || !eyeInventory.isBoundToHeldStaff()) {
            return;
        }
        ItemStack input = eyeInventory.getStackInSlot(CraftsmanEyeInventory.SLOT_INPUT);
        if (input.isEmpty()) {
            return;
        }
        processingInput = true;
        try {
            ItemStack normalized = CraftsmanEyeGenetics.normalize(input);
            if (normalized != input) {
                input = normalized;
                eyeInventory.setInventorySlotContents(CraftsmanEyeInventory.SLOT_INPUT, input);
            }
            if (!CraftsmanEyeGenetics.isAnalyzable(input)) {
                return;
            }
            if (!CraftsmanEyeGenetics.isAnalyzed(input)) {
                ItemStack staff = eyeInventory.getStaff();
                if (staff.isEmpty() || ToolHelper.getCurrentDurability(staff) <= 0
                    || !CraftsmanEyeGenetics.analyze(player, input)) {
                    return;
                }
                ToolHelper.damageTool(staff, 1, player);
            }
            if (CraftsmanEyeGenetics.isAnalyzed(input)) {
                eyeInventory.setInventorySlotContents(CraftsmanEyeInventory.SLOT_PAGE_ONE, input);
                eyeInventory.setInventorySlotContents(CraftsmanEyeInventory.SLOT_INPUT, ItemStack.EMPTY);
                fullSyncRequired = true;
            }
        } finally {
            processingInput = false;
        }
    }

    private boolean hasPageSpecimen() {
        for (int page = CraftsmanEyeInventory.SLOT_PAGE_ONE;
             page <= CraftsmanEyeInventory.SLOT_PAGE_FIVE; page++) {
            if (!eyeInventory.getStackInSlot(page).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static final class InputSlot extends Slot {
        private final ContainerCraftsmanEye container;

        private InputSlot(CraftsmanEyeInventory inventory, int index, int xPosition, int yPosition,
                          ContainerCraftsmanEye container) {
            super(inventory, index, xPosition, yPosition);
            this.container = container;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return container.canAcceptInput(stack);
        }

        @Override
        public void onSlotChanged() {
            super.onSlotChanged();
            container.processInput();
        }

        @Override
        public int getSlotStackLimit() {
            return container.eyeInventory.getInventoryStackLimit();
        }
    }

    private static final class PageSlot extends Slot {
        private final int page;
        private final ContainerCraftsmanEye container;

        private PageSlot(CraftsmanEyeInventory inventory, int page, int xPosition, int yPosition,
                         ContainerCraftsmanEye container) {
            super(inventory, page, xPosition, yPosition);
            this.page = page;
            this.container = container;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return container.canAcceptPage(page, stack);
        }

        @Override
        public int getSlotStackLimit() {
            return container.eyeInventory.getInventoryStackLimit();
        }
    }

    private static final class LockedSlot extends Slot {
        private LockedSlot(net.minecraft.inventory.IInventory inventory, int index, int xPosition, int yPosition) {
            super(inventory, index, xPosition, yPosition);
        }

        @Override
        public boolean canTakeStack(EntityPlayer player) {
            return false;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }
    }
}
