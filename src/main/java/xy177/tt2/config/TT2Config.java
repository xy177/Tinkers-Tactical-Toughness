package xy177.tt2.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class TT2Config {

    private static final String CATEGORY_SPEAR = "spear";

    public static boolean enableSwiftShield = true;
    public static boolean enableHeavyShield = true;
    public static boolean enableNunchaku = true;
    public static boolean enableDoppelhander = true;
    public static boolean enableMaraca = true;
    public static boolean enableSpear = true;
    public static boolean enableScoutArmor = true;
    public static boolean enableConstructArmorExtraModifierSlots = true;
    public static int constructArmorExtraModifierSlots = 1;

    public static double constructArmorSetMiningSpeedPerPiece = 0.15;
    public static int constructArmorSetDefenseDamageRecoveryDelayTicks = 20;
    public static int constructArmorSetDefenseDamageRecoveryIntervalTicks = 20;
    public static double constructArmorSetDefenseDamageRecoveryPercent = 0.20;
    public static double constructArmorSetDefenseDamageGainMultiplier = 0.5;
    public static double constructArmorSetMaxDefenseDamage = 0.5;

    public static double parryThresholdPercent = 50.0;
    public static double cooldownCoefficient = 1.0;
    public static int perfectParryWindowTicks = 30;

    public static final float plateShieldDamageReduction = 0.15f;

    public static double imbalanceDurationMultiplier = 0.75;
    public static double imbalanceSpeedReduction = 1.0;
    public static double imbalanceDamageReduction = 1.0;
    public static double imbalanceBossDamageReduction = 0.5;
    public static double imbalanceKnockbackReduction = 1.0;
    public static double imbalanceBossKnockbackReduction = 0.5;
    public static double imbalanceDamageTakenIncrease = 0.5;
    public static boolean enableImbalanceStatusOutline = true;
    public static int imbalanceOutlineColor = 0xE34A32;
    public static int imbalanceImmunityOutlineColor = 0x55D9E8;
    public static boolean imbalanceOutlineThroughWalls = true;

    public static double nunchakuAttackSpeed = 2.5;
    public static float nunchakuComboGainPerHit = 0.1f;
    public static float nunchakuComboCapBindingMultiplier = 0.45f;
    public static int nunchakuComboDecayDelay = 60;

    public static double doppelhanderBaseBlockReduction = 0.6;
    public static double doppelhanderBlockGainPerHandleModifier = 5.0;
    public static int doppelhanderDefensiveStanceDurationSeconds = 20;
    public static double doppelhanderBlockHealPercent = 0.05;
    public static double doppelhanderDamageBonusArmorCoefficient = 1.5;
    public static double doppelhanderDamageBonusMinPercent = 20.0;
    public static double doppelhanderAoeRadius = 4.5;

    public static double scoutRangedDamageCoefficient = 2.0;
    public static double scoutDodgeChanceCoefficient = 1.0;
    public static double scoutDodgeDamageFactor = 0.2;
    public static double scoutDodgeChanceCap = 0.8;
    public static double scoutFallDamageReduction = 0.8;
    public static double scoutEnvironmentalDamageReduction = 0.5;

    public static double maracaAttackMelodyAllyBonus = 0.30;
    public static double maracaAttackMelodySelfBonus = 0.45;
    public static int maracaAttackMelodyDurationTicks = 1200;
    public static double maracaPartyDurationCoefficient = 1.0;

    public static double craftsmanStaffMovementSpeedCoefficient = 0.10;
    public static double craftsmanStaffCombatDamageCoefficient = 1.0;
    public static double craftsmanStaffFellingDamageCoefficient = 1.2;
    public static double craftsmanStaffCombatFellingDamageCoefficient = 1.35;
    public static double craftsmanStaffInsightSpellDamageCoefficient = 1.0;

    public static double spearAttackSpeed = 1.1;
    public static double spearDamageCoefficient = 0.7;

    public static double spearStabMinReach = 2.0;
    public static double spearStabMaxReach = 4.5;
    public static double spearStabCreativeMaxReach = 6.5;
    public static double spearStabAimTolerance = 0.125;
    public static double spearStabMinimumAttackRecharge = 1.0;
    public static int spearStabMaxTargets = 0;
    public static double spearStabBasePushback = 0.4;
    public static int spearStabDurabilityPerTarget = 1;
    public static double spearStabExhaustionPerTarget = 0.1;

    public static String spearChargeCalculationMode = "fixed";
    public static int spearChargeFixedBraceDelayTicks = 10;
    public static double spearChargeFixedDamageMultiplier = 1.1;
    public static int spearChargeFixedKnockOffMountDurationTicks = 100;
    public static int spearChargeFixedPushbackDurationTicks = 200;
    public static int spearChargeFixedDamageDurationTicks = 500;
    public static double spearChargeFixedKnockOffMountSpeed = 9.0;

    public static double spearChargeHandleCalculationScale = 1.0;
    public static int spearChargeHandleBraceDelayHighestTicks = 15;
    public static double spearChargeHandleDamageMultiplierLowest = 0.7;
    public static int spearChargeHandleKnockOffMountDurationHighestTicks = 100;
    public static int spearChargeHandlePushbackDurationHighestTicks = 200;
    public static int spearChargeHandleDamageDurationHighestTicks = 300;
    public static double spearChargeHandleKnockOffMountSpeedHighest = 14.0;
    public static int spearChargeHandleBraceDelayLowestTicks = 8;
    public static double spearChargeHandleDamageMultiplierHighest = 1.2;
    public static int spearChargeHandleKnockOffMountDurationLowestTicks = 50;
    public static int spearChargeHandlePushbackDurationLowestTicks = 110;
    public static int spearChargeHandleDamageDurationLowestTicks = 175;
    public static double spearChargeHandleKnockOffMountSpeedLowest = 9.0;

    public static boolean spearChargeHandleLimitsEnabled = true;
    public static int spearChargeHandleBraceDelayMinimumTicks = 8;
    public static int spearChargeHandleBraceDelayMaximumTicks = 15;
    public static double spearChargeHandleDamageMultiplierMinimum = 0.7;
    public static double spearChargeHandleDamageMultiplierMaximum = 1.2;
    public static int spearChargeHandleKnockOffMountDurationMinimumTicks = 50;
    public static int spearChargeHandleKnockOffMountDurationMaximumTicks = 100;
    public static int spearChargeHandlePushbackDurationMinimumTicks = 110;
    public static int spearChargeHandlePushbackDurationMaximumTicks = 200;
    public static int spearChargeHandleDamageDurationMinimumTicks = 175;
    public static int spearChargeHandleDamageDurationMaximumTicks = 300;
    public static double spearChargeHandleKnockOffMountSpeedMinimum = 9.0;
    public static double spearChargeHandleKnockOffMountSpeedMaximum = 14.0;

    public static double spearChargeMinimumClosingSpeedForDamage = 4.6;
    public static double spearChargeMinimumWielderSpeedForPushback = 5.1;
    public static int spearChargeSameTargetDelayTicks = 10;
    public static double spearChargeBaseDamage = 1.0;
    public static double spearChargeBasePushback = 0.4;
    public static int spearChargeDurabilityCost = 1;
    public static double spearChargeExhaustion = 0.1;
    public static int spearChargeMaxTargets = 0;
    public static double spearChargeDamageCap = 0.0;
    public static double spearChargeMovementMultiplier = 1.0;
    public static boolean spearChargeBrieflyIgnoreSlowTouches = true;
    public static boolean spearChargeCanKnockTargetsOffMounts = true;

    public static double spearStabAnimationStrength = 1.0;
    public static double spearBraceAnimationStrength = 1.0;
    public static double spearFatigueAnimationStrength = 1.0;
    public static double spearFatigueAnimationSpeed = 1.0;

    public static int spearLungeMaxLevel = 3;
    public static double spearLungeForwardBoostPerLevel = 0.458;
    public static double spearLungeExhaustionPerLevel = 4.0;
    public static int spearLungeDurabilityCost = 1;
    public static int spearLungeMinFoodLevel = 7;
    public static double spearLungeMaxHorizontalSpeed = 0.0;
    public static int spearLungePistonCost = 1;
    public static int spearLungeKnightSlimeIngotCost = 1;
    public static int spearLungeModifierSlotsPerLevel = 1;
    public static boolean spearLungeDisallowRiding = true;
    public static boolean spearLungeDisallowWater = true;
    public static boolean spearLungeDisallowElytraFlight = true;

    public static boolean enableDefenseDamage = true;
    public static double defenseDamageBossHitPercent = 0.075;
    public static double defenseDamageNormalHitPercent = 0.0375;
    public static double defenseDamageMinimumEfficiency = 0.25;
    public static int defenseDamageTriggerIntervalTicks = 15;
    public static int defenseDamageRecoveryDelayTicks = 40;
    public static int defenseDamageRecoveryIntervalTicks = 30;
    public static double defenseDamageRecoveryPercent = 0.15;

    public static void init(File configFile) {
        Configuration cfg = new Configuration(configFile);
        boolean removedLegacyImbalanceGlowMode = false;
        try {
            cfg.load();
            if (cfg.hasKey(Configuration.CATEGORY_GENERAL, "imbalanceGlowMode")) {
                cfg.getCategory(Configuration.CATEGORY_GENERAL).remove("imbalanceGlowMode");
                removedLegacyImbalanceGlowMode = true;
            }
            cfg.setCategoryComment(
                CATEGORY_SPEAR,
                desc(
                    "矛的基础属性、戳刺、冲锋和专属强化“突进”的设置。所有 tick 时间均为 20 tick = 1 秒。",
                    "Settings for the Spear's base stats, stab, charge, and exclusive Lunge modifier. All times use 20 ticks = 1 second."
                )
            );

            enableSwiftShield = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableSwiftShield",
                true,
                desc(
                    "是否注册并启用迅捷盾。设为 false 后，该物品不会出现在游戏中，需要重启生效。",
                    "Whether to register and enable the Swift Shield. Set to false to remove it from the game. Requires restart."
                )
            ).getBoolean();

            enableHeavyShield = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableHeavyShield",
                true,
                desc(
                    "是否注册并启用重装盾。设为 false 后，该物品不会出现在游戏中，需要重启生效。",
                    "Whether to register and enable the Heavy Shield. Set to false to remove it from the game. Requires restart."
                )
            ).getBoolean();

            enableNunchaku = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableNunchaku",
                true,
                desc(
                    "是否注册并启用双节棍。设为 false 后，该物品不会出现在游戏中，需要重启生效。",
                    "Whether to register and enable the Nunchaku. Set to false to remove it from the game. Requires restart."
                )
            ).getBoolean();

            enableDoppelhander = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableDoppelhander",
                true,
                desc(
                    "是否注册并启用德式双手剑。设为 false 后，该物品不会出现在游戏中，需要重启生效。",
                    "Whether to register and enable the Doppelhander. Set to false to remove it from the game. Requires restart."
                )
            ).getBoolean();

            enableMaraca = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableMaraca",
                true,
                desc(
                    "是否注册并启用沙锤。设为 false 后，该物品不会出现在游戏中，需要重启生效。",
                    "Whether to register and enable the Maraca. Set to false to remove it from the game. Requires restart."
                )
            ).getBoolean();

            enableSpear = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableSpear",
                true,
                desc(
                    "是否注册并启用矛。设为 false 后，该物品及其专属突进强化不会出现在游戏中，需要重启生效。",
                    "Whether to register and enable the Spear. Set to false to remove the item and its exclusive Lunge modifier from the game. Requires restart."
                )
            ).getBoolean();

            enableScoutArmor = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableScoutArmor",
                true,
                desc(
                    "是否注册并启用斥候套装。设为 false 后，该套护甲不会出现在游戏中，需要重启生效。",
                    "Whether to register and enable the Scout Armor set. Set to false to remove it from the game. Requires restart."
                )
            ).getBoolean();

            spearAttackSpeed = spearDouble(
                cfg, "attackSpeed", 1.1, 0.0, 100.0,
                "矛的攻击速度。数值越高，攻击条恢复越快。",
                "Spear attack speed. Higher values refill the attack indicator faster."
            );
            spearDamageCoefficient = spearDouble(
                cfg, "damageCoefficient", 0.7, 0.0, 100.0,
                "矛头材料攻击力乘以此数值后，计入矛的面板伤害。",
                "The Spear head material's attack value is multiplied by this number for the displayed damage."
            );

            spearStabMinReach = spearDouble(
                cfg, "stabMinReach", 2.0, 0.0, 64.0,
                "目标离玩家至少这么远，戳刺才能命中，单位为格。",
                "A target must be at least this far from the player to be hit by a stab, in blocks."
            );
            spearStabMaxReach = spearDouble(
                cfg, "stabMaxReach", 4.5, 0.0, 64.0,
                "生存模式下戳刺最远能命中的距离，单位为格。",
                "Farthest distance a stab can hit in Survival mode, in blocks."
            );
            spearStabCreativeMaxReach = spearDouble(
                cfg, "stabCreativeMaxReach", 6.5, 0.0, 64.0,
                "创造模式下戳刺最远能命中的距离，单位为格。",
                "Farthest distance a stab can hit in Creative mode, in blocks."
            );
            spearStabAimTolerance = spearDouble(
                cfg, "stabAimTolerance", 0.125, 0.0, 8.0,
                "戳刺判定的宽松程度。数值越大，准星稍有偏差也越容易命中，单位为格。",
                "How forgiving the stab aim is. Higher values allow a wider miss around the crosshair, in blocks."
            );
            spearStabMinimumAttackRecharge = spearDouble(
                cfg, "stabMinimumAttackRecharge", 1.0, 0.0, 1.0,
                "按左键时，攻击条至少恢复到这个比例才会发动戳刺。1.0 表示完全恢复，0.5 表示恢复一半。",
                "How full the attack indicator must be before a left-click can stab. 1.0 means full; 0.5 means half full."
            );
            spearStabMaxTargets = spearInt(
                cfg, "stabMaxTargets", 0, 0, Integer.MAX_VALUE,
                "一次戳刺最多能命中多少个目标。0 表示不限制。",
                "Maximum targets one stab can hit. 0 means unlimited."
            );
            spearStabBasePushback = spearDouble(
                cfg, "stabBasePushback", 0.4, 0.0, 100.0,
                "戳刺命中后把目标推开的力度。",
                "How strongly a successful stab pushes the target away."
            );
            spearStabDurabilityPerTarget = spearInt(
                cfg, "stabDurabilityPerTarget", 1, 0, Integer.MAX_VALUE,
                "戳刺每命中一个生物时，矛消耗的耐久。",
                "Spear durability consumed for each living target hit by a stab."
            );
            spearStabExhaustionPerTarget = spearDouble(
                cfg, "stabExhaustionPerTarget", 0.1, 0.0, 1000.0,
                "戳刺每命中一个目标增加的饥饿消耗值。累计约 4 点通常会消耗 1 点饱和度或饥饿值。",
                "Exhaustion added for each target hit by a stab. About 4 exhaustion normally consumes 1 saturation or food point."
            );

            spearChargeCalculationMode = spearString(
                cfg, "chargeCalculationMode", "fixed",
                "冲锋数值的计算方式：fixed 始终使用下方固定数值；handle 根据坚韧手柄的耐久系数计算。",
                "How charge values are chosen: fixed always uses the fixed values below; handle calculates them from the tough handle durability modifier."
            );
            if ("handle".equalsIgnoreCase(spearChargeCalculationMode)) {
                spearChargeCalculationMode = "handle";
            } else {
                spearChargeCalculationMode = "fixed";
            }

            spearChargeFixedBraceDelayTicks = spearInt(
                cfg, "chargeFixedBraceDelayTicks", 10, 0, 72000,
                "固定模式下，按住右键架矛后要等待多少 tick，撞到目标才会触发冲锋效果。",
                "In fixed mode, ticks after bracing the Spear before colliding with a target can trigger charge effects."
            );
            spearChargeFixedDamageMultiplier = spearDouble(
                cfg, "chargeFixedDamageMultiplier", 1.1, 0.0, 100.0,
                "固定模式下，双方迎面接近的速度带来多少冲锋伤害。数值越大，速度增加的伤害越多。",
                "In fixed mode, how much damage comes from the speed at which wielder and target approach each other. Higher values add more damage."
            );
            spearChargeFixedKnockOffMountDurationTicks = spearInt(
                cfg, "chargeFixedKnockOffMountDurationTicks", 100, 0, 72000,
                "固定模式下，开始检测目标后，冲锋还能把目标撞下坐骑的持续时间，单位为 tick。",
                "In fixed mode, how long after target detection starts that a charge can still knock a target off its mount, in ticks."
            );
            spearChargeFixedPushbackDurationTicks = spearInt(
                cfg, "chargeFixedPushbackDurationTicks", 200, 0, 72000,
                "固定模式下，开始检测目标后，冲锋还能击退目标的持续时间，单位为 tick。",
                "In fixed mode, how long after target detection starts that a charge can still push targets back, in ticks."
            );
            spearChargeFixedDamageDurationTicks = spearInt(
                cfg, "chargeFixedDamageDurationTicks", 500, 0, 72000,
                "固定模式下，开始检测目标后，冲锋还能造成伤害的持续时间，单位为 tick。",
                "In fixed mode, how long after target detection starts that a charge can still deal damage, in ticks."
            );
            spearChargeFixedKnockOffMountSpeed = spearDouble(
                cfg, "chargeFixedKnockOffMountSpeed", 9.0, 0.0, 1000.0,
                "固定模式下，要把目标撞下坐骑，玩家自己朝准星方向移动至少要达到的速度，单位为格每秒。",
                "In fixed mode, minimum speed the wielder must move toward the crosshair to knock a target off its mount, in blocks per second."
            );

            spearChargeHandleCalculationScale = spearDouble(
                cfg, "chargeHandleCalculationScale", 1.0, 0.0, 100.0,
                "手柄模式的计算倍率。计算位置 = 坚韧手柄耐久系数 x 此值；每项结果 = 位置 0 参考值 +（位置 1 参考值 - 位置 0 参考值）x 计算位置。下方会按实际作用注明最高值和最低值。",
                "Handle-mode calculation scale. Position = tough handle durability modifier x this value; each result = position 0 reference + (position 1 reference - position 0 reference) x position. The descriptions below identify the higher and lower values."
            );
            spearChargeHandleBraceDelayHighestTicks = spearInt(
                cfg, "chargeHandleBraceDelayHighestTicks", 15, 0, 72000,
                "手柄模式架矛等待时间的最高参考值（计算位置 0），单位为 tick。",
                "Higher reference value for handle-mode brace delay (position 0), in ticks."
            );
            spearChargeHandleDamageMultiplierLowest = spearDouble(
                cfg, "chargeHandleDamageMultiplierLowest", 0.7, 0.0, 100.0,
                "手柄模式速度伤害倍率的最低参考值（计算位置 0）。",
                "Lower reference value for the handle-mode speed-based damage multiplier (position 0)."
            );
            spearChargeHandleKnockOffMountDurationHighestTicks = spearInt(
                cfg, "chargeHandleKnockOffMountDurationHighestTicks", 100, 0, 72000,
                "手柄模式“能把目标撞下坐骑”持续时间的最高参考值（计算位置 0），单位为 tick。",
                "Higher reference value for how long handle-mode charges can knock targets off mounts (position 0), in ticks."
            );
            spearChargeHandlePushbackDurationHighestTicks = spearInt(
                cfg, "chargeHandlePushbackDurationHighestTicks", 200, 0, 72000,
                "手柄模式击退持续时间的最高参考值（计算位置 0），单位为 tick。",
                "Higher reference value for handle-mode pushback time (position 0), in ticks."
            );
            spearChargeHandleDamageDurationHighestTicks = spearInt(
                cfg, "chargeHandleDamageDurationHighestTicks", 300, 0, 72000,
                "手柄模式伤害持续时间的最高参考值（计算位置 0），单位为 tick。",
                "Higher reference value for handle-mode damage-dealing time (position 0), in ticks."
            );
            spearChargeHandleKnockOffMountSpeedHighest = spearDouble(
                cfg, "chargeHandleKnockOffMountSpeedHighest", 14.0, 0.0, 1000.0,
                "手柄模式“撞下坐骑所需玩家速度”的最高参考值（计算位置 0），单位为格每秒。",
                "Higher reference value for the wielder speed needed to knock targets off mounts in handle mode (position 0), in blocks per second."
            );
            spearChargeHandleBraceDelayLowestTicks = spearInt(
                cfg, "chargeHandleBraceDelayLowestTicks", 8, 0, 72000,
                "手柄模式架矛等待时间的最低参考值（计算位置 1），单位为 tick。",
                "Lower reference value for handle-mode brace delay (position 1), in ticks."
            );
            spearChargeHandleDamageMultiplierHighest = spearDouble(
                cfg, "chargeHandleDamageMultiplierHighest", 1.2, 0.0, 100.0,
                "手柄模式速度伤害倍率的最高参考值（计算位置 1）。",
                "Higher reference value for the handle-mode speed-based damage multiplier (position 1)."
            );
            spearChargeHandleKnockOffMountDurationLowestTicks = spearInt(
                cfg, "chargeHandleKnockOffMountDurationLowestTicks", 50, 0, 72000,
                "手柄模式“能把目标撞下坐骑”持续时间的最低参考值（计算位置 1），单位为 tick。",
                "Lower reference value for how long handle-mode charges can knock targets off mounts (position 1), in ticks."
            );
            spearChargeHandlePushbackDurationLowestTicks = spearInt(
                cfg, "chargeHandlePushbackDurationLowestTicks", 110, 0, 72000,
                "手柄模式击退持续时间的最低参考值（计算位置 1），单位为 tick。",
                "Lower reference value for handle-mode pushback time (position 1), in ticks."
            );
            spearChargeHandleDamageDurationLowestTicks = spearInt(
                cfg, "chargeHandleDamageDurationLowestTicks", 175, 0, 72000,
                "手柄模式伤害持续时间的最低参考值（计算位置 1），单位为 tick。",
                "Lower reference value for handle-mode damage-dealing time (position 1), in ticks."
            );
            spearChargeHandleKnockOffMountSpeedLowest = spearDouble(
                cfg, "chargeHandleKnockOffMountSpeedLowest", 9.0, 0.0, 1000.0,
                "手柄模式“撞下坐骑所需玩家速度”的最低参考值（计算位置 1），单位为格每秒。",
                "Lower reference value for the wielder speed needed to knock targets off mounts in handle mode (position 1), in blocks per second."
            );

            spearChargeHandleLimitsEnabled = spearBoolean(
                cfg, "chargeHandleLimitsEnabled", true,
                "是否限制手柄模式的计算结果。设为 true 后，每项结果都不会低于下方最小值，也不会高于下方最大值。",
                "Whether to limit handle-mode results. When true, each result stays between its minimum and maximum below."
            );
            spearChargeHandleBraceDelayMinimumTicks = spearInt(
                cfg, "chargeHandleBraceDelayMinimumTicks", 8, 0, 72000,
                "手柄模式计算出的架矛等待时间允许的最小值，单位为 tick。",
                "Minimum allowed brace delay calculated in handle mode, in ticks."
            );
            spearChargeHandleBraceDelayMaximumTicks = spearInt(
                cfg, "chargeHandleBraceDelayMaximumTicks", 15, 0, 72000,
                "手柄模式计算出的架矛等待时间允许的最大值，单位为 tick。",
                "Maximum allowed brace delay calculated in handle mode, in ticks."
            );
            spearChargeHandleDamageMultiplierMinimum = spearDouble(
                cfg, "chargeHandleDamageMultiplierMinimum", 0.7, 0.0, 100.0,
                "手柄模式计算出的速度伤害倍率允许的最小值。",
                "Minimum allowed speed-based damage multiplier calculated in handle mode."
            );
            spearChargeHandleDamageMultiplierMaximum = spearDouble(
                cfg, "chargeHandleDamageMultiplierMaximum", 1.2, 0.0, 100.0,
                "手柄模式计算出的速度伤害倍率允许的最大值。",
                "Maximum allowed speed-based damage multiplier calculated in handle mode."
            );
            spearChargeHandleKnockOffMountDurationMinimumTicks = spearInt(
                cfg, "chargeHandleKnockOffMountDurationMinimumTicks", 50, 0, 72000,
                "手柄模式计算出的“能把目标撞下坐骑”持续时间允许的最小值，单位为 tick。",
                "Minimum allowed time for knocking targets off mounts calculated in handle mode, in ticks."
            );
            spearChargeHandleKnockOffMountDurationMaximumTicks = spearInt(
                cfg, "chargeHandleKnockOffMountDurationMaximumTicks", 100, 0, 72000,
                "手柄模式计算出的“能把目标撞下坐骑”持续时间允许的最大值，单位为 tick。",
                "Maximum allowed time for knocking targets off mounts calculated in handle mode, in ticks."
            );
            spearChargeHandlePushbackDurationMinimumTicks = spearInt(
                cfg, "chargeHandlePushbackDurationMinimumTicks", 110, 0, 72000,
                "手柄模式计算出的击退持续时间允许的最小值，单位为 tick。",
                "Minimum allowed pushback time calculated in handle mode, in ticks."
            );
            spearChargeHandlePushbackDurationMaximumTicks = spearInt(
                cfg, "chargeHandlePushbackDurationMaximumTicks", 200, 0, 72000,
                "手柄模式计算出的击退持续时间允许的最大值，单位为 tick。",
                "Maximum allowed pushback time calculated in handle mode, in ticks."
            );
            spearChargeHandleDamageDurationMinimumTicks = spearInt(
                cfg, "chargeHandleDamageDurationMinimumTicks", 175, 0, 72000,
                "手柄模式计算出的伤害持续时间允许的最小值，单位为 tick。",
                "Minimum allowed damage-dealing time calculated in handle mode, in ticks."
            );
            spearChargeHandleDamageDurationMaximumTicks = spearInt(
                cfg, "chargeHandleDamageDurationMaximumTicks", 300, 0, 72000,
                "手柄模式计算出的伤害持续时间允许的最大值，单位为 tick。",
                "Maximum allowed damage-dealing time calculated in handle mode, in ticks."
            );
            spearChargeHandleKnockOffMountSpeedMinimum = spearDouble(
                cfg, "chargeHandleKnockOffMountSpeedMinimum", 9.0, 0.0, 1000.0,
                "手柄模式计算出的“撞下坐骑所需速度”允许的最小值，单位为格每秒。",
                "Minimum allowed wielder speed needed to knock targets off mounts in handle mode, in blocks per second."
            );
            spearChargeHandleKnockOffMountSpeedMaximum = spearDouble(
                cfg, "chargeHandleKnockOffMountSpeedMaximum", 14.0, 0.0, 1000.0,
                "手柄模式计算出的“撞下坐骑所需速度”允许的最大值，单位为格每秒。",
                "Maximum allowed wielder speed needed to knock targets off mounts in handle mode, in blocks per second."
            );

            spearChargeMinimumClosingSpeedForDamage = spearDouble(
                cfg, "chargeMinimumClosingSpeedForDamage", 4.6, 0.0, 1000.0,
                "冲锋要造成伤害，玩家与目标沿准星方向相互接近的速度差至少要达到此值，单位为格每秒。",
                "Minimum closing speed toward the target required for charge damage, in blocks per second."
            );
            spearChargeMinimumWielderSpeedForPushback = spearDouble(
                cfg, "chargeMinimumWielderSpeedForPushback", 5.1, 0.0, 1000.0,
                "冲锋要击退目标，玩家自己朝准星方向移动至少要达到的速度，单位为格每秒。",
                "Minimum speed the wielder must move toward the crosshair for a charge to push targets back, in blocks per second."
            );
            spearChargeSameTargetDelayTicks = spearInt(
                cfg, "chargeSameTargetDelayTicks", 10, 0, 72000,
                "同一目标被一次冲锋效果影响后，至少等待多少 tick 才能再次被这次冲锋影响。",
                "Ticks before the same braced charge can affect the same target again."
            );
            spearChargeBaseDamage = spearDouble(
                cfg, "chargeBaseDamage", 1.0, 0.0, 1000000.0,
                "速度达到要求后，每次冲锋伤害都会固定加入的基础值。",
                "Fixed base value added whenever a charge is fast enough to deal damage."
            );
            spearChargeBasePushback = spearDouble(
                cfg, "chargeBasePushback", 0.4, 0.0, 100.0,
                "速度达到要求后，冲锋把目标推开的力度。",
                "How strongly a charge pushes targets away when moving fast enough."
            );
            spearChargeDurabilityCost = spearInt(
                cfg, "chargeDurabilityCost", 1, 0, Integer.MAX_VALUE,
                "冲锋每次对一个生物实际造成伤害、击退或把它撞下坐骑时，矛消耗的耐久。",
                "Spear durability consumed each time a charge damages, pushes, or knocks a living target off its mount."
            );
            spearChargeExhaustion = spearDouble(
                cfg, "chargeExhaustion", 0.1, 0.0, 1000.0,
                "冲锋每次成功影响一个目标增加的饥饿消耗值。累计约 4 点通常会消耗 1 点饱和度或饥饿值。",
                "Exhaustion added each time a charge successfully affects a target. About 4 exhaustion normally consumes 1 saturation or food point."
            );
            spearChargeMaxTargets = spearInt(
                cfg, "chargeMaxTargets", 0, 0, Integer.MAX_VALUE,
                "一次按住右键架矛期间，最多能影响多少个不同目标。0 表示不限制。",
                "Maximum different targets affected during one continuous right-click brace. 0 means unlimited."
            );
            spearChargeDamageCap = spearDouble(
                cfg, "chargeDamageCap", 0.0, 0.0, 1000000.0,
                "速度和基础值算出的单次冲锋伤害，在材料词条与强化继续修改前最多达到多少。0 表示不限制；词条仍可让最终伤害更高。",
                "Maximum charge damage from speed and base value before material traits and modifiers change it. 0 means unlimited; traits may still raise the final damage."
            );
            spearChargeMovementMultiplier = spearDouble(
                cfg, "chargeMovementMultiplier", 1.0, 0.0, 100.0,
                "架矛时相对平常移动速度的倍率。1.0 表示不减速，0.5 表示只保留一半速度。",
                "Movement speed while bracing compared with normal. 1.0 means full speed; 0.5 means half speed."
            );
            spearChargeBrieflyIgnoreSlowTouches = spearBoolean(
                cfg, "chargeBrieflyIgnoreSlowTouches", true,
                "撞到目标但速度不够，没有触发伤害、击退或撞下坐骑时，是否仍暂时忽略这个目标，避免贴住目标时每 tick 重复检查。忽略时间由 chargeSameTargetDelayTicks 决定。",
                "Whether a target touched too slowly to trigger damage, pushback, or being knocked off a mount is briefly ignored, avoiding checks every tick while overlapping it. The delay uses chargeSameTargetDelayTicks."
            );
            spearChargeCanKnockTargetsOffMounts = spearBoolean(
                cfg, "chargeCanKnockTargetsOffMounts", true,
                "移动速度达到要求时，冲锋是否能把骑乘中的目标撞下坐骑。",
                "Whether a charge moving fast enough can knock a mounted target off its mount."
            );

            spearStabAnimationStrength = spearDouble(
                cfg, "stabAnimationStrength", 1.0, 0.0, 10.0,
                "戳刺时矛和手臂的动作幅度。1.0 与高版本原版相同；0 会关闭额外动作。只影响画面，不影响攻击距离或伤害。",
                "Strength of the Spear and arm movement during a stab. 1.0 matches modern vanilla; 0 disables the extra motion. Visual only; reach and damage are unchanged."
            );
            spearBraceAnimationStrength = spearDouble(
                cfg, "braceAnimationStrength", 1.0, 0.0, 10.0,
                "按住右键架矛和收回时的动作幅度。1.0 与高版本原版相同；0 会关闭额外动作。只影响画面。",
                "Strength of the motion while raising and lowering the Spear with right-click. 1.0 matches modern vanilla; 0 disables the extra motion. Visual only."
            );
            spearFatigueAnimationStrength = spearDouble(
                cfg, "fatigueAnimationStrength", 1.0, 0.0, 10.0,
                "架矛过久后矛头下垂和摇晃的明显程度。1.0 与高版本原版相同；0 表示不显示这些疲劳动作。只影响画面。",
                "How strongly the Spear droops and sways after being braced too long. 1.0 matches modern vanilla; 0 hides these fatigue motions. Visual only."
            );
            spearFatigueAnimationSpeed = spearDouble(
                cfg, "fatigueAnimationSpeed", 1.0, 0.0, 10.0,
                "架矛过久后矛头摇晃的速度。1.0 与高版本原版相同，数值越大摇晃越快。只影响画面。",
                "Speed of the Spear sway after it has been braced too long. 1.0 matches modern vanilla; higher values sway faster. Visual only."
            );

            spearLungeMaxLevel = spearInt(
                cfg, "lungeMaxLevel", 3, 1, 100,
                "矛的专属强化“突进”最多能升到多少级。",
                "Maximum level of the Spear-exclusive Lunge modifier."
            );
            spearLungeForwardBoostPerLevel = spearDouble(
                cfg, "lungeForwardBoostPerLevel", 0.458, 0.0, 100.0,
                "每级突进提供的向前冲刺力度。数值越大，冲出的距离通常越远。",
                "Forward boost per Lunge level. Higher values usually travel farther."
            );
            spearLungeExhaustionPerLevel = spearDouble(
                cfg, "lungeExhaustionPerLevel", 4.0, 0.0, 1000.0,
                "每级突进增加的饥饿消耗值。累计约 4 点通常会消耗 1 点饱和度或饥饿值。",
                "Exhaustion added per Lunge level. About 4 exhaustion normally consumes 1 saturation or food point."
            );
            spearLungeDurabilityCost = spearInt(
                cfg, "lungeDurabilityCost", 1, 0, Integer.MAX_VALUE,
                "每次成功发动突进额外消耗的耐久。",
                "Extra durability consumed by each successful Lunge."
            );
            spearLungeMinFoodLevel = spearInt(
                cfg, "lungeMinFoodLevel", 7, 0, 20,
                "非创造模式发动突进所需的最低饥饿值。20 表示全满，2 点约等于 1 个饥饿图标。",
                "Minimum food level required for Lunge outside Creative mode. 20 is full; 2 points equal about one food icon."
            );
            spearLungeMaxHorizontalSpeed = spearDouble(
                cfg, "lungeMaxHorizontalSpeed", 0.0, 0.0, 1000.0,
                "突进后左右和前后移动速度合计最多能达到多少。0 表示不限制。",
                "Maximum combined sideways and forward speed after Lunge. 0 means unlimited."
            );
            spearLungePistonCost = spearInt(
                cfg, "lungePistonCost", 1, 0, 64,
                "每升一级突进需要消耗多少个活塞。",
                "Pistons consumed for each Lunge level."
            );
            spearLungeKnightSlimeIngotCost = spearInt(
                cfg, "lungeKnightSlimeIngotCost", 1, 0, 64,
                "每升一级突进需要消耗多少个骑士史莱姆锭。",
                "Knightslime ingots consumed for each Lunge level."
            );
            spearLungeModifierSlotsPerLevel = spearInt(
                cfg, "lungeModifierSlotsPerLevel", 1, 0, 10,
                "每升一级突进占用多少个强化槽。",
                "Modifier slots consumed for each Lunge level."
            );
            spearLungeDisallowRiding = spearBoolean(
                cfg, "lungeDisallowRiding", true,
                "设为 true 时，骑着坐骑不能发动突进。",
                "When true, Lunge cannot activate while mounted."
            );
            spearLungeDisallowWater = spearBoolean(
                cfg, "lungeDisallowWater", true,
                "设为 true 时，身处水中不能发动突进。",
                "When true, Lunge cannot activate while in water."
            );
            spearLungeDisallowElytraFlight = spearBoolean(
                cfg, "lungeDisallowElytraFlight", true,
                "设为 true 时，使用鞘翅飞行不能发动突进。",
                "When true, Lunge cannot activate while flying with an Elytra."
            );

            enableConstructArmorExtraModifierSlots = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableConstructArmorExtraModifierSlots",
                true,
                desc(
                    "是否让匠魂盔甲自带的基础四件盔甲获得额外初始强化槽。\n该效果会写入隐藏标记，已有盔甲在玩家物品栏中被检测到时会按标记补发缺失的额外槽。",
                    "Whether to grant extra starting modifier slots to Construct's Armory's four base armor pieces.\nThis writes a hidden marker; existing armor found in player inventories will receive missing extra slots based on that marker."
                )
            ).getBoolean();

            constructArmorExtraModifierSlots = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "constructArmorExtraModifierSlots",
                1,
                desc(
                    "匠魂盔甲自带基础四件盔甲获得的额外初始强化槽数量。\n默认值：1",
                    "Number of extra starting modifier slots granted to Construct's Armory's four base armor pieces.\nDefault: 1"
                ),
                0, 10
            ).getInt();

            constructArmorSetMiningSpeedPerPiece = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "constructArmorSetMiningSpeedPerPiece",
                0.15,
                desc(
                    "匠魂盔甲自带基础四件盔甲套装奖励：每件提供的挖掘速度提升比例。0.15 = 每件 15%。\n默认值：0.15",
                    "Construct's Armory base armor set bonus: mining speed bonus per worn piece. 0.15 = 15% per piece.\nDefault: 0.15"
                ),
                0.0, 10.0
            ).getDouble();

            constructArmorSetDefenseDamageRecoveryDelayTicks = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "constructArmorSetDefenseDamageRecoveryDelayTicks",
                20,
                desc(
                    "穿戴至少 1 件匠魂盔甲基础盔甲时，防御损伤开始恢复前的固定等待时间。20 tick = 1 秒。\n默认值：20",
                    "Fixed recovery delay before Defense Damage starts recovering while wearing at least 1 base Construct's Armory armor piece. 20 ticks = 1 second.\nDefault: 20"
                ),
                0, 1200
            ).getInt();

            constructArmorSetDefenseDamageRecoveryIntervalTicks = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "constructArmorSetDefenseDamageRecoveryIntervalTicks",
                20,
                desc(
                    "穿戴至少 2 件匠魂盔甲基础盔甲时，防御损伤恢复间隔。20 tick = 1 秒。\n默认值：20",
                    "Defense Damage recovery interval while wearing at least 2 base Construct's Armory armor pieces. 20 ticks = 1 second.\nDefault: 20"
                ),
                1, 1200
            ).getInt();

            constructArmorSetDefenseDamageRecoveryPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "constructArmorSetDefenseDamageRecoveryPercent",
                0.20,
                desc(
                    "穿戴至少 2 件匠魂盔甲基础盔甲时，每次恢复移除的防御损伤比例。0.20 = 20%。\n默认值：0.20",
                    "Defense Damage removed on each recovery tick while wearing at least 2 base Construct's Armory armor pieces. 0.20 = 20%.\nDefault: 0.20"
                ),
                0.0, 1.0
            ).getDouble();

            constructArmorSetDefenseDamageGainMultiplier = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "constructArmorSetDefenseDamageGainMultiplier",
                0.5,
                desc(
                    "穿戴至少 3 件匠魂盔甲基础盔甲时，敌人造成的防御损伤倍率。0.5 = 防御损伤减半。\n默认值：0.5",
                    "Defense Damage gain multiplier from enemies while wearing at least 3 base Construct's Armory armor pieces. 0.5 = half Defense Damage.\nDefault: 0.5"
                ),
                0.0, 10.0
            ).getDouble();

            constructArmorSetMaxDefenseDamage = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "constructArmorSetMaxDefenseDamage",
                0.5,
                desc(
                    "穿戴 4 件匠魂盔甲基础盔甲时，防御损伤最高累计值。0.5 = 最多损失 50% 防御效果。\n默认值：0.5",
                    "Maximum accumulated Defense Damage while wearing 4 base Construct's Armory armor pieces. 0.5 = lose at most 50% defense efficiency.\nDefault: 0.5"
                ),
                0.0, 1.0
            ).getDouble();

            parryThresholdPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "parryThresholdPercent",
                50.0,
                desc(
                    "迅捷盾格挡值上限的百分比系数。\n格挡值上限 = 大板材料攻击力 x 坚韧手柄耐久系数 x (该值 / 100)。\n默认值：50.0",
                    "Max Parry Percentage Multiplier.\nMax Parry = Plate Material Attack x Tough Handle Durability Multiplier x (Value / 100).\nDefault Value: 50.0"
                ),
                0.0, 1000.0
            ).getDouble();

            cooldownCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "cooldownCoefficient",
                1.0,
                desc(
                    "迅捷盾破防冷却时长系数。数值越大，冷却越长。\n冷却时长（tick）= ceil((5 / 坚韧手柄耐久系数) x 该系数 x 20)，最少 30 tick。\n默认值：1.0",
                    "Shield Break Cooldown Multiplier. Larger values result in longer cooldowns.\nCooldown (ticks) = ceil((5 / Tough Handle Durability Multiplier) x This Multiplier x 20), minimum 30 ticks.\nDefault: 1.0"
                ),
                0.0, 1000.0
            ).getDouble();

            perfectParryWindowTicks = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "perfectParryWindowTicks",
                30,
                desc(
                    "精准格挡判定持续时间（tick）。举盾动作发生后，在此时间内受到攻击会被视为精准格挡。\n20 tick = 1 秒。默认值：30（1.5 秒）",
                    "Perfect Guard Duration (Ticks). Attacks received within this window after raising the shield are considered a Perfect Guard.\n20 ticks = 1 second. Default: 30 (1.5s)"
                ),
                1, 200
            ).getInt();

            imbalanceDurationMultiplier = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceDurationMultiplier",
                0.75,
                desc(
                    "失衡持续时间系数（秒/伤害点）。\n失衡时长（秒）= 造成伤害 x 该值。\n默认值：0.75",
                    "Stagger Duration Coefficient (sec/dmg).\nStagger Duration (sec) = Damage Dealt x Value.\nDefault: 0.75"
                )
            ).getDouble();

            imbalanceSpeedReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceSpeedReduction",
                1.0,
                desc(
                    "失衡效果：非 Boss 单位的移速降低比例。1.0 = 100%，对 Boss 无效。\n默认值：1.0",
                    "Stagger Effect: Movement speed reduction for non-boss entities (1.0 = 100%, no effect on Bosses).\nDefault: 1.0"
                )
            ).getDouble();

            imbalanceDamageReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceDamageReduction",
                1.0,
                desc(
                    "失衡效果：非 Boss 单位的伤害输出降低比例。1.0 = 100%。\n默认值：1.0",
                    "Stagger Effect: Damage output reduction for non-boss entities (1.0 = 100%).\nDefault: 1.0"
                )
            ).getDouble();

            imbalanceBossDamageReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceBossDamageReduction",
                0.5,
                desc(
                    "失衡效果：Boss 单位的伤害输出降低比例。0.5 = 50%。\n默认值：0.5",
                    "Stagger Effect: Damage output reduction for Bosses (0.5 = 50%).\nDefault: 0.5"
                )
            ).getDouble();

            imbalanceKnockbackReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceKnockbackReduction",
                1.0,
                desc(
                    "失衡效果：非 Boss 单位的击退强度降低比例。1.0 = 100%。\n默认值：1.0",
                    "Stagger Effect: Knockback reduction for non-boss entities (1.0 = 100%).\nDefault: 1.0"
                )
            ).getDouble();

            imbalanceBossKnockbackReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceBossKnockbackReduction",
                0.5,
                desc(
                    "失衡效果：Boss 单位的击退强度降低比例。0.5 = 50%。\n默认值：0.5",
                    "Stagger Effect: Knockback reduction for Bosses (0.5 = 50%).\nDefault: 0.5"
                )
            ).getDouble();

            imbalanceDamageTakenIncrease = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceDamageTakenIncrease",
                0.5,
                desc(
                    "失衡效果：目标额外承受伤害的比例。0.5 = 50%。\n默认值：0.5",
                    "Stagger Effect: Damage taken increase (0.5 = 50%).\nDefault: 0.5"
                )
            ).getDouble();

            enableImbalanceStatusOutline = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableImbalanceStatusOutline",
                true,
                desc(
                    "是否为失衡和失衡免疫显示不同颜色的实体轮廓。关闭后不会显示 TT2 的状态轮廓。",
                    "Whether to show differently colored entity outlines for Imbalance and Imbalance Immunity. Disabling this removes TT2's status outlines."
                )
            ).getBoolean();

            imbalanceOutlineColor = parseHexColor(cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceOutlineColor",
                "E34A32",
                desc(
                    "失衡状态的轮廓颜色。填写 6 位十六进制 RGB 颜色，不要包含 #。\n默认值：E34A32",
                    "Outline color for Imbalance. Enter a 6-digit hexadecimal RGB color without #.\nDefault: E34A32"
                )
            ).getString(), 0xE34A32);

            imbalanceImmunityOutlineColor = parseHexColor(cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceImmunityOutlineColor",
                "55D9E8",
                desc(
                    "失衡免疫状态的轮廓颜色。填写 6 位十六进制 RGB 颜色，不要包含 #。\n默认值：55D9E8",
                    "Outline color for Imbalance Immunity. Enter a 6-digit hexadecimal RGB color without #.\nDefault: 55D9E8"
                )
            ).getString(), 0x55D9E8);

            imbalanceOutlineThroughWalls = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceOutlineThroughWalls",
                true,
                desc(
                    "状态轮廓是否可以隔着方块看见。开启时与原版发光效果一致。",
                    "Whether status outlines can be seen through blocks. Enabled matches vanilla glowing."
                )
            ).getBoolean();

            nunchakuAttackSpeed = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "nunchakuAttackSpeed",
                2.5,
                desc(
                    "双节棍攻击速度。原版剑为 1.6，数值越高越快。\n范围：0.1 ~ 4.0",
                    "Nunchaku Attack Speed. Original sword = 1.6, higher value means faster.\nRange: 0.1 ~ 4.0"
                ),
                0.1, 4.0
            ).getDouble();

            nunchakuComboGainPerHit = (float) cfg.get(
                Configuration.CATEGORY_GENERAL,
                "nunchakuComboGainPerHit",
                0.1,
                desc(
                    "双节棍每次命中获得的连击伤害加成。0.1 = 10%。\n范围：0.01 ~ 1.0",
                    "Combo damage bonus per nunchaku hit. 0.1 = 10%.\nRange: 0.01 ~ 1.0"
                ),
                0.01, 1.0
            ).getDouble();

            nunchakuComboCapBindingMultiplier = (float) cfg.get(
                Configuration.CATEGORY_GENERAL,
                "nunchakuComboCapBindingMultiplier",
                0.45,
                desc(
                    "连击上限与绑定结系数的乘数。\n范围：0.1 ~ 10.0",
                    "Multiplier for combo limit and binding knot modifier.\nRange: 0.1 ~ 10.0"
                ),
                0.1, 10.0
            ).getDouble();

            nunchakuComboDecayDelay = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "nunchakuComboDecayDelay",
                60,
                desc(
                    "停止命中后，连击效果保留的 tick 数。\n20 tick = 1 秒。默认值：60",
                    "Ticks that combo effect remains after stopping hits.\n20 ticks = 1 second. Default: 60"
                ),
                1, 600
            ).getInt();

            doppelhanderBaseBlockReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "doppelhanderBaseBlockReduction",
                0.6,
                desc(
                    "德式双手剑基础格挡减伤比例。0.6 = 60%。",
                    "Base damage reduction when blocking with the Doppelhander. 0.6 = 60%."
                ),
                0.0, 1.0
            ).getDouble();

            doppelhanderBlockGainPerHandleModifier = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "doppelhanderBlockGainPerHandleModifier",
                5.0,
                desc(
                    "德式双手剑每次成功格挡后额外提升的格挡比例系数。\n实际提升 = 手柄系数 x 该值。",
                    "Extra block percentage gained on each successful block scales with handle modifier times this value."
                ),
                0.0, 100.0
            ).getDouble();

            doppelhanderDefensiveStanceDurationSeconds = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "doppelhanderDefensiveStanceDurationSeconds",
                20,
                desc(
                    "德式双手剑进入防御姿态后的持续时间（秒）。",
                    "Duration of the Doppelhander's Defensive Stance in seconds."
                ),
                1, 600
            ).getInt();

            doppelhanderBlockHealPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "doppelhanderBlockHealPercent",
                0.05,
                desc(
                    "防御姿态期间，每次成功格挡回复的最大生命值百分比。0.05 = 5%。",
                    "Percentage of max health restored on each successful block during Defensive Stance. 0.05 = 5%."
                ),
                0.0, 1.0
            ).getDouble();

            doppelhanderDamageBonusArmorCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "doppelhanderDamageBonusArmorCoefficient",
                1.5,
                desc(
                    "防御姿态期间，攻击力提升公式中每点护甲对应的伤害加成系数。",
                    "Damage bonus coefficient per point of provided armor during Defensive Stance."
                ),
                0.0, 100.0
            ).getDouble();

            doppelhanderDamageBonusMinPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "doppelhanderDamageBonusMinPercent",
                20.0,
                desc(
                    "防御姿态期间，至少获得的攻击力提升百分比。20 = 20%。",
                    "Minimum damage bonus percentage for the Doppelhander during Defensive Stance. 20 = 20%."
                ),
                0.0, 1000.0
            ).getDouble();

            doppelhanderAoeRadius = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "doppelhanderAoeRadius",
                4.5,
                desc(
                    "防御姿态期间，范围攻击的半径。",
                    "Radius of the Doppelhander's area attack during Defensive Stance."
                ),
                0.5, 16.0
            ).getDouble();

            scoutRangedDamageCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "scoutRangedDamageCoefficient",
                2.0,
                desc(
                    "斥候套装对远程伤害加成的系数。\n实际加成百分比 = 各部位护甲板材料在该部位的护甲韧性平均值 x 该系数。",
                    "Coefficient for Scout Armor ranged damage bonus.\nActual bonus percent = average plate toughness for each piece x this coefficient."
                ),
                0.0, 100.0
            ).getDouble();

            scoutDodgeChanceCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "scoutDodgeChanceCoefficient",
                1.0,
                desc(
                    "斥候套装闪避概率系数。\n每个部位的闪避贡献 = 该部位护甲值对应的减伤百分比 x 该系数。",
                    "Coefficient for Scout Armor dodge chance.\nEach piece contributes armor-reduction-percent x this coefficient."
                ),
                0.0, 100.0
            ).getDouble();

            scoutDodgeDamageFactor = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "scoutDodgeDamageFactor",
                0.2,
                desc(
                    "斥候套装成功闪避时仍然承受的伤害比例。0.2 = 承受原伤害的 20%。",
                    "Damage factor taken on a successful Scout Armor dodge. 0.2 = take 20% of the original damage."
                ),
                0.0, 1.0
            ).getDouble();

            scoutDodgeChanceCap = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "scoutDodgeChanceCap",
                0.8,
                desc(
                    "斥候套装总闪避概率上限。0.8 = 80%。该上限会按部位比例分摊到各件。",
                    "Maximum total dodge chance for Scout Armor. 0.8 = 80%. This cap is distributed across armor slots by part ratio."
                ),
                0.0, 1.0
            ).getDouble();

            scoutFallDamageReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "scoutFallDamageReduction",
                0.8,
                desc(
                    "斥候套装对摔落伤害的总减伤比例。0.8 = 80%。该值会按部位比例分摊到各件。",
                    "Total fall damage reduction granted by Scout Armor. 0.8 = 80%. This value is distributed across pieces by part ratio."
                ),
                0.0, 1.0
            ).getDouble();

            scoutEnvironmentalDamageReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "scoutEnvironmentalDamageReduction",
                0.5,
                desc(
                    "斥候套装对环境伤害（如熔岩、火焰、仙人掌等）的总减伤比例。0.5 = 50%。该值会按部位比例分摊到各件。",
                    "Total environmental damage reduction granted by Scout Armor for hazards such as lava, fire, and cactus. 0.5 = 50%. This value is distributed across pieces by part ratio."
                ),
                0.0, 1.0
            ).getDouble();

            maracaAttackMelodyAllyBonus = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "maracaAttackMelodyAllyBonus",
                0.30,
                desc(
                    "沙锤“攻击力提升”旋律给予友方的攻击伤害提升比例。0.30 = 30%。",
                    "Attack damage bonus granted to allies by the Maraca Attack Melody. 0.30 = 30%."
                ),
                0.0, 10.0
            ).getDouble();

            maracaAttackMelodySelfBonus = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "maracaAttackMelodySelfBonus",
                0.45,
                desc(
                    "沙锤“攻击力提升”旋律给予演奏者自身的攻击伤害提升比例。0.45 = 45%。",
                    "Attack damage bonus granted to the performer by the Maraca Attack Melody. 0.45 = 45%."
                ),
                0.0, 10.0
            ).getDouble();

            maracaAttackMelodyDurationTicks = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "maracaAttackMelodyDurationTicks",
                1200,
                desc(
                    "沙锤“攻击力提升”旋律的基础持续时间。20 tick = 1 秒，默认 1200 tick = 60 秒。",
                    "Base duration of the Maraca Attack Melody. 20 ticks = 1 second. Default: 1200 ticks = 60 seconds."
                ),
                1, 72000
            ).getInt();

            maracaPartyDurationCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "maracaPartyDurationCoefficient",
                1.0,
                desc(
                    "沙锤“狂欢派对！”持续时间系数。实际持续时间 = 被清空的其他旋律剩余时间总和 x 该系数。",
                    "Duration coefficient for the Maraca Party melody. Actual duration = total remaining duration of removed melodies x this value."
                ),
                0.0, 10.0
            ).getDouble();

            craftsmanStaffMovementSpeedCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "craftsmanStaffMovementSpeedCoefficient",
                0.10,
                desc(
                    "工匠手杖移动速度加成系数。实际加成 = 两个手柄耐久系数的平均值 x 此值。0.10 = 10%。",
                    "Tinker's Staff movement speed coefficient. Actual bonus = average handle modifier x this value. 0.10 = 10% per modifier point."
                ),
                0.0, 10.0
            ).getDouble();

            craftsmanStaffCombatDamageCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "craftsmanStaffCombatDamageCoefficient",
                1.0,
                desc(
                    "工匠手杖仅安装战斗模板时的伤害系数。\n默认值：1.0",
                    "Damage coefficient for a Tinker's Staff with Combat but without Felling.\nDefault: 1.0"
                ),
                0.0, 100.0
            ).getDouble();

            craftsmanStaffFellingDamageCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "craftsmanStaffFellingDamageCoefficient",
                1.2,
                desc(
                    "工匠手杖仅安装砍伐模板时的伤害系数。\n默认值：1.2",
                    "Damage coefficient for a Tinker's Staff with Felling but without Combat.\nDefault: 1.2"
                ),
                0.0, 100.0
            ).getDouble();

            craftsmanStaffCombatFellingDamageCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "craftsmanStaffCombatFellingDamageCoefficient",
                1.35,
                desc(
                    "工匠手杖同时安装战斗与砍伐模板时的伤害系数。\n默认值：1.35",
                    "Damage coefficient for a Tinker's Staff with both Combat and Felling.\nDefault: 1.35"
                ),
                0.0, 100.0
            ).getDouble();

            craftsmanStaffInsightSpellDamageCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "craftsmanStaffInsightSpellDamageCoefficient",
                1.0,
                desc(
                    "洞察工匠手杖施法时，将手杖当前面板伤害加入法术基础伤害的系数。新的法术基础伤害 = 法术原基础伤害 + 手杖当前面板伤害 x 此值。\n默认值：1.0",
                    "Coefficient for adding the current displayed attack damage of an Insight Tinker's Staff to spell base damage. New spell base damage = original spell base damage + current displayed staff damage x this value.\nDefault: 1.0"
                ),
                0.0, 100.0
            ).getDouble();

            enableDefenseDamage = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableDefenseDamage",
                true,
                desc(
                    "是否启用防御损伤系统。受到非玩家生物伤害会逐渐降低护甲、护甲韧性与部分受击防御词条效果。",
                    "Whether to enable Defense Damage. Damage from non-player living entities gradually weakens armor, toughness, and some defensive on-hit armor traits."
                )
            ).getBoolean();

            defenseDamageBossHitPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "defenseDamageBossHitPercent",
                0.075,
                desc(
                    "Boss 每次命中玩家时增加的防御损伤。0.075 = 7.5%。",
                    "Defense Damage added by each boss hit. 0.075 = 7.5%."
                ),
                0.0, 1.0
            ).getDouble();

            defenseDamageNormalHitPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "defenseDamageNormalHitPercent",
                0.0375,
                desc(
                    "非 Boss 生物每次命中玩家时增加的防御损伤。0.0375 = 3.75%。",
                    "Defense Damage added by each non-boss mob hit. 0.0375 = 3.75%."
                ),
                0.0, 1.0
            ).getDouble();

            defenseDamageMinimumEfficiency = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "defenseDamageMinimumEfficiency",
                0.25,
                desc(
                    "防御损伤最多会把护甲、护甲韧性与相关防御词条效果降低到原本的比例。0.25 = 最低保留 25%。",
                    "Minimum remaining efficiency for armor, toughness, and related defensive traits under Defense Damage. 0.25 = keep at least 25%."
                ),
                0.0, 1.0
            ).getDouble();

            defenseDamageTriggerIntervalTicks = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "defenseDamageTriggerIntervalTicks",
                15,
                desc(
                    "防御损伤对同一名玩家的触发间隔。15 tick = 0.75 秒；间隔结束前，高频攻击不会重复累积新的防御损伤。",
                    "Trigger interval for Defense Damage on the same player. 15 ticks = 0.75 seconds; hits during this interval will not add more Defense Damage."
                ),
                0, 1200
            ).getInt();

            defenseDamageRecoveryDelayTicks = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "defenseDamageRecoveryDelayTicks",
                40,
                desc(
                    "玩家多久没有受到会造成防御损伤的伤害后开始恢复。40 tick = 2 秒。",
                    "How long after the last Defense Damage hit before recovery starts. 40 ticks = 2 seconds."
                ),
                0, 1200
            ).getInt();

            defenseDamageRecoveryIntervalTicks = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "defenseDamageRecoveryIntervalTicks",
                30,
                desc(
                    "防御损伤每隔多久恢复一次。30 tick = 1.5 秒。",
                    "Interval between Defense Damage recovery ticks. 30 ticks = 1.5 seconds."
                ),
                1, 1200
            ).getInt();

            defenseDamageRecoveryPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "defenseDamageRecoveryPercent",
                0.15,
                desc(
                    "每次恢复移除的防御损伤比例。0.15 = 15%。",
                    "Defense Damage removed on each recovery tick. 0.15 = 15%."
                ),
                0.0, 1.0
            ).getDouble();
        } finally {
            if (removedLegacyImbalanceGlowMode || cfg.hasChanged()) cfg.save();
        }
    }

    private static boolean spearBoolean(Configuration cfg, String key, boolean defaultValue,
                                        String chinese, String english) {
        return cfg.get(CATEGORY_SPEAR, key, defaultValue, desc(chinese, english)).getBoolean();
    }

    private static int spearInt(Configuration cfg, String key, int defaultValue, int minValue, int maxValue,
                                String chinese, String english) {
        return cfg.get(CATEGORY_SPEAR, key, defaultValue, desc(chinese, english), minValue, maxValue).getInt();
    }

    private static double spearDouble(Configuration cfg, String key, double defaultValue,
                                      double minValue, double maxValue, String chinese, String english) {
        return cfg.get(CATEGORY_SPEAR, key, defaultValue, desc(chinese, english), minValue, maxValue).getDouble();
    }

    private static String spearString(Configuration cfg, String key, String defaultValue,
                                      String chinese, String english) {
        return cfg.get(CATEGORY_SPEAR, key, defaultValue, desc(chinese, english)).getString();
    }

    private static int parseHexColor(String value, int fallback) {
        if (value == null) return fallback;
        String normalized = value.trim();
        if (normalized.startsWith("#")) normalized = normalized.substring(1);
        if (normalized.startsWith("0x") || normalized.startsWith("0X")) {
            normalized = normalized.substring(2);
        }
        if (normalized.length() != 6) return fallback;
        try {
            return Integer.parseInt(normalized, 16) & 0xFFFFFF;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static String desc(String chinese, String english) {
        return chinese + "\n\n" + english;
    }
}
