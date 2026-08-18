package com.demonicrous.furusato.client;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

/** Entry point for Furusato settings categories. */
public final class GuiFurusatoConfigHub extends GuiScreen {
    private static final int FONT_SETTINGS = 10;
    private static final int DONE = 200;

    private final GuiScreen parentScreen;
    private GuiResponsiveButton fontSettingsButton;

    public GuiFurusatoConfigHub(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        fontSettingsButton = addButton(new GuiResponsiveButton(
                FONT_SETTINGS, center - 100, height / 2 - 10, 200, 20, ""));
        fontSettingsButton.setFullText(
                fontRenderer, I18n.format("furusato.config.category.font"));
        addButton(new GuiButton(DONE, center - 100, height - 27, 200, 20,
                I18n.format("gui.done")));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) {
            return;
        }
        if (button.id == FONT_SETTINGS) {
            mc.displayGuiScreen(new GuiFontSettings(this));
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
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawButtonTooltip(fontSettingsButton, mouseX, mouseY);
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
