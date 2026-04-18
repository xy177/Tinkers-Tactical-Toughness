package xy177.tt2.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import slimeknights.tconstruct.library.TinkerRegistry;
import xy177.tt2.config.TT2Config;
import xy177.tt2.events.ShieldEvents;
import xy177.tt2.init.TT2Items;

import java.io.File;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        TT2Config.init(new File(event.getModConfigurationDirectory(), "tt2.cfg"));
    }

    public void init(FMLInitializationEvent event) {
        // ★ 参照参考模组，在 init 阶段调用 registerToolCrafting（同时注册工具站+工具锻炉）
        // 此时 Item 注册已完成，时序安全
        TinkerRegistry.registerToolCrafting(TT2Items.TRAVELER_SHIELD);

        // 注册格挡事件处理器
        MinecraftForge.EVENT_BUS.register(new ShieldEvents());
    }

    public void postInit(FMLPostInitializationEvent event) {
    }
}
