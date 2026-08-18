package com.demonicrous.furusato.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class GuiScalePolicyTest {
    @Test
    public void cyclesForwardFromAutoThroughEight() {
        int scale = GuiScalePolicy.AUTO;
        for (int expected = 1; expected <= GuiScalePolicy.MAX_MANUAL; expected++) {
            scale = GuiScalePolicy.next(scale);
            assertEquals(expected, scale);
        }
        assertEquals(GuiScalePolicy.AUTO, GuiScalePolicy.next(scale));
    }

    @Test
    public void cyclesBackwardFromAutoToEight() {
        assertEquals(GuiScalePolicy.MAX_MANUAL,
                GuiScalePolicy.previous(GuiScalePolicy.AUTO));
        assertEquals(GuiScalePolicy.AUTO,
                GuiScalePolicy.previous(GuiScalePolicy.MIN_MANUAL));
    }

    @Test
    public void invalidStoredValuesFallBackToAuto() {
        assertEquals(GuiScalePolicy.AUTO, GuiScalePolicy.normalize(-1));
        assertEquals(GuiScalePolicy.AUTO, GuiScalePolicy.normalize(9));
    }

    @Test
    public void cyclesOnlyThroughScalesSupportedByTheWindow() {
        assertEquals(2, GuiScalePolicy.maximumForDimensions(640, 480));
        assertEquals(4, GuiScalePolicy.maximumForDimensions(1920, 1080));
        assertEquals(6, GuiScalePolicy.maximumForDimensions(2560, 1440));
        assertEquals(GuiScalePolicy.AUTO, GuiScalePolicy.next(5, 5));
        assertEquals(5, GuiScalePolicy.next(4, 5));
    }
}
