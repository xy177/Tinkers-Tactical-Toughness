package xy177.tt2.logic;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import xy177.tt2.config.TT2Config;

public final class SpearAttackTiming {

    private SpearAttackTiming() {
    }

    public static int animationDurationTicks(EntityLivingBase living) {
        double attackSpeed = Math.max(0.001D, TT2Config.spearAttackSpeed);
        int duration = (int) Math.floor(20.0D / attackSpeed);

        PotionEffect haste = living.getActivePotionEffect(MobEffects.HASTE);
        if (haste != null) {
            duration -= 1 + haste.getAmplifier();
        }
        PotionEffect fatigue = living.getActivePotionEffect(MobEffects.MINING_FATIGUE);
        if (fatigue != null) {
            duration += 2 * (1 + fatigue.getAmplifier());
        }
        return Math.max(1, duration);
    }
}
