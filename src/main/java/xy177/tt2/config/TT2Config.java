package xy177.tt2.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class TT2Config {

    public static boolean enableSwiftShield = true;
    public static boolean enableHeavyShield = true;
    public static boolean enableNunchaku = true;
    public static boolean enableDoppelhander = true;

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
    public static int imbalanceGlowMode = 0;

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

    public static void init(File configFile) {
        Configuration cfg = new Configuration(configFile);
        try {
            cfg.load();

            enableSwiftShield = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableSwiftShield",
                true,
                "是否注册并启用迅捷盾。设为 false 后，该物品不会出现在游戏中，需要重启生效。\n\n" +
                "Whether to register and enable the Swift Shield. Set to false to remove it from the game. Requires restart."
            ).getBoolean();

            enableHeavyShield = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableHeavyShield",
                true,
                "是否注册并启用重装盾。设为 false 后，该物品不会出现在游戏中，需要重启生效。\n\n" +
                "Whether to register and enable the Heavy Shield. Set to false to remove it from the game. Requires restart."
            ).getBoolean();

            enableNunchaku = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableNunchaku",
                true,
                "是否注册并启用双节棍。设为 false 后，该物品不会出现在游戏中，需要重启生效。\n\n" +
                "Whether to register and enable the Nunchaku. Set to false to remove it from the game. Requires restart."
            ).getBoolean();

            enableDoppelhander = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableDoppelhander",
                true,
                "是否注册并启用德式双手剑。设为 false 后，该物品不会出现在游戏中，需要重启生效。\n\n" +
                "Whether to register and enable the Doppelhander. Set to false to remove it from the game. Requires restart."
            ).getBoolean();

            parryThresholdPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "parryThresholdPercent",
                50.0,
                "迅捷盾格挡值上限的百分比系数。\n" +
                "格挡值上限 = 盘材料攻击力 × 大板手柄耐久系数 × (此值 / 100)。\n" +
                "默认值：50.0\n\n" +
                "Max Parry Percentage Multiplier.\n" +
                "Max Parry = Plate Material Attack x Tough Handle Durability Multiplier x (Value / 100).\n" +
                "Default Value: 50.0"
            ).getDouble();

            cooldownCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "cooldownCoefficient",
                1.0,
                "迅捷盾破防冷却时长系数。数值越大，冷却越长。\n" +
                "冷却时长（tick）= ceil((5 / 大板手柄耐久系数) × 此系数 × 20)，最低 30 tick。\n" +
                "默认值：1.0\n\n" +
                "Shield Break Cooldown Multiplier. Larger values result in longer cooldowns.\n" +
                "Cooldown (ticks) = ceil((5 / Tough Handle Durability Multiplier) x This Multiplier x 20), minimum 30 ticks.\n" +
                "Default: 1.0"
            ).getDouble();

            perfectParryWindowTicks = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "perfectParryWindowTicks",
                30,
                "精准格挡判定持续时间（tick）。举盾动作发生后，在此时间内受到攻击会被视为精准格挡。\n" +
                "20 tick = 1 秒。默认值：30（1.5 秒）\n\n" +
                "Perfect Guard Duration (Ticks). Attacks received within this window after raising the shield are considered a Perfect Guard.\n" +
                "20 ticks = 1 second. Default: 30 (1.5s)",
                1, 200
            ).getInt();

            imbalanceDurationMultiplier = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceDurationMultiplier",
                0.75,
                "失衡持续时间系数（秒/伤害点）。\n" +
                "失衡时长（秒）= 造成伤害 × 此值。\n" +
                "默认值：0.75\n\n" +
                "Stagger Duration Coefficient (sec/dmg).\n" +
                "Stagger Duration (sec) = Damage Dealt x Value.\n" +
                "Default: 0.75"
            ).getDouble();

            imbalanceSpeedReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceSpeedReduction",
                1.0,
                "失衡效果：非 Boss 单位的移速降低比例。1.0 = 100%，对 Boss 无效。\n" +
                "默认值：1.0\n\n" +
                "Stagger Effect: Movement speed reduction for non-boss entities (1.0 = 100%, no effect on Bosses).\n" +
                "Default: 1.0"
            ).getDouble();

            imbalanceDamageReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceDamageReduction",
                1.0,
                "失衡效果：非 Boss 单位的伤害输出降低比例。1.0 = 100%。\n" +
                "默认值：1.0\n\n" +
                "Stagger Effect: Damage output reduction for non-boss entities (1.0 = 100%).\n" +
                "Default: 1.0"
            ).getDouble();

            imbalanceBossDamageReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceBossDamageReduction",
                0.5,
                "失衡效果：Boss 单位的伤害输出降低比例。0.5 = 50%。\n" +
                "默认值：0.5\n\n" +
                "Stagger Effect: Damage output reduction for Bosses (0.5 = 50%).\n" +
                "Default: 0.5"
            ).getDouble();

            imbalanceKnockbackReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceKnockbackReduction",
                1.0,
                "失衡效果：非 Boss 单位的击退强度降低比例。1.0 = 100%。\n" +
                "默认值：1.0\n\n" +
                "Stagger Effect: Knockback reduction for non-boss entities (1.0 = 100%).\n" +
                "Default: 1.0"
            ).getDouble();

            imbalanceBossKnockbackReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceBossKnockbackReduction",
                0.5,
                "失衡效果：Boss 单位的击退强度降低比例。0.5 = 50%。\n" +
                "默认值：0.5\n\n" +
                "Stagger Effect: Knockback reduction for Bosses (0.5 = 50%).\n" +
                "Default: 0.5"
            ).getDouble();

            imbalanceDamageTakenIncrease = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceDamageTakenIncrease",
                0.5,
                "失衡效果：目标额外承受伤害的比例。0.5 = 50%。\n" +
                "默认值：0.5\n\n" +
                "Stagger Effect: Damage taken increase (0.5 = 50%).\n" +
                "Default: 0.5"
            ).getDouble();

            imbalanceGlowMode = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceGlowMode",
                0,
                "失衡发光显示模式：\n" +
                "0 = 仅在纯失衡免疫期发光\n" +
                "1 = 仅在失衡期间发光\n" +
                "2 = 失衡与失衡免疫期间都发光\n\n" +
                "Stagger Glow Display Mode:\n" +
                "0 = Glow only during pure Imbalance Immunity period (after Stagger ends)\n" +
                "1 = Glow during Stagger, but not during Imbalance Immunity alone\n" +
                "2 = Glow during both Stagger and Imbalance Immunity periods",
                0, 2
            ).getInt();

            nunchakuAttackSpeed = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "nunchakuAttackSpeed",
                2.5,
                "双节棍攻击速度。原版剑为 1.6，数值越高越快。\n" +
                "范围：0.1 ~ 4.0\n\n" +
                "Nunchaku Attack Speed. Original sword = 1.6, higher value means faster.\n" +
                "Range: 0.1 ~ 4.0",
                0.1, 4.0
            ).getDouble();

            nunchakuComboGainPerHit = (float) cfg.get(
                Configuration.CATEGORY_GENERAL,
                "nunchakuComboGainPerHit",
                0.1,
                "双节棍每次命中获得的连击伤害加成。0.1 = 10%。\n" +
                "范围：0.01 ~ 1.0\n\n" +
                "Combo damage bonus per nunchaku hit. 0.1 = 10%.\n" +
                "Range: 0.01 ~ 1.0",
                0.01, 1.0
            ).getDouble();

            nunchakuComboCapBindingMultiplier = (float) cfg.get(
                Configuration.CATEGORY_GENERAL,
                "nunchakuComboCapBindingMultiplier",
                0.45,
                "连击上限与绑定结系数的乘数。\n" +
                "范围：0.1 ~ 10.0\n\n" +
                "Multiplier for combo limit and binding knot modifier.\n" +
                "Range: 0.1 ~ 10.0",
                0.1, 10.0
            ).getDouble();

            nunchakuComboDecayDelay = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "nunchakuComboDecayDelay",
                60,
                "停止命中后，连击效果保留的 tick 数。\n" +
                "20 tick = 1 秒。默认值：60\n\n" +
                "Ticks that combo effect remains after stopping hits.\n" +
                "20 ticks = 1 second. Default: 60",
                1, 600
            ).getInt();

            doppelhanderBaseBlockReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "doppelhanderBaseBlockReduction",
                0.6,
                "德式双手剑基础格挡减伤比例。0.6 = 60%。\n\n" +
                "Base damage reduction when blocking with the Doppelhander. 0.6 = 60%.",
                0.0, 1.0
            ).getDouble();

            doppelhanderBlockGainPerHandleModifier = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "doppelhanderBlockGainPerHandleModifier",
                5.0,
                "德式双手剑每次成功格挡后额外提升的阻挡比例系数。\n" +
                "实际提升 = 手柄系数 × 此值。\n\n" +
                "Extra block percentage gained on each successful block scales with handle modifier times this value.",
                0.0, 100.0
            ).getDouble();

            doppelhanderDefensiveStanceDurationSeconds = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "doppelhanderDefensiveStanceDurationSeconds",
                20,
                "德式双手剑进入防御姿态后的持续时间（秒）。\n\n" +
                "Duration of the Doppelhander's Defensive Stance in seconds.",
                1, 600
            ).getInt();

            doppelhanderBlockHealPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "doppelhanderBlockHealPercent",
                0.05,
                "防御姿态期间，每次成功格挡回复的生命值百分比。0.05 = 5%。\n\n" +
                "Percentage of max health restored on each successful block during Defensive Stance. 0.05 = 5%.",
                0.0, 1.0
            ).getDouble();

            doppelhanderDamageBonusArmorCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "doppelhanderDamageBonusArmorCoefficient",
                1.5,
                "防御姿态期间，攻击力提升公式中每点护甲对应的伤害加成系数。\n\n" +
                "Damage bonus coefficient per point of provided armor during Defensive Stance.",
                0.0, 100.0
            ).getDouble();

            doppelhanderDamageBonusMinPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "doppelhanderDamageBonusMinPercent",
                20.0,
                "防御姿态期间，最少获得的攻击力提升百分比。20 = 20%。\n\n" +
                "Minimum damage bonus percentage for the Doppelhander during Defensive Stance. 20 = 20%.",
                0.0, 1000.0
            ).getDouble();

            doppelhanderAoeRadius = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "doppelhanderAoeRadius",
                4.5,
                "防御姿态期间，范围攻击的半径。\n\n" +
                "Radius of the Doppelhander's area attack during Defensive Stance.",
                0.5, 16.0
            ).getDouble();

        } finally {
            if (cfg.hasChanged()) cfg.save();
        }
    }
}
