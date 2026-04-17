package xy177.tt2;

import xy177.tt2.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(
    modid   = TT2.MOD_ID,
    name    = TT2.MOD_NAME,
    version = TT2.VERSION,
    dependencies = TT2.DEPS,
    acceptedMinecraftVersions = "[1.12.2]"
)
public class TT2 {

    public static final String MOD_ID  = "tt2";
    public static final String MOD_NAME = "Tinkers' Tactical Toughness";
    public static final String VERSION  = "1.0";
    public static final String DEPS =
        "required-after:mantle;" +
        "required-after:tconstruct;" +
        "required-after:conarm;";

    @SidedProxy(
        clientSide = "xy177.tt2.proxy.ClientProxy",
        serverSide = "xy177.tt2.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.Instance
    public static TT2 instance;

    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
