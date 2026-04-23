package xy177.tt2.init;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.tt2.TT2;
import xy177.tt2.config.TT2Config;
import xy177.tt2.tools.Doppelhander;
import xy177.tt2.tools.HeavyShield;
import xy177.tt2.tools.SwiftShield;
import xy177.tt2.tools.TinkerNunchaku;

@Mod.EventBusSubscriber(modid = TT2.MOD_ID)
public class TT2Items {

    public static SwiftShield SWIFT_SHIELD;
    public static HeavyShield HEAVY_SHIELD;
    public static TinkerNunchaku NUNCHAKU;
    public static Doppelhander DOPPELHANDER;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        if (TT2Config.enableSwiftShield) {
            SWIFT_SHIELD = new SwiftShield();
            SWIFT_SHIELD.setRegistryName(TT2.MOD_ID, "swift_shield");
            event.getRegistry().register(SWIFT_SHIELD);
        }

        if (TT2Config.enableHeavyShield) {
            HEAVY_SHIELD = new HeavyShield();
            HEAVY_SHIELD.setRegistryName(TT2.MOD_ID, "heavy_shield");
            event.getRegistry().register(HEAVY_SHIELD);
        }

        if (TT2Config.enableNunchaku) {
            NUNCHAKU = new TinkerNunchaku();
            NUNCHAKU.setRegistryName(TT2.MOD_ID, "nunchaku");
            event.getRegistry().register(NUNCHAKU);
        }

        if (TT2Config.enableDoppelhander) {
            DOPPELHANDER = new Doppelhander();
            DOPPELHANDER.setRegistryName(TT2.MOD_ID, "doppelhander");
            event.getRegistry().register(DOPPELHANDER);
        }
    }
}
