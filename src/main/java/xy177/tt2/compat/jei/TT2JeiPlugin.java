package xy177.tt2.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import xy177.tt2.client.gui.GuiCraftsmanEye;

import java.awt.Rectangle;
import java.util.List;

@JEIPlugin
public final class TT2JeiPlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        registry.addAdvancedGuiHandlers(new CraftsmanEyeGuiHandler());
    }

    private static final class CraftsmanEyeGuiHandler
        implements IAdvancedGuiHandler<GuiCraftsmanEye> {

        @Override
        public Class<GuiCraftsmanEye> getGuiContainerClass() {
            return GuiCraftsmanEye.class;
        }

        @Override
        public List<Rectangle> getGuiExtraAreas(GuiCraftsmanEye gui) {
            return gui.getExtraGuiAreas();
        }
    }
}
