package com.demonicrous.furusato.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/** Small reusable slider used by the Furusato UI component preview. */
final class GuiFurusatoSlider extends GuiButton {
    private double value;
    private boolean dragging;

    GuiFurusatoSlider(int id, int x, int y, int width, double initialValue) {
        super(id, x, y, width, 20, "");
        value = clamp(initialValue);
    }

    double getValue() {
        return value;
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
        if (!super.mousePressed(minecraft, mouseX, mouseY)) {
            return false;
        }
        updateValue(mouseX);
        dragging = true;
        return true;
    }

    @Override
    protected void mouseDragged(Minecraft minecraft, int mouseX, int mouseY) {
        if (dragging) {
            updateValue(mouseX);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        dragging = false;
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY,
            float partialTicks) {
        if (!visible) {
            return;
        }
        hovered = mouseX >= x && mouseY >= y
                && mouseX < x + width && mouseY < y + height;
        mouseDragged(minecraft, mouseX, mouseY);
        int valueWidth = 38;
        int trackLeft = x + 2;
        int trackRight = x + width - valueWidth - 6;
        int trackY = y + height / 2 - 2;
        drawRect(trackLeft, trackY, trackRight, trackY + 4, 0xFF202020);
        drawRect(trackLeft + 1, trackY + 1, trackRight - 1,
                trackY + 3, 0xFF606060);
        int filled = trackLeft + (int) Math.round((trackRight - trackLeft) * value);
        drawRect(trackLeft + 1, trackY + 1, filled, trackY + 3, 0xFF7070D8);
        int handleColor = hovered ? 0xFFFFFFFF : 0xFFD0D0D0;
        drawRect(filled - 4, trackY - 6, filled + 5, trackY + 10, 0xFF000000);
        drawRect(filled - 3, trackY - 5, filled + 4, trackY + 9, handleColor);
        String percentage = Math.round(value * 100.0D) + "%";
        minecraft.fontRenderer.drawStringWithShadow(percentage,
                x + width - valueWidth, y + 6, 0xFFFFFF);
    }

    private void updateValue(int mouseX) {
        int valueWidth = 38;
        double trackLeft = x + 2.0D;
        double trackWidth = Math.max(1.0D, width - valueWidth - 8.0D);
        value = clamp((mouseX - trackLeft) / trackWidth);
    }

    private static double clamp(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }
}
