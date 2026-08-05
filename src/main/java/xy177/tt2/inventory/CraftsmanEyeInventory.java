package xy177.tt2.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.UUID;

/** Persistent six-slot inventory stored directly on the Craftsman's Eye staff. */
public final class CraftsmanEyeInventory implements IInventory {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_PAGE_ONE = 1;
    public static final int SLOT_PAGE_FIVE = 5;
    public static final int SLOT_COUNT = 6;

    private static final String TAG_INVENTORY = "TT2CraftsmanEyeInventory";
    private static final String TAG_UID_MOST = "TT2CraftsmanEyeUidMost";
    private static final String TAG_UID_LEAST = "TT2CraftsmanEyeUidLeast";
    private static final String TAG_LEGACY_SLOTS = "Slots";
    private static final String TAG_LEGACY_MIGRATED = "TT2CraftsmanEyeLegacyMigrated";

    private final EntityPlayer player;
    private final EnumHand hand;
    private final ItemStack originalStaff;
    private final boolean clientSide;
    private boolean awaitingClientBinding;
    private long uidMost;
    private long uidLeast;
    private final NonNullList<ItemStack> stacks = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private Runnable inputChangedListener;

    public CraftsmanEyeInventory(EntityPlayer player, EnumHand hand, ItemStack staff) {
        this.player = player;
        this.hand = hand;
        this.originalStaff = staff;
        this.clientSide = player.world.isRemote;
        NBTTagCompound root = staff != null && staff.hasTagCompound() ? staff.getTagCompound() : null;
        boolean hasBinding = root != null && root.hasKey(TAG_UID_MOST, 4) && root.hasKey(TAG_UID_LEAST, 4);
        if (!hasBinding && !clientSide && staff != null && !staff.isEmpty()) {
            root = getOrCreateRoot(staff);
            UUID uid = UUID.randomUUID();
            root.setLong(TAG_UID_MOST, uid.getMostSignificantBits());
            root.setLong(TAG_UID_LEAST, uid.getLeastSignificantBits());
            hasBinding = true;
        }
        this.awaitingClientBinding = clientSide && !hasBinding;
        this.uidMost = root == null ? 0L : root.getLong(TAG_UID_MOST);
        this.uidLeast = root == null ? 0L : root.getLong(TAG_UID_LEAST);
        readFromStaff(staff);
    }

    public ItemStack getStaff() {
        ItemStack held = player.getHeldItem(hand);
        return isUsableHeldStaff(held) ? held : originalStaff;
    }

    public boolean isBoundToHeldStaff() {
        return isUsableHeldStaff(player.getHeldItem(hand));
    }

    /** Invoked after any code path writes a specimen into the pending-analysis slot. */
    void setInputChangedListener(Runnable listener) {
        this.inputChangedListener = listener;
    }

    @Override
    public int getSizeInventory() {
        return SLOT_COUNT;
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
        ItemStack result = ItemStackHelper.getAndSplit(stacks, index, count);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack result = ItemStackHelper.getAndRemove(stacks, index);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        stacks.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }
        markDirty();
        if (index == SLOT_INPUT && inputChangedListener != null) {
            inputChangedListener.run();
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        // Server owns the persisted data. Client slot updates are display-only.
        if (clientSide) {
            return;
        }
        ItemStack staff = getStaff();
        if (staff == null || staff.isEmpty()) {
            return;
        }
        writeInventory(staff);
    }

    private void writeInventory(ItemStack staff) {
        NBTTagCompound root = getOrCreateRoot(staff);
        NBTTagCompound stored = new NBTTagCompound();
        for (int index = 0; index < SLOT_COUNT; index++) {
            ItemStack stack = stacks.get(index);
            if (!stack.isEmpty()) {
                NBTTagCompound itemTag = new NBTTagCompound();
                stack.writeToNBT(itemTag);
                stored.setTag(Integer.toString(index), itemTag);
            }
        }
        root.setTag(TAG_INVENTORY, stored);
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return this.player == player && isUsableHeldStaff(player.getHeldItem(hand));
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index >= 0 && index < SLOT_COUNT && stack != null && !stack.isEmpty();
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
        for (int index = 0; index < SLOT_COUNT; index++) {
            stacks.set(index, ItemStack.EMPTY);
        }
        markDirty();
    }

    @Override
    public String getName() {
        return "container.tt2.craftsman_eye";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(getName());
    }

    private void readFromStaff(ItemStack staff) {
        if (staff == null || staff.isEmpty() || !staff.hasTagCompound()) {
            return;
        }
        NBTTagCompound root = staff.getTagCompound();
        if (root.hasKey(TAG_INVENTORY, 10)) {
            NBTTagCompound stored = root.getCompoundTag(TAG_INVENTORY);
            for (int index = 0; index < SLOT_COUNT; index++) {
                String key = Integer.toString(index);
                if (stored.hasKey(key, 10)) {
                    stacks.set(index, new ItemStack(stored.getCompoundTag(key)));
                }
            }
        }
        if (!root.getBoolean(TAG_LEGACY_MIGRATED) && readLegacyInventory(root) && !clientSide) {
            writeInventory(staff);
            root.setBoolean(TAG_LEGACY_MIGRATED, true);
        }
    }

    /** Migrates the old Forestry alyzer layout: slot 0 was virtual honey, slots 1-6 were samples. */
    private boolean readLegacyInventory(NBTTagCompound root) {
        if (!root.hasKey(TAG_LEGACY_SLOTS, 10)) {
            return false;
        }
        NBTTagCompound legacy = root.getCompoundTag(TAG_LEGACY_SLOTS);
        boolean foundSample = false;
        for (int index = 0; index < SLOT_COUNT; index++) {
            String key = Integer.toString(index + 1);
            if (legacy.hasKey(key, 10)) {
                ItemStack stack = new ItemStack(legacy.getCompoundTag(key));
                if (!stack.isEmpty() && stacks.get(index).isEmpty()) {
                    stacks.set(index, stack);
                }
                foundSample |= !stack.isEmpty();
            }
        }
        return foundSample;
    }

    private boolean isUsableHeldStaff(ItemStack stack) {
        if (!awaitingClientBinding) {
            return isBoundTo(stack);
        }
        if (!isSameStaffItem(stack)) {
            return false;
        }
        NBTTagCompound root = stack.getTagCompound();
        if (root != null && root.hasKey(TAG_UID_MOST, 4) && root.hasKey(TAG_UID_LEAST, 4)) {
            uidMost = root.getLong(TAG_UID_MOST);
            uidLeast = root.getLong(TAG_UID_LEAST);
            awaitingClientBinding = false;
        }
        return true;
    }

    private boolean isSameStaffItem(ItemStack stack) {
        return stack != null && !stack.isEmpty() && originalStaff != null && !originalStaff.isEmpty()
            && stack.getItem() == originalStaff.getItem();
    }

    private boolean isBoundTo(ItemStack stack) {
        if (!isSameStaffItem(stack) || !stack.hasTagCompound()) {
            return false;
        }
        NBTTagCompound root = stack.getTagCompound();
        return root.hasKey(TAG_UID_MOST, 4) && root.hasKey(TAG_UID_LEAST, 4)
            && root.getLong(TAG_UID_MOST) == uidMost && root.getLong(TAG_UID_LEAST) == uidLeast;
    }

    private static NBTTagCompound getOrCreateRoot(ItemStack stack) {
        NBTTagCompound root = stack.getTagCompound();
        if (root == null) {
            root = new NBTTagCompound();
            stack.setTagCompound(root);
        }
        return root;
    }
}
