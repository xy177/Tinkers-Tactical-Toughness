package xy177.tt2.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;
import slimeknights.tconstruct.library.utils.ToolHelper;
import xy177.tt2.compat.CraftsmanEyeGenetics;

import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** TT2-owned recreation of Forestry's analyzer error and hint ledgers. */
final class CraftsmanEyeLedgerManager {

    private static final int MIN_SIZE = 24;
    private static final int MAX_WIDTH = 124;
    private static final ResourceLocation LEDGER_RIGHT =
        new ResourceLocation("forestry", "textures/gui/ledger.png");
    private static final ResourceLocation LEDGER_LEFT =
        new ResourceLocation("forestry", "textures/gui/ledger_left.png");
    private static final ResourceLocation HINT_ICON =
        new ResourceLocation("forestry", "textures/gui/misc/hint.png");
    private static final ResourceLocation NO_SPECIMEN_ICON =
        new ResourceLocation("forestry", "textures/gui/errors/no_specimen.png");

    private static LedgerKind openedLedger;

    private final GuiCraftsmanEye gui;
    private final CraftsmanEyePageRenderer renderer;
    private final int maxWidth;
    private final int animationSpeed;
    private final ErrorLedger errorLedger;
    @Nullable
    private final HintLedger hintLedger;

    CraftsmanEyeLedgerManager(GuiCraftsmanEye gui, CraftsmanEyePageRenderer renderer) {
        this.gui = gui;
        this.renderer = renderer;
        this.maxWidth = Math.min(MAX_WIDTH,
            (gui.getScreenWidthValue() - gui.getPanelWidthValue()) / 2);
        this.animationSpeed = renderer.getLedgerAnimationSpeed();
        this.errorLedger = new ErrorLedger(this);
        this.hintLedger = createHintLedger();
    }

    void draw() {
        if (hintLedger != null) {
            hintLedger.update();
            hintLedger.draw(gui.getGuiLeftValue() + gui.getPanelWidthValue(),
                gui.getGuiTopValue() + 8);
        }

        errorLedger.setState(getErrorState());
        if (errorLedger.isVisible()) {
            errorLedger.update();
            errorLedger.draw(gui.getGuiLeftValue() - errorLedger.getWidth(),
                gui.getGuiTopValue() + 8);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    List<Rectangle> getLedgerAreas() {
        if (hintLedger == null || !hintLedger.isVisible()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new Rectangle(
            hintLedger.currentX, hintLedger.currentY,
            hintLedger.getWidth(), hintLedger.getHeight()));
    }

    boolean drawTooltip(int mouseX, int mouseY) {
        Ledger ledger = getAtPosition(mouseX, mouseY);
        if (ledger == null || ledger.getTooltip().isEmpty()) {
            return false;
        }
        GuiUtils.drawHoveringText(Collections.singletonList(ledger.getTooltip()),
            mouseX, mouseY, gui.getScreenWidthValue(), gui.getScreenHeightValue(),
            -1, gui.getPageFont());
        return true;
    }

    boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return false;
        }
        Ledger ledger = getAtPosition(mouseX, mouseY);
        if (ledger == null) {
            return false;
        }
        ledger.toggleOpen();
        return true;
    }

