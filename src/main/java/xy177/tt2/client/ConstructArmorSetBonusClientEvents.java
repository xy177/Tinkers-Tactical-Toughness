package xy177.tt2.client;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.Util;
import xy177.tt2.config.TT2Config;
import xy177.tt2.events.DefenseDamageEvents;

@SideOnly(Side.CLIENT)
public class ConstructArmorSetBonusClientEvents {

    private static final String FREE_MODIFIERS_KEY = "tooltip.tool.modifiers";

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        EntityPlayer player = event.getEntityPlayer();
        if (player == null || !Config.extraTooltips || !Util.isShiftKeyDown()
            || !DefenseDamageEvents.isBaseConstructArmor(stack)) {
            return;
        }

        int pieces = DefenseDamageEvents.getBaseConstructArmorPieces(player);
        if (pieces <= 0) {
            return;
        }
        addSetBonusTooltip(event, Util.translate(getSetBonusKey(Math.min(4, pieces))));
    }

    private String getSetBonusKey(int pieces) {
        String prefix = TT2Config.enableDefenseDamage
            ? "tooltip.tt2.construct_armor.set_bonus_"
            : "tooltip.tt2.construct_armor.set_bonus_mining_";
        return prefix + pieces;
    }

    private void addSetBonusTooltip(ItemTooltipEvent event, String tooltip) {
        String freeModifiers = net.minecraft.util.text.translation.I18n.translateToLocal(FREE_MODIFIERS_KEY);
        for (int i = 0; i < event.getToolTip().size(); i++) {
            String line = event.getToolTip().get(i);
            if (line != null && line.startsWith(freeModifiers + ":")) {
                event.getToolTip().add(i, tooltip);
                return;
            }
        }
        event.getToolTip().add(tooltip);
    }
}
