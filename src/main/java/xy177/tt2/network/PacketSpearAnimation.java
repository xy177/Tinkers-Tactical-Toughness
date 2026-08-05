package xy177.tt2.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.tt2.TT2;

public class PacketSpearAnimation implements IMessage {

    private int entityId;
    private EnumHand hand;
    private int requestSequence;
    private int animationSequence;
    private int durationTicks;

    public PacketSpearAnimation() {
    }

    public PacketSpearAnimation(int entityId, EnumHand hand, int requestSequence,
                                int animationSequence, int durationTicks) {
        this.entityId = entityId;
        this.hand = hand;
        this.requestSequence = requestSequence;
        this.animationSequence = animationSequence;
        this.durationTicks = durationTicks;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
        hand = buf.readBoolean() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
        requestSequence = buf.readInt();
        animationSequence = buf.readInt();
        durationTicks = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeBoolean(hand == EnumHand.OFF_HAND);
        buf.writeInt(requestSequence);
        buf.writeInt(animationSequence);
        buf.writeInt(durationTicks);
    }

    public static class Handler implements IMessageHandler<PacketSpearAnimation, IMessage> {

        @Override
        public IMessage onMessage(PacketSpearAnimation message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(
                () -> TT2.proxy.handleSpearAnimation(
                    message.entityId,
                    message.hand,
                    message.requestSequence,
                    message.animationSequence,
                    message.durationTicks
                )
            );
            return null;
        }
    }
}
