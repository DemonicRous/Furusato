package com.demonicrous.furusato.asm;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public final class CompatibilityDiagnosticsTest {
    @Test
    public void filtersPlatformAndFurusatoTransformers() {
        List<String> result = CompatibilityDiagnostics.thirdPartyTransformerClassNames(
                Arrays.asList(
                        "net.minecraftforge.fml.common.asm.ForgeTransformer",
                        "net.minecraft.launchwrapper.VanillaTransformer",
                        "com.demonicrous.furusato.asm.UnicodeGuiScaleTransformer",
                        "example.optimization.CustomTransformer"));

        assertEquals(Arrays.asList("example.optimization.CustomTransformer"), result);
    }
}
