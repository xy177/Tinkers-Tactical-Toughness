package xy177.tt2.init;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.tt2.TT2;
import xy177.tt2.tools.HeavyShield;
import xy177.tt2.tools.SwiftShield;
import xy177.tt2.tools.TinkerNunchaku;

@Mod.EventBusSubscriber(modid = TT2.MOD_ID)
public class TT2Items {

    public static SwiftShield    SWIFT_SHIELD;
    public static HeavyShield    HEAVY_SHIELD;
    public static TinkerNunchaku NUNCHAKU;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        SWIFT_SHIELD = new SwiftShield();
        SWIFT_SHIELD.setRegistryName(TT2.MOD_ID, "swift_shield");
        event.getRegistry().register(SWIFT_SHIELD);

        HEAVY_SHIELD = new HeavyShield();
        HEAVY_SHIELD.setRegistryName(TT2.MOD_ID, "heavy_shield");
        event.getRegistry().register(HEAVY_SHIELD);

        NUNCHAKU = new TinkerNunchaku();
        NUNCHAKU.setRegistryName(TT2.MOD_ID, "nunchaku");
        event.getRegistry().register(NUNCHAKU);
    }
}
