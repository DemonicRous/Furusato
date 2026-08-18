package com.demonicrous.furusato.client;

/** Time-based position state used by the transformed vanilla hotbar selector. */
public final class HotbarAnimation {
    private static final double SPEED = 18.0D;
    private static double displayedX = Double.NaN;
    private static long lastFrameNanos;

    private HotbarAnimation() {
    }

    public static int adjustSelectorX(int targetX) {
        long now = System.nanoTime();
        if (Double.isNaN(displayedX) || lastFrameNanos == 0L
                || now - lastFrameNanos > 500000000L) {
            displayedX = targetX;
        } else {
            double seconds = Math.min(0.05D,
                    Math.max(0.0D, (now - lastFrameNanos) / 1.0E9D));
            double blend = 1.0D - Math.exp(-SPEED * seconds);
            displayedX += (targetX - displayedX) * blend;
            if (Math.abs(targetX - displayedX) < 0.05D) {
                displayedX = targetX;
            }
        }
        lastFrameNanos = now;
        return (int) Math.round(displayedX);
    }
}
