package xy177.tt2.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import c4.conarm.lib.ArmoryRegistry;
import slimeknights.tconstruct.library.TinkerRegistry;
import xy177.tt2.config.TT2Config;
import xy177.tt2.events.DefenseDamageEvents;
import xy177.tt2.events.DoppelhanderEvents;
import xy177.tt2.events.HeavyShieldEvents;
import xy177.tt2.events.ScoutArmorEvents;
import xy177.tt2.events.ShieldEvents;
import xy177.tt2.init.TT2Items;
import xy177.tt2.network.TT2Network;

import java.io.File;

public class CommonProxy {

    protected ScoutArmorEvents scoutArmorEvents;

    public void preInit(FMLPreInitializationEvent event) {
        TT2Config.init(new File(event.getModConfigurationDirectory(), "tt2.cfg"));
        TT2Network.init();

        if (TT2Config.enableNunchaku) {
            TinkerRegistry.addTrait(xy177.tt2.tools.TinkerNunchaku.COMBO_TRAIT);
        }
    }

    public void init(FMLInitializationEvent event) {
        if (TT2Config.enableDefenseDamage) {
            MinecraftForge.EVENT_BUS.register(new DefenseDamageEvents());
        }

        if (TT2Config.enableSwiftShield && TT2Items.SWIFT_SHIELD != null) {
            TinkerRegistry.registerToolCrafting(TT2Items.SWIFT_SHIELD);
            MinecraftForge.EVENT_BUS.register(new ShieldEvents());
        }

        if (TT2Config.enableHeavyShield && TT2Items.HEAVY_SHIELD != null) {
            TinkerRegistry.registerToolCrafting(TT2Items.HEAVY_SHIELD);
            MinecraftForge.EVENT_BUS.register(new HeavyShieldEvents());
        }

        if (TT2Config.enableNunchaku && TT2Items.NUNCHAKU != null) {
            TinkerRegistry.registerToolCrafting(TT2Items.NUNCHAKU);
        }

        if (TT2Config.enableDoppelhander && TT2Items.DOPPELHANDER != null) {
            TinkerRegistry.registerToolCrafting(TT2Items.DOPPELHANDER);
            MinecraftForge.EVENT_BUS.register(new DoppelhanderEvents());
        }

        if (TT2Config.enableScoutArmor) {
            if (TT2Items.SCOUT_HELMET != null) {
                ArmoryRegistry.registerArmorForging(TT2Items.SCOUT_HELMET);
            }
            if (TT2Items.SCOUT_CHESTPLATE != null) {
                ArmoryRegistry.registerArmorForging(TT2Items.SCOUT_CHESTPLATE);
            }
            if (TT2Items.SCOUT_LEGGINGS != null) {
                ArmoryRegistry.registerArmorForging(TT2Items.SCOUT_LEGGINGS);
            }
            if (TT2Items.SCOUT_BOOTS != null) {
                ArmoryRegistry.registerArmorForging(TT2Items.SCOUT_BOOTS);
            }
            scoutArmorEvents = new ScoutArmorEvents();
            MinecraftForge.EVENT_BUS.register(scoutArmorEvents);
        }
    }

    public void postInit(FMLPostInitializationEvent event) {
    }

    public ScoutArmorEvents getScoutArmorEvents() {
        return scoutArmorEvents;
    }
}
