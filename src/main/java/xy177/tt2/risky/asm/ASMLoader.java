package xy177.tt2.risky.asm;

import net.minecraftforge.fml.relauncher.IFMLCallHook;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.Name("tt2")
@IFMLLoadingPlugin.TransformerExclusions("xy177.tt2.risky")
@IFMLLoadingPlugin.SortingIndex(1010)
@IFMLLoadingPlugin.MCVersion("1.12.2")
public class ASMLoader implements IFMLLoadingPlugin, IFMLCallHook {

    public ASMLoader() {
        System.out.println("Loaded?");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"xy177.tt2.risky.asm.ASMTinkerAnimate"};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> map) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public Void call() throws Exception {
        return null;
    }
}
