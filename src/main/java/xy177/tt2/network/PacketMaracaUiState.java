package xy177.tt2.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.tt2.TT2;
import xy177.tt2.events.MaracaEvents;

public class PacketMaracaUiState implements IMessage {

    private boolean open;

    public PacketMaracaUiState() {
    }

    public PacketMaracaUiState(boolean open) {
        this.open = open;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        open = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(open);
    }

    public static class Handler implements IMessageHandler<PacketMaracaUiState, IMessage> {

        @Override
        public IMessage onMessage(PacketMaracaUiState message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                MaracaEvents events = TT2.proxy.getMaracaEvents();
                if (events != null) {
                    events.setUiOpen(ctx.getServerHandler().player, message.open);
                }
            });
            return null;
        }
    }
}

