package xy177.tt2.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.tt2.TT2;
import xy177.tt2.events.MaracaEvents;

public class PacketMaracaAction implements IMessage {

    public enum Action {
        QUICK,
        SLOW,
        RESONANCE,
        PERFORM
    }

    private Action action;
    private int targetEntityId = -1;

    public PacketMaracaAction() {
    }

    public PacketMaracaAction(Action action) {
        this.action = action;
    }

    public PacketMaracaAction(Action action, int targetEntityId) {
        this.action = action;
        this.targetEntityId = targetEntityId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        action = Action.values()[buf.readByte()];
        targetEntityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(action.ordinal());
        buf.writeInt(targetEntityId);
    }

    public static class Handler implements IMessageHandler<PacketMaracaAction, IMessage> {

        @Override
        public IMessage onMessage(PacketMaracaAction message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                MaracaEvents events = TT2.proxy.getMaracaEvents();
                if (events == null) {
                    return;
                }
                switch (message.action) {
                    case QUICK:
                        events.handleQuickKey(ctx.getServerHandler().player, message.targetEntityId);
                        break;
                    case SLOW:
                        events.handleSlowKey(ctx.getServerHandler().player, message.targetEntityId);
                        break;
                    case RESONANCE:
                        events.handleSwapKey(ctx.getServerHandler().player);
                        break;
                    case PERFORM:
                        events.handleDropKey(ctx.getServerHandler().player);
                        break;
                }
            });
            return null;
        }
    }
}

