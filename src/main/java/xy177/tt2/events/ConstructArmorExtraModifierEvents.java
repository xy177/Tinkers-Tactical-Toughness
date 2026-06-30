package xy177.tt2.events;

import c4.conarm.common.ConstructsRegistry;
import c4.conarm.lib.armor.ArmorCore;
import c4.conarm.lib.events.ArmoryEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import slimeknights.tconstruct.library.utils.TagUtil;
import xy177.tt2.config.TT2Config;

public class ConstructArmorExtraModifierEvents {

    private static final String TAG_TT2_EXTRA_SLOTS = "TT2ConstructArmorExtraModifierSlots";

    @SubscribeEvent
    public void onArmorBuild(ArmoryEvent.OnItemBuilding event) {
        if (event.tag != null && isBaseConstructArmor(event.armor)) {
            applyBuiltSlots(event.tag);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote) {
            return;
        }

        EntityPlayer player = event.player;
        for (ItemStack stack : player.inventory.mainInventory) {
            applyMissingSlots(stack);
        }
        for (ItemStack stack : player.inventory.armorInventory) {
            applyMissingSlots(stack);
        }
        for (ItemStack stack : player.inventory.offHandInventory) {
            applyMissingSlots(stack);
        }
    }

    private void applyMissingSlots(ItemStack stack) {
        if (!isBaseConstructArmor(stack)) {
            return;
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && applyMissingSlots(tag)) {
            stack.setTagCompound(tag);
        }
    }

    private boolean applyMissingSlots(NBTTagCompound tag) {
        if (!TT2Config.enableConstructArmorExtraModifierSlots) {
            return false;
        }

        int target = Math.max(0, TT2Config.constructArmorExtraModifierSlots);
        if (target <= 0) {
            return false;
        }

        NBTTagCompound extra = TagUtil.getExtraTag(tag);
        int applied = Math.max(0, extra.getInteger(TAG_TT2_EXTRA_SLOTS));
        int missing = target - applied;
        if (missing <= 0) {
            return false;
        }

        addFreeModifiers(TagUtil.getToolTag(tag), missing);
        addFreeModifiers(tag.getCompoundTag("StatsOriginal"), missing);
        extra.setInteger(TAG_TT2_EXTRA_SLOTS, target);
        TagUtil.setExtraTag(tag, extra);
        return true;
    }

    private void applyBuiltSlots(NBTTagCompound tag) {
        if (!TT2Config.enableConstructArmorExtraModifierSlots) {
            return;
        }

        int target = Math.max(0, TT2Config.constructArmorExtraModifierSlots);
        if (target <= 0) {
            return;
        }

        addFreeModifiers(TagUtil.getToolTag(tag), target);
        addFreeModifiers(tag.getCompoundTag("StatsOriginal"), target);

        NBTTagCompound extra = TagUtil.getExtraTag(tag);
        extra.setInteger(TAG_TT2_EXTRA_SLOTS, target);
        TagUtil.setExtraTag(tag, extra);
    }

    private void addFreeModifiers(NBTTagCompound stats, int amount) {
        if (stats != null) {
            stats.setInteger("FreeModifiers", Math.max(0, stats.getInteger("FreeModifiers")) + amount);
        }
    }

    private boolean isBaseConstructArmor(ItemStack stack) {
        return stack != null && !stack.isEmpty() && isBaseConstructArmor(stack.getItem());
    }

    private boolean isBaseConstructArmor(Object armor) {
        return armor instanceof ArmorCore
            && (armor == ConstructsRegistry.helmet
            || armor == ConstructsRegistry.chestplate
            || armor == ConstructsRegistry.leggings
            || armor == ConstructsRegistry.boots);
    }
}
