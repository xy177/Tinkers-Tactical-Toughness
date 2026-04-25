package xy177.tt2.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.tt2.events.ScoutArmorEvents;
import xy177.tt2.network.PacketScoutExtraJump;
import xy177.tt2.network.TT2Network;

public class ScoutArmorClientEvents {

    private boolean lastJumpPressed = false;
    private int usedAirJumps = 0;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        if (player == null) {
            reset();
            return;
        }

        boolean jumpPressed = mc.gameSettings.keyBindJump.isKeyDown();
        if (player.onGround) {
            usedAirJumps = 0;
        }

        if (jumpPressed
                && !lastJumpPressed
                && !player.onGround
                && ScoutArmorEvents.canUseExtraJump(player, usedAirJumps)) {
            ScoutArmorEvents.performExtraJump(player);
            usedAirJumps++;
            TT2Network.CHANNEL.sendToServer(new PacketScoutExtraJump());
        }

        lastJumpPressed = jumpPressed;
    }

    private void reset() {
        lastJumpPressed = false;
        usedAirJumps = 0;
    }
}
