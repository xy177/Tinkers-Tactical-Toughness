package xy177.tt2.potion;

import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.tt2.TT2;

@Mod.EventBusSubscriber(modid = TT2.MOD_ID)
public class TT2Potions {

    public static PotionParryWindow PARRY_WINDOW;
    public static PotionOpportunity OPPORTUNITY;
    public static PotionImbalance IMBALANCE;
    public static PotionImbalanceImmunity IMBALANCE_IMMUNITY;
    public static PotionDefensiveStance DEFENSIVE_STANCE;
    public static PotionMaracaSelfAttack MARACA_SELF_ATTACK;
    public static PotionMaracaAttack MARACA_ATTACK;
    public static PotionMaracaGuard MARACA_GUARD;
    public static PotionMaracaStability MARACA_STABILITY;
    public static PotionMaracaParty MARACA_PARTY;

    @SubscribeEvent
    public static void registerPotions(RegistryEvent.Register<Potion> event) {
        PARRY_WINDOW = register(event, new PotionParryWindow(), "parry_window");
        OPPORTUNITY = register(event, new PotionOpportunity(), "opportunity");
        IMBALANCE = register(event, new PotionImbalance(), "imbalance");
        IMBALANCE_IMMUNITY = register(event, new PotionImbalanceImmunity(), "imbalance_immunity");
        DEFENSIVE_STANCE = register(event, new PotionDefensiveStance(), "defensive_stance");
        MARACA_SELF_ATTACK = register(event, new PotionMaracaSelfAttack(), "maraca_self_attack");
        MARACA_ATTACK = register(event, new PotionMaracaAttack(), "maraca_attack");
        MARACA_GUARD = register(event, new PotionMaracaGuard(), "maraca_guard");
        MARACA_STABILITY = register(event, new PotionMaracaStability(), "maraca_stability");
        MARACA_PARTY = register(event, new PotionMaracaParty(), "maraca_party");
    }

    private static <T extends Potion> T register(RegistryEvent.Register<Potion> event, T potion, String name) {
        potion.setRegistryName(TT2.MOD_ID, name);
        event.getRegistry().register(potion);
        return potion;
    }
}

