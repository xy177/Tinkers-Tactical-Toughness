package xy177.tt2.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
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
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

        // 注册工具站 GUI 中的槽位坐标（两个部件：盘 + 大板）
        if (TT2Items.TRAVELER_SHIELD != null) {
            ToolBuildGuiInfo info = new ToolBuildGuiInfo(TT2Items.TRAVELER_SHIELD);
            info.addSlotPosition(24, 30); // 盘（pan）
            info.addSlotPosition(48, 52); // 大板（large plate）
            TinkerRegistryClient.addToolBuilding(info);
        }
    }
}
