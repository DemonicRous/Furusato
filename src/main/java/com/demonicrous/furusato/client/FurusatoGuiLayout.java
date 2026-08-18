package com.demonicrous.furusato.client;

/** Pure layout calculations shared by Furusato screens and unit tests. */
final class FurusatoGuiLayout {
    private FurusatoGuiLayout() {
    }

    static int contentWidth(int screenWidth, int minimum, int maximum, int margin) {
        return Math.min(maximum, Math.max(minimum, screenWidth - margin * 2));
    }

    static int visibleRows(int panelHeight, int padding, int rowHeight) {
        return Math.max(1, (panelHeight - padding * 2) / rowHeight);
    }

    static int clampScroll(int requested, int rowCount, int visibleRows) {
        return Math.max(0, Math.min(requested, Math.max(0, rowCount - visibleRows)));
    }

    static int centeredTop(int areaTop, int areaBottom, int contentHeight) {
        int available = Math.max(0, areaBottom - areaTop);
        return areaTop + Math.max(0, (available - contentHeight) / 2);
    }
}
