package xy177.tt2.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Loader;
import org.lwjgl.opengl.GL11;
import xy177.tt2.TT2;
import xy177.tt2.inventory.ContainerCraftsmanEye;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** TT2-owned portable analyzer GUI with Forestry-compatible page rendering. */
public final class GuiCraftsmanEye extends GuiContainer {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(TT2.MOD_ID, "textures/gui/craftsman_eye.png");
    private static final String FORESTRY_RENDERER =
        "xy177.tt2.client.gui.ForestryCraftsmanEyeRenderer";
    private static final int STAFF_X = 223;
    private static final int STAFF_Y = 8;
    private static final int PANEL_WIDTH = 246;
    private static final int PANEL_HEIGHT = 238;

    private final ContainerCraftsmanEye craftsmanEye;
    private final CraftsmanEyeLayout textLayout;
    private final CraftsmanEyePageRenderer pageRenderer;
    private final List<PageItem> pageItems = new ArrayList<>();
    private CraftsmanEyeLedgerManager ledgerManager;
    private boolean rendererFailed;

    public GuiCraftsmanEye(ContainerCraftsmanEye craftsmanEye) {
        super(craftsmanEye);
        this.craftsmanEye = craftsmanEye;
        this.textLayout = new CraftsmanEyeLayout(this);
        this.pageRenderer = createPageRenderer();
        this.xSize = PANEL_WIDTH;
        this.ySize = PANEL_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();
        ledgerManager = null;
        if (pageRenderer != null) {
            try {
                ledgerManager = new CraftsmanEyeLedgerManager(this, pageRenderer);
            } catch (RuntimeException | LinkageError failure) {
                TT2.logger.error("Could not initialize the Tinker's Eye ledgers", failure);
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        pageItems.clear();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        ItemStack staff = craftsmanEye.getStaff();
        if (!staff.isEmpty()) {
            RenderHelper.enableGUIStandardItemLighting();
            int staffX = guiLeft + STAFF_X;
            int staffY = guiTop + STAFF_Y;
            mc.getRenderItem().renderItemAndEffectIntoGUI(staff, staffX, staffY);
            FontRenderer itemFont = staff.getItem().getFontRenderer(staff);
            mc.getRenderItem().renderItemOverlayIntoGUI(
                itemFont == null ? fontRenderer : itemFont, staff, staffX, staffY, null);
            RenderHelper.disableStandardItemLighting();
        }

        drawAnalysisContent();
        if (ledgerManager != null) {
            ledgerManager.draw();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        boolean staffHovered = isStaffHovered(mouseX, mouseY);
        if (ledgerManager != null && ledgerManager.drawTooltip(mouseX, mouseY)) {
            return;
        }
        if (mc.player.inventory.getItemStack().isEmpty()) {
            for (PageItem item : pageItems) {
                if (item.contains(mouseX, mouseY)) {
                    drawPageItemTooltip(item.stack, mouseX, mouseY);
                    return;
                }
            }
            if (staffHovered) {
                GuiUtils.drawHoveringText(singleton(I18n.format("gui.tt2.craftsman_eye.durability_cost")),
                    mouseX, mouseY, width, height, -1, fontRenderer);
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (ledgerManager != null && ledgerManager.handleMouseClicked(mouseX, mouseY, mouseButton)) {
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        if (isStaffHovered(mouseX, mouseY)) {
            drawStaffSlotHighlight();
        }
    }

    /** Mirrors Forestry's GUI hook used by JEI to keep item lists clear of ledgers. */
    public List<Rectangle> getExtraGuiAreas() {
        return ledgerManager == null ? Collections.emptyList() : ledgerManager.getLedgerAreas();
    }

    private void drawAnalysisContent() {
        int page = craftsmanEye.getActivePage();
        ItemStack specimen = craftsmanEye.getActiveSpecimen();
        if (page > 0 && !specimen.isEmpty() && pageRenderer != null && !rendererFailed) {
            try {
                if (pageRenderer.drawPage(this, specimen, page)) {
                    return;
                }
            } catch (RuntimeException | LinkageError failure) {
                rendererFailed = true;
                TT2.logger.error("Could not render the Tinker's Eye Forestry page", failure);
            }
        }
        drawAnalyticsOverview();
    }

    private void drawAnalyticsOverview() {
        int screenColor = getPageColor("gui.screen", 0xFFFFFF);
        textLayout.startPage(screenColor);
        textLayout.newLine();
        String title = I18n.format("gui.tt2.craftsman_eye.title").toUpperCase(Locale.ENGLISH);
        textLayout.drawCenteredLine(title, 8, 208, screenColor);
        textLayout.newLine();
        fontRenderer.drawSplitString(I18n.format("gui.tt2.craftsman_eye.help"),
            guiLeft + 16, guiTop + 42, 200, screenColor);
        textLayout.newLine();
        textLayout.newLine();
        textLayout.newLine();
        textLayout.newLine();
        textLayout.drawLine(I18n.format("for.gui.alyzer.overview") + ":", 16);
        textLayout.newLine();
        textLayout.drawLine("I  : " + I18n.format("for.gui.general"), 16);
        textLayout.newLine();
        textLayout.drawLine("II : " + I18n.format("for.gui.environment"), 16);
        textLayout.newLine();
        textLayout.drawLine("III: " + I18n.format("for.gui.produce"), 16);
        textLayout.newLine();
        textLayout.drawLine("IV : " + I18n.format("for.gui.evolution"), 16);
    }

    private int getPageColor(String key, int fallback) {
        return pageRenderer == null ? fallback : pageRenderer.getColor(key, fallback);
    }

    private static CraftsmanEyePageRenderer createPageRenderer() {
        if (!Loader.isModLoaded("forestry")) {
            return null;
        }
        try {
            Class<?> type = Class.forName(FORESTRY_RENDERER);
            return (CraftsmanEyePageRenderer) type.newInstance();
        } catch (ReflectiveOperationException | LinkageError failure) {
            TT2.logger.error("Could not initialize the Tinker's Eye Forestry renderer", failure);
            return null;
        }
    }

    CraftsmanEyeLayout getTextLayout() {
        return textLayout;
    }

    FontRenderer getPageFont() {
        return fontRenderer;
    }

    Minecraft getMinecraftClient() {
        return mc;
    }

    int getGuiLeftValue() {
        return guiLeft;
    }

    int getGuiTopValue() {
        return guiTop;
    }

    int getScreenWidthValue() {
        return width;
    }

    int getScreenHeightValue() {
        return height;
    }

    int getPanelWidthValue() {
        return PANEL_WIDTH;
    }

    ItemStack getActiveSpecimenForLedger() {
        return craftsmanEye.getActiveSpecimen();
    }

    ItemStack getStaffForLedger() {
        return craftsmanEye.getStaff();
    }

    void renderPageItem(ItemStack stack, int x, int y) {
        renderPageItem(stack, x, y, true, true);
    }

    void renderPageIcon(ItemStack stack, int x, int y) {
        renderPageItem(stack, x, y, true, false);
    }

    void renderPageDecoration(ItemStack stack, int x, int y) {
        renderPageItem(stack, x, y, false, false);
    }

    private void renderPageItem(ItemStack stack, int x, int y,
                                boolean renderOverlay, boolean showTooltip) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        int screenX = guiLeft + x;
        int screenY = guiTop + y;
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, screenX, screenY);
        if (renderOverlay) {
            FontRenderer itemFont = stack.getItem().getFontRenderer(stack);
            mc.getRenderItem().renderItemOverlayIntoGUI(
                itemFont == null ? fontRenderer : itemFont, stack, screenX, screenY, null);
        }
        RenderHelper.disableStandardItemLighting();
        if (showTooltip) {
            pageItems.add(new PageItem(stack, screenX, screenY));
        }
    }

    void drawAtlasRegion(int x, int y, int textureX, int textureY, int width, int height) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft + x, guiTop + y, textureX, textureY, width, height);
    }

    void bindLedgerTexture(ResourceLocation texture) {
        mc.getTextureManager().bindTexture(texture);
    }

    void drawLedgerTextureRegion(int x, int y, int textureX, int textureY,
                                 int width, int height) {
        drawTexturedModalRect(x, y, textureX, textureY, width, height);
    }

    void drawLedgerIcon(ResourceLocation texture, int x, int y) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(texture);
        drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 16, 16, 16.0F, 16.0F);
    }

