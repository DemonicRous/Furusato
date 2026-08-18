package com.demonicrous.furusato.client;

/** Frame-rate-independent target/display state for smooth list scrolling. */
final class SmoothScrollState {
    private double target;
    private double displayed;
    private double maximum;

    void setMaximum(double maximum) {
        this.maximum = Math.max(0.0D, maximum);
        target = clamp(target);
        displayed = clamp(displayed);
    }

    void scrollBy(double amount) {
        target = clamp(target + amount);
    }

    void reset() {
        target = 0.0D;
        displayed = 0.0D;
    }

    double update(double deltaSeconds) {
        double safeDelta = Math.max(0.0D, Math.min(0.1D, deltaSeconds));
        double blend = 1.0D - Math.exp(-14.0D * safeDelta);
        displayed += (target - displayed) * blend;
        if (Math.abs(target - displayed) < 0.001D) {
            displayed = target;
        }
        return displayed;
    }

    double getDisplayed() {
        return displayed;
    }

    private double clamp(double value) {
        return Math.max(0.0D, Math.min(maximum, value));
    }
}
