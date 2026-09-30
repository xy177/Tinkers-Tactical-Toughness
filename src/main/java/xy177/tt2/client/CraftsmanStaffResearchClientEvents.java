package xy177.tt2.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.tt2.compat.CraftsmanStaffCompat;
import xy177.tt2.modifiers.ModCraftsmanStaffTemplate;
import xy177.tt2.network.PacketCraftsmanStaffResearchTool;
import xy177.tt2.network.TT2Network;
import xy177.tt2.tools.CraftsmanStaff;

public final class CraftsmanStaffResearchClientEvents {

    @SubscribeEvent
    public void onMouseWheel(MouseEvent event) {
        if (event.getDwheel() == 0) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = minecraft.player;
        if (player == null || minecraft.currentScreen != null
            || !minecraft.gameSettings.keyBindSneak.isKeyDown()) {
            return;
        }

        EnumHand hand = findResearchStaffHand(player);
        if (hand == null || CraftsmanStaffCompat.getResearchTools().isEmpty()) {
            return;
        }
        ItemStack staff = player.getHeldItem(hand);
        int step = event.getDwheel() < 0 ? 1 : -1;
        String selectedId = CraftsmanStaffCompat.cycleSelectedResearchTool(staff, step);

        event.setCanceled(true);
        TT2Network.CHANNEL.sendToServer(new PacketCraftsmanStaffResearchTool(hand, selectedId));
        player.sendStatusMessage(new TextComponentTranslation(
            "message.tt2.craftsman_staff.research_tool.selected",
            CraftsmanStaffCompat.getResearchToolSelectionName(staff)), true);
    }

    private static EnumHand findResearchStaffHand(EntityPlayer player) {
        if (isResearchStaff(player.getHeldItemMainhand())) {
            return EnumHand.MAIN_HAND;
        }
        return isResearchStaff(player.getHeldItemOffhand()) ? EnumHand.OFF_HAND : null;
    }

    private static boolean isResearchStaff(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof CraftsmanStaff
            && ModCraftsmanStaffTemplate.has(stack, ModCraftsmanStaffTemplate.Type.RESEARCH);
    }
}
