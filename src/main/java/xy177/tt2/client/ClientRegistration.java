package xy177.tt2.client;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import slimeknights.tconstruct.common.ModelRegisterUtil;
import xy177.tt2.TT2;
import xy177.tt2.init.TT2Items;

@Mod.EventBusSubscriber(modid = TT2.MOD_ID, value = Side.CLIENT)
public class ClientRegistration {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        if (TT2Items.SWIFT_SHIELD != null) {
            ModelRegisterUtil.registerToolModel(TT2Items.SWIFT_SHIELD);
        }
        if (TT2Items.HEAVY_SHIELD != null) {
            ModelRegisterUtil.registerToolModel(TT2Items.HEAVY_SHIELD);
        }
    }
}
