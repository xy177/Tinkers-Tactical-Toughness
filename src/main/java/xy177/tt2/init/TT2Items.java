package xy177.tt2.init;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.tt2.TT2;
import xy177.tt2.tools.TravelerShield;

@Mod.EventBusSubscriber(modid = TT2.MOD_ID)
public class TT2Items {

    public static TravelerShield TRAVELER_SHIELD;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        TRAVELER_SHIELD = new TravelerShield();
        TRAVELER_SHIELD.setRegistryName(TT2.MOD_ID, "traveler_shield");
        event.getRegistry().register(TRAVELER_SHIELD);
        // ★ 不在此处调用 registerToolCrafting，时序太早
        // 合成注册已移至 CommonProxy.init()
    }
}
