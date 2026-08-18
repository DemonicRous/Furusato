package com.demonicrous.furusato.client;

import com.demonicrous.furusato.asm.FurusatoEarlyConfig;
import com.demonicrous.furusato.asm.PatchDiagnostics;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Mouse;

/** Entry point for Furusato settings categories. */
public final class GuiFurusatoConfigHub extends GuiFurusatoScreen {
    private static final int GENERAL_SETTINGS = 9;
    private static final int FONT_SETTINGS = 10;
    private static final int DIAGNOSTICS = 11;
    private static final int UI_SHOWCASE = 12;
    private static final int EFFECTS_SETTINGS = 13;
    private static final int DONE = 200;

    private final GuiScreen parentScreen;
    private GuiResponsiveButton generalSettingsButton;
    private GuiResponsiveButton fontSettingsButton;
    private GuiResponsiveButton diagnosticsButton;
    private GuiResponsiveButton uiShowcaseButton;
    private GuiResponsiveButton effectsSettingsButton;
    private GuiButton doneButton;
    private List<GuiResponsiveButton> categoryButtons = Collections.emptyList();
    private final SmoothScrollState scroll = new SmoothScrollState();
    private final GuiFurusatoScrollBar scrollBar = new GuiFurusatoScrollBar(scroll);
    private long lastFrameNanos = System.nanoTime();

    public GuiFurusatoConfigHub(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        generalSettingsButton = addButton(new GuiResponsiveButton(
                GENERAL_SETTINGS, center - 100, 0, 200, 20, ""));
        generalSettingsButton.setFullText(
                fontRenderer, I18n.format("furusato.config.category.general"));
        fontSettingsButton = addButton(new GuiResponsiveButton(
                FONT_SETTINGS, center - 100, 0, 200, 20, ""));
        fontSettingsButton.setFullText(
                fontRenderer, I18n.format(FurusatoEarlyConfig.isRestartPending()
                        ? "furusato.config.category.font.restart"
                        : "furusato.config.category.font"));
        effectsSettingsButton = addButton(new GuiResponsiveButton(
                EFFECTS_SETTINGS, center - 100, 0, 200, 20, ""));
        effectsSettingsButton.setFullText(
                fontRenderer, I18n.format("furusato.config.category.effects"));
        diagnosticsButton = addButton(new GuiResponsiveButton(
                DIAGNOSTICS, center - 100, 0, 200, 20, ""));
        boolean warning = FurusatoEarlyConfig.isSafeModeEnabled()
                || PatchDiagnostics.hasWarnings();
        diagnosticsButton.setFullText(
                fontRenderer, I18n.format(warning
                        ? "furusato.config.category.diagnostics.warning"
                        : "furusato.config.category.diagnostics"));
        uiShowcaseButton = addButton(new GuiResponsiveButton(
                UI_SHOWCASE, center - 100, 0, 200, 20, ""));
        uiShowcaseButton.setFullText(
                fontRenderer, I18n.format("furusato.config.category.ui"));
        categoryButtons = Arrays.asList(generalSettingsButton, fontSettingsButton,
                effectsSettingsButton, diagnosticsButton, uiShowcaseButton);
        doneButton = addButton(new GuiButton(DONE, center - 100, height - 27,
                200, 20, I18n.format("gui.done")));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) {
            return;
        }
        if (button.id == GENERAL_SETTINGS) {
            mc.displayGuiScreen(new GuiGeneralSettings(this));
        } else if (button.id == FONT_SETTINGS) {
            mc.displayGuiScreen(new GuiFontSettings(this));
        } else if (button.id == EFFECTS_SETTINGS) {
            mc.displayGuiScreen(new GuiEffectsSettings(this));
        } else if (button.id == DIAGNOSTICS) {
            mc.displayGuiScreen(new GuiDiagnostics(this));
        } else if (button.id == UI_SHOWCASE) {
            mc.displayGuiScreen(new GuiUiShowcase(this));
        } else if (button.id == DONE) {
            mc.displayGuiScreen(parentScreen);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, I18n.format("furusato.config.title"),
                width / 2, 24, 0xFFFFFF);
        drawCenteredLines(I18n.format("furusato.config.subtitle"), 48, 0xA0A0A0);
        int viewportTop = viewportTop();
        int viewportHeight = viewportHeight(viewportTop);
        int capacity = viewportCapacity(viewportHeight);
        scroll.setMaximum(Math.max(0, categoryButtons.size() - capacity));
        long now = System.nanoTime();
        double displayed = scroll.update((now - lastFrameNanos) / 1.0E9D);
        lastFrameNanos = now;
        int viewportBottom = viewportTop + viewportHeight;
        beginScissor(width / 2 - 106, viewportTop,
                width / 2 + 116, viewportBottom);
        for (int index = 0; index < categoryButtons.size(); index++) {
            GuiResponsiveButton button = categoryButtons.get(index);
            button.y = viewportTop + (int) Math.round((index - displayed) * 30.0D);
            button.visible = button.y + button.height > viewportTop
                    && button.y < viewportBottom;
            if (button.visible) {
                button.drawButton(mc, mouseX, mouseY, partialTicks);
            }
        }
        endScissor();
        doneButton.drawButton(mc, mouseX, mouseY, partialTicks);
        scrollBar.setBounds(width / 2 + 106, viewportTop,
                viewportHeight, categoryButtons.size(), capacity);
        scrollBar.draw(mc, mouseX, mouseY);
        drawButtonTooltip(generalSettingsButton, mouseX, mouseY);
        drawButtonTooltip(fontSettingsButton, mouseX, mouseY);
        drawButtonTooltip(effectsSettingsButton, mouseX, mouseY);
        drawButtonTooltip(diagnosticsButton, mouseX, mouseY);
        drawButtonTooltip(uiShowcaseButton, mouseX, mouseY);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
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

    private int viewportTop() {
        return Math.min(64, Math.max(50, height - 166));
    }

    private int viewportHeight(int top) {
        return Math.max(30, Math.min(110, height - top - 39));
    }

    private int viewportCapacity(int viewportHeight) {
        return Math.max(1, Math.min(4, (viewportHeight + 10) / 30));
    }

    private void drawCenteredLines(String text, int top, int color) {
        int lineWidth = Math.max(40, width - 30);
        List<String> lines = fontRenderer.listFormattedStringToWidth(text, lineWidth);
        for (int index = 0; index < lines.size(); index++) {
            drawCenteredString(fontRenderer, lines.get(index),
                    width / 2, top + index * fontRenderer.FONT_HEIGHT, color);
        }
    }

    private void drawButtonTooltip(GuiResponsiveButton button, int mouseX, int mouseY) {
        if (button != null && button.hasHiddenText() && button.isMouseOver(mouseX, mouseY)) {
            List<String> lines = fontRenderer.listFormattedStringToWidth(
                    button.getFullText(), Math.max(80, width - 40));
            drawHoveringText(lines.isEmpty()
                    ? Collections.singletonList(button.getFullText()) : lines, mouseX, mouseY);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
