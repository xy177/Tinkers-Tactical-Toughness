package xy177.tt2.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.tt2.events.SpearEvents;

public class PacketSpearStab implements IMessage {

    private int sequence;

    public PacketSpearStab() {
    }

    public PacketSpearStab(int sequence) {
        this.sequence = sequence;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        sequence = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(sequence);
    }

    public static class Handler implements IMessageHandler<PacketSpearStab, IMessage> {

        @Override
        public IMessage onMessage(PacketSpearStab message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(
                () -> SpearEvents.handleStabRequest(ctx.getServerHandler().player, message.sequence)
            );
            return null;
        }
    }
}
