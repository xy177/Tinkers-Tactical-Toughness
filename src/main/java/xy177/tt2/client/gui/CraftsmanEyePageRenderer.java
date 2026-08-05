package xy177.tt2.client.gui;

import net.minecraft.item.ItemStack;

import java.util.List;

/** Optional client renderer for Forestry's portable analyzer pages. */
interface CraftsmanEyePageRenderer {

    int getColor(String key, int fallback);

    boolean drawPage(GuiCraftsmanEye gui, ItemStack specimen, int page);

    List<String> getHints(ItemStack specimen);

    List<String> getItemTooltip(ItemStack stack);

    boolean areHintsEnabled();

    int getLedgerAnimationSpeed();
}
