package com.demonicrous.furusato.asm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class HotbarSelectorTransformerTest {
    private static final String CLASS_NAME =
            "net.minecraft.client.gui.GuiIngame";
    private static final String HELPER =
            "com/demonicrous/furusato/client/HotbarAnimation";

    @Test
    public void patchesExactlyOneVanillaSelectorCoordinate() throws IOException {
        byte[] original = readClass("/net/minecraft/client/gui/GuiIngame.class");
        byte[] transformed = new HotbarSelectorTransformer().transform(
                CLASS_NAME, CLASS_NAME, original);

        ClassNode node = new ClassNode();
        new ClassReader(transformed).accept(node, 0);
        int helperCalls = 0;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (HELPER.equals(call.owner)
                            && "adjustSelectorX".equals(call.name)) {
                        helperCalls++;
                    }
                }
            }
        }
        assertEquals(1, helperCalls);
    }

    private static byte[] readClass(String resource) throws IOException {
        InputStream input = HotbarSelectorTransformerTest.class
                .getResourceAsStream(resource);
        assertNotNull(input);
        try (InputStream stream = input;
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = stream.read(buffer)) >= 0) {
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }
}
