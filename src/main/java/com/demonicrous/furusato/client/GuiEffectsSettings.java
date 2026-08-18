package com.demonicrous.furusato.client;

import com.demonicrous.furusato.asm.FurusatoEarlyConfig;
import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

/** Runtime settings for Furusato HUD, container and blur effects. */
public final class GuiEffectsSettings extends GuiFurusatoScreen {
    private static final int HOTBAR = 10;
    private static final int HOTBAR_PULSE = 11;
    private static final int HOTBAR_DURATION = 12;
    private static final int CONTAINERS = 13;
    private static final int CONTAINER_DURATION = 14;
    private static final int BLUR = 15;
    private static final int BLUR_RADIUS = 16;
    private static final int DONE = 200;

    private final GuiScreen parentScreen;
    private GuiFurusatoToggle hotbarToggle;
    private GuiFurusatoToggle pulseToggle;
    private GuiFurusatoSlider hotbarDuration;
    private GuiFurusatoToggle containerToggle;
    private GuiFurusatoSlider containerDuration;
    private GuiFurusatoToggle blurToggle;
    private GuiFurusatoSlider blurRadius;
    private String statusKey;
    private int statusColor = 0xA0A0A0;

    public GuiEffectsSettings(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int panelWidth = responsiveContentWidth(300, 500, 16);
        int left = (width - panelWidth) / 2;
        int gap = 18;
        int columnWidth = (panelWidth - 28 - gap) / 2;
        int first = left + 14;
        int second = first + columnWidth + gap;
        int top = Math.max(42, height / 2 - 76);

        hotbarToggle = addButton(new GuiFurusatoToggle(HOTBAR, first, top,
                columnWidth, FurusatoEarlyConfig.isHotbarAnimationEnabled()));
        pulseToggle = addButton(new GuiFurusatoToggle(HOTBAR_PULSE, first,
                top + 30, columnWidth, FurusatoEarlyConfig.isHotbarPulseEnabled()));
        hotbarDuration = addButton(new GuiFurusatoSlider(HOTBAR_DURATION, first,
                top + 68, columnWidth, normalize(
                        FurusatoEarlyConfig.getHotbarDurationMillis(), 50, 180)));

        containerToggle = addButton(new GuiFurusatoToggle(CONTAINERS, second,
                top, columnWidth, FurusatoEarlyConfig.isContainerAnimationEnabled()));
        containerDuration = addButton(new GuiFurusatoSlider(CONTAINER_DURATION,
                second, top + 38, columnWidth, normalize(
                        FurusatoEarlyConfig.getContainerDurationMillis(), 80, 400)));
        blurToggle = addButton(new GuiFurusatoToggle(BLUR, second, top + 68,
                columnWidth, FurusatoEarlyConfig.isContainerBlurEnabled()));
        blurRadius = addButton(new GuiFurusatoSlider(BLUR_RADIUS, second,
                top + 106, columnWidth, normalize(
                        FurusatoEarlyConfig.getBlurRadius(), 2, 12)));
        addButton(new GuiButton(DONE, width / 2 - 100, height - 27, 200, 20,
                I18n.format("gui.done")));
        refreshLabels();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == HOTBAR || button.id == HOTBAR_PULSE
                || button.id == CONTAINERS || button.id == BLUR) {
            saveSettings();
            refreshLabels();
        } else if (button.id == DONE) {
            mc.displayGuiScreen(parentScreen);
        }
    }

    private void refreshLabels() {
        hotbarToggle.displayString = I18n.format("furusato.effects.hotbar",
                onOff(hotbarToggle.getValue()));
        pulseToggle.displayString = I18n.format("furusato.effects.hotbarPulse",
                onOff(pulseToggle.getValue()));
        containerToggle.displayString = I18n.format("furusato.effects.containers",
                onOff(containerToggle.getValue()));
        blurToggle.displayString = I18n.format("furusato.effects.blur",
                onOff(blurToggle.getValue()));
    }

    private String onOff(boolean enabled) {
        return I18n.format(enabled ? "options.on" : "options.off");
    }

    private void saveSettings() {
        boolean saved = FurusatoEarlyConfig.setEffectsSettings(
                hotbarToggle.getValue(), pulseToggle.getValue(),
                denormalize(hotbarDuration.getValue(), 50, 180),
                containerToggle.getValue(),
                denormalize(containerDuration.getValue(), 80, 400),
                blurToggle.getValue(),
                denormalize(blurRadius.getValue(), 2, 12));
        statusKey = saved ? "furusato.general.saved"
                : "furusato.general.saveFailed";
        statusColor = saved ? 0x55FF55 : 0xFF5555;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int top = Math.max(42, height / 2 - 76);
        drawCenteredString(fontRenderer, I18n.format("furusato.effects.title"),
                width / 2, 18, 0xFFFFFF);
        drawCenteredString(fontRenderer, I18n.format("furusato.effects.subtitle"),
                width / 2, 31, 0xA0A0A0);
        int panelWidth = responsiveContentWidth(300, 500, 16);
        int left = (width - panelWidth) / 2;
        int gap = 18;
        int columnWidth = (panelWidth - 28 - gap) / 2;
        int first = left + 14;
        int second = first + columnWidth + gap;
        drawFurusatoPanel(first + 4, top + 2, columnWidth - 8, 88);
        drawFurusatoPanel(second + 4, top + 2, columnWidth - 8, 50);
        drawFurusatoPanel(second + 4, top + 70, columnWidth - 8, 50);
        drawString(fontRenderer, I18n.format("furusato.effects.hotbarSpeed",
                        denormalize(hotbarDuration.getValue(), 50, 180)),
                first + 2, top + 59, 0xA0A0A0);
        drawString(fontRenderer, I18n.format("furusato.effects.containerSpeed",
                        denormalize(containerDuration.getValue(), 80, 400)),
                second + 2, top + 29, 0xA0A0A0);
        drawString(fontRenderer, I18n.format("furusato.effects.blurRadius",
                        denormalize(blurRadius.getValue(), 2, 12)),
                second + 2, top + 97, 0xA0A0A0);
        if (statusKey != null) {
            drawCenteredString(fontRenderer, I18n.format(statusKey),
                    width / 2, top + 136, statusColor);
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
    public void onGuiClosed() {
        if (hotbarToggle != null && settingsDiffer()) {
            saveSettings();
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (settingsDiffer()) {
            saveSettings();
        }
    }

    private boolean settingsDiffer() {
        return hotbarToggle.getValue()
                        != FurusatoEarlyConfig.isHotbarAnimationEnabled()
                || pulseToggle.getValue()
                        != FurusatoEarlyConfig.isHotbarPulseEnabled()
                || denormalize(hotbarDuration.getValue(), 50, 180)
                        != FurusatoEarlyConfig.getHotbarDurationMillis()
                || containerToggle.getValue()
                        != FurusatoEarlyConfig.isContainerAnimationEnabled()
                || denormalize(containerDuration.getValue(), 80, 400)
                        != FurusatoEarlyConfig.getContainerDurationMillis()
                || blurToggle.getValue()
                        != FurusatoEarlyConfig.isContainerBlurEnabled()
                || denormalize(blurRadius.getValue(), 2, 12)
                        != FurusatoEarlyConfig.getBlurRadius();
    }

    private static double normalize(int value, int minimum, int maximum) {
        return (value - minimum) / (double) (maximum - minimum);
    }

    private static int denormalize(double value, int minimum, int maximum) {
        return minimum + (int) Math.round(value * (maximum - minimum));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
