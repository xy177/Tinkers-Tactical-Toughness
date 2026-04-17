package xy177.tt2.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        // 注册特性、修饰符、材料属性等
    }

    public void init(FMLInitializationEvent event) {
        // 注册配方等
    }

    public void postInit(FMLPostInitializationEvent event) {
        // 处理跨模组兼容等
    }
}
