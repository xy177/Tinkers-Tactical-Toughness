package xy177.tt2.events;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import slimeknights.tconstruct.library.events.TinkerCraftingEvent;
import xy177.tt2.logic.ModifierWorktableLogic;

public class HiddenModifierEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onToolModify(TinkerCraftingEvent.ToolModifyEvent event) {
        ItemStack result = event.getItemStack();
        ItemStack original = event.getToolBeforeModification();
        ModifierWorktableLogic.preserveHiddenModifiersAfterRebuild(result, original);
        ModifierWorktableLogic.repairCurrentHiddenModifiers(result, original);
    }
}
