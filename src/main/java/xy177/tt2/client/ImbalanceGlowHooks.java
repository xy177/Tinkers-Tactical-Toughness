package xy177.tt2.client;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.client.Minecraft;
import xy177.tt2.config.TT2Config;
import xy177.tt2.potion.TT2Potions;

public final class ImbalanceGlowHooks {

    private ImbalanceGlowHooks() {
    }

    public static boolean overrideOutlineSelection(boolean vanillaResult, Entity entity,
                                                   Entity viewer) {
        if (vanillaResult || !TT2Config.enableImbalanceStatusOutline
            || !(entity instanceof EntityLivingBase)) {
            return vanillaResult;
        }
        EntityLivingBase living = (EntityLivingBase) entity;
        if (!living.isPotionActive(TT2Potions.IMBALANCE)
            && !living.isPotionActive(TT2Potions.IMBALANCE_IMMUNITY)) {
            return false;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        boolean viewerSleeping = viewer instanceof EntityLivingBase
            && ((EntityLivingBase) viewer).isPlayerSleeping();
        if (entity == viewer && minecraft.gameSettings.thirdPersonView == 0
            && !viewerSleeping) {
            return false;
        }
        return TT2Config.imbalanceOutlineThroughWalls || minecraft.player == null
            || minecraft.player.canEntityBeSeen(entity);
    }

    public static int overrideOutlineColor(int vanillaColor, Entity entity) {
        if (!TT2Config.enableImbalanceStatusOutline
            || !(entity instanceof EntityLivingBase)) {
            return vanillaColor;
        }
        EntityLivingBase living = (EntityLivingBase) entity;
        if (living.isPotionActive(TT2Potions.IMBALANCE)) {
            return TT2Config.imbalanceOutlineColor;
        }
        if (living.isPotionActive(TT2Potions.IMBALANCE_IMMUNITY)) {
            return TT2Config.imbalanceImmunityOutlineColor;
        }
        return vanillaColor;
    }
}
