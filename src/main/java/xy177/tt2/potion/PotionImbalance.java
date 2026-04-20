package xy177.tt2.potion;

import net.minecraft.potion.Potion;


public class PotionImbalance extends Potion {

    public PotionImbalance() {
        super(true, 0x8B0000); // 深红色
        setPotionName("effect.tt2.imbalance");
        setIconIndex(1, 0);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }
}
