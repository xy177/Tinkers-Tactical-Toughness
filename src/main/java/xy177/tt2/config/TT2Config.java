package xy177.tt2.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class TT2Config {



    public static boolean enableSwiftShield = true;

    public static boolean enableHeavyShield = true;

    public static boolean enableNunchaku = true;

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

    public static void init(File configFile) {
        Configuration cfg = new Configuration(configFile);
        try {
            cfg.load();

            enableSwiftShield = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableSwiftShield",
                true,
                "是否注册并启用迅捷盾。设为 false 后该物品不会出现在游戏中，需重启生效。\n" +
                "Whether to register and enable the Swift Shield. Set to false to remove it from the game. Requires restart."
            ).getBoolean();

            enableHeavyShield = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableHeavyShield",
                true,
                "是否注册并启用重装盾。设为 false 后该物品不会出现在游戏中，需重启生效。\n" +
                "Whether to register and enable the Heavy Shield. Set to false to remove it from the game. Requires restart."
            ).getBoolean();

            enableNunchaku = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableNunchaku",
                true,
                "是否注册并启用双节棍。设为 false 后该物品不会出现在游戏中，需重启生效。\n" +
                "Whether to register and enable the Nunchaku. Set to false to remove it from the game. Requires restart."
            ).getBoolean();

            parryThresholdPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "parryThresholdPercent",
                50.0,
                "招架值上限百分比系数。\n" +
                "招架值上限 = 盘材料顶端攻击 x 大板手柄耐久倍率 x (此值 / 100)。\n" +
                "默认值: 50.0\n"+
                "Max Parry Percentage Multiplier.\n" +
                "Max Parry = Plate Material Attack x Tough Handle Durability Multiplier x (Value / 100).\n" +
                "Default Value: 50.0"
            ).getDouble();

            cooldownCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "cooldownCoefficient",
                1.0,
                "破盾冷却时长系数。数值越大冷却越长。\n" +
                "冷却时长(tick) = ceil((5 / 大板手柄耐久系数) x 此系数 x 20)，最低 30 tick（1.5 秒）。\n" +
                "默认值: 1.0\n" +
                "Shield Break Cooldown Multiplier. Larger values result in longer cooldowns.\n" +
                "Cooldown (ticks) = ceil((5 / Tough Handle Durability Multiplier) x This Multiplier x 20), minimum 30 ticks (1.5s).\n" +
                "Default: 1.0"
            ).getDouble();

            perfectParryWindowTicks = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "perfectParryWindowTicks",
                30,
                "严阵以待持续时间（tick）。举盾动作发生后此时间内受到攻击视为精准格挡。\n" +
                "20 tick = 1秒。默认值: 30（即1.5秒）\n"+
                "Perfect Guard Duration (Ticks). Attacks received within this window after raising the shield are considered a Perfect Guard.\n" +
                "20 ticks = 1 second. Default: 30 (1.5s)",
                1, 200
            ).getInt();

            imbalanceDurationMultiplier = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceDurationMultiplier",
                0.75,
                "失衡持续时长系数（秒/伤害点）。\n" +
                "失衡时长(秒) = 所造成伤害 x 此值。默认值: 0.75\n"+
                "Stagger Duration Coefficient (sec/dmg).\n" +
                "Stagger Duration (sec) = Damage Dealt x Value.\n" +
                "Default: 0.75"
            ).getDouble();

            imbalanceSpeedReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceSpeedReduction",
                1.0,
                "失衡效果：非 Boss 移动速度降低（1.0 = 100%，对 Boss 无效）。默认值: 1.0\n" +
                "Stagger Effect: Movement speed reduction for non-boss entities (1.0 = 100%, no effect on Bosses).\n" +
                "Default: 1.0"
            ).getDouble();

            imbalanceDamageReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceDamageReduction",
                1.0,
                "失衡效果：非 Boss 伤害输出降低（1.0 = 100%）。默认值: 1.0\n" +
                "Stagger Effect: Damage output reduction for non-boss entities (1.0 = 100%).\n" +
                "Default: 1.0"
            ).getDouble();

            imbalanceBossDamageReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceBossDamageReduction",
                0.5,
                "失衡效果：Boss 伤害输出降低（0.5 = 50%）。默认值: 0.5\n" +
                "Stagger Effect: Damage output reduction for Bosses (0.5 = 50%).\n" +
                "Default: 0.5"
            ).getDouble();

            imbalanceKnockbackReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceKnockbackReduction",
                1.0,
                "失衡效果：非 Boss 击退降低（1.0 = 100%）。默认值: 1.0\n" +
                "Stagger Effect: Knockback reduction for non-boss entities (1.0 = 100%).\n" +
                "Default: 1.0"
            ).getDouble();

            imbalanceBossKnockbackReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceBossKnockbackReduction",
                0.5,
                "失衡效果：Boss 击退降低（0.5 = 50%）。默认值: 0.5\n" +
                "Stagger Effect: Knockback reduction for Bosses (0.5 = 50%).\n" +
                "Default: 0.5"
            ).getDouble();

            imbalanceDamageTakenIncrease = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceDamageTakenIncrease",
                0.5,
                "失衡效果：受到伤害增加（0.5 = 50%）。默认值: 0.5\n" +
                "Stagger Effect: Damage taken increase (0.5 = 50%).\n" +
                "Default: 0.5"
            ).getDouble();

            imbalanceGlowMode = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceGlowMode",
                0,
                "失衡发光显示模式:\n" +
                "0 = 仅在纯失衡免疫期（失衡结束后）发光\n" +
                "1 = 有[失衡]时发光，仅有[失衡免疫]时不发光\n" +
                "2 = [失衡]和[失衡免疫]期间都发光\n" +
                "Stagger Glow Display Mode:\n" +
                "0 = Glow only during pure Imbalance Immunity period (after Stagger ends)\n" +
                "1 = Glow during [Stagger], but not during [Imbalance Immunity] alone\n" +
                "2 = Glow during both [Stagger] and [Imbalance Immunity] periods",
                0, 2
            ).getInt();

            nunchakuAttackSpeed = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "nunchakuAttackSpeed",
                2.5,
                "双节棍攻击速度。原版剑=1.6，数值越高越快。范围: 0.1~4.0\n"+
                "Nunchaku Attack Speed. Original sword=1.6, higher value means faster. Range: 0.1~4.0",
                0.1, 4.0
            ).getDouble();

            nunchakuComboGainPerHit = (float) cfg.get(
                Configuration.CATEGORY_GENERAL,
                "nunchakuComboGainPerHit",
                0.1,
                "双节棍每次命中获得的连击伤害加成（0.1 = 10%）。范围: 0.01~1.0\n"+
                "Combo damage bonus per nunchaku hit (0.1 = 10%). Range: 0.01~1.0",
                0.01, 1.0
            ).getDouble();

            nunchakuComboCapBindingMultiplier = (float) cfg.get(
                Configuration.CATEGORY_GENERAL,
                "nunchakuComboCapBindingMultiplier",
                0.45,
                "连击上限与绑定结 modifier 的乘数。范围: 0.1~10.0\n"+
                "Multiplier for combo limit and binding knot modifier. Range: 0.1~10.0",
                0.1, 10.0
            ).getDouble();

            nunchakuComboDecayDelay = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "nunchakuComboDecayDelay",
                60,
                "停止命中后连击效果保持的 tick 数（20 tick = 1 秒）。默认: 60\n"+
                "Ticks that combo effect remains after stopping hits (20 ticks = 1 second). Default: 60",
                1, 600
            ).getInt();

        } finally {
            if (cfg.hasChanged()) cfg.save();
        }
    }
}
