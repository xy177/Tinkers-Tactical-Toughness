package xy177.tt2.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;
import c4.conarm.lib.armor.ArmorCore;
import slimeknights.mantle.client.gui.GuiElement;
import slimeknights.mantle.client.gui.GuiElementScalable;
import slimeknights.mantle.client.gui.GuiModule;
import slimeknights.mantle.client.gui.GuiMultiModule;
import c4.conarm.common.ConstructsRegistry;
import slimeknights.tconstruct.library.client.Icons;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.modifiers.IModifierDisplay;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.tools.common.client.module.GuiInfoPanel;
import xy177.tt2.TT2;
import xy177.tt2.init.TT2Items;
import xy177.tt2.inventory.ContainerModifierWorktable;
import xy177.tt2.logic.ModifierWorktableLogic;
import xy177.tt2.tile.TileModifierWorktable;
import slimeknights.tconstruct.library.utils.TinkerUtil;

import java.io.IOException;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class GuiModifierWorktable extends GuiMultiModule {

    private static final ResourceLocation BG = new ResourceLocation(TT2.MOD_ID, "textures/gui/modifier_worktable.png");
    private static final ResourceLocation TOOLSTATION = new ResourceLocation("tconstruct", "textures/gui/toolstation.png");
    private static final int PANEL_TOP_OFFSET = 11;
    private static final int MODIFIER_LEFT = 28;
    private static final int MODIFIER_TOP = 15;
    private static final int MODIFIER_SIZE = 18;
    private static final int MAX_MODIFIERS = 16;
    private static final int INFO_PANEL_HEIGHT = 83;
    private static final GuiElement PANEL_DECORATION_L = new GuiElement(36, 174, 5, 4);
    private static final GuiElement PANEL_DECORATION_R = new GuiElement(45, 174, 9, 4);
    private static final GuiElement BEAM_LEFT = new GuiElement(0, 187, 2, 7);
    private static final GuiElement BEAM_RIGHT = new GuiElement(131, 187, 2, 7);
    private static final GuiElementScalable BEAM_CENTER = new GuiElementScalable(2, 187, 129, 7);
    private final ContainerModifierWorktable container;
    private final TileModifierWorktable tile;
    private final GuiInfoPanel toolInfo;
    private final GuiInfoPanel modifierInfo;

    public GuiModifierWorktable(InventoryPlayer playerInventory, TileModifierWorktable tile) {
        super(new ContainerModifierWorktable(playerInventory, tile));
        this.container = (ContainerModifierWorktable) inventorySlots;
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 184;
        this.realWidth = xSize;
        this.realHeight = ySize;

        this.toolInfo = new GuiInfoPanel(this, inventorySlots);
        this.toolInfo.metal();
        this.modifierInfo = new GuiInfoPanel(this, inventorySlots);
        this.modifierInfo.metal();
        this.modifierInfo.yOffset = PANEL_TOP_OFFSET + INFO_PANEL_HEIGHT + 4;
        addModule(this.toolInfo);
        addModule(this.modifierInfo);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.toolInfo.xOffset = 2;
        this.toolInfo.yOffset = PANEL_TOP_OFFSET;
        this.modifierInfo.xOffset = this.toolInfo.xOffset;
        this.modifierInfo.yOffset = this.toolInfo.yOffset + INFO_PANEL_HEIGHT + 4;
        rebuildButtons();
        updateInfoPanels();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRenderer.drawString(I18n.format("container.tt2.modifier_worktable"), 8, 6, 0x404040);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.getTextureManager().bindTexture(BG);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        drawPanelDecorations();
        drawPatternIcons();
        for (GuiModule module : modules) {
            module.handleDrawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (buttonList.size() != currentButtonCount()) {
            rebuildButtons();
        }
        updateInfoPanels();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        mc.playerController.sendEnchantPacket(inventorySlots.windowId, button.id);
        container.enchantItem(mc.player, button.id);
        rebuildButtons();
        updateInfoPanels();
    }

    private void rebuildButtons() {
        buttonList.clear();
        int max = currentButtonCount();
        for (int i = 0; i < max; i++) {
            GuiButton button = new ModifierButton(i, guiLeft + MODIFIER_LEFT + i % 4 * MODIFIER_SIZE,
                guiTop + MODIFIER_TOP + i / 4 * MODIFIER_SIZE);
            button.enabled = !container.isSelectingModifier() || i == 0 || i != container.getSelectedIndex() + 1;
            buttonList.add(button);
        }
    }

    private int currentButtonCount() {
        int count = container.isSelectingModifier() ? container.getModifiers().size() + 1 : container.getActions().size();
        return Math.min(count, MAX_MODIFIERS);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        for (GuiButton button : buttonList) {
            if (mouseX >= button.x && mouseY >= button.y && mouseX < button.x + button.width && mouseY < button.y + button.height) {
                drawHoveringText(buttonTooltip(button.id), mouseX, mouseY);
                break;
            }
        }
    }

    private void drawPatternIcons() {
        drawPattern(TileModifierWorktable.SLOT_TOOL, Icons.ICON_Pickaxe);
        drawPattern(TileModifierWorktable.SLOT_INPUT_1, Icons.ICON_Ingot);
        drawPattern(TileModifierWorktable.SLOT_INPUT_2, Icons.ICON_Quartz);
    }

    private void drawPattern(int slotIndex, GuiElement icon) {
        Slot slot = inventorySlots.getSlot(slotIndex);
        if (slot == null || slot.getHasStack()) {
            return;
        }
        GlStateManager.enableBlend();
        GlStateManager.color(1F, 1F, 1F, 0.55F);
        mc.getTextureManager().bindTexture(Icons.ICON);
        icon.draw(guiLeft + slot.xPos - 1, guiTop + slot.yPos - 1);
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.disableBlend();
    }

    private void drawPanelDecorations() {
        mc.getTextureManager().bindTexture(TOOLSTATION);
        Rectangle topArea = toolInfo.getArea();
        int beamX = topArea.x - BEAM_LEFT.w;
        int beamY = guiTop;
        beamX += BEAM_LEFT.draw(beamX, beamY);
        beamX += BEAM_CENTER.drawScaledX(beamX, beamY, topArea.width);
        BEAM_RIGHT.draw(beamX, beamY);
        drawPanelDecoration(toolInfo.getArea());
        drawPanelDecoration(modifierInfo.getArea());
    }

    private void drawPanelDecoration(Rectangle area) {
        PANEL_DECORATION_L.draw(area.x + 5, area.y - PANEL_DECORATION_L.h);
        PANEL_DECORATION_R.draw(area.x + area.width - 5 - PANEL_DECORATION_R.w, area.y - PANEL_DECORATION_R.h);
    }

    private void updateInfoPanels() {
        if (!ModifierWorktableLogic.isTinkerItem(tile.getStackInSlot(TileModifierWorktable.SLOT_TOOL))) {
            toolInfo.setCaption(I18n.format("gui.tt2.modifier_worktable.panel.title"));
            toolInfo.setText(I18n.format("gui.tt2.modifier_worktable.panel.empty"));
        } else if (!container.isSelectingModifier()) {
            toolInfo.setCaption(I18n.format("gui.tt2.modifier_worktable.panel.title"));
            toolInfo.setText(I18n.format("gui.tt2.modifier_worktable.panel.select_action"));
        } else {
            toolInfo.setCaption(actionTitle(container.getSelectedAction()));
            toolInfo.setText(actionInfo(container.getSelectedAction()));
        }

        modifierInfo.setCaption(I18n.format("gui.tt2.modifier_worktable.modifiers"));
        if (container.isSelectingModifier() && container.getModifiers().isEmpty()) {
            modifierInfo.setText(new String[] { TextFormatting.RED + I18n.format("gui.tt2.modifier_worktable.no_targets") });
        } else if (container.isSelectingModifier()) {
            updateModifierInfo();
        } else {
            modifierInfo.setText(new String[0]);
        }
    }

    private void updateModifierInfo() {
        if (container.getSelectedAction() == ModifierWorktableLogic.TYPE_EXTRACT_EXPERIENCE) {
            modifierInfo.setText(new String[0]);
            return;
        }
        IModifier modifier = ModifierWorktableLogic.getModifier(container.getSelectedModifier());
        if (modifier == null) {
            modifierInfo.setText(new String[0]);
            return;
        }
        NBTTagCompound tag = TinkerUtil.getModifierTag(tile.getStackInSlot(TileModifierWorktable.SLOT_TOOL),
            container.getSelectedModifier());
        ModifierNBT data = ModifierNBT.readTag(tag);
        List<String> lines = new ArrayList<>();
        lines.add(data.getColorString() + modifier.getTooltip(tag, true));
        List<String> tooltip = new ArrayList<>();
        tooltip.add(data.getColorString() + modifier.getLocalizedDesc());
        tooltip.addAll(modifier.getExtraInfo(tile.getStackInSlot(TileModifierWorktable.SLOT_TOOL),
            tag));
        modifierInfo.setText(lines, tooltip);
    }

    private ItemStack modifierIcon(String id) {
        ItemStack specialIcon = specialModifierIcon(id);
        if (!specialIcon.isEmpty()) {
            return specialIcon;
        }
        IModifier modifier = ModifierWorktableLogic.getModifier(id);
        if (!(modifier instanceof IModifierDisplay)) {
            return ItemStack.EMPTY;
        }
        List<ItemStack> icons = new ArrayList<>();
        for (List<ItemStack> group : ((IModifierDisplay) modifier).getItems()) {
            for (ItemStack stack : group) {
                if (!stack.isEmpty() && stack.getItem() != TT2Items.MODIFIER_CRYSTAL) {
                    icons.add(stack);
                }
            }
        }
        if (icons.isEmpty()) {
            return ItemStack.EMPTY;
        }
        long time = mc.world == null ? 0 : mc.world.getTotalWorldTime();
        return icons.get((int) ((time / 20) % icons.size()));
    }

    private String buttonTooltip(int id) {
        if (container.isSelectingModifier()) {
            if (id == 0) {
                return I18n.format("gui.tt2.modifier_worktable.back");
            }
            if (container.getSelectedAction() == ModifierWorktableLogic.TYPE_EXTRACT_EXPERIENCE) {
                return I18n.format("gui.tt2.modifier_worktable.experience_target");
            }
            String modifierId = id - 1 < container.getModifiers().size() ? container.getModifiers().get(id - 1) : "";
            IModifier modifier = ModifierWorktableLogic.getModifier(modifierId);
            return modifier == null ? modifierId : modifier.getLocalizedName();
        }
        int action = id < container.getActions().size() ? container.getActions().get(id) : 0;
        return action == 0 ? "" : actionTitle(action);
    }

    private String actionTitle(int action) {
        return I18n.format(actionLangKey(action, "title"));
    }

    private String actionInfo(int action) {
        return I18n.format(actionLangKey(action, "info"));
    }

    private String actionLangKey(int action, String suffix) {
        String key = ModifierWorktableLogic.actionKey(action);
        if (action == ModifierWorktableLogic.TYPE_EXTRACT_FORTIFY) {
            key = isArmorInToolSlot() ? "extract_polishing" : "extract_sharpening";
        }
        return "gui.tt2.modifier_worktable.action." + key + "." + suffix;
    }

    private boolean isArmorInToolSlot() {
        ItemStack stack = tile.getStackInSlot(TileModifierWorktable.SLOT_TOOL);
        return !stack.isEmpty() && stack.getItem() instanceof ArmorCore;
    }

    private ItemStack specialModifierIcon(String id) {
        if (container.getSelectedAction() == ModifierWorktableLogic.TYPE_EXTRACT_EXPERIENCE) {
            return new ItemStack(TT2Items.EXPERIENCE_BOTTLE);
        }
        Material material = materialFromModifier(id);
        if (material == null || material == Material.UNKNOWN) {
            return ItemStack.EMPTY;
        }
        if (id.startsWith("extratrait") || id.startsWith("moretcon.extratrait2")) {
            ItemStack representative = material.getRepresentativeItem();
            return representative.isEmpty() ? TinkerRegistry.getShard(material) : representative;
        }
        if (id.startsWith("fortify")) {
            return TinkerTools.sharpeningKit.getItemstackWithMaterial(material);
        }
        if (id.startsWith("polished_armor")) {
            return ConstructsRegistry.polishingKit.getItemstackWithMaterial(material);
        }
        return ItemStack.EMPTY;
    }

    private Material materialFromModifier(String id) {
        String prefix = "";
        if (id.startsWith("polished_armor")) {
            prefix = "polished_armor";
        } else if (id.startsWith("moretcon.extratrait2")) {
            prefix = "moretcon.extratrait2";
        } else if (id.startsWith("extratrait2")) {
            prefix = "extratrait2";
        } else if (id.startsWith("extratrait_armor")) {
            prefix = "extratrait_armor";
        } else if (id.startsWith("extratrait")) {
            prefix = "extratrait";
        } else if (id.startsWith("fortify")) {
            prefix = "fortify";
        }
        if (prefix.isEmpty() || id.length() <= prefix.length()) {
            return null;
        }
        Material match = null;
        String suffix = id.substring(prefix.length());
        for (Material material : TinkerRegistry.getAllMaterials()) {
            if (suffix.startsWith(material.getIdentifier())
                && (match == null || material.getIdentifier().length() > match.getIdentifier().length())) {
                match = material;
            }
        }
        return match;
    }

    private class ModifierButton extends GuiButton {

        ModifierButton(int id, int x, int y) {
            super(id, x, y, MODIFIER_SIZE, MODIFIER_SIZE, "");
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (!visible) {
                return;
            }
            hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
            mc.getTextureManager().bindTexture(BG);
            GL11.glColor4f(1F, 1F, 1F, 1F);
            int v = 15;
            if (!enabled) {
                v += MODIFIER_SIZE;
            } else if (hovered) {
                v += MODIFIER_SIZE * 2;
            }
            GuiModifierWorktable.this.drawTexturedModalRect(x, y, 176, v, MODIFIER_SIZE, MODIFIER_SIZE);
            ItemStack icon = buttonIcon(id);
            if (icon.isEmpty()) {
                String label = container.isSelectingModifier() && id == 0 ? "<" : Integer.toString(id + 1);
                drawCenteredString(mc.fontRenderer, label, x + width / 2, y + 5, 0xFFFFFF);
                return;
            }
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemIntoGUI(icon, x + 1, y + 1);
            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, icon, x + 1, y + 1, null);
            RenderHelper.disableStandardItemLighting();
        }
    }

    private ItemStack buttonIcon(int id) {
        if (container.isSelectingModifier()) {
            if (id == 0 || id - 1 >= container.getModifiers().size()) {
                return ItemStack.EMPTY;
            }
            return modifierIcon(container.getModifiers().get(id - 1));
        }
        if (id >= container.getActions().size()) {
            return ItemStack.EMPTY;
        }
        return ModifierWorktableLogic.actionIcon(container.getActions().get(id));
    }
}
