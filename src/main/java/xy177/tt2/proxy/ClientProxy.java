package xy177.tt2.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import xy177.tt2.events.HeavyShieldClientEvents;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import slimeknights.tconstruct.library.TinkerRegistryClient;
import slimeknights.tconstruct.library.client.ToolBuildGuiInfo;
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
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

        // 迅捷盾：2 个槽位（盘 + 大板）
        if (TT2Items.SWIFT_SHIELD != null) {
            ToolBuildGuiInfo infoTraveler = new ToolBuildGuiInfo(TT2Items.SWIFT_SHIELD);
            infoTraveler.addSlotPosition(24, 30); // 盘
            infoTraveler.addSlotPosition(48, 52); // 大板
            TinkerRegistryClient.addToolBuilding(infoTraveler);
        }

        // 重装盾：3 个槽位（牌板 + 大板 + 坚韧手柄）
        if (TT2Items.HEAVY_SHIELD != null) {
            ToolBuildGuiInfo infoPlate = ToolBuildGuiInfo.default3Part(TT2Items.HEAVY_SHIELD);
            TinkerRegistryClient.addToolBuilding(infoPlate);
        }
    }
}
