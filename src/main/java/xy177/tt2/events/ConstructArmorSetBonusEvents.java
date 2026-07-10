package xy177.tt2.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.tt2.config.TT2Config;

public class ConstructArmorSetBonusEvents {

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player == null) {
            return;
        }

        int pieces = DefenseDamageEvents.getBaseConstructArmorPieces(player);
        if (pieces > 0) {
            event.setNewSpeed(event.getNewSpeed() * (1f + pieces * (float) TT2Config.constructArmorSetMiningSpeedPerPiece));
        }
    }

}
