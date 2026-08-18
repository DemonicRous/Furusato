package com.demonicrous.furusato.client;

/** Rules shared by the font settings screen and its tests. */
public final class GuiScalePolicy {
    public static final int AUTO = 0;
    public static final int MIN_MANUAL = 1;
    public static final int MAX_MANUAL = 8;

    private GuiScalePolicy() {
    }

    public static int previous(int value) {
        int normalized = normalize(value);
        return normalized <= AUTO ? MAX_MANUAL : normalized - 1;
    }

    public static int next(int value) {
        return next(value, MAX_MANUAL);
    }

    public static int next(int value, int maximum) {
        int normalized = normalize(value);
        int cappedMaximum = Math.max(MIN_MANUAL, Math.min(MAX_MANUAL, maximum));
        return normalized >= cappedMaximum ? AUTO : normalized + 1;
    }

    /** Mirrors ScaledResolution's minimum logical size constraint. */
    public static int maximumForDimensions(int displayWidth, int displayHeight) {
        int maximum = MIN_MANUAL;
        while (maximum < MAX_MANUAL
                && displayWidth / (maximum + 1) >= 320
                && displayHeight / (maximum + 1) >= 240) {
            maximum++;
        }
        return maximum;
    }

    public static int normalize(int value) {
        if (value < AUTO || value > MAX_MANUAL) {
            return AUTO;
        }
        return value;
    }
}
