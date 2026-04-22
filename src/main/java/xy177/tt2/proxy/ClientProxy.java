package xy177.tt2.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import slimeknights.mantle.client.book.repository.FileRepository;
import slimeknights.tconstruct.library.TinkerRegistryClient;
import slimeknights.tconstruct.library.book.TinkerBook;
import slimeknights.tconstruct.library.client.ToolBuildGuiInfo;
import xy177.tt2.client.NunchakuClientHandler;
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

        MinecraftForge.EVENT_BUS.register(new HeavyShieldClientEvents());

        MinecraftForge.EVENT_BUS.register(new NunchakuClientHandler());

        TinkerBook.INSTANCE.addRepository(new FileRepository("tt2:book"));
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

        // 迅捷盾：2 个槽位（盘 + 大板）
        if (TT2Items.SWIFT_SHIELD != null) {
            ToolBuildGuiInfo infoSwift = new ToolBuildGuiInfo(TT2Items.SWIFT_SHIELD);
            infoSwift.addSlotPosition(24, 30);
            infoSwift.addSlotPosition(48, 52);
            TinkerRegistryClient.addToolBuilding(infoSwift);
        }

        // 重装盾：3 个槽位（牌板 + 大板 + 坚韧手柄）
        if (TT2Items.HEAVY_SHIELD != null) {
            ToolBuildGuiInfo infoHeavy = ToolBuildGuiInfo.default3Part(TT2Items.HEAVY_SHIELD);
            TinkerRegistryClient.addToolBuilding(infoHeavy);
        }

        // 双节棍：3 个槽位（杆1 + 杆2 + 绑定结）
        if (TT2Items.NUNCHAKU != null) {
            ToolBuildGuiInfo infoNunchaku = new ToolBuildGuiInfo(TT2Items.NUNCHAKU);
            infoNunchaku.addSlotPosition(16, 32); // 杆1
            infoNunchaku.addSlotPosition(48, 44); // 杆2
            infoNunchaku.addSlotPosition(16, 56); // 绑定结
            TinkerRegistryClient.addToolBuilding(infoNunchaku);
        }
    }
}
