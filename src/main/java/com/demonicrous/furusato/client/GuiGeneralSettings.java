package com.demonicrous.furusato.client;

import com.demonicrous.furusato.asm.FurusatoEarlyConfig;
import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

/** General Furusato configuration with explicit save feedback. */
public final class GuiGeneralSettings extends GuiFurusatoScreen {
    private static final int DIAGNOSTIC_LOGGING = 10;
    private static final int SAFE_MODE = 11;
    private static final int RESET_DEFAULTS = 12;
    private static final int REVERT_PENDING = 13;
    private static final int DONE = 200;

    private final GuiScreen parentScreen;
    private GuiResponsiveButton loggingButton;
    private GuiResponsiveButton safeModeButton;
    private GuiResponsiveButton resetButton;
    private GuiResponsiveButton revertButton;
    private boolean confirmReset;
    private boolean restartRequired;
    private String statusKey;
    private int statusColor = 0xA0A0A0;

    public GuiGeneralSettings(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int center = width / 2;
        int top = height / 2 - 70;
        loggingButton = addButton(new GuiResponsiveButton(
                DIAGNOSTIC_LOGGING, center - 100, top, 200, 20, ""));
        safeModeButton = addButton(new GuiResponsiveButton(
                SAFE_MODE, center - 100, top + 30, 200, 20, ""));
        safeModeButton.enabled = false;
        resetButton = addButton(new GuiResponsiveButton(
                RESET_DEFAULTS, center - 100, top + 60, 200, 20, ""));
        revertButton = addButton(new GuiResponsiveButton(
                REVERT_PENDING, center - 100, top + 90, 200, 20, ""));
        addButton(new GuiButton(DONE, center - 100, height - 27, 200, 20,
                I18n.format("gui.done")));
        refreshLabels();
    }

    private void refreshLabels() {
        loggingButton.setFullText(fontRenderer, I18n.format(
                "furusato.general.logging",
                I18n.format(FurusatoEarlyConfig.isDiagnosticLoggingEnabled()
                        ? "options.on" : "options.off")));
        safeModeButton.setFullText(fontRenderer, I18n.format(
                "furusato.general.safeMode",
                I18n.format(FurusatoEarlyConfig.isSafeModeEnabled()
                        ? "options.on" : "options.off")));
        resetButton.setFullText(fontRenderer, I18n.format(confirmReset
                ? "furusato.general.reset.confirm" : "furusato.general.reset"));
        revertButton.setFullText(fontRenderer,
                I18n.format("furusato.general.revertPending"));
        revertButton.enabled = FurusatoEarlyConfig.isRestartPending();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) {
            return;
        }
        if (button.id == DIAGNOSTIC_LOGGING) {
            boolean enabled = !FurusatoEarlyConfig.isDiagnosticLoggingEnabled();
            showSaveResult(FurusatoEarlyConfig.setDiagnosticLoggingEnabled(enabled));
            confirmReset = false;
            refreshLabels();
        } else if (button.id == RESET_DEFAULTS) {
            if (!confirmReset) {
                confirmReset = true;
                refreshLabels();
                return;
            }
            boolean patchWasDisabled = !FurusatoEarlyConfig.isUnicodeGuiScaleEnabled();
            boolean saved = FurusatoEarlyConfig.resetDefaults();
            restartRequired |= saved && patchWasDisabled
                    && FurusatoEarlyConfig.unicodeGuiScaleRequiresRestart();
            confirmReset = false;
            showSaveResult(saved);
            refreshLabels();
        } else if (button.id == REVERT_PENDING) {
            boolean saved = FurusatoEarlyConfig.revertPendingUnicodeGuiScaleChange();
            showSaveResult(saved);
            restartRequired = FurusatoEarlyConfig.isRestartPending();
            refreshLabels();
        } else if (button.id == DONE) {
            mc.displayGuiScreen(parentScreen);
        }
    }

    private void showSaveResult(boolean saved) {
        statusKey = saved ? "furusato.general.saved" : "furusato.general.saveFailed";
        statusColor = saved ? 0x55FF55 : 0xFF5555;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, I18n.format("furusato.general.title"),
                width / 2, height / 2 - 88, 0xFFFFFF);
        if (statusKey != null) {
            drawCenteredString(fontRenderer, I18n.format(statusKey),
                    width / 2, height / 2 + 45, statusColor);
        }
        if (restartRequired || FurusatoEarlyConfig.isRestartPending()) {
            drawCenteredString(fontRenderer, I18n.format("furusato.general.restart"),
                    width / 2, height / 2 + 58, 0xFFAA00);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
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
