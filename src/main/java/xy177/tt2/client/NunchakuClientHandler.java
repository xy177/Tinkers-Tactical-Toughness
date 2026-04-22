package xy177.tt2.client;

import xy177.tt2.tools.TinkerNunchaku;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class NunchakuClientHandler {

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;

        if (player == null || mc.world == null || mc.isGamePaused()) return;

        ItemStack held = player.getHeldItemMainhand();
        if (!(held.getItem() instanceof TinkerNunchaku)) {
            clearSpinning(player);
            return;
        }

        boolean attackKeyDown = mc.gameSettings.keyBindUseItem.isKeyDown();

        if (attackKeyDown && player.getActiveItemStack().isEmpty()) {
            setSpinning(player, true);

            if (player.getCooledAttackStrength(0.5F) >= 1.0F) {
                RayTraceResult mop = mc.objectMouseOver;
                if (mop != null
                        && mop.typeOfHit == RayTraceResult.Type.ENTITY
                        && mop.entityHit != null
                        && mop.entityHit != player) {
                    Entity target = mop.entityHit;
                    mc.playerController.attackEntity(player, target);
                    player.isSwingInProgress = false;
                }
            }
        } else {
            clearSpinning(player);
        }
    }

    private static void setSpinning(EntityPlayerSP player, boolean value) {
        NBTTagCompound nbt = player.getEntityData();
        if (nbt.getBoolean(TinkerNunchaku.KEY_SPINNING) != value) {
            nbt.setBoolean(TinkerNunchaku.KEY_SPINNING, value);
        }
    }

    private static void clearSpinning(EntityPlayerSP player) {
        NBTTagCompound nbt = player.getEntityData();
        if (nbt.hasKey(TinkerNunchaku.KEY_SPINNING)) {
            nbt.removeTag(TinkerNunchaku.KEY_SPINNING);
        }
    }
}
