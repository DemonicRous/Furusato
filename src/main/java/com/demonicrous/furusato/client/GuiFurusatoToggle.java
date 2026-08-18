package com.demonicrous.furusato.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/** Standalone toggle using the same visual language as Furusato sliders. */
final class GuiFurusatoToggle extends GuiButton {
    private static final int TRACK_WIDTH = 28;
    private boolean value;

    GuiFurusatoToggle(int id, int x, int y, int width, boolean initialValue) {
        super(id, x, y, width, 20, "");
        value = initialValue;
    }

    boolean getValue() {
        return value;
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
        if (!super.mousePressed(minecraft, mouseX, mouseY)) {
            return false;
        }
        value = !value;
        return true;
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY,
            float partialTicks) {
        if (!visible) {
            return;
        }
        hovered = mouseX >= x && mouseY >= y
                && mouseX < x + width && mouseY < y + height;
        int trackLeft = x + width - TRACK_WIDTH - 2;
        int trackTop = y + height / 2 - 2;
        int trackRight = trackLeft + TRACK_WIDTH;
        drawRect(trackLeft, trackTop, trackRight, trackTop + 5, 0xFF201D24);
        drawRect(trackLeft + 1, trackTop + 1, trackRight - 1, trackTop + 4,
                value ? 0xFF7568B8 : 0xFF55515C);
        int handleCenter = value ? trackRight - 4 : trackLeft + 4;
        int handleColor = hovered ? 0xFFE8E4F0 : 0xFFC4C0CA;
        drawRect(handleCenter - 3, trackTop - 4,
                handleCenter + 4, trackTop + 9, 0xFF08060A);
        drawRect(handleCenter - 2, trackTop - 3,
                handleCenter + 3, trackTop + 8, handleColor);
        String label = displayString;
        int labelWidth = Math.max(0, trackLeft - x - 8);
        if (minecraft.fontRenderer.getStringWidth(label) > labelWidth) {
            String ellipsis = "...";
            label = minecraft.fontRenderer.trimStringToWidth(label,
                    Math.max(0, labelWidth
                            - minecraft.fontRenderer.getStringWidth(ellipsis)))
                    + ellipsis;
        }
        minecraft.fontRenderer.drawStringWithShadow(label,
                x + 2, y + 6, enabled ? 0xFFFFFF : 0xA0A0A0);
    }
}
