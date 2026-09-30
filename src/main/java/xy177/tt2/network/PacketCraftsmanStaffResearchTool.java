package xy177.tt2.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import xy177.tt2.compat.CraftsmanStaffCompat;
import xy177.tt2.modifiers.ModCraftsmanStaffTemplate;
import xy177.tt2.tools.CraftsmanStaff;

public class PacketCraftsmanStaffResearchTool implements IMessage {

    private EnumHand hand;
    private String itemId;

    public PacketCraftsmanStaffResearchTool() {
    }

    public PacketCraftsmanStaffResearchTool(EnumHand hand, String itemId) {
        this.hand = hand;
        this.itemId = itemId == null ? "" : itemId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        hand = buf.readBoolean() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
        itemId = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(hand == EnumHand.OFF_HAND);
        ByteBufUtils.writeUTF8String(buf, itemId == null ? "" : itemId);
    }

    public static class Handler implements IMessageHandler<PacketCraftsmanStaffResearchTool, IMessage> {

        @Override
        public IMessage onMessage(PacketCraftsmanStaffResearchTool message, MessageContext ctx) {
            ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
                if (message.itemId == null || message.itemId.length() > 128) {
                    return;
                }
                ItemStack staff = ctx.getServerHandler().player.getHeldItem(message.hand);
                if (staff.isEmpty() || !(staff.getItem() instanceof CraftsmanStaff)
                    || !ModCraftsmanStaffTemplate.has(staff, ModCraftsmanStaffTemplate.Type.RESEARCH)) {
                    return;
                }
                CraftsmanStaffCompat.setSelectedResearchTool(
                    staff, message.itemId.isEmpty() ? null : message.itemId);
            });
            return null;
        }
    }
}
