package xy177.tt2.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xy177.tt2.config.TT2Config;
import xy177.tt2.init.TT2Items;

public class SpearClientEvents {

    @SubscribeEvent
    public void onRenderSpecificHand(RenderSpecificHandEvent event) {
        SpearAnimationHooks.renderFirstPerson(event);
    }

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        SpearHeldItemLayer.install(event.getRenderer());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            SpearAnimationController.captureInputState();
        } else {
            SpearAnimationController.clientTick();
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote) {
            SpearAnimationController.clear();
        }
    }

    @SubscribeEvent
    public void onInputUpdate(InputUpdateEvent event) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null || event.getEntityPlayer() != player || !player.isHandActive()) {
            return;
        }
        ItemStack active = player.getActiveItemStack();
        if (active.isEmpty() || TT2Items.SPEAR == null || active.getItem() != TT2Items.SPEAR) {
            return;
        }

        float rawForward = event.getMovementInput().moveForward;
        if (!player.isRiding()) {
            float compensation = (float) (5.0 * TT2Config.spearChargeMovementMultiplier);
            event.getMovementInput().moveForward *= compensation;
            event.getMovementInput().moveStrafe *= compensation;
        }

        boolean canSprint = !player.isRiding() && rawForward >= 0.8F
            && (player.getFoodStats().getFoodLevel() > 6 || player.capabilities.allowFlying)
            && !player.isPotionActive(MobEffects.BLINDNESS)
            && !player.collidedHorizontally;
        if (canSprint && Minecraft.getMinecraft().gameSettings.keyBindSprint.isKeyDown()) {
            player.setSprinting(true);
        }
    }
}
