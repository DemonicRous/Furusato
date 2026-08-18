package com.demonicrous.furusato.client;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Mouse;

/** Interactive, non-persistent preview of Furusato's reusable UI controls. */
public final class GuiUiShowcase extends GuiFurusatoScreen {
    private static final int TOGGLE = 10;
    private static final int SLIDER = 11;
    private static final int DONE = 200;
    private static final int ROW_HEIGHT = 16;
    private static final int VISIBLE_ROWS = 4;
    private static final int SAMPLE_ROWS = 8;

    private final GuiScreen parentScreen;
    private boolean toggleEnabled = true;
    private final SmoothScrollState scroll = new SmoothScrollState();
    private final GuiFurusatoScrollBar scrollBar = new GuiFurusatoScrollBar(scroll);
    private long lastFrameNanos = System.nanoTime();
    private GuiResponsiveButton toggleButton;
    private GuiFurusatoSlider slider;

    public GuiUiShowcase(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int panelWidth = responsiveContentWidth(280, 420, 16);
        int left = (width - panelWidth) / 2;
        int top = Math.max(20, (height - 224) / 2);
        toggleButton = addButton(new GuiResponsiveButton(
                TOGGLE, left + 14, top + 48, panelWidth - 28, 20, ""));
        slider = addButton(new GuiFurusatoSlider(
                SLIDER, left + 14, top + 82, panelWidth - 28, 0.65D));
        addButton(new GuiButton(DONE, width / 2 - 100,
                Math.min(height - 27, top + 204), 200, 20, I18n.format("gui.done")));
        refreshToggle();
    }

    private void refreshToggle() {
        toggleButton.setFullText(fontRenderer, I18n.format(
                "furusato.ui.toggle", I18n.format(toggleEnabled
                        ? "options.on" : "options.off")));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == TOGGLE) {
            toggleEnabled = !toggleEnabled;
            refreshToggle();
        } else if (button.id == DONE) {
            mc.displayGuiScreen(parentScreen);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int panelWidth = responsiveContentWidth(280, 420, 16);
        int left = (width - panelWidth) / 2;
        int top = Math.max(20, (height - 224) / 2);
        drawFurusatoPanel(left + 6, top + 6, panelWidth - 12, 182);
        drawCenteredString(fontRenderer, I18n.format("furusato.ui.title"),
                width / 2, top + 12, 0xFFFFFF);
        drawCenteredString(fontRenderer, I18n.format("furusato.ui.subtitle"),
                width / 2, top + 27, 0xA0A0A0);

        drawString(fontRenderer, I18n.format("furusato.ui.slider"),
                left + 18, top + 71, 0xA0A0A0);
        drawScrollablePreview(left + 14, top + 112,
                panelWidth - 28, mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawScrollablePreview(int left, int top, int panelWidth,
            int mouseX, int mouseY) {
        int panelHeight = VISIBLE_ROWS * ROW_HEIGHT + 8;
        scroll.setMaximum(SAMPLE_ROWS - VISIBLE_ROWS);
        long now = System.nanoTime();
        double displayedOffset = scroll.update((now - lastFrameNanos) / 1.0E9D);
        lastFrameNanos = now;
        drawRect(left, top, left + panelWidth, top + panelHeight, 0x70000000);
        drawRect(left, top, left + panelWidth, top + 1, 0x405000FF);
        int firstRow = Math.max(0, (int) Math.floor(displayedOffset));
        int lastRow = Math.min(SAMPLE_ROWS, firstRow + VISIBLE_ROWS + 2);
        beginScissor(left, top + 4, left + panelWidth - 7,
                top + 4 + VISIBLE_ROWS * ROW_HEIGHT);
        for (int itemIndex = firstRow; itemIndex < lastRow; itemIndex++) {
            int rowTop = top + 4 + (int) Math.round(
                    (itemIndex - displayedOffset) * ROW_HEIGHT);
            boolean hovered = mouseX >= left && mouseX < left + panelWidth
                    && mouseY >= rowTop && mouseY < rowTop + ROW_HEIGHT;
            if (hovered) {
                drawRect(left + 3, rowTop, left + panelWidth - 8,
                        rowTop + ROW_HEIGHT, 0x22FFFFFF);
            }
            drawString(fontRenderer, I18n.format("furusato.ui.row", itemIndex + 1),
                    left + 8, rowTop + 4, hovered ? 0xFFFFFF : 0xB0B0B0);
        }
        endScissor();
        int trackTop = top + 4;
        int trackHeight = VISIBLE_ROWS * ROW_HEIGHT;
        scrollBar.setBounds(left + panelWidth - 16, trackTop,
                trackHeight, SAMPLE_ROWS, VISIBLE_ROWS);
        scrollBar.draw(mc, mouseX, mouseY);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scroll.setMaximum(SAMPLE_ROWS - VISIBLE_ROWS);
            scroll.scrollBy(wheel < 0 ? 1.0D : -1.0D);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
            throws IOException {
        if (mouseButton == 0 && scrollBar.mousePressed(mouseX, mouseY)) {
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton,
            long timeSinceLastClick) {
        if (clickedMouseButton == 0) {
            scrollBar.mouseDragged(mouseY);
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        scrollBar.mouseReleased();
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parentScreen);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
