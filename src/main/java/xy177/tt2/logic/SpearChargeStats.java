package xy177.tt2.logic;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.materials.MaterialTypes;
import slimeknights.tconstruct.library.utils.TagUtil;
import xy177.tt2.config.TT2Config;

public final class SpearChargeStats {

    public final int braceDelayTicks;
    public final double damageMultiplier;
    public final int knockOffMountDurationTicks;
    public final int pushbackDurationTicks;
    public final int damageDurationTicks;
    public final double knockOffMountSpeed;

    private SpearChargeStats(int braceDelayTicks, double damageMultiplier,
                             int knockOffMountDurationTicks, int pushbackDurationTicks,
                             int damageDurationTicks, double knockOffMountSpeed) {
        this.braceDelayTicks = Math.max(0, braceDelayTicks);
        this.damageMultiplier = Math.max(0.0, damageMultiplier);
        this.knockOffMountDurationTicks = Math.max(0, knockOffMountDurationTicks);
        this.pushbackDurationTicks = Math.max(0, pushbackDurationTicks);
        this.damageDurationTicks = Math.max(0, damageDurationTicks);
        this.knockOffMountSpeed = Math.max(0.0, knockOffMountSpeed);
    }

    public static SpearChargeStats resolve(ItemStack spear) {
        if (!"handle".equalsIgnoreCase(TT2Config.spearChargeCalculationMode)) {
            return new SpearChargeStats(
                TT2Config.spearChargeFixedBraceDelayTicks,
                TT2Config.spearChargeFixedDamageMultiplier,
                TT2Config.spearChargeFixedKnockOffMountDurationTicks,
                TT2Config.spearChargeFixedPushbackDurationTicks,
                TT2Config.spearChargeFixedDamageDurationTicks,
                TT2Config.spearChargeFixedKnockOffMountSpeed
            );
        }

        double progress = handleDurabilityModifier(spear)
            * TT2Config.spearChargeHandleCalculationScale;
        int braceDelay = roundedLerp(
            TT2Config.spearChargeHandleBraceDelayHighestTicks,
            TT2Config.spearChargeHandleBraceDelayLowestTicks,
            progress
        );
        double damageMultiplier = lerp(
            TT2Config.spearChargeHandleDamageMultiplierLowest,
            TT2Config.spearChargeHandleDamageMultiplierHighest,
            progress
        );
        int knockOffMountDuration = roundedLerp(
            TT2Config.spearChargeHandleKnockOffMountDurationHighestTicks,
            TT2Config.spearChargeHandleKnockOffMountDurationLowestTicks,
            progress
        );
        int pushbackDuration = roundedLerp(
            TT2Config.spearChargeHandlePushbackDurationHighestTicks,
            TT2Config.spearChargeHandlePushbackDurationLowestTicks,
            progress
        );
        int damageDuration = roundedLerp(
            TT2Config.spearChargeHandleDamageDurationHighestTicks,
            TT2Config.spearChargeHandleDamageDurationLowestTicks,
            progress
        );
        double knockOffMountSpeed = lerp(
            TT2Config.spearChargeHandleKnockOffMountSpeedHighest,
            TT2Config.spearChargeHandleKnockOffMountSpeedLowest,
            progress
        );

        if (TT2Config.spearChargeHandleLimitsEnabled) {
            braceDelay = clamp(braceDelay,
                TT2Config.spearChargeHandleBraceDelayMinimumTicks,
                TT2Config.spearChargeHandleBraceDelayMaximumTicks);
            damageMultiplier = clamp(damageMultiplier,
                TT2Config.spearChargeHandleDamageMultiplierMinimum,
                TT2Config.spearChargeHandleDamageMultiplierMaximum);
            knockOffMountDuration = clamp(knockOffMountDuration,
                TT2Config.spearChargeHandleKnockOffMountDurationMinimumTicks,
                TT2Config.spearChargeHandleKnockOffMountDurationMaximumTicks);
            pushbackDuration = clamp(pushbackDuration,
                TT2Config.spearChargeHandlePushbackDurationMinimumTicks,
                TT2Config.spearChargeHandlePushbackDurationMaximumTicks);
            damageDuration = clamp(damageDuration,
                TT2Config.spearChargeHandleDamageDurationMinimumTicks,
                TT2Config.spearChargeHandleDamageDurationMaximumTicks);
            knockOffMountSpeed = clamp(knockOffMountSpeed,
                TT2Config.spearChargeHandleKnockOffMountSpeedMinimum,
                TT2Config.spearChargeHandleKnockOffMountSpeedMaximum);
        }

        return new SpearChargeStats(braceDelay, damageMultiplier, knockOffMountDuration,
            pushbackDuration, damageDuration, knockOffMountSpeed);
    }

    public static double handleDurabilityModifier(ItemStack spear) {
        try {
            NBTTagList materials = TagUtil.getBaseMaterialsTagList(spear);
            if (materials.tagCount() > 1) {
                Material material = TinkerRegistry.getMaterial(materials.getStringTagAt(1));
                if (material != null && material != Material.UNKNOWN) {
                    HandleMaterialStats stats = material.getStats(MaterialTypes.HANDLE);
                    if (stats != null && Float.isFinite(stats.modifier)) {
                        return stats.modifier;
                    }
                }
            }
        } catch (RuntimeException ignored) {
        }
        return 1.0;
    }

    private static int roundedLerp(double start, double end, double progress) {
        double value = lerp(start, end, progress);
        if (value >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value <= Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) Math.round(value);
    }

    private static double lerp(double start, double end, double progress) {
        return start + (end - start) * progress;
    }

    private static int clamp(int value, int first, int second) {
        int min = Math.min(first, second);
        int max = Math.max(first, second);
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double first, double second) {
        double min = Math.min(first, second);
        double max = Math.max(first, second);
        return Math.max(min, Math.min(max, value));
    }
}
