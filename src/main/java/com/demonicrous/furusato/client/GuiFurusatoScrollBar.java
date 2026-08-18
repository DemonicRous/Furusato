package com.demonicrous.furusato.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

/** Draggable scrollbar using Minecraft 1.12.2 Creative Inventory's thumb. */
final class GuiFurusatoScrollBar extends Gui {
    private static final ResourceLocation CREATIVE_TABS = new ResourceLocation(
            "minecraft", "textures/gui/container/creative_inventory/tabs.png");
    private static final int WIDTH = 12;
    private static final int THUMB_HEIGHT = 15;

    private final SmoothScrollState scroll;
    private int x;
    private int y;
    private int height;
    private boolean visible;
    private boolean dragging;
    private int dragOffset;

    GuiFurusatoScrollBar(SmoothScrollState scroll) {
        this.scroll = scroll;
    }

    void setBounds(int x, int y, int height, int rowCount, int visibleRows) {
        this.x = x;
        this.y = y;
        this.height = Math.max(THUMB_HEIGHT, height);
        visible = rowCount > visibleRows;
        scroll.setMaximum(Math.max(0, rowCount - visibleRows));
    }

    void draw(Minecraft minecraft) {
        if (!visible) {
            return;
        }
        drawRect(x, y, x + WIDTH, y + height, 0xFF8B8B8B);
        drawRect(x + 1, y + 1, x + WIDTH - 1, y + height - 1, 0xFFB8B8B8);
        int thumbY = thumbY();
        minecraft.getTextureManager().bindTexture(CREATIVE_TABS);
        drawTexturedModalRect(x, thumbY, 232, 0, WIDTH, THUMB_HEIGHT);
    }

    boolean mousePressed(int mouseX, int mouseY) {
        if (!visible || mouseX < x || mouseX >= x + WIDTH
                || mouseY < y || mouseY >= y + height) {
            return false;
        }
        int thumbY = thumbY();
        dragOffset = mouseY >= thumbY && mouseY < thumbY + THUMB_HEIGHT
                ? mouseY - thumbY : THUMB_HEIGHT / 2;
        dragging = true;
        updateFromMouse(mouseY);
        return true;
    }

    void mouseDragged(int mouseY) {
        if (dragging) {
            updateFromMouse(mouseY);
        }
    }

    void mouseReleased() {
        dragging = false;
    }

    private int thumbY() {
        int range = Math.max(1, height - THUMB_HEIGHT);
        double maximum = scroll.getMaximum();
        double fraction = maximum <= 0.0D ? 0.0D
                : scroll.getDisplayed() / maximum;
        return y + (int) Math.round(range * fraction);
    }

    private void updateFromMouse(int mouseY) {
        int range = Math.max(1, height - THUMB_HEIGHT);
        double fraction = (mouseY - dragOffset - y) / (double) range;
        scroll.setTarget(scroll.getMaximum()
                * Math.max(0.0D, Math.min(1.0D, fraction)));
    }
}
