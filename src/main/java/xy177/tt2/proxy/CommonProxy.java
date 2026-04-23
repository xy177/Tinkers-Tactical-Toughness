package xy177.tt2.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import slimeknights.tconstruct.library.TinkerRegistry;
import xy177.tt2.config.TT2Config;
import xy177.tt2.events.DoppelhanderEvents;
import xy177.tt2.events.HeavyShieldEvents;
import xy177.tt2.events.ShieldEvents;
import xy177.tt2.init.TT2Items;

import java.io.File;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        TT2Config.init(new File(event.getModConfigurationDirectory(), "tt2.cfg"));

        if (TT2Config.enableNunchaku) {
            TinkerRegistry.addTrait(xy177.tt2.tools.TinkerNunchaku.COMBO_TRAIT);
        }
    }

    public void init(FMLInitializationEvent event) {
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
    }

    public void postInit(FMLPostInitializationEvent event) {
    }
}
