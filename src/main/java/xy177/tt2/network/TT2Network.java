package xy177.tt2.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import xy177.tt2.TT2;

public final class TT2Network {

    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(TT2.MOD_ID);
    private static int nextPacketId = 0;
    private static boolean initialized = false;

    private TT2Network() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        CHANNEL.registerMessage(PacketScoutExtraJump.Handler.class, PacketScoutExtraJump.class, nextPacketId++, Side.SERVER);
    }
}
