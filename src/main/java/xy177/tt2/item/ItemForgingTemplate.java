package xy177.tt2.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.TinkerRegistry;
import xy177.tt2.compat.CraftsmanStaffCompat;

import javax.annotation.Nullable;
import java.util.List;

public class ItemForgingTemplate extends Item {

    private final String tooltipKey;
    private final TextFormatting tooltipColor;
    private final boolean researchTemplate;

    public ItemForgingTemplate(String variant, TextFormatting tooltipColor) {
        this(variant, tooltipColor, false);
    }

    public ItemForgingTemplate(String variant, TextFormatting tooltipColor, boolean researchTemplate) {
        this.tooltipKey = "tooltip.tt2.forging_template." + variant;
        this.tooltipColor = tooltipColor;
        this.researchTemplate = researchTemplate;
        setTranslationKey("tt2.forging_template");
        setCreativeTab(TinkerRegistry.tabGeneral);
        setMaxStackSize(64);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(tooltipColor + I18n.translateToLocal(tooltipKey));
        if (researchTemplate) {
            if (GuiScreen.isShiftKeyDown()) {
                CraftsmanStaffCompat.addResearchTooltip(tooltip);
            } else {
                tooltip.add(TextFormatting.DARK_GRAY
                    + I18n.translateToLocal("tooltip.tt2.forging_template.research.hold_shift"));
            }
        }
    }
}
