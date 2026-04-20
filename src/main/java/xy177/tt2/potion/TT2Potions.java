package xy177.tt2.potion;

import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.tt2.TT2;

@Mod.EventBusSubscriber(modid = TT2.MOD_ID)
public class TT2Potions {

    public static PotionParryWindow   PARRY_WINDOW;

    public static PotionOpportunity   OPPORTUNITY;

    public static PotionImbalance     IMBALANCE;

    public static PotionImbalanceImmunity IMBALANCE_IMMUNITY;

    @SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> event) {
        PARRY_WINDOW = register(event, new PotionParryWindow(), "parry_window");
        OPPORTUNITY  = register(event, new PotionOpportunity(),  "opportunity");
        IMBALANCE    = register(event, new PotionImbalance(),    "imbalance");
        IMBALANCE_IMMUNITY = register(event, new PotionImbalanceImmunity(), "imbalance_immunity");
    }

    private static <T extends Potion> T register(RegistryEvent.Register<Potion> event, T potion, String name) {
        potion.setRegistryName(TT2.MOD_ID, name);
        event.getRegistry().register(potion);
        return potion;
    }
}
