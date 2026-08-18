package com.demonicrous.furusato.client;

/** Chooses layout mode without being fooled by Minecraft changing GUI scale. */
final class FurusatoViewport {
    private static final int COMPACT_PHYSICAL_WIDTH = 960;
    private static final int COMPACT_PHYSICAL_HEIGHT = 540;

    private FurusatoViewport() {
    }

    static boolean isCompact(int scaledWidth, int scaledHeight,
            int physicalWidth, int physicalHeight) {
        return physicalWidth < COMPACT_PHYSICAL_WIDTH
                || physicalHeight < COMPACT_PHYSICAL_HEIGHT
                || scaledWidth < 420 || scaledHeight < 240;
    }
}
