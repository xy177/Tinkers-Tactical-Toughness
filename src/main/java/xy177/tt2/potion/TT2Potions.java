package xy177.tt2.potion;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.potion.Potion;
import xy177.tt2.TT2;

@Mod.EventBusSubscriber(modid = TT2.MOD_ID)
public class TT2Potions {

    /** 精准格挡窗口期标记效果 */
    public static PotionParryWindow PARRY_WINDOW;

    @SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> event) {
        PARRY_WINDOW = new PotionParryWindow();
        PARRY_WINDOW.setRegistryName(TT2.MOD_ID, "parry_window");
        event.getRegistry().register(PARRY_WINDOW);
    }
}