    void drawLedgerStaffErrorIcon(int x, int y) {
        ItemStack staff = craftsmanEye.getStaff();
        if (!staff.isEmpty()) {
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(staff, x, y);
            RenderHelper.disableStandardItemLighting();
        }
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GL11.glLineWidth(2.0F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x + 2, y + 2, 0.0D).color(255, 32, 32, 255).endVertex();
        buffer.pos(x + 14, y + 14, 0.0D).color(255, 32, 32, 255).endVertex();
        buffer.pos(x + 14, y + 2, 0.0D).color(255, 32, 32, 255).endVertex();
        buffer.pos(x + 2, y + 14, 0.0D).color(255, 32, 32, 255).endVertex();
        tessellator.draw();
        GL11.glLineWidth(1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawPageItemTooltip(ItemStack stack, int mouseX, int mouseY) {
        if (pageRenderer == null) {
            renderToolTip(stack, mouseX, mouseY);
            return;
        }
        try {
            List<String> tooltip = pageRenderer.getItemTooltip(stack);
            GuiUtils.drawHoveringText(tooltip, mouseX, mouseY,
                width, height, -1, fontRenderer);
        } catch (RuntimeException | LinkageError failure) {
            renderToolTip(stack, mouseX, mouseY);
        }
    }

    private boolean isStaffHovered(int mouseX, int mouseY) {
        return isStaffRegion(mouseX - guiLeft, mouseY - guiTop);
    }

    private static boolean isStaffRegion(int mouseX, int mouseY) {
        return mouseX >= STAFF_X - 1 && mouseX <= STAFF_X + 16
            && mouseY >= STAFF_Y - 1 && mouseY <= STAFF_Y + 16;
    }

    private void drawStaffSlotHighlight() {
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.colorMask(true, true, true, false);
        drawGradientRect(STAFF_X, STAFF_Y, STAFF_X + 16, STAFF_Y + 16,
            -2130706433, -2130706433);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
    }

    private static List<String> singleton(String text) {
        List<String> result = new ArrayList<>(1);
        result.add(text);
        return result;
    }

    private static final class PageItem {
        private final ItemStack stack;
        private final int x;
        private final int y;

        private PageItem(ItemStack stack, int x, int y) {
            this.stack = stack;
            this.x = x;
            this.y = y;
        }

        private boolean contains(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16;
        }
    }
}
