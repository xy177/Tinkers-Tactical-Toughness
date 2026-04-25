package xy177.tt2.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.tt2.TT2;
import xy177.tt2.events.ScoutArmorEvents;

public class PacketScoutExtraJump implements IMessage {

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<PacketScoutExtraJump, IMessage> {

        @Override
        public IMessage onMessage(PacketScoutExtraJump message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                if (TT2.proxy.getScoutArmorEvents() != null) {
                    TT2.proxy.getScoutArmorEvents().handleExtraJumpRequest(ctx.getServerHandler().player);
                }
            });
            return null;
        }
    }
}
