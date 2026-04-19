package xy177.tt2.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class TT2Config {

    // 招架值上限 = 盘顶端攻击 * 大板手柄耐久系数 * (parryThresholdPercent / 100)
    public static double parryThresholdPercent = 50.0;

    // 破盾冷却时间（ticks）= 5 / (盘材料耐久 / 200.0) * cooldownCoefficient * 20
    public static double cooldownCoefficient = 1.0;

    // 精准格挡窗口期（ticks），举盾后此时间内受击为精准格挡（默认30tick = 1.5s）
    public static int perfectParryWindowTicks = 30;

    public static void init(File configFile) {
        Configuration cfg = new Configuration(configFile);
        try {
            cfg.load();

            parryThresholdPercent = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "parryThresholdPercent",
                50.0,
                "招架值上限百分比系数。\n" +
                "招架值上限 = 盘材料顶端攻击 x 大板手柄耐久倍率 x (此值 / 100)。\n" +
                "默认值: 50.0"
            ).getDouble();

            cooldownCoefficient = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "cooldownCoefficient",
                1.0,
                "破盾冷却时长系数。数值越大冷却越长。\n" +
                "冷却时长(tick) = ceil((5 / 大板手柄耐久系数) x 此系数 x 20)，最低 30 tick（1.5 秒）。\n" +
                "默认值: 1.0"
            ).getDouble();

            perfectParryWindowTicks = cfg.get(
                Configuration.CATEGORY_GENERAL,
                "perfectParryWindowTicks",
                30,
                "精准格挡窗口期（tick）。举盾动作发生后此时间内受到攻击视为精准格挡。\n" +
                "20 tick = 1秒。默认值: 30（即1.5秒）",
                1, 200
            ).getInt();

        } finally {
            if (cfg.hasChanged()) cfg.save();
        }
    }
}
