package com.demonicrous.furusato.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SmoothScrollStateTest {
    @Test
    public void approachesTargetWithoutJumping() {
        SmoothScrollState state = new SmoothScrollState();
        state.setMaximum(4.0D);
        state.scrollBy(2.0D);

        double firstFrame = state.update(1.0D / 60.0D);

        assertTrue(firstFrame > 0.0D);
        assertTrue(firstFrame < 2.0D);
        for (int index = 0; index < 120; index++) {
            state.update(1.0D / 60.0D);
        }
        assertEquals(2.0D, state.getDisplayed(), 0.001D);
    }

    @Test
    public void targetIsClampedToBounds() {
        SmoothScrollState state = new SmoothScrollState();
        state.setMaximum(3.0D);
        state.scrollBy(20.0D);
        for (int index = 0; index < 120; index++) {
            state.update(1.0D / 60.0D);
        }
        assertEquals(3.0D, state.getDisplayed(), 0.001D);
    }

    @Test
    public void scrollbarCanSetAnAbsoluteTarget() {
        SmoothScrollState state = new SmoothScrollState();
        state.setMaximum(6.0D);
        state.setTarget(4.5D);
        for (int index = 0; index < 120; index++) {
            state.update(1.0D / 60.0D);
        }
        assertEquals(4.5D, state.getDisplayed(), 0.001D);
    }
}
