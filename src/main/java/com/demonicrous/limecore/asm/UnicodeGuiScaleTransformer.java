package com.demonicrous.limecore.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Preserves an odd GUI scale when Minecraft uses its Unicode font renderer. */
public final class UnicodeGuiScaleTransformer implements IClassTransformer {
    private static final Logger LOGGER = LogManager.getLogger("Lime Core/ASM");
    private static final String DEOBF_TARGET = "net.minecraft.client.gui.ScaledResolution";
    private static final String OBF_TARGET = "bip";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !isTarget(name, transformedName)) {
            return basicClass;
        }

        ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, 0);
        int replacements = 0;

        for (MethodNode method : classNode.methods) {
            if (!"<init>".equals(method.name)) {
                continue;
            }

            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) {
                    continue;
                }

                MethodInsnNode call = (MethodInsnNode) instruction;
                if (!isUnicodeCheck(call)) {
                    continue;
                }

                // Consume the FontRenderer instance and replace isUnicode() with false.
                InsnList replacement = new InsnList();
                replacement.add(new InsnNode(Opcodes.POP));
                replacement.add(new InsnNode(Opcodes.ICONST_0));
                method.instructions.insertBefore(call, replacement);
                method.instructions.remove(call);
                replacements++;
            }
        }

        if (replacements == 0) {
            LOGGER.warn("Could not locate the Unicode GUI-scale check in {}", transformedName);
            return basicClass;
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        LOGGER.info("Preserved odd Unicode GUI scales ({} replacement(s))", replacements);
        return writer.toByteArray();
    }

    private static boolean isTarget(String name, String transformedName) {
        return DEOBF_TARGET.equals(transformedName)
                || DEOBF_TARGET.equals(name)
                || OBF_TARGET.equals(name);
    }

    private static boolean isUnicodeCheck(MethodInsnNode call) {
        return call.getOpcode() == Opcodes.INVOKEVIRTUAL
                && "()Z".equals(call.desc)
                && ("isUnicode".equals(call.name) || "func_152349_b".equals(call.name));
    }
}

