package com.demonicrous.furusato.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FurusatoGuiLayoutTest {
    @Test
    public void contentWidthRespectsMarginsAndMaximum() {
        assertEquals(288, FurusatoGuiLayout.contentWidth(320, 280, 620, 16));
        assertEquals(620, FurusatoGuiLayout.contentWidth(1920, 280, 620, 16));
    }

    @Test
    public void scrollingIsClampedToAvailableRows() {
        assertEquals(0, FurusatoGuiLayout.clampScroll(-2, 6, 3));
        assertEquals(2, FurusatoGuiLayout.clampScroll(2, 6, 3));
        assertEquals(3, FurusatoGuiLayout.clampScroll(9, 6, 3));
        assertEquals(0, FurusatoGuiLayout.clampScroll(2, 3, 3));
    }

    @Test
    public void contentCanBeCenteredInsideAvailableArea() {
        assertEquals(40, FurusatoGuiLayout.centeredTop(10, 110, 40));
        assertEquals(10, FurusatoGuiLayout.centeredTop(10, 30, 40));
    }
}
