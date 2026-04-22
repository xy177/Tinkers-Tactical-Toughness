package xy177.tt2.risky.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ASMTinkerAnimate implements IClassTransformer {
    public static boolean loaded = false;
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(!loaded){
            loaded = true;
            System.out.println("Correctly loaded asm.");
        }
        if(transformedName.equals("slimeknights.tconstruct.library.client.texture.AbstractColoredTexture")) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            System.out.println("Found AbstractColoredTexture!");

            if(!tryTransformAbstractTexture(classNode)) return basicClass;

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);
            byte[] result = writer.toByteArray();
            System.out.println("Overwrite success!");

            return result;
        }
        if(transformedName.equals("slimeknights.tconstruct.library.client.texture.MetalTextureTexture")) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            System.out.println("Found MetalTextureTexture!");

            if(!tryTransformMetalTexture(classNode)) return basicClass;

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);
            byte[] result = writer.toByteArray();
            System.out.println("Overwrite success!");

            return result;
        }
        return basicClass;
    }
    protected boolean tryTransformAbstractTexture(ClassNode classNode){
        System.out.println("Found AbstractTexture");

        MethodNode searched = null;
        for (MethodNode node:classNode.methods){
            if(node.name.equals("load")){
                searched = node;
                break;
            }
        }
        if(searched == null) return false;

        System.out.println("Found method!");

        searched.instructions.clear();
        searched.localVariables.clear();
        searched.tryCatchBlocks.clear();
        searched.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
        searched.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));
        searched.instructions.add(new VarInsnNode(Opcodes.ALOAD,2));
        searched.instructions.add(new VarInsnNode(Opcodes.ALOAD,3));
        searched.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "xy177/tt2/risky/asm/NewTinkerTexture",
                "load",
                "(" +
                        "Ljava/lang/Object;" +
                        "Lnet/minecraft/client/resources/IResourceManager;" +
                        "Lnet/minecraft/util/ResourceLocation;" +
                        "Ljava/util/function/Function;" +
                        ")Z",
                false
        ));
        searched.instructions.add(new InsnNode(
                Opcodes.IRETURN
        ));

        System.out.println("Instruction Replaced.");

        return true;
    }
    protected boolean tryTransformMetalTexture(ClassNode classNode){
        System.out.println("Found MetalTexture");


        MethodNode searched = null;
        for (MethodNode node:classNode.methods){
            if(node.name.equals("processData")){
                searched = node;
                break;
            }
        }
        if(searched == null) return false;

        System.out.println("Found method!");

        searched.instructions.clear();
        searched.localVariables.clear();
        searched.tryCatchBlocks.clear();
        searched.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
        searched.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));
        searched.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "xy177/tt2/risky/asm/NewTinkerTexture",
                "processData",
                "(" +
                        "Ljava/lang/Object;" +
                        "[I" +
                      ")V",
                false
        ));
        searched.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
        searched.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));
        searched.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESPECIAL,
                "slimeknights/tconstruct/library/client/texture/AbstractColoredTexture",
                "processData",
                "([I)V",
                false
        ));
        searched.instructions.add(new InsnNode(
                Opcodes.RETURN
        ));

        System.out.println("Instruction Replaced.");

        return true;
    }
}
