package dev.lukebemish.emojiidentifiers;

import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.invoke.ConstantBootstraps;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.security.ProtectionDomain;

public final class CharacterTransformer implements ClassFileTransformer {
    String CHARACTER_NAME = "java/lang/Character";

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!className.equals(CHARACTER_NAME)) {
            return classfileBuffer;
        }
        return transformCharacterClass(classfileBuffer);
    }

    @Override
    public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!className.equals(CHARACTER_NAME)) {
            return classfileBuffer;
        }
        return transformCharacterClass(classfileBuffer);
    }

    private static byte[] transformCharacterClass(byte[] original) {
        ClassWriter writer = new ClassWriter(0);
        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                class ExtraCheckVisitor extends MethodVisitor {
                    ExtraCheckVisitor(int api, MethodVisitor methodVisitor) {
                        super(api, methodVisitor);
                    }

                    @Override
                    public void visitInsn(int opcode) {
                        if (opcode != Opcodes.IRETURN) {
                            super.visitInsn(opcode);
                        }
                        var label = new Label();
                        visitJumpInsn(Opcodes.IFNE, label);

                        var systemClassloader = invoke(
                                ClassLoader.class.descriptorString(),
                                new Handle(
                                        Opcodes.H_INVOKESTATIC,
                                        Type.getInternalName(ClassLoader.class),
                                        "getSystemClassLoader",
                                        MethodType.methodType(ClassLoader.class).descriptorString(),
                                        false
                                )
                        );

                        var emojiCheckerClass = invoke(
                                Class.class.descriptorString(),
                                new Handle(
                                        Opcodes.H_INVOKESTATIC,
                                        Type.getInternalName(Class.class),
                                        "forName",
                                        MethodType.methodType(Class.class, String.class, boolean.class, ClassLoader.class).descriptorString(),
                                        false
                                ),
                                "dev.lukebemish.emojiidentifiers.EmojiChecker",
                                booleanConstant(false),
                                systemClassloader
                        );

                        var lookup = invoke(
                                MethodHandles.Lookup.class.descriptorString(),
                                new Handle(
                                        Opcodes.H_INVOKESTATIC,
                                        Type.getInternalName(MethodHandles.class),
                                        "publicLookup",
                                        MethodType.methodType(MethodHandles.Lookup.class).descriptorString(),
                                        false
                                )
                        );

                        var isEmojiHandle = invoke(
                                MethodHandle.class.descriptorString(),
                                new Handle(
                                        Opcodes.H_INVOKEVIRTUAL,
                                        Type.getInternalName(MethodHandles.Lookup.class),
                                        "findStatic",
                                        MethodType.methodType(MethodHandle.class, Class.class, String.class, MethodType.class).descriptorString(),
                                        false
                                ),
                                lookup,
                                emojiCheckerClass,
                                "isEmoji",
                                Type.getMethodType(Type.getType(boolean.class), Type.getType(int.class))
                        );

                        visitLdcInsn(isEmojiHandle);
                        visitVarInsn(Opcodes.ILOAD, 0);
                        visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "invokeExact", descriptor, false);
                        super.visitInsn(Opcodes.IRETURN);

                        visitLabel(label);
                        super.visitInsn(Opcodes.ICONST_1);
                        super.visitInsn(Opcodes.IRETURN);
                    }
                }
                var delegate = super.visitMethod(access, name, descriptor, signature, exceptions);
                if ((name.equals("isJavaIdentifierStart") || name.equals("isJavaIdentifierPart")) && descriptor.equals("(I)Z")) {
                    return new ExtraCheckVisitor(Opcodes.ASM9, delegate);
                }
                return delegate;
            }
        };
        ClassReader reader = new ClassReader(original);
        reader.accept(visitor, 0);
        return writer.toByteArray();
    }

    private static ConstantDynamic invoke(String descriptor, Object handle, Object... args) {
        Object[] fullArgs = new Object[args.length+1];
        System.arraycopy(args, 0, fullArgs, 1, args.length);
        fullArgs[0] = handle;
        return new ConstantDynamic(
                "invoke",
                descriptor,
                new Handle(
                        Opcodes.H_INVOKESTATIC,
                        Type.getInternalName(ConstantBootstraps.class),
                        "invoke",
                        MethodType.methodType(Object.class, MethodHandles.Lookup.class, String.class, Class.class, MethodHandle.class, Object[].class).descriptorString(),
                        false
                ),
                fullArgs
        );
    }

    private static ConstantDynamic booleanConstant(boolean bool) {
        return new ConstantDynamic(
                // booleans are fucky in ConstantDynamics. Here's an alternative...
                bool ? "TRUE" : "FALSE",
                Boolean.class.descriptorString(),
                new Handle(
                        Opcodes.H_INVOKESTATIC,
                        Type.getInternalName(ConstantBootstraps.class),
                        "getStaticFinal",
                        MethodType.methodType(Object.class, MethodHandles.Lookup.class, String.class, Class.class, Class.class).descriptorString(),
                        false
                ),
                Type.getType(Boolean.class)
        );
    }
}
