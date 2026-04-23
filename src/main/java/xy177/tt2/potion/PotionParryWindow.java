package xy177.tt2.potion;

import net.minecraft.potion.Potion;

public class PotionParryWindow extends Potion {

    public PotionParryWindow() {
        super(
            false,
            0x00AAFF
        );
        setPotionName("effect.tt2.parry_window");
        setIconIndex(6, 1);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }
}
