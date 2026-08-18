package com.demonicrous.furusato.asm;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Test;

public final class FurusatoPluginTest {
    @After
    public void tearDown() {
        System.clearProperty("furusato.safeMode");
        PatchDiagnostics.resetForTests();
    }

    @Test
    public void safeModeDoesNotRegisterTransformers() {
        System.setProperty("furusato.safeMode", "true");

        String[] transformers = new FurusatoPlugin().getASMTransformerClass();

        assertEquals(0, transformers.length);
        assertEquals("SAFE_MODE", PatchDiagnostics.snapshot()
                .get("unicode_gui_scale").getStatus());
    }
}
