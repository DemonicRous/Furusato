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
        displayString = Math.round(value * 100.0D) + "%";
        super.drawButton(minecraft, mouseX, mouseY, partialTicks);
        if (!visible) {
            return;
        }
        int trackLeft = x + 8;
        int trackRight = x + width - 8;
        int trackY = y + height - 4;
        drawRect(trackLeft, trackY, trackRight, trackY + 1, 0xFF303030);
        int filled = trackLeft + (int) Math.round((trackRight - trackLeft) * value);
        drawRect(trackLeft, trackY, filled, trackY + 1, 0xFF8080FF);
        drawRect(filled - 1, trackY - 2, filled + 1, trackY + 3, 0xFFFFFFFF);
    }

    private void updateValue(int mouseX) {
        value = clamp((mouseX - (x + 8.0D)) / Math.max(1.0D, width - 16.0D));
    }

    private static double clamp(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }
}
