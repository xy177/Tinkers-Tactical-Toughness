package xy177.tt2.events;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.UUID;

/**
 * 重装盾客户端专属事件。
 * 在 ClientProxy.init() 中注册到 Forge 事件总线。
 */
@SideOnly(Side.CLIENT)
public class HeavyShieldClientEvents {

    private static final UUID BLOCKING_SPEED_UUID =
        UUID.fromString("b1e2f3a4-c5d6-7e8f-90a1-b2c3d4e5f601");


    @SubscribeEvent
    public void onFovUpdate(FOVUpdateEvent event) {
        EntityPlayer player = event.getEntity();

        IAttributeInstance speedAttr =
            player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (speedAttr == null) return;

        // 只要格挡速度修改器存在，就锁定 FOV（无论 isHandActive 是 true 还是 false）
        if (speedAttr.getModifier(BLOCKING_SPEED_UUID) != null) {
            event.setNewfov(1.0f);
        }
    }
}
