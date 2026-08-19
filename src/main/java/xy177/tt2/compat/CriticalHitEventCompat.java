package xy177.tt2.compat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.fml.common.Loader;
import xy177.tt2.config.TT2Config;

public final class CriticalHitEventCompat {

    private CriticalHitEventCompat() {
    }

    public static float resolveDamageModifier(float tinkerMultiplier, EntityLivingBase attacker, Entity target) {
        if (!TT2Config.enableCriticalHitEventSync
            || !Loader.isModLoaded("mixinbooter")
            || !(attacker instanceof EntityPlayer)
            || target == null
            || attacker.world.isRemote) {
            return tinkerMultiplier;
        }

        CriticalHitEvent event = ForgeHooks.getCriticalHit(
            (EntityPlayer) attacker,
            target,
            true,
            tinkerMultiplier
        );
        return event == null ? 1.0F : event.getDamageModifier();
    }
}
