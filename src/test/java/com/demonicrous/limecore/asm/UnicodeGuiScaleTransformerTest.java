package com.demonicrous.limecore.asm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import net.minecraft.client.gui.ScaledResolution;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class UnicodeGuiScaleTransformerTest {
    private static final String TARGET = "net.minecraft.client.gui.ScaledResolution";

    @Test
    public void replacesUnicodeCheckAndPreservesStackBalance() throws IOException {
        LimeCoreEarlyConfig.setUnicodeGuiScaleEnabledForTests(true);
        byte[] transformed = new UnicodeGuiScaleTransformer().transform(
                TARGET,
                TARGET,
                readClassBytes()
        );

        ClassNode node = new ClassNode();
        new ClassReader(transformed).accept(node, 0);
        int replacementPairs = 0;

        for (MethodNode method : node.methods) {
            if (!"<init>".equals(method.name)) {
                continue;
            }
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    assertFalse("isUnicode call must be removed",
                            "()Z".equals(call.desc)
                                    && ("isUnicode".equals(call.name)
                                    || "func_152349_b".equals(call.name)));
                } else if (instruction instanceof InsnNode
                        && instruction.getOpcode() == org.objectweb.asm.Opcodes.POP
                        && instruction.getNext() != null
                        && instruction.getNext().getOpcode() == org.objectweb.asm.Opcodes.ICONST_0) {
                    replacementPairs++;
                }
            }
        }

        assertEquals(1, replacementPairs);
    }

    @Test
    public void returnsOriginalClassWhenPatchIsDisabled() throws IOException {
        byte[] original = readClassBytes();
        LimeCoreEarlyConfig.setUnicodeGuiScaleEnabledForTests(false);
        try {
            byte[] transformed = new UnicodeGuiScaleTransformer().transform(
                    TARGET, TARGET, original);
            assertSame(original, transformed);
        } finally {
            LimeCoreEarlyConfig.setUnicodeGuiScaleEnabledForTests(true);
        }
    }

    @Test
    public void rejectsUnicodeMethodOnUnexpectedOwner() throws IOException {
        byte[] original = readClassBytes();
        ClassNode node = new ClassNode();
        new ClassReader(original).accept(node, 0);

        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if ("isUnicode".equals(call.name) || "func_152349_b".equals(call.name)) {
                        call.owner = "example/UnexpectedOwner";
                    }
                }
            }
        }

        org.objectweb.asm.ClassWriter writer = new org.objectweb.asm.ClassWriter(0);
        node.accept(writer);
        byte[] unexpectedOwner = writer.toByteArray();
        byte[] transformed = new UnicodeGuiScaleTransformer().transform(
                TARGET, TARGET, unexpectedOwner);
        assertSame(unexpectedOwner, transformed);
    }

    private static byte[] readClassBytes() throws IOException {
        String resource = "/" + TARGET.replace('.', '/') + ".class";
        try (InputStream input = ScaledResolution.class.getResourceAsStream(resource)) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        }
    }
}
