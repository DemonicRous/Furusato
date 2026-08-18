package com.demonicrous.furusato.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FurusatoViewportTest {
    @Test
    public void largePhysicalWindowUsesDesktopLayout() {
        assertFalse(FurusatoViewport.isCompact(640, 360, 1920, 1080));
    }

    @Test
    public void physicalWindowKeepsCompactModeAfterGuiScaleDrops() {
        assertTrue(FurusatoViewport.isCompact(640, 360, 800, 450));
    }

    @Test
    public void smallScaledViewportAlsoUsesCompactMode() {
        assertTrue(FurusatoViewport.isCompact(400, 220, 1920, 1080));
    }
}
