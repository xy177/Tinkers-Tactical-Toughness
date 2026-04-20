package xy177.tt2.potion;

import net.minecraft.potion.Potion;


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
