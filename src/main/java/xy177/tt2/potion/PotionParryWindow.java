package xy177.tt2.potion;

import net.minecraft.potion.Potion;

/**
 * 精准格挡窗口期药水效果。
 * - 正面效果，无粒子显示
 * - 行为与普通药水完全相同：自然到期消失，可被牛奶清除
 * 仅作为时间标记使用，逻辑判断在 ShieldEvents 中通过 isPotionActive 完成。
 */
public class PotionParryWindow extends Potion {

    public PotionParryWindow() {
        super(
            false,     // isBadEffect = false → 正面效果
            0x00AAFF   // 液体颜色：浅蓝色
        );
        setPotionName("effect.tt2.parry_window");
        setIconIndex(6, 1);
    }

    /** 无 tick 行为 */
    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }
}
