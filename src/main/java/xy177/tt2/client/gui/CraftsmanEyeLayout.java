package xy177.tt2.client.gui;

/** Text layout matching Forestry's portable analyzer. */
final class CraftsmanEyeLayout {

    private static final int LINE_HEIGHT = 12;

    private final GuiCraftsmanEye gui;
    private int defaultColor;
    int column0;
    int column1;
    int column2;
    int line;

    CraftsmanEyeLayout(GuiCraftsmanEye gui) {
        this.gui = gui;
    }

    void startPage(int defaultColor) {
        this.defaultColor = defaultColor;
        this.line = 12;
    }

    void startPage(int column0, int column1, int column2, int defaultColor) {
        this.column0 = column0;
        this.column1 = column1;
        this.column2 = column2;
        startPage(defaultColor);
    }

    int getLineY() {
        return line;
    }

    void newLine() {
        line += LINE_HEIGHT;
    }

    void newLineCompressed() {
        line += 10;
    }

    void newLine(int height) {
        line += height;
    }

    void drawRow(String text0, String text1, String text2, int color0, int color1, int color2) {
        drawLine(text0, column0, color0);
        drawLine(text1, column1, color1);
        drawLine(text2, column2, color2);
    }

    void drawLine(String text, int x) {
        drawLine(text, x, defaultColor);
    }

    void drawLine(String text, int x, int color) {
        gui.getPageFont().drawString(text, gui.getGuiLeftValue() + x,
            gui.getGuiTopValue() + line, color);
    }

    void drawSplitLine(String text, int x, int maxWidth, int color) {
        gui.getPageFont().drawSplitString(text, gui.getGuiLeftValue() + x,
            gui.getGuiTopValue() + line, maxWidth, color);
    }

    void drawCenteredLine(String text, int x, int width, int color) {
        int offset = (width - gui.getPageFont().getStringWidth(text)) / 2;
        drawLine(text, x + offset, color);
    }
}
