package xy177.tt2.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import slimeknights.tconstruct.library.TinkerRegistryClient;
import slimeknights.tconstruct.library.book.TinkerBook;
import slimeknights.tconstruct.library.client.ToolBuildGuiInfo;
import xy177.tt2.client.NunchakuClientHandler;
import xy177.tt2.client.book.TT2ToolSectionTransformer;
import xy177.tt2.config.TT2Config;
import xy177.tt2.events.HeavyShieldClientEvents;
import xy177.tt2.init.TT2Items;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        if (TT2Config.enableHeavyShield) {
            MinecraftForge.EVENT_BUS.register(new HeavyShieldClientEvents());
        }

        if (TT2Config.enableNunchaku) {
            MinecraftForge.EVENT_BUS.register(new NunchakuClientHandler());
        }

        TinkerBook.INSTANCE.addTransformer(new TT2ToolSectionTransformer());
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

        if (TT2Config.enableSwiftShield && TT2Items.SWIFT_SHIELD != null) {
            ToolBuildGuiInfo info = new ToolBuildGuiInfo(TT2Items.SWIFT_SHIELD);
            info.addSlotPosition(24, 30);
            info.addSlotPosition(48, 52);
            TinkerRegistryClient.addToolBuilding(info);
        }

        if (TT2Config.enableHeavyShield && TT2Items.HEAVY_SHIELD != null) {
            TinkerRegistryClient.addToolBuilding(
                ToolBuildGuiInfo.default3Part(TT2Items.HEAVY_SHIELD));
        }

        if (TT2Config.enableNunchaku && TT2Items.NUNCHAKU != null) {
            ToolBuildGuiInfo info = new ToolBuildGuiInfo(TT2Items.NUNCHAKU);
            info.addSlotPosition(16, 44);
            info.addSlotPosition(48, 56);
            info.addSlotPosition(48, 32);
            TinkerRegistryClient.addToolBuilding(info);
        }

        if (TT2Config.enableDoppelhander && TT2Items.DOPPELHANDER != null) {
            ToolBuildGuiInfo info = new ToolBuildGuiInfo(TT2Items.DOPPELHANDER);
            info.addSlotPosition(56, 16);
            info.addSlotPosition(24, 48);
            info.addSlotPosition(40, 32);
            info.addSlotPosition(8, 64);
            TinkerRegistryClient.addToolBuilding(info);
        }
    }
}
