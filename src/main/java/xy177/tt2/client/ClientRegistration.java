package xy177.tt2.client;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import slimeknights.tconstruct.common.ModelRegisterUtil;
import xy177.tt2.TT2;
import xy177.tt2.init.TT2Items;

/**
 * 客户端模型注册。
 * 使用 @Mod.EventBusSubscriber(Side.CLIENT) 静态注册，
 * 比在 ClientProxy 里实例注册更可靠。
 * ModelRegisterUtil.registerToolModel 会在
 * assets/tt2/models/item/tools/traveler_shield.tcon.json
 * 寻找模型定义，并交由 TCon 的材质渲染系统处理。
 */
@Mod.EventBusSubscriber(modid = TT2.MOD_ID, value = Side.CLIENT)
public class ClientRegistration {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        if (TT2Items.TRAVELER_SHIELD != null) {
            ModelRegisterUtil.registerToolModel(TT2Items.TRAVELER_SHIELD);
        }
    }
}
