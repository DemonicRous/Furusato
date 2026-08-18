package com.demonicrous.limecore.client;

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
}
