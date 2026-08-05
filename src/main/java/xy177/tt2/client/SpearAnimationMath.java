package xy177.tt2.client;

import net.minecraft.item.ItemStack;
import xy177.tt2.config.TT2Config;
import xy177.tt2.logic.SpearChargeStats;

final class SpearAnimationMath {

    private SpearAnimationMath() {
    }

    static AttackPose attack(float attackTime) {
        float time = clamp01(attackTime);
        float prepare = inOutSine(progress(time, 0.0F, 0.05F));
        float attack = inQuad(progress(time, 0.05F, 0.2F));
        float thrust = outBack(progress(time, 0.05F, 0.2F));
        float retract = inOutExpo(progress(time, 0.4F, 1.0F));
        return new AttackPose(prepare, attack, thrust, retract);
    }

    static UsePose use(ItemStack stack, float elapsedTicks) {
        SpearChargeStats stats = SpearChargeStats.resolve(stack);
        float time = Math.max(0.0F, elapsedTicks);
        float delay = stats.braceDelayTicks;
        float raise = progress(time, 0.0F, delay);
        float raiseStart = progress(raise, 0.0F, 0.5F);
        float raiseMiddle = progress(raise, 0.5F, 0.8F);
        float raiseEnd = progress(raise, 0.8F, 1.0F);
        float sway = progress(time, delay + stats.knockOffMountDurationTicks - 20.0F,
            delay + stats.knockOffMountDurationTicks);
        float lower = outCubic(inOutElastic(progress(
            time - 20.0F,
            delay + stats.pushbackDurationTicks - 40.0F,
            delay + stats.pushbackDurationTicks
        )));
        float raiseBack = progress(time,
            delay + stats.damageDurationTicks - 5.0F,
            delay + stats.damageDurationTicks);
        float intensity = 2.0F * outCirc(sway) - 2.0F * inCirc(raiseBack);
        float swaySpeed = (float) TT2Config.spearFatigueAnimationSpeed;
        float slow = (float) Math.sin(Math.toRadians(19.0F * time * swaySpeed)) * intensity;
        float fast = (float) Math.sin(Math.toRadians(30.0F * time * swaySpeed)) * intensity;
        float raiseMovement = 1.0F - outBack(1.0F - raise);
        return new UsePose(raise, raiseStart, raiseMiddle, raiseEnd, sway, lower,
            raiseBack, intensity, slow, fast, raiseMovement);
    }

    static float progress(float value, float start, float end) {
        if (end <= start) {
            return value >= end ? 1.0F : 0.0F;
        }
        return clamp01((value - start) / (end - start));
    }

    static float inOutBack(float x) {
        if (x < 0.5F) {
            return 4.0F * x * x * (7.189819F * x - 2.5949094F) / 2.0F;
        }
        float d = 2.0F * x - 2.0F;
        return (d * d * (3.5949094F * d + 2.5949094F) + 2.0F) / 2.0F;
    }

    private static float inOutSine(float x) {
        return -(float) (Math.cos(Math.PI * x) - 1.0D) / 2.0F;
    }

    private static float inQuad(float x) {
        return x * x;
    }

    private static float outBack(float x) {
        float d = x - 1.0F;
        return 1.0F + 2.70158F * d * d * d + 1.70158F * d * d;
    }

    private static float outCubic(float x) {
        float d = 1.0F - x;
        return 1.0F - d * d * d;
    }

    private static float outCirc(float x) {
        float d = x - 1.0F;
        return (float) Math.sqrt(Math.max(0.0F, 1.0F - d * d));
    }

    private static float inCirc(float x) {
        return 1.0F - (float) Math.sqrt(Math.max(0.0F, 1.0F - x * x));
    }

    private static float inOutExpo(float x) {
        if (x == 0.0F || x == 1.0F) {
            return x;
        }
        if (x < 0.5F) {
            return (float) Math.pow(2.0D, 20.0F * x - 10.0F) / 2.0F;
        }
        return (2.0F - (float) Math.pow(2.0D, -20.0F * x + 10.0F)) / 2.0F;
    }

    private static float inOutElastic(float x) {
        if (x == 0.0F || x == 1.0F) {
            return x;
        }
        float wave = (float) Math.sin((20.0F * x - 11.125F) * 1.3962634801864624D);
        if (x < 0.5F) {
            return -(float) Math.pow(2.0D, 20.0F * x - 10.0F) * wave / 2.0F;
        }
        return (float) Math.pow(2.0D, -20.0F * x + 10.0F) * wave / 2.0F + 1.0F;
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    static final class AttackPose {
        final float prepare;
        final float attack;
        final float thrust;
        final float retract;

        private AttackPose(float prepare, float attack, float thrust, float retract) {
            this.prepare = prepare;
            this.attack = attack;
            this.thrust = thrust;
            this.retract = retract;
        }
    }

    static final class UsePose {
        final float raise;
        final float raiseStart;
        final float raiseMiddle;
        final float raiseEnd;
        final float sway;
        final float lower;
        final float raiseBack;
        final float intensity;
        final float slow;
        final float fast;
        final float raiseMovement;

        private UsePose(float raise, float raiseStart, float raiseMiddle, float raiseEnd,
                        float sway, float lower, float raiseBack, float intensity,
                        float slow, float fast, float raiseMovement) {
            this.raise = raise;
            this.raiseStart = raiseStart;
            this.raiseMiddle = raiseMiddle;
            this.raiseEnd = raiseEnd;
            this.sway = sway;
            this.lower = lower;
            this.raiseBack = raiseBack;
            this.intensity = intensity;
            this.slow = slow;
            this.fast = fast;
            this.raiseMovement = raiseMovement;
        }
    }
}
