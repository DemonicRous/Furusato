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
        int panelWidth = responsiveContentWidth(300, 620, 16);
        int left = (width - panelWidth) / 2;
        int top = contentTop();
        CardLayout layout = CardLayout.create(left, panelWidth);

        hotbarToggle = addButton(new GuiFurusatoToggle(HOTBAR,
                layout.hotbarX + 10, top + 29, layout.hotbarWidth - 20,
                FurusatoEarlyConfig.isHotbarAnimationEnabled()));
        pulseToggle = addButton(new GuiFurusatoToggle(HOTBAR_PULSE,
                layout.hotbarX + 10, top + 51, layout.hotbarWidth - 20,
                FurusatoEarlyConfig.isHotbarPulseEnabled()));
        hotbarDuration = addButton(new GuiFurusatoSlider(HOTBAR_DURATION,
                layout.hotbarX + 11, top + 91, layout.hotbarWidth - 22,
                normalize(FurusatoEarlyConfig.getHotbarDurationMillis(), 50, 180))
                .setShowPercentage(false));

        containerToggle = addButton(new GuiFurusatoToggle(CONTAINERS,
                layout.containerX + 10, top + 29, layout.containerWidth - 20,
                FurusatoEarlyConfig.isContainerAnimationEnabled()));
        containerDuration = addButton(new GuiFurusatoSlider(CONTAINER_DURATION,
                layout.containerX + 11, top + 69, layout.containerWidth - 22,
                normalize(FurusatoEarlyConfig.getContainerDurationMillis(), 80, 400))
                .setShowPercentage(false));
        blurToggle = addButton(new GuiFurusatoToggle(BLUR,
                layout.blurX + 10, top + 29, layout.blurWidth - 20,
                FurusatoEarlyConfig.isContainerBlurEnabled()));
        blurRadius = addButton(new GuiFurusatoSlider(BLUR_RADIUS,
                layout.blurX + 11, top + 69, layout.blurWidth - 22,
                normalize(FurusatoEarlyConfig.getBlurRadius(), 2, 12))
                .setShowPercentage(false));
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
        hotbarToggle.displayString = I18n.format("furusato.effects.hotbar.label");
        pulseToggle.displayString = I18n.format("furusato.effects.hotbarPulse.label");
        containerToggle.displayString = I18n.format("furusato.effects.containers.label");
        blurToggle.displayString = I18n.format("furusato.effects.blur.label");
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
        statusColor = saved ? 0xA8A4B0 : 0xFF5555;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int top = contentTop();
        drawCenteredString(fontRenderer, I18n.format("furusato.effects.title"),
                width / 2, 18, 0xFFFFFF);
        drawCenteredString(fontRenderer, I18n.format("furusato.effects.subtitle"),
                width / 2, 31, 0xA0A0A0);
        int panelWidth = responsiveContentWidth(300, 620, 16);
        int left = (width - panelWidth) / 2;
        CardLayout layout = CardLayout.create(left, panelWidth);
        drawEffectCard(layout.hotbarX, top, layout.hotbarWidth,
                I18n.format("furusato.effects.card.hotbar"));
        drawEffectCard(layout.containerX, top, layout.containerWidth,
                I18n.format("furusato.effects.card.containers"));
        drawEffectCard(layout.blurX, top, layout.blurWidth,
                I18n.format("furusato.effects.card.blur"));
        drawCardValue(I18n.format("furusato.effects.hotbarSpeed",
                        denormalize(hotbarDuration.getValue(), 50, 180)),
                layout.hotbarX, top + 79, layout.hotbarWidth);
        drawCardValue(I18n.format("furusato.effects.containerSpeed",
                        denormalize(containerDuration.getValue(), 80, 400)),
                layout.containerX, top + 57, layout.containerWidth);
        drawCardValue(I18n.format("furusato.effects.blurRadius",
                        denormalize(blurRadius.getValue(), 2, 12)),
                layout.blurX, top + 57, layout.blurWidth);
        if (statusKey != null) {
            drawCenteredString(fontRenderer, I18n.format(statusKey),
                    width / 2, top + 132, statusColor);
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

    private int contentTop() {
        return Math.max(48, Math.min(height - 174, height / 2 - 66));
    }

    private void drawEffectCard(int x, int y, int cardWidth, String title) {
        drawFurusatoPanel(x + 6, y + 6, cardWidth - 12, 108);
        drawCenteredString(fontRenderer, title, x + cardWidth / 2,
                y + 9, 0xE8E4F0);
        drawRect(x + 11, y + 24, x + cardWidth - 11, y + 25,
                0x505000FF);
    }

    private void drawCardValue(String value, int x, int y, int cardWidth) {
        int availableWidth = Math.max(0, cardWidth - 24);
        String rendered = value;
        if (fontRenderer.getStringWidth(rendered) > availableWidth) {
            String ellipsis = "...";
            int textWidth = Math.max(0, availableWidth
                    - fontRenderer.getStringWidth(ellipsis));
            rendered = fontRenderer.trimStringToWidth(rendered, textWidth)
                    + ellipsis;
        }
        drawString(fontRenderer, rendered, x + 12, y, 0xA8A4B0);
    }

    private static final class CardLayout {
        private static final int GAP = 12;
        private final int hotbarX;
        private final int hotbarWidth;
        private final int containerX;
        private final int containerWidth;
        private final int blurX;
        private final int blurWidth;

        private CardLayout(int hotbarX, int hotbarWidth, int containerX,
                int containerWidth, int blurX, int blurWidth) {
            this.hotbarX = hotbarX;
            this.hotbarWidth = hotbarWidth;
            this.containerX = containerX;
            this.containerWidth = containerWidth;
            this.blurX = blurX;
            this.blurWidth = blurWidth;
        }

        private static CardLayout create(int left, int width) {
            int cardWidth = (width - GAP * 2) / 3;
            int finalWidth = width - cardWidth * 2 - GAP * 2;
            int second = left + cardWidth + GAP;
            int third = second + cardWidth + GAP;
            return new CardLayout(left, cardWidth, second, cardWidth,
                    third, finalWidth);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
