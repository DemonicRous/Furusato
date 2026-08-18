package com.demonicrous.furusato.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

/** Draggable scrollbar matching Furusato's standalone slider. */
final class GuiFurusatoScrollBar extends Gui {
    static final int WIDTH = 9;
    private static final int THUMB_HEIGHT = 16;

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

    void draw(Minecraft minecraft, int mouseX, int mouseY) {
        if (!visible) {
            return;
        }
        int trackLeft = x + WIDTH / 2 - 2;
        int trackRight = trackLeft + 4;
        drawRect(trackLeft, y, trackRight, y + height, 0xFF202020);
        drawRect(trackLeft + 1, y + 1, trackRight - 1, y + height - 1,
                0xFF606060);
        int thumbY = thumbY();
        int filledBottom = Math.min(y + height - 1, thumbY + THUMB_HEIGHT / 2);
        drawRect(trackLeft + 1, y + 1, trackRight - 1, filledBottom,
                0xFF7070D8);
        boolean hovered = mouseX >= x && mouseX < x + WIDTH
                && mouseY >= thumbY && mouseY < thumbY + THUMB_HEIGHT;
        drawRect(x, thumbY, x + WIDTH, thumbY + THUMB_HEIGHT, 0xFF000000);
        drawRect(x + 1, thumbY + 1, x + WIDTH - 1, thumbY + THUMB_HEIGHT - 1,
                hovered || dragging ? 0xFFFFFFFF : 0xFFD0D0D0);
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
