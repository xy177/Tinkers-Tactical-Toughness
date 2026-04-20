package xy177.tt2.potion;

import net.minecraft.potion.Potion;


public class PotionImbalanceImmunity extends Potion {

    public PotionImbalanceImmunity() {
        super(true, 0x4B0082); // 靛紫色
        setPotionName("effect.tt2.imbalance_immunity");
        setIconIndex(2, 0);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }
}
