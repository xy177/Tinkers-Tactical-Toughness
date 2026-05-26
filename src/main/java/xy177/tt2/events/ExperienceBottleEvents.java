package xy177.tt2.events;

import c4.conarm.lib.tinkering.TinkersArmor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import c4.conarm.common.inventory.ContainerArmorStation;
import c4.conarm.common.inventory.SlotArmorStationIn;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.tools.common.inventory.ContainerToolStation;
import slimeknights.tconstruct.tools.common.inventory.SlotToolStationIn;
import xy177.tt2.init.TT2Items;
import xy177.tt2.item.ItemExperienceBottle;
import xy177.tt2.modifiers.ModExperienceTransfer;

import java.util.HashSet;
import java.util.Set;

public class ExperienceBottleEvents {

    @SubscribeEvent
    public void onOpen(PlayerContainerEvent.Open event) {
        if (event.getContainer() instanceof ContainerToolStation) {
            patchToolStation(event.getEntityPlayer(), (ContainerToolStation) event.getContainer());
        } else if (event.getContainer() instanceof ContainerArmorStation) {
            patchArmorStation((ContainerArmorStation) event.getContainer());
        }
    }

    private void patchToolStation(EntityPlayer player, ContainerToolStation container) {
        Set<IInventory> inputInventories = inputInventories(container);
        for (int i = 0; i < container.inventorySlots.size(); i++) {
            Slot slot = container.inventorySlots.get(i);
            if (slot.inventory instanceof InventoryCraftResult) {
                container.inventorySlots.set(i, new ExperienceSlot(slot, container, player));
            } else if (slot instanceof SlotToolStationIn) {
                container.inventorySlots.set(i, new ExperienceToolInputSlot((SlotToolStationIn) slot, container));
            } else if (inputInventories.contains(slot.inventory)) {
                container.inventorySlots.set(i, new ExperienceInputSlot(slot, container));
            }
        }
    }

    private void patchArmorStation(ContainerArmorStation container) {
        Set<IInventory> inputInventories = inputInventories(container);
        for (int i = 0; i < container.inventorySlots.size(); i++) {
            Slot slot = container.inventorySlots.get(i);
            if (slot.inventory instanceof InventoryCraftResult) {
                container.inventorySlots.set(i, new ExperienceSlot(slot, container));
            } else if (slot instanceof SlotArmorStationIn) {
                container.inventorySlots.set(i, new ExperienceArmorInputSlot((SlotArmorStationIn) slot, container));
            } else if (inputInventories.contains(slot.inventory)) {
                container.inventorySlots.set(i, new ExperienceInputSlot(slot, container));
            }
        }
    }

    private static Set<IInventory> inputInventories(Container container) {
        Set<IInventory> inventories = new HashSet<>();
        for (Slot slot : container.inventorySlots) {
            if (slot instanceof SlotToolStationIn || slot instanceof SlotArmorStationIn) {
                inventories.add(slot.inventory);
            }
        }
        return inventories;
    }

    private static void refreshExperienceOutput(Container container) {
        for (Slot slot : container.inventorySlots) {
            if (slot instanceof ExperienceSlot) {
                ((ExperienceSlot) slot).updateSpecialOutput();
                container.detectAndSendChanges();
                return;
            }
        }
    }

    private static class ExperienceToolInputSlot extends SlotToolStationIn {

        private final Container container;

        ExperienceToolInputSlot(SlotToolStationIn original, ContainerToolStation container) {
            super(original.inventory, original.getSlotIndex(), original.xPos, original.yPos, container);
            this.container = container;
            this.dormant = original.dormant;
            this.restriction = original.restriction;
            this.icon = original.icon;
        }

        @Override
        public void onSlotChanged() {
            super.onSlotChanged();
            refreshExperienceOutput(container);
        }

        @Override
        public ItemStack onTake(EntityPlayer player, ItemStack stack) {
            ItemStack result = super.onTake(player, stack);
            refreshExperienceOutput(container);
            return result;
        }
    }

    private static class ExperienceArmorInputSlot extends SlotArmorStationIn {

        private final Container container;

        ExperienceArmorInputSlot(SlotArmorStationIn original, ContainerArmorStation container) {
            super(original.inventory, original.getSlotIndex(), original.xPos, original.yPos, container);
            this.container = container;
            this.dormant = original.dormant;
            this.restriction = original.restriction;
            this.icon = original.icon;
        }

        @Override
        public void onSlotChanged() {
            super.onSlotChanged();
            refreshExperienceOutput(container);
        }

        @Override
        public ItemStack onTake(EntityPlayer player, ItemStack stack) {
            ItemStack result = super.onTake(player, stack);
            refreshExperienceOutput(container);
            return result;
        }
    }

    private static class ExperienceInputSlot extends Slot {

        private final Container container;

        ExperienceInputSlot(Slot original, Container container) {
            super(original.inventory, original.getSlotIndex(), original.xPos, original.yPos);
            this.container = container;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return inventory.isItemValidForSlot(getSlotIndex(), stack);
        }

