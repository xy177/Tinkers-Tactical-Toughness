package xy177.tt2.potion;

import net.minecraft.potion.Potion;


public class PotionOpportunity extends Potion {

    public PotionOpportunity() {
        super(false, 0xFFD700); // 金色
        setPotionName("effect.tt2.opportunity");
        setIconIndex(5, 0);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }
}
