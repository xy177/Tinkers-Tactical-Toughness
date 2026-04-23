package xy177.tt2.potion;

import net.minecraft.potion.Potion;

public class PotionDefensiveStance extends Potion {

    public PotionDefensiveStance() {
        super(false, 0x3F6FB5);
        setPotionName("effect.tt2.defensive_stance");
        setIconIndex(7, 1);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }
}
