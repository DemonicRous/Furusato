package com.demonicrous.furusato.client;

/** Rules shared by the font settings screen and its tests. */
public final class GuiScalePolicy {
    public static final int AUTO = 0;
    public static final int MIN_MANUAL = 1;
    public static final int MAX_MANUAL = 3;

    private GuiScalePolicy() {
    }

    public static int previous(int value) {
        int normalized = normalize(value);
        return normalized <= AUTO ? MAX_MANUAL : normalized - 1;
    }

    public static int next(int value) {
        int normalized = normalize(value);
        return normalized >= MAX_MANUAL ? AUTO : normalized + 1;
    }

    public static int normalize(int value) {
        if (value < AUTO || value > MAX_MANUAL) {
            return AUTO;
        }
        return value;
    }
}
