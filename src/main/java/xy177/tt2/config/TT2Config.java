package xy177.tt2.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class TT2Config {

    public static boolean enableSwiftShield = true;
    public static boolean enableHeavyShield = true;
    public static boolean enableNunchaku = true;
    public static boolean enableDoppelhander = true;
    public static boolean enableScoutArmor = true;

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

    public static double scoutRangedDamageCoefficient = 2.0;
    public static double scoutDodgeChanceCoefficient = 1.0;
    public static double scoutDodgeDamageFactor = 0.2;
    public static double scoutDodgeChanceCap = 0.8;
    public static double scoutFallDamageReduction = 0.8;
    public static double scoutEnvironmentalDamageReduction = 0.5;

    public static boolean enableDefenseDamage = true;
    public static double defenseDamageBossHitPercent = 0.075;
    public static double defenseDamageNormalHitPercent = 0.0375;
    public static double defenseDamageMinimumEfficiency = 0.25;
    public static int defenseDamageRecoveryDelayTicks = 40;
    public static int defenseDamageRecoveryIntervalTicks = 30;
    public static double defenseDamageRecoveryPercent = 0.15;

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

            enableScoutArmor = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableScoutArmor",
                true,
                "是否注册并启用斥候套装。设为 false 后，该套护甲不会出现在游戏中，需要重启生效。\n\n" +
                "Whether to register and enable the Scout Armor set. Set to false to remove it from the game. Requires restart."
            ).getBoolean();

            parryThresholdPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "parryThresholdPercent",
                50.0,
                "迅捷盾格挡值上限的百分比系数。\n" +
                "格挡值上限 = 大板材料攻击力 × 坚韧手柄耐久系数 × (此值 / 100)。\n" +
                "默认值：50.0\n\n" +
                "Max Parry Percentage Multiplier.\n" +
                "Max Parry = Plate Material Attack x Tough Handle Durability Multiplier x (Value / 100).\n" +
                "Default Value: 50.0",
                0.0, 1000.0
            ).getDouble();

            cooldownCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "cooldownCoefficient",
                1.0,
                "迅捷盾破防冷却时长系数。数值越大，冷却越长。\n" +
                "冷却时长（tick）= ceil((5 / 坚韧手柄耐久系数) × 此系数 × 20)，最少 30 tick。\n" +
                "默认值：1.0\n\n" +
                "Shield Break Cooldown Multiplier. Larger values result in longer cooldowns.\n" +
                "Cooldown (ticks) = ceil((5 / Tough Handle Durability Multiplier) x This Multiplier x 20), minimum 30 ticks.\n" +
                "Default: 1.0",
                0.0, 1000.0
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
                "德式双手剑每次成功格挡后额外提升的格挡比例系数。\n" +
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
                "防御姿态期间，至少获得的攻击力提升百分比。20 = 20%。\n\n" +
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

            scoutRangedDamageCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "scoutRangedDamageCoefficient",
                2.0,
                "斥候套装对远程伤害加成的系数。\n" +
                "实际加成百分比 = 各部位护甲板材料在该部位的护甲韧性平均值 × 此系数。\n\n" +
                "Coefficient for Scout Armor ranged damage bonus.\n" +
                "Actual bonus percent = average plate toughness for each piece x this coefficient.",
                0.0, 100.0
            ).getDouble();

            scoutDodgeChanceCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "scoutDodgeChanceCoefficient",
                1.0,
                "斥候套装闪避概率系数。\n" +
                "每个部位的闪避贡献 = 该部位护甲值对应的减伤百分比 × 此系数。\n\n" +
                "Coefficient for Scout Armor dodge chance.\n" +
                "Each piece contributes armor-reduction-percent x this coefficient.",
                0.0, 100.0
            ).getDouble();

            scoutDodgeDamageFactor = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "scoutDodgeDamageFactor",
                0.2,
                "斥候套装成功闪避时仍然承受的伤害比例。0.2 = 承受原伤害的 20%。\n\n" +
                "Damage factor taken on a successful Scout Armor dodge. 0.2 = take 20% of the original damage.",
                0.0, 1.0
            ).getDouble();

            scoutDodgeChanceCap = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "scoutDodgeChanceCap",
                0.8,
                "斥候套装总闪避概率上限。0.8 = 80%。该上限会按部位比例分摊到各件。\n\n" +
                "Maximum total dodge chance for Scout Armor. 0.8 = 80%. This cap is distributed across armor slots by part ratio.",
                0.0, 1.0
            ).getDouble();

            scoutFallDamageReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "scoutFallDamageReduction",
                0.8,
                "斥候套装对摔落伤害的总减伤比例。0.8 = 80%。该值会按部位比例分摊到各件。\n\n" +
                "Total fall damage reduction granted by Scout Armor. 0.8 = 80%. This value is distributed across pieces by part ratio.",
                0.0, 1.0
            ).getDouble();

            scoutEnvironmentalDamageReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "scoutEnvironmentalDamageReduction",
                0.5,
                "斥候套装对环境伤害（如熔岩、火焰、仙人掌等）的总减伤比例。0.5 = 50%。该值会按部位比例分摊到各件。\n\n" +
                "Total environmental damage reduction granted by Scout Armor for hazards such as lava, fire, and cactus. 0.5 = 50%. This value is distributed across pieces by part ratio.",
                0.0, 1.0
            ).getDouble();

            enableDefenseDamage = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableDefenseDamage",
                true,
                "是否启用防御损伤系统。受到非玩家生物伤害会逐渐降低护甲、防御韧性与部分受击防御词条效果。\n\n" +
                "Whether to enable Defense Damage. Damage from non-player living entities gradually weakens armor, toughness, and some defensive on-hit armor traits."
            ).getBoolean();

            defenseDamageBossHitPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "defenseDamageBossHitPercent",
                0.075,
                "Boss 每次命中玩家时增加的防御损伤。0.075 = 7.5%。\n\n" +
                "Defense Damage added by each boss hit. 0.075 = 7.5%.",
                0.0, 1.0
            ).getDouble();

            defenseDamageNormalHitPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "defenseDamageNormalHitPercent",
                0.0375,
                "非 Boss 生物每次命中玩家时增加的防御损伤。0.0375 = 3.75%。\n\n" +
                "Defense Damage added by each non-boss mob hit. 0.0375 = 3.75%.",
                0.0, 1.0
            ).getDouble();

            defenseDamageMinimumEfficiency = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "defenseDamageMinimumEfficiency",
                0.25,
                "防御损伤最多会把护甲、防御韧性与相关防御词条效果降低到原本的比例。0.25 = 最低保留 25%。\n\n" +
                "Minimum remaining efficiency for armor, toughness, and related defensive traits under Defense Damage. 0.25 = keep at least 25%.",
                0.0, 1.0
            ).getDouble();

            defenseDamageRecoveryDelayTicks = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "defenseDamageRecoveryDelayTicks",
                40,
                "玩家多久没有受到会造成防御损伤的伤害后开始恢复。20 ticks = 1 秒。\n\n" +
                "How long after the last Defense Damage hit before recovery starts. 20 ticks = 1 second.",
                0, 1200
            ).getInt();

            defenseDamageRecoveryIntervalTicks = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "defenseDamageRecoveryIntervalTicks",
                30,
                "防御损伤每隔多久恢复一次。20 ticks = 1 秒。\n\n" +
                "Interval between Defense Damage recovery ticks. 20 ticks = 1 second.",
                1, 1200
            ).getInt();

            defenseDamageRecoveryPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "defenseDamageRecoveryPercent",
                0.15,
                "每次恢复移除的防御损伤比例。0.15 = 15%。\n\n" +
                "Defense Damage removed on each recovery tick. 0.15 = 15%.",
                0.0, 1.0
            ).getDouble();
        } finally {
            if (cfg.hasChanged()) cfg.save();
        }
    }
}
