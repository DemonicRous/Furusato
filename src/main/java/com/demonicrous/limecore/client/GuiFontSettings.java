package com.demonicrous.limecore.client;

import com.demonicrous.limecore.asm.LimeCoreEarlyConfig;
import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;

/** Font and GUI scale settings inspired by modern Minecraft versions. */
public final class GuiFontSettings extends GuiScreen {
    private static final int PREVIOUS_SCALE = 10;
    private static final int NEXT_SCALE = 11;
    private static final int FORCE_UNICODE = 12;
    private static final int PRESERVE_ODD_SCALE = 13;
    private static final int DONE = 200;

    private final GuiScreen parentScreen;
    private GuiButton scaleButton;
    private GuiButton unicodeButton;
    private GuiButton patchButton;

    public GuiFontSettings(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        int top = height / 2 - 55;

        addButton(new GuiButton(PREVIOUS_SCALE, center - 100, top, 20, 20, "<"));
        scaleButton = addButton(new GuiButton(NEXT_SCALE, center - 75, top, 175, 20, ""));
        unicodeButton = addButton(new GuiButton(
                FORCE_UNICODE, center - 100, top + 28, 200, 20, ""));
        patchButton = addButton(new GuiButton(
                PRESERVE_ODD_SCALE, center - 100, top + 56, 200, 20, ""));
        addButton(new GuiButton(DONE, center - 100, height - 27, 200, 20,
                I18n.format("gui.done")));
        refreshLabels();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) {
            return;
        }

        GameSettings settings = mc.gameSettings;
        if (button.id == PREVIOUS_SCALE) {
            settings.guiScale = GuiScalePolicy.previous(settings.guiScale);
            applyScale();
        } else if (button.id == NEXT_SCALE) {
            settings.guiScale = GuiScalePolicy.next(settings.guiScale);
            applyScale();
        } else if (button.id == FORCE_UNICODE) {
            settings.forceUnicodeFont = !settings.forceUnicodeFont;
            mc.fontRenderer.setUnicodeFlag(mc.getLanguageManager().isCurrentLocaleUnicode()
                    || settings.forceUnicodeFont);
            settings.saveOptions();
            refreshLabels();
        } else if (button.id == PRESERVE_ODD_SCALE) {
            LimeCoreEarlyConfig.setUnicodeGuiScaleEnabled(
                    !LimeCoreEarlyConfig.isUnicodeGuiScaleEnabled());
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
        String selectedText = selected == GuiScalePolicy.AUTO
                ? I18n.format("options.guiScale.auto")
                : Integer.toString(selected);
        int effective = new ScaledResolution(mc).getScaleFactor();
        scaleButton.displayString = I18n.format(
                "limecore.font.guiScale", selectedText, effective);
        unicodeButton.displayString = I18n.format(
                "limecore.font.forceUnicode",
                I18n.format(mc.gameSettings.forceUnicodeFont ? "options.on" : "options.off"));
        patchButton.displayString = I18n.format(
                "limecore.font.preserveOddScale",
                I18n.format(LimeCoreEarlyConfig.isUnicodeGuiScaleEnabled()
                        ? "options.on" : "options.off"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, I18n.format("limecore.font.title"),
                width / 2, height / 2 - 90, 0xFFFFFF);

        int previewTop = height / 2 + 22;
        drawRect(width / 2 - 100, previewTop, width / 2 + 100,
                previewTop + 38, 0x88000000);
        drawCenteredString(fontRenderer, I18n.format("limecore.font.preview"),
                width / 2, previewTop + 7, 0xA0A0A0);
        drawCenteredString(fontRenderer, I18n.format("limecore.font.sample"),
                width / 2, previewTop + 21, 0xFFFFFF);

        drawCenteredString(fontRenderer, I18n.format("limecore.font.restartHint"),
                width / 2, previewTop + 43, 0x808080);
        super.drawScreen(mouseX, mouseY, partialTicks);
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
