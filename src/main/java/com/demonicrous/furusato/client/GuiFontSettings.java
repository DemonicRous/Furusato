package com.demonicrous.furusato.client;

import com.demonicrous.furusato.asm.FurusatoEarlyConfig;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

/** Font and GUI scale settings inspired by modern Minecraft versions. */
public final class GuiFontSettings extends GuiScreen {
    private static final int NEXT_SCALE = 11;
    private static final int FORCE_UNICODE = 12;
    private static final int PRESERVE_ODD_SCALE = 13;
    private static final int DONE = 200;

    private final GuiScreen parentScreen;
    private GuiResponsiveButton scaleButton;
    private GuiResponsiveButton unicodeButton;
    private GuiResponsiveButton patchButton;
    private List<String> scaleHelp = Collections.emptyList();
    private List<String> unicodeHelp = Collections.emptyList();
    private List<String> patchHelp = Collections.emptyList();

    public GuiFontSettings(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        int top = 42;

        scaleButton = addButton(new GuiResponsiveButton(
                NEXT_SCALE, center - 100, top, 200, 20, ""));
        unicodeButton = addButton(new GuiResponsiveButton(
                FORCE_UNICODE, center - 100, top + 30, 200, 20, ""));
        patchButton = addButton(new GuiResponsiveButton(
                PRESERVE_ODD_SCALE, center - 100, top + 60, 200, 20, ""));
        addButton(new GuiButton(DONE, center - 100, height - 27, 200, 20,
                I18n.format("gui.done")));
        refreshLabels();
        int helpWidth = Math.max(80, Math.min(260, width - 30));
        scaleHelp = fontRenderer.listFormattedStringToWidth(
                I18n.format("furusato.font.help.scale"), helpWidth);
        unicodeHelp = fontRenderer.listFormattedStringToWidth(
                I18n.format("furusato.font.help.unicode"), helpWidth);
        patchHelp = fontRenderer.listFormattedStringToWidth(
                I18n.format("furusato.font.help.oddScale"), helpWidth);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) {
            return;
        }

        GameSettings settings = mc.gameSettings;
        if (button.id == NEXT_SCALE) {
            settings.guiScale = GuiScalePolicy.next(settings.guiScale);
            applyScale();
        } else if (button.id == FORCE_UNICODE) {
            settings.forceUnicodeFont = !settings.forceUnicodeFont;
            mc.fontRenderer.setUnicodeFlag(mc.getLanguageManager().isCurrentLocaleUnicode()
                    || settings.forceUnicodeFont);
            settings.saveOptions();
            refreshLabels();
        } else if (button.id == PRESERVE_ODD_SCALE) {
            FurusatoEarlyConfig.setUnicodeGuiScaleEnabled(
                    !FurusatoEarlyConfig.isUnicodeGuiScaleEnabled());
            refreshLabels();
        } else if (button.id == DONE) {
            settings.saveOptions();
            mc.displayGuiScreen(parentScreen);
        }
    }

    private void applyScale() {
        mc.gameSettings.saveOptions();
        ScaledResolution resolution = new ScaledResolution(mc);
        setWorldAndResolution(mc, resolution.getScaledWidth(), resolution.getScaledHeight());
    }

    private void refreshLabels() {
        if (scaleButton == null || unicodeButton == null || patchButton == null) {
            return;
        }
        int selected = GuiScalePolicy.normalize(mc.gameSettings.guiScale);
        String selectedText = I18n.format("furusato.font.scale." + selected);
        scaleButton.setFullText(fontRenderer, I18n.format(
                "furusato.font.guiScale.short", selectedText));
        unicodeButton.setFullText(fontRenderer, I18n.format(
                "furusato.font.forceUnicode.short",
                I18n.format(mc.gameSettings.forceUnicodeFont ? "options.on" : "options.off")));
        patchButton.setFullText(fontRenderer, I18n.format(
                "furusato.font.preserveOddScale.short",
                I18n.format(FurusatoEarlyConfig.isUnicodeGuiScaleEnabled()
                        ? "options.on" : "options.off")));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, I18n.format("furusato.font.title"),
                width / 2, 18, 0xFFFFFF);

        int previewTop = 145;
        drawRect(width / 2 - 100, previewTop, width / 2 + 100,
                previewTop + 38, 0x88000000);
        drawCenteredString(fontRenderer, I18n.format(
                        "furusato.font.previewMode",
                        I18n.format(mc.gameSettings.forceUnicodeFont
                                ? "furusato.font.mode.unicode"
                                : "furusato.font.mode.default")),
                width / 2, previewTop + 7, 0xA0A0A0);
        drawCenteredString(fontRenderer, I18n.format("furusato.font.sample"),
                width / 2, previewTop + 21, 0xFFFFFF);

        drawCenteredLines(fontRenderer.listFormattedStringToWidth(
                        I18n.format("furusato.font.restartHint"),
                        Math.max(40, width - 30)),
                193, 0x808080);
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawHelpTooltip(mouseX, mouseY);
    }

    private void drawCenteredLines(String text, int top, int color) {
        drawCenteredLines(fontRenderer.listFormattedStringToWidth(
                text, Math.max(40, width - 30)), top, color);
    }

    private void drawCenteredLines(List<String> lines, int top, int color) {
        for (int index = 0; index < lines.size(); index++) {
            drawCenteredString(fontRenderer, lines.get(index),
                    width / 2, top + index * fontRenderer.FONT_HEIGHT, color);
        }
    }

    private void drawHelpTooltip(int mouseX, int mouseY) {
        if (scaleButton != null && scaleButton.isMouseOver(mouseX, mouseY)) {
            drawHoveringText(scaleHelp, mouseX, mouseY);
        } else if (unicodeButton != null && unicodeButton.isMouseOver(mouseX, mouseY)) {
            drawHoveringText(unicodeHelp, mouseX, mouseY);
        } else if (patchButton != null && patchButton.isMouseOver(mouseX, mouseY)) {
            drawHoveringText(patchHelp, mouseX, mouseY);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.gameSettings.saveOptions();
            mc.displayGuiScreen(parentScreen);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        mc.gameSettings.saveOptions();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
