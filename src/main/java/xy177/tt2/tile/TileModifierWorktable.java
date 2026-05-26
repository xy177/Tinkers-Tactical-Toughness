package xy177.tt2.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class TileModifierWorktable extends TileEntity implements IInventory {

    public static final int SLOT_TOOL = 0;
    public static final int SLOT_INPUT_1 = 1;
    public static final int SLOT_INPUT_2 = 2;
    public static final int SLOT_OUTPUT = 3;

    private final NonNullList<ItemStack> stacks = NonNullList.withSize(4, ItemStack.EMPTY);

    @Override
    public int getSizeInventory() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return stacks.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = ItemStackHelper.getAndSplit(stacks, index, count);
        if (!stack.isEmpty()) {
            markDirty();
        }
        return stack;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = ItemStackHelper.getAndRemove(stacks, index);
        markDirty();
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        stacks.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }
        markDirty();
    }

    @Override
    public String getName() {
        return "container.tt2.modifier_worktable";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(getName());
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return world != null && world.getTileEntity(pos) == this
            && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index != SLOT_OUTPUT;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        for (int i = 0; i < stacks.size(); i++) {
            stacks.set(i, ItemStack.EMPTY);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        clear();
        ItemStackHelper.loadAllItems(compound, stacks);
        stacks.set(SLOT_OUTPUT, ItemStack.EMPTY);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        ItemStack output = stacks.get(SLOT_OUTPUT);
        stacks.set(SLOT_OUTPUT, ItemStack.EMPTY);
        ItemStackHelper.saveAllItems(compound, stacks);
        stacks.set(SLOT_OUTPUT, output);
        return compound;
    }
}