        @Override
        public void onSlotChanged() {
            super.onSlotChanged();
            refreshExperienceOutput(container);
        }

        @Override
        public ItemStack onTake(EntityPlayer player, ItemStack stack) {
            ItemStack result = super.onTake(player, stack);
            refreshExperienceOutput(container);
            return result;
        }
    }

    private static class ExperienceSlot extends Slot {

        private final Slot original;
        private final Container container;
        private boolean specialOutput;
        private ItemStack lastSpecial = ItemStack.EMPTY;

        ExperienceSlot(Slot original, ContainerToolStation container, EntityPlayer player) {
            super(original.inventory, original.getSlotIndex(), original.xPos, original.yPos);
            this.original = original;
            this.container = container;
        }

        ExperienceSlot(Slot original, ContainerArmorStation container) {
            super(original.inventory, original.getSlotIndex(), original.xPos, original.yPos);
            this.original = original;
            this.container = container;
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return original.isItemValid(stack);
        }

        @Override
        public ItemStack getStack() {
            updateSpecialOutput();
            return original.getStack();
        }

        @Override
        public boolean getHasStack() {
            updateSpecialOutput();
            return original.getHasStack();
        }

        @Override
        public boolean canTakeStack(EntityPlayer player) {
            return original.canTakeStack(player) || !getExperienceResult().isEmpty();
        }

        @Override
        public ItemStack decrStackSize(int amount) {
            updateSpecialOutput();
            return original.decrStackSize(amount);
        }

        @Override
        public void putStack(ItemStack stack) {
            original.putStack(stack);
        }

        @Override
        public ItemStack onTake(EntityPlayer player, ItemStack stack) {
            updateSpecialOutput();
            if (specialOutput) {
                original.putStack(ItemStack.EMPTY);
                specialOutput = false;
                consumeExperienceBottle();
                return stack;
            }
            return original.onTake(player, stack);
        }

        private void updateSpecialOutput() {
            ItemStack special = getExperienceResult();
            ItemStack current = original.getStack();
            if (current.isEmpty() || (specialOutput && ItemStack.areItemStacksEqual(current, lastSpecial))) {
                original.putStack(special);
                specialOutput = !special.isEmpty();
                lastSpecial = special.isEmpty() ? ItemStack.EMPTY : special.copy();
            } else if (specialOutput) {
                specialOutput = false;
                lastSpecial = ItemStack.EMPTY;
            }
        }

        private ItemStack getExperienceResult() {
            ItemStack tool = findTool();
            if (tool.isEmpty() || !(tool.getItem() instanceof ToolCore || tool.getItem() instanceof TinkersArmor)) {
                return ItemStack.EMPTY;
            }
            ItemStack bottle = findBottle();
            int experience = ItemExperienceBottle.getExperience(bottle);
            return ModExperienceTransfer.transferExperience(tool, experience);
        }

        private void consumeExperienceBottle() {
            boolean consumed = false;
            for (Slot slot : container.inventorySlots) {
                if (!isStationInput(slot)) {
                    continue;
                }
                ItemStack stack = slot.getStack();
                if (!stack.isEmpty() && stack.getItem() == TT2Items.EXPERIENCE_BOTTLE
                    && ItemExperienceBottle.getExperience(stack) > 0) {
                    stack.shrink(1);
                    slot.putStack(stack.isEmpty() ? ItemStack.EMPTY : stack);
                    consumed = true;
                    break;
                }
            }
            if (consumed) {
                clearToolSlot();
                container.onCraftMatrixChanged(null);
            }
        }

        private ItemStack findBottle() {
            for (Slot slot : container.inventorySlots) {
                if (!isStationInput(slot)) {
                    continue;
                }
                ItemStack stack = slot.getStack();
                if (!stack.isEmpty() && stack.getItem() == TT2Items.EXPERIENCE_BOTTLE
                    && ItemExperienceBottle.getExperience(stack) > 0) {
                    return stack;
                }
            }
            return ItemStack.EMPTY;
        }

        private ItemStack findTool() {
            for (Slot slot : container.inventorySlots) {
                if (!isStationInput(slot)) {
                    continue;
                }
                ItemStack stack = slot.getStack();
                if (!stack.isEmpty() && (stack.getItem() instanceof ToolCore || stack.getItem() instanceof TinkersArmor)) {
                    return stack;
                }
            }
            return ItemStack.EMPTY;
        }

        private void clearToolSlot() {
            for (Slot slot : container.inventorySlots) {
                if (!isStationInput(slot)) {
                    continue;
                }
                ItemStack stack = slot.getStack();
                if (!stack.isEmpty() && (stack.getItem() instanceof ToolCore || stack.getItem() instanceof TinkersArmor)) {
                    slot.putStack(ItemStack.EMPTY);
                    return;
                }
            }
        }

        private boolean isStationInput(Slot slot) {
            return slot instanceof SlotToolStationIn || slot instanceof SlotArmorStationIn;
        }
    }
}
