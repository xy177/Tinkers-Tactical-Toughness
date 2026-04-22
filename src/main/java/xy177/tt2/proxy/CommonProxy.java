package xy177.tt2.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import slimeknights.tconstruct.library.TinkerRegistry;
import xy177.tt2.config.TT2Config;
import xy177.tt2.events.HeavyShieldEvents;
import xy177.tt2.events.ShieldEvents;
import xy177.tt2.init.TT2Items;

import java.io.File;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        TT2Config.init(new File(event.getModConfigurationDirectory(), "tt2.cfg"));
        // 注册连击词条到匠魂（必须在 preInit 阶段）
        TinkerRegistry.addTrait(TinkerNunchaku_ref());
    }

    // 延迟引用，避免静态初始化顺序问题
    private static slimeknights.tconstruct.library.traits.ITrait TinkerNunchaku_ref() {
        return xy177.tt2.tools.TinkerNunchaku.COMBO_TRAIT;
    }

    public void init(FMLInitializationEvent event) {
        TinkerRegistry.registerToolCrafting(TT2Items.SWIFT_SHIELD);
        TinkerRegistry.registerToolCrafting(TT2Items.HEAVY_SHIELD);
        TinkerRegistry.registerToolCrafting(TT2Items.NUNCHAKU);

        MinecraftForge.EVENT_BUS.register(new ShieldEvents());
        MinecraftForge.EVENT_BUS.register(new HeavyShieldEvents());
    }

    public void postInit(FMLPostInitializationEvent event) {
    }
}
