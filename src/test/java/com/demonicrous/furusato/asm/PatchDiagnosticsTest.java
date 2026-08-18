package com.demonicrous.furusato.asm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public final class PatchDiagnosticsTest {
    @Before
    public void setUp() {
        PatchDiagnostics.resetForTests();
    }

    @After
    public void tearDown() {
        PatchDiagnostics.resetForTests();
    }

    @Test
    public void snapshotKeepsTheStateAtTheTimeItWasTaken() {
        PatchDiagnostics.register("unicode_gui_scale");
        Map<String, PatchDiagnostics.Result> before = PatchDiagnostics.snapshot();

        PatchDiagnostics.applied("unicode_gui_scale", "patched");
        Map<String, PatchDiagnostics.Result> after = PatchDiagnostics.snapshot();

        assertEquals("REGISTERED", before.get("unicode_gui_scale").getStatus());
        assertEquals("APPLIED", after.get("unicode_gui_scale").getStatus());
        assertEquals("patched", after.get("unicode_gui_scale").getDetail());
        assertFalse(before == after);
    }
}
