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

    public static double maracaAttackMelodyAllyBonus = 0.30;
    public static double maracaAttackMelodySelfBonus = 0.45;
    public static int maracaAttackMelodyDurationTicks = 1200;
    public static double maracaPartyDurationCoefficient = 1.0;

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
        try {
            cfg.load();

            enableSwiftShield = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableSwiftShield",
                true,
                "鏄惁娉ㄥ唽骞跺惎鐢ㄨ繀鎹风浘銆傝涓?false 鍚庯紝璇ョ墿鍝佷笉浼氬嚭鐜板湪娓告垙涓紝闇€瑕侀噸鍚敓鏁堛€俓n\n" +
                "Whether to register and enable the Swift Shield. Set to false to remove it from the game. Requires restart."
            ).getBoolean();

            enableHeavyShield = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableHeavyShield",
                true,
                "鏄惁娉ㄥ唽骞跺惎鐢ㄩ噸瑁呯浘銆傝涓?false 鍚庯紝璇ョ墿鍝佷笉浼氬嚭鐜板湪娓告垙涓紝闇€瑕侀噸鍚敓鏁堛€俓n\n" +
                "Whether to register and enable the Heavy Shield. Set to false to remove it from the game. Requires restart."
            ).getBoolean();

            enableNunchaku = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableNunchaku",
                true,
                "鏄惁娉ㄥ唽骞跺惎鐢ㄥ弻鑺傛銆傝涓?false 鍚庯紝璇ョ墿鍝佷笉浼氬嚭鐜板湪娓告垙涓紝闇€瑕侀噸鍚敓鏁堛€俓n\n" +
                "Whether to register and enable the Nunchaku. Set to false to remove it from the game. Requires restart."
            ).getBoolean();

            enableDoppelhander = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableDoppelhander",
                true,
                "鏄惁娉ㄥ唽骞跺惎鐢ㄥ痉寮忓弻鎵嬪墤銆傝涓?false 鍚庯紝璇ョ墿鍝佷笉浼氬嚭鐜板湪娓告垙涓紝闇€瑕侀噸鍚敓鏁堛€俓n\n" +
                "Whether to register and enable the Doppelhander. Set to false to remove it from the game. Requires restart."
            ).getBoolean();

            enableScoutArmor = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableScoutArmor",
                true,
                "鏄惁娉ㄥ唽骞跺惎鐢ㄦ枼鍊欏瑁呫€傝涓?false 鍚庯紝璇ュ鎶ょ敳涓嶄細鍑虹幇鍦ㄦ父鎴忎腑锛岄渶瑕侀噸鍚敓鏁堛€俓n\n" +
                "Whether to register and enable the Scout Armor set. Set to false to remove it from the game. Requires restart."
            ).getBoolean();

            parryThresholdPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "parryThresholdPercent",
                50.0,
                "杩呮嵎鐩炬牸鎸″€间笂闄愮殑鐧惧垎姣旂郴鏁般€俓n" +
                "鏍兼尅鍊间笂闄?= 澶ф澘鏉愭枡鏀诲嚮鍔?脳 鍧氶煣鎵嬫焺鑰愪箙绯绘暟 脳 (姝ゅ€?/ 100)銆俓n" +
                "榛樿鍊硷細50.0\n\n" +
                "Max Parry Percentage Multiplier.\n" +
                "Max Parry = Plate Material Attack x Tough Handle Durability Multiplier x (Value / 100).\n" +
                "Default Value: 50.0",
                0.0, 1000.0
            ).getDouble();

            cooldownCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "cooldownCoefficient",
                1.0,
                "杩呮嵎鐩剧牬闃插喎鍗存椂闀跨郴鏁般€傛暟鍊艰秺澶э紝鍐峰嵈瓒婇暱銆俓n" +
                "鍐峰嵈鏃堕暱锛坱ick锛? ceil((5 / 鍧氶煣鎵嬫焺鑰愪箙绯绘暟) 脳 姝ょ郴鏁?脳 20)锛屾渶灏?30 tick銆俓n" +
                "榛樿鍊硷細1.0\n\n" +
                "Shield Break Cooldown Multiplier. Larger values result in longer cooldowns.\n" +
                "Cooldown (ticks) = ceil((5 / Tough Handle Durability Multiplier) x This Multiplier x 20), minimum 30 ticks.\n" +
                "Default: 1.0",
                0.0, 1000.0
            ).getDouble();

            perfectParryWindowTicks = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "perfectParryWindowTicks",
                30,
                "绮惧噯鏍兼尅鍒ゅ畾鎸佺画鏃堕棿锛坱ick锛夈€備妇鐩惧姩浣滃彂鐢熷悗锛屽湪姝ゆ椂闂村唴鍙楀埌鏀诲嚮浼氳瑙嗕负绮惧噯鏍兼尅銆俓n" +
                "20 tick = 1 绉掋€傞粯璁ゅ€硷細30锛?.5 绉掞級\n\n" +
                "Perfect Guard Duration (Ticks). Attacks received within this window after raising the shield are considered a Perfect Guard.\n" +
                "20 ticks = 1 second. Default: 30 (1.5s)",
                1, 200
            ).getInt();

            imbalanceDurationMultiplier = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceDurationMultiplier",
                0.75,
                "澶辫　鎸佺画鏃堕棿绯绘暟锛堢/浼ゅ鐐癸級銆俓n" +
                "澶辫　鏃堕暱锛堢锛? 閫犳垚浼ゅ 脳 姝ゅ€笺€俓n" +
                "榛樿鍊硷細0.75\n\n" +
                "Stagger Duration Coefficient (sec/dmg).\n" +
                "Stagger Duration (sec) = Damage Dealt x Value.\n" +
                "Default: 0.75"
            ).getDouble();

            imbalanceSpeedReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceSpeedReduction",
                1.0,
                "澶辫　鏁堟灉锛氶潪 Boss 鍗曚綅鐨勭Щ閫熼檷浣庢瘮渚嬨€?.0 = 100%锛屽 Boss 鏃犳晥銆俓n" +
                "榛樿鍊硷細1.0\n\n" +
                "Stagger Effect: Movement speed reduction for non-boss entities (1.0 = 100%, no effect on Bosses).\n" +
                "Default: 1.0"
            ).getDouble();

            imbalanceDamageReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceDamageReduction",
                1.0,
                "澶辫　鏁堟灉锛氶潪 Boss 鍗曚綅鐨勪激瀹宠緭鍑洪檷浣庢瘮渚嬨€?.0 = 100%銆俓n" +
                "榛樿鍊硷細1.0\n\n" +
                "Stagger Effect: Damage output reduction for non-boss entities (1.0 = 100%).\n" +
                "Default: 1.0"
            ).getDouble();

            imbalanceBossDamageReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceBossDamageReduction",
                0.5,
                "澶辫　鏁堟灉锛欱oss 鍗曚綅鐨勪激瀹宠緭鍑洪檷浣庢瘮渚嬨€?.5 = 50%銆俓n" +
                "榛樿鍊硷細0.5\n\n" +
                "Stagger Effect: Damage output reduction for Bosses (0.5 = 50%).\n" +
                "Default: 0.5"
            ).getDouble();

            imbalanceKnockbackReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceKnockbackReduction",
                1.0,
                "澶辫　鏁堟灉锛氶潪 Boss 鍗曚綅鐨勫嚮閫€寮哄害闄嶄綆姣斾緥銆?.0 = 100%銆俓n" +
                "榛樿鍊硷細1.0\n\n" +
                "Stagger Effect: Knockback reduction for non-boss entities (1.0 = 100%).\n" +
                "Default: 1.0"
            ).getDouble();

            imbalanceBossKnockbackReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceBossKnockbackReduction",
                0.5,
                "澶辫　鏁堟灉锛欱oss 鍗曚綅鐨勫嚮閫€寮哄害闄嶄綆姣斾緥銆?.5 = 50%銆俓n" +
                "榛樿鍊硷細0.5\n\n" +
                "Stagger Effect: Knockback reduction for Bosses (0.5 = 50%).\n" +
                "Default: 0.5"
            ).getDouble();

            imbalanceDamageTakenIncrease = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceDamageTakenIncrease",
                0.5,
                "澶辫　鏁堟灉锛氱洰鏍囬澶栨壙鍙椾激瀹崇殑姣斾緥銆?.5 = 50%銆俓n" +
                "榛樿鍊硷細0.5\n\n" +
                "Stagger Effect: Damage taken increase (0.5 = 50%).\n" +
                "Default: 0.5"
            ).getDouble();

            imbalanceGlowMode = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "imbalanceGlowMode",
                0,
                "澶辫　鍙戝厜鏄剧ず妯″紡锛歕n" +
                "0 = 浠呭湪绾け琛″厤鐤湡鍙戝厜\n" +
                "1 = 浠呭湪澶辫　鏈熼棿鍙戝厜\n" +
                "2 = 澶辫　涓庡け琛″厤鐤湡闂撮兘鍙戝厜\n\n" +
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
                "鍙岃妭妫嶆敾鍑婚€熷害銆傚師鐗堝墤涓?1.6锛屾暟鍊艰秺楂樿秺蹇€俓n" +
                "鑼冨洿锛?.1 ~ 4.0\n\n" +
                "Nunchaku Attack Speed. Original sword = 1.6, higher value means faster.\n" +
                "Range: 0.1 ~ 4.0",
                0.1, 4.0
            ).getDouble();

            nunchakuComboGainPerHit = (float) cfg.get(
                Configuration.CATEGORY_GENERAL,
                "nunchakuComboGainPerHit",
                0.1,
                "鍙岃妭妫嶆瘡娆″懡涓幏寰楃殑杩炲嚮浼ゅ鍔犳垚銆?.1 = 10%銆俓n" +
                "鑼冨洿锛?.01 ~ 1.0\n\n" +
                "Combo damage bonus per nunchaku hit. 0.1 = 10%.\n" +
                "Range: 0.01 ~ 1.0",
                0.01, 1.0
            ).getDouble();

            nunchakuComboCapBindingMultiplier = (float) cfg.get(
                Configuration.CATEGORY_GENERAL,
                "nunchakuComboCapBindingMultiplier",
                0.45,
                "杩炲嚮涓婇檺涓庣粦瀹氱粨绯绘暟鐨勪箻鏁般€俓n" +
                "鑼冨洿锛?.1 ~ 10.0\n\n" +
                "Multiplier for combo limit and binding knot modifier.\n" +
                "Range: 0.1 ~ 10.0",
                0.1, 10.0
            ).getDouble();

            nunchakuComboDecayDelay = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "nunchakuComboDecayDelay",
                60,
                "鍋滄鍛戒腑鍚庯紝杩炲嚮鏁堟灉淇濈暀鐨?tick 鏁般€俓n" +
                "20 tick = 1 绉掋€傞粯璁ゅ€硷細60\n\n" +
                "Ticks that combo effect remains after stopping hits.\n" +
                "20 ticks = 1 second. Default: 60",
                1, 600
            ).getInt();

            doppelhanderBaseBlockReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "doppelhanderBaseBlockReduction",
                0.6,
                "寰峰紡鍙屾墜鍓戝熀纭€鏍兼尅鍑忎激姣斾緥銆?.6 = 60%銆俓n\n" +
                "Base damage reduction when blocking with the Doppelhander. 0.6 = 60%.",
                0.0, 1.0
            ).getDouble();

            doppelhanderBlockGainPerHandleModifier = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "doppelhanderBlockGainPerHandleModifier",
                5.0,
                "寰峰紡鍙屾墜鍓戞瘡娆℃垚鍔熸牸鎸″悗棰濆鎻愬崌鐨勬牸鎸℃瘮渚嬬郴鏁般€俓n" +
                "瀹為檯鎻愬崌 = 鎵嬫焺绯绘暟 脳 姝ゅ€笺€俓n\n" +
                "Extra block percentage gained on each successful block scales with handle modifier times this value.",
                0.0, 100.0
            ).getDouble();

            doppelhanderDefensiveStanceDurationSeconds = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "doppelhanderDefensiveStanceDurationSeconds",
                20,
                "寰峰紡鍙屾墜鍓戣繘鍏ラ槻寰″Э鎬佸悗鐨勬寔缁椂闂达紙绉掞級銆俓n\n" +
                "Duration of the Doppelhander's Defensive Stance in seconds.",
                1, 600
            ).getInt();

            doppelhanderBlockHealPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "doppelhanderBlockHealPercent",
                0.05,
                "闃插尽濮挎€佹湡闂达紝姣忔鎴愬姛鏍兼尅鍥炲鐨勭敓鍛藉€肩櫨鍒嗘瘮銆?.05 = 5%銆俓n\n" +
                "Percentage of max health restored on each successful block during Defensive Stance. 0.05 = 5%.",
                0.0, 1.0
            ).getDouble();

            doppelhanderDamageBonusArmorCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "doppelhanderDamageBonusArmorCoefficient",
                1.5,
                "闃插尽濮挎€佹湡闂达紝鏀诲嚮鍔涙彁鍗囧叕寮忎腑姣忕偣鎶ょ敳瀵瑰簲鐨勪激瀹冲姞鎴愮郴鏁般€俓n\n" +
                "Damage bonus coefficient per point of provided armor during Defensive Stance.",
                0.0, 100.0
            ).getDouble();

            doppelhanderDamageBonusMinPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "doppelhanderDamageBonusMinPercent",
                20.0,
                "闃插尽濮挎€佹湡闂达紝鑷冲皯鑾峰緱鐨勬敾鍑诲姏鎻愬崌鐧惧垎姣斻€?0 = 20%銆俓n\n" +
                "Minimum damage bonus percentage for the Doppelhander during Defensive Stance. 20 = 20%.",
                0.0, 1000.0
            ).getDouble();

            doppelhanderAoeRadius = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "doppelhanderAoeRadius",
                4.5,
                "闃插尽濮挎€佹湡闂达紝鑼冨洿鏀诲嚮鐨勫崐寰勩€俓n\n" +
                "Radius of the Doppelhander's area attack during Defensive Stance.",
                0.5, 16.0
            ).getDouble();

            scoutRangedDamageCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "scoutRangedDamageCoefficient",
                2.0,
                "鏂ュ€欏瑁呭杩滅▼浼ゅ鍔犳垚鐨勭郴鏁般€俓n" +
                "瀹為檯鍔犳垚鐧惧垎姣?= 鍚勯儴浣嶆姢鐢叉澘鏉愭枡鍦ㄨ閮ㄤ綅鐨勬姢鐢查煣鎬у钩鍧囧€?脳 姝ょ郴鏁般€俓n\n" +
                "Coefficient for Scout Armor ranged damage bonus.\n" +
                "Actual bonus percent = average plate toughness for each piece x this coefficient.",
                0.0, 100.0
            ).getDouble();

            scoutDodgeChanceCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "scoutDodgeChanceCoefficient",
                1.0,
                "鏂ュ€欏瑁呴棯閬挎鐜囩郴鏁般€俓n" +
                "姣忎釜閮ㄤ綅鐨勯棯閬胯础鐚?= 璇ラ儴浣嶆姢鐢插€煎搴旂殑鍑忎激鐧惧垎姣?脳 姝ょ郴鏁般€俓n\n" +
                "Coefficient for Scout Armor dodge chance.\n" +
                "Each piece contributes armor-reduction-percent x this coefficient.",
                0.0, 100.0
            ).getDouble();

            scoutDodgeDamageFactor = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "scoutDodgeDamageFactor",
                0.2,
                "鏂ュ€欏瑁呮垚鍔熼棯閬挎椂浠嶇劧鎵垮彈鐨勪激瀹虫瘮渚嬨€?.2 = 鎵垮彈鍘熶激瀹崇殑 20%銆俓n\n" +
                "Damage factor taken on a successful Scout Armor dodge. 0.2 = take 20% of the original damage.",
                0.0, 1.0
            ).getDouble();

            scoutDodgeChanceCap = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "scoutDodgeChanceCap",
                0.8,
                "鏂ュ€欏瑁呮€婚棯閬挎鐜囦笂闄愩€?.8 = 80%銆傝涓婇檺浼氭寜閮ㄤ綅姣斾緥鍒嗘憡鍒板悇浠躲€俓n\n" +
                "Maximum total dodge chance for Scout Armor. 0.8 = 80%. This cap is distributed across armor slots by part ratio.",
                0.0, 1.0
            ).getDouble();

            scoutFallDamageReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "scoutFallDamageReduction",
                0.8,
                "鏂ュ€欏瑁呭鎽旇惤浼ゅ鐨勬€诲噺浼ゆ瘮渚嬨€?.8 = 80%銆傝鍊间細鎸夐儴浣嶆瘮渚嬪垎鎽婂埌鍚勪欢銆俓n\n" +
                "Total fall damage reduction granted by Scout Armor. 0.8 = 80%. This value is distributed across pieces by part ratio.",
                0.0, 1.0
            ).getDouble();

            scoutEnvironmentalDamageReduction = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "scoutEnvironmentalDamageReduction",
                0.5,
                "鏂ュ€欏瑁呭鐜浼ゅ锛堝鐔斿博銆佺伀鐒般€佷粰浜烘帉绛夛級鐨勬€诲噺浼ゆ瘮渚嬨€?.5 = 50%銆傝鍊间細鎸夐儴浣嶆瘮渚嬪垎鎽婂埌鍚勪欢銆俓n\n" +
                "Total environmental damage reduction granted by Scout Armor for hazards such as lava, fire, and cactus. 0.5 = 50%. This value is distributed across pieces by part ratio.",
                0.0, 1.0
            ).getDouble();

            maracaAttackMelodyAllyBonus = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "maracaAttackMelodyAllyBonus",
                0.30,
                "娌欓敜鈥滄敾鍑诲姏鎻愬崌鈥濇棆寰嬬粰浜堝弸鏂圭殑鏀诲嚮浼ゅ鎻愬崌姣斾緥銆?.30 = 30%銆俓n\n" +
            "Attack damage bonus granted to allies by the Maraca Attack Melody. 0.30 = 30%.",
                0.0, 10.0
            ).getDouble();

            maracaAttackMelodySelfBonus = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "maracaAttackMelodySelfBonus",
                0.45,
                "娌欓敜鈥滄敾鍑诲姏鎻愬崌鈥濇棆寰嬬粰浜堟紨濂忚€呰嚜韬殑鏀诲嚮浼ゅ鎻愬崌姣斾緥銆?.45 = 45%銆俓n\n" +
            "Attack damage bonus granted to the performer by the Maraca Attack Melody. 0.45 = 45%.",
                0.0, 10.0
            ).getDouble();

            maracaAttackMelodyDurationTicks = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "maracaAttackMelodyDurationTicks",
                1200,
                "娌欓敜鈥滄敾鍑诲姏鎻愬崌鈥濇棆寰嬬殑鍩虹鎸佺画鏃堕棿銆?0 tick = 1 绉掞紝榛樿 1200 tick = 60 绉掋€俓n\n" +
            "Base duration of the Maraca Attack Melody. 20 ticks = 1 second. Default: 1200 ticks = 60 seconds.",
                1, 72000
            ).getInt();

            maracaPartyDurationCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "maracaPartyDurationCoefficient",
                1.0,
                "娌欓敜鈥滅媯娆㈡淳瀵癸紒鈥濇寔缁椂闂寸郴鏁般€傚疄闄呮寔缁椂闂?= 琚竻绌虹殑鍏朵粬鏃嬪緥鍓╀綑鏃堕棿鎬诲拰 x 璇ョ郴鏁般€俓n\n" +
            "Duration coefficient for the Maraca Party melody. Actual duration = total remaining duration of removed melodies x this value.",
                0.0, 10.0
            ).getDouble();

            enableDefenseDamage = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "enableDefenseDamage",
                true,
                "鏄惁鍚敤闃插尽鎹熶激绯荤粺銆傚彈鍒伴潪鐜╁鐢熺墿浼ゅ浼氶€愭笎闄嶄綆鎶ょ敳銆侀槻寰￠煣鎬т笌閮ㄥ垎鍙楀嚮闃插尽璇嶆潯鏁堟灉銆俓n\n" +
                "Whether to enable Defense Damage. Damage from non-player living entities gradually weakens armor, toughness, and some defensive on-hit armor traits."
            ).getBoolean();

            defenseDamageBossHitPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "defenseDamageBossHitPercent",
                0.075,
                "Boss 姣忔鍛戒腑鐜╁鏃跺鍔犵殑闃插尽鎹熶激銆?.075 = 7.5%銆俓n\n" +
                "Defense Damage added by each boss hit. 0.075 = 7.5%.",
                0.0, 1.0
            ).getDouble();

            defenseDamageNormalHitPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "defenseDamageNormalHitPercent",
                0.0375,
                "闈?Boss 鐢熺墿姣忔鍛戒腑鐜╁鏃跺鍔犵殑闃插尽鎹熶激銆?.0375 = 3.75%銆俓n\n" +
                "Defense Damage added by each non-boss mob hit. 0.0375 = 3.75%.",
                0.0, 1.0
            ).getDouble();

            defenseDamageMinimumEfficiency = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "defenseDamageMinimumEfficiency",
                0.25,
                "闃插尽鎹熶激鏈€澶氫細鎶婃姢鐢层€侀槻寰￠煣鎬т笌鐩稿叧闃插尽璇嶆潯鏁堟灉闄嶄綆鍒板師鏈殑姣斾緥銆?.25 = 鏈€浣庝繚鐣?25%銆俓n\n" +
                "Minimum remaining efficiency for armor, toughness, and related defensive traits under Defense Damage. 0.25 = keep at least 25%.",
                0.0, 1.0
            ).getDouble();

            defenseDamageTriggerIntervalTicks = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "defenseDamageTriggerIntervalTicks",
                15,
                "闃插尽鎹熶激瀵瑰悓涓€鍚嶇帺瀹剁殑瑙﹀彂闂撮殧銆?5 ticks = 0.75 绉掞紱鍦ㄩ棿闅旂粨鏉熷墠锛岄珮棰戞敾鍑讳笉浼氶噸澶嶇疮璁℃柊鐨勯槻寰℃崯浼ゃ€俓n\n" +
                "Trigger interval for Defense Damage on the same player. 15 ticks = 0.75 seconds; hits during this interval will not add more Defense Damage.",
                0, 1200
            ).getInt();

            defenseDamageRecoveryDelayTicks = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "defenseDamageRecoveryDelayTicks",
                40,
                "鐜╁澶氫箙娌℃湁鍙楀埌浼氶€犳垚闃插尽鎹熶激鐨勪激瀹冲悗寮€濮嬫仮澶嶃€?0 ticks = 1 绉掋€俓n\n" +
                "How long after the last Defense Damage hit before recovery starts. 20 ticks = 1 second.",
                0, 1200
            ).getInt();

            defenseDamageRecoveryIntervalTicks = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "defenseDamageRecoveryIntervalTicks",
                30,
                "闃插尽鎹熶激姣忛殧澶氫箙鎭㈠涓€娆°€?0 ticks = 1 绉掋€俓n\n" +
                "Interval between Defense Damage recovery ticks. 20 ticks = 1 second.",
                1, 1200
            ).getInt();

            defenseDamageRecoveryPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "defenseDamageRecoveryPercent",
                0.15,
                "姣忔鎭㈠绉婚櫎鐨勯槻寰℃崯浼ゆ瘮渚嬨€?.15 = 15%銆俓n\n" +
                "Defense Damage removed on each recovery tick. 0.15 = 15%.",
                0.0, 1.0
            ).getDouble();
        } finally {
            if (cfg.hasChanged()) cfg.save();
        }
    }
}