    @Nullable
    private HintLedger createHintLedger() {
        if (!renderer.areHintsEnabled()) {
            return null;
        }
        ItemStack specimen = gui.getActiveSpecimenForLedger();
        if (specimen.isEmpty()) {
            return null;
        }
        try {
            List<String> hints = renderer.getHints(specimen);
            return hints == null || hints.isEmpty() ? null
                : new HintLedger(this, new ArrayList<>(hints));
        } catch (RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    @Nullable
    private Ledger getAtPosition(int mouseX, int mouseY) {
        if (hintLedger != null && hintLedger.intersects(mouseX, mouseY)) {
            return hintLedger;
        }
        return errorLedger.isVisible() && errorLedger.intersects(mouseX, mouseY)
            ? errorLedger : null;
    }

    @Nullable
    private ErrorInfo getErrorState() {
        ItemStack specimen = gui.getActiveSpecimenForLedger();
        if (specimen.isEmpty()) {
            return new ErrorInfo(
                I18n.format("for.errors.no_specimen.desc"),
                I18n.format("for.errors.no_specimen.help"),
                NO_SPECIMEN_ICON, false);
        }

        ItemStack normalized = CraftsmanEyeGenetics.normalize(specimen);
        if (CraftsmanEyeGenetics.isAnalyzable(normalized)
            && !CraftsmanEyeGenetics.isAnalyzed(normalized)) {
            ItemStack staff = gui.getStaffForLedger();
            if (staff.isEmpty() || ToolHelper.getCurrentDurability(staff) <= 0) {
                return new ErrorInfo(
                    I18n.format("gui.tt2.craftsman_eye.error.no_durability.title"),
                    I18n.format("gui.tt2.craftsman_eye.error.no_durability.help"),
                    null, true);
            }
        }
        return null;
    }

    private int color(String key, int fallback) {
        return renderer.getColor(key, fallback);
    }

    private enum LedgerKind {
        ERROR,
        HINT
    }

    private abstract static class Ledger {
        final CraftsmanEyeLedgerManager manager;
        final LedgerKind kind;
        final int maxWidth;
        final int maxTextWidth;
        final int headerColor;
        final int textColor;
        final int overlayColor;
        final ResourceLocation texture;
        int maxHeight = MIN_SIZE;
        int currentX;
        int currentY;
        float currentWidth = MIN_SIZE;
        float currentHeight = MIN_SIZE;
        boolean open;
        long lastUpdateTime;

        Ledger(CraftsmanEyeLedgerManager manager, LedgerKind kind,
               String colorName, boolean rightSide) {
            this.manager = manager;
            this.kind = kind;
            this.maxWidth = manager.maxWidth;
            this.maxTextWidth = maxWidth - 18;
            this.headerColor = manager.color("ledger." + colorName + ".header", 0xE1C92F);
            this.textColor = manager.color("ledger." + colorName + ".text", 0x000000);
            this.overlayColor = manager.color("ledger." + colorName + ".background",
                kind == LedgerKind.ERROR ? 0xFF3535 : 0xEA38FF);
            this.texture = rightSide ? LEDGER_RIGHT : LEDGER_LEFT;
            if (openedLedger == kind) {
                setFullyOpen();
            }
        }

        void update() {
            long updateTime;
            if (lastUpdateTime == 0L) {
                lastUpdateTime = System.currentTimeMillis();
                updateTime = lastUpdateTime + Math.round(16.667F);
            } else {
                updateTime = System.currentTimeMillis();
            }
            float moveAmount = manager.animationSpeed * (updateTime - lastUpdateTime) / 16.667F;
            lastUpdateTime = updateTime;

            currentWidth = approach(currentWidth, open ? maxWidth : MIN_SIZE, moveAmount);
            currentHeight = approach(currentHeight, open ? maxHeight : MIN_SIZE, moveAmount);
        }

        void draw(int x, int y) {
            currentX = x;
            currentY = y;
            drawContents(x, y);
        }

        abstract void drawContents(int x, int y);

        abstract String getTooltip();

        boolean isVisible() {
            return true;
        }

        boolean intersects(int mouseX, int mouseY) {
            return mouseX >= currentX && mouseX <= currentX + currentWidth
                && mouseY >= currentY && mouseY <= currentY + getHeight();
        }

        int getWidth() {
            return Math.round(currentWidth);
        }

        int getHeight() {
            return Math.round(currentHeight);
        }

        boolean isFullyOpened() {
            return currentWidth >= maxWidth;
        }

        void toggleOpen() {
            open = !open;
            openedLedger = open ? kind : null;
        }

        void setFullyOpen() {
            open = true;
            currentWidth = maxWidth;
            currentHeight = maxHeight;
        }

        void drawBackground(int x, int y) {
            float red = (overlayColor >> 16 & 0xFF) / 255.0F;
            float green = (overlayColor >> 8 & 0xFF) / 255.0F;
            float blue = (overlayColor & 0xFF) / 255.0F;
            GlStateManager.color(red, green, blue, 1.0F);
            manager.gui.bindLedgerTexture(texture);
            int height = getHeight();
            int width = getWidth();
            manager.gui.drawLedgerTextureRegion(x, y + 4,
                0, 256 - height + 4, 4, height - 4);
            manager.gui.drawLedgerTextureRegion(x + 4, y,
                256 - width + 4, 0, width - 4, 4);
            manager.gui.drawLedgerTextureRegion(x, y, 0, 0, 4, 4);
            manager.gui.drawLedgerTextureRegion(x + 4, y + 4,
                256 - width + 4, 256 - height + 4, width - 4, height - 4);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        int drawHeader(String text, int x, int y) {
            return drawSplitText(text, x, y, maxTextWidth, headerColor, true);
        }

        int drawSplitText(String text, int x, int y, int width) {
            return drawSplitText(text, x, y, width, textColor, false);
        }

        int drawSplitText(String text, int x, int y, int width,
                          int color, boolean shadow) {
            int originalY = y;
            FontRenderer font = manager.gui.getPageFont();
            for (String line : font.listFormattedStringToWidth(text, width)) {
                font.drawString(line, x, y, color, shadow);
                y += font.FONT_HEIGHT;
            }
            return y - originalY;
        }

        private static float approach(float current, float target, float amount) {
            if (current < target) {
                return Math.min(target, current + amount);
            }
            if (current > target) {
                return Math.max(target, current - amount);
            }
            return current;
        }
    }

    private static final class HintLedger extends Ledger {
        private final String hintText;
        private final String hintTooltip;

        HintLedger(CraftsmanEyeLedgerManager manager, List<String> hints) {
            super(manager, LedgerKind.HINT, "hint", true);
            String hint = hints.get(new Random().nextInt(hints.size()));
            this.hintText = I18n.format("for.hints." + hint + ".desc");
            this.hintTooltip = I18n.format("for.hints." + hint + ".tag");
            int lineCount = manager.gui.getPageFont()
                .listFormattedStringToWidth(hintText, maxTextWidth).size();
            this.maxHeight = (lineCount + 1) * manager.gui.getPageFont().FONT_HEIGHT + 20;
            if (openedLedger == LedgerKind.HINT) {
                setFullyOpen();
            }
        }

        @Override
        void drawContents(int x, int y) {
            drawBackground(x, y);
            manager.gui.drawLedgerIcon(HINT_ICON, x + 3, y + 4);
            if (isFullyOpened()) {
                drawHeader(I18n.format("for.gui.didyouknow") + '?', x + 22, y + 8);
                drawSplitText(hintText, x + 12, y + 20, maxTextWidth);
            }
        }

        @Override
        String getTooltip() {
            return hintTooltip;
        }
    }

    private static final class ErrorLedger extends Ledger {
        @Nullable
        private ErrorInfo state;

        ErrorLedger(CraftsmanEyeLedgerManager manager) {
            super(manager, LedgerKind.ERROR, "error", false);
            this.maxHeight = 72;
            this.open = false;
            this.currentWidth = MIN_SIZE;
            this.currentHeight = MIN_SIZE;
        }

        void setState(@Nullable ErrorInfo state) {
            this.state = state;
            if (state != null) {
                FontRenderer font = manager.gui.getPageFont();
                int lineCount = font.listFormattedStringToWidth(state.title, maxTextWidth).size()
                    + font.listFormattedStringToWidth(state.help, maxTextWidth).size();
                this.maxHeight = lineCount * font.FONT_HEIGHT + 20;
            }
        }

        @Override
        void drawContents(int x, int y) {
            if (state == null) {
                return;
            }
            drawBackground(x, y);
            y += 4;
            if (state.staffIcon) {
                manager.gui.drawLedgerStaffErrorIcon(x + 5, y);
            } else if (state.icon != null) {
                manager.gui.drawLedgerIcon(state.icon, x + 5, y);
            }
            y += 4;
            if (isFullyOpened()) {
                y += drawHeader(state.title, x + 24, y);
                y += 4;
                drawSplitText(state.help, x + 14, y, maxTextWidth);
            }
        }

        @Override
        String getTooltip() {
            return state == null ? "" : state.title;
        }

        @Override
        boolean isVisible() {
            return state != null;
        }
    }

    private static final class ErrorInfo {
        final String title;
        final String help;
        @Nullable
        final ResourceLocation icon;
        final boolean staffIcon;

        ErrorInfo(String title, String help, @Nullable ResourceLocation icon, boolean staffIcon) {
            this.title = title;
            this.help = help;
            this.icon = icon;
            this.staffIcon = staffIcon;
        }
    }
}
