package xy177.tt2.risky.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Only redirects the published trait/modifier contracts; unregistered IDs pass through unchanged. */
public final class ConsumableTransformer implements IClassTransformer {
    private static final String TRAIT = "slimeknights/tconstruct/library/traits/ITrait";
    private static final String ARMOR = "c4/conarm/lib/traits/IArmorTrait";
    private static final String MOD = "slimeknights/tconstruct/library/modifiers/IModifier";
    private static final String BASE_MOD = "slimeknights/tconstruct/library/modifiers/Modifier";
    private static final String STACK = "net/minecraft/item/ItemStack";
    private static final String NBT = "net/minecraft/nbt/NBTTagCompound";
    private static final String API = "xy177/tt2/api/consumable/ConsumableUses";
    private static final String HOOK = "xy177/tt2/consumable/ConsumableCallbacks";
    private static final Set<String> CALLBACKS = new HashSet<>(Arrays.asList(
        "onUpdate", "onArmorTick", "miningSpeed", "beforeBlockBreak", "afterBlockBreak", "blockHarvestDrops",
        "isCriticalHit", "damage", "onHit", "knockBack", "afterHit", "onBlock", "onPlayerHurt",
        "onToolDamage", "onToolHeal", "onRepair", "getAttributeModifiers"));

    @Override public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null || transformedName.startsWith("xy177.tt2.api.consumable.")
            || transformedName.startsWith("xy177.tt2.consumable.")
            || transformedName.startsWith("xy177.tt2.risky.") || transformedName.startsWith("org.")
            || transformedName.startsWith("java.") || transformedName.startsWith("net.minecraft.")) return bytes;
        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        boolean changed = false;
        List<MethodNode> wrappers = new ArrayList<>();
        for (MethodNode method : node.methods) {
            if ((node.name.equals("slimeknights/tconstruct/library/utils/ToolBuilder")
                || node.name.equals("c4/conarm/lib/tinkering/ArmorBuilder"))
                && (method.name.equals("tryModifyTool") || method.name.equals("tryModifyArmor"))) {
                InsnList entry = new InsnList();
                entry.add(new VarInsnNode(Opcodes.ALOAD, 0));
                entry.add(new VarInsnNode(Opcodes.ALOAD, 1));
                entry.add(new VarInsnNode(Opcodes.ILOAD, 2));
                entry.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "xy177/tt2/consumable/ConsumableCrystalRecipes",
                    "apply", method.desc, false));
                LabelNode normal = new LabelNode();
                entry.add(new InsnNode(Opcodes.DUP));
                entry.add(new JumpInsnNode(Opcodes.IFNULL, normal));
                entry.add(new InsnNode(Opcodes.ARETURN));
                entry.add(normal);
                entry.add(new FrameNode(Opcodes.F_SAME1, 0, null, 1, new Object[]{STACK}));
                entry.add(new InsnNode(Opcodes.POP));
                method.instructions.insert(entry);
                changed = true;
            }
            boolean hit = false;
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (!(insn instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) insn;
                String receiver = null;
                String hook = HOOK;
                if (call.getOpcode() == Opcodes.INVOKEINTERFACE
                    && (call.owner.equals(TRAIT) || call.owner.equals(ARMOR)) && CALLBACKS.contains(call.name)) {
                    receiver = TRAIT;
                    if (call.name.equals("afterHit")) hit = true;
                } else if (call.getOpcode() == Opcodes.INVOKEINTERFACE && call.owner.equals(ARMOR)
                    && (call.name.startsWith("on") || call.name.equals("getModifications") || call.name.equals("disableRendering"))) {
                    receiver = ARMOR;
                    hook = "xy177/tt2/consumable/ConsumableArmorCallbacks";
                } else if ((call.getOpcode() == Opcodes.INVOKEINTERFACE && call.owner.equals(MOD)
                    || call.getOpcode() == Opcodes.INVOKEVIRTUAL && (call.owner.equals(BASE_MOD)
                        || call.owner.equals("slimeknights/tconstruct/library/traits/AbstractTrait")
                        || call.owner.equals("slimeknights/tconstruct/library/modifiers/ModifierTrait")))
                    && (call.name.equals("applyEffect") || call.name.equals("getTooltip") || call.name.equals("canApply"))) {
                    receiver = MOD;
                }
                if (receiver != null) {
                    call.desc = "(L" + receiver + ";" + call.desc.substring(1);
                    call.owner = hook;
                    call.itf = false;
                    call.setOpcode(Opcodes.INVOKESTATIC);
                    changed = true;
                }
            }
            if (hit && !method.name.startsWith("tt2$") && itemStackArgument(method) >= 0
                && (node.name.equals("slimeknights/tconstruct/library/utils/ToolHelper")
                    || node.name.startsWith("xy177/tt2/tools/") || node.name.equals("xy177/tt2/events/ShieldEvents"))) {
                wrappers.add(wrapAction(node.name, method));
                changed = true;
            }
            if ((node.name.equals("slimeknights/tconstruct/library/tinkering/TinkersItem")
                || node.name.equals("c4/conarm/lib/tinkering/TinkersArmor"))
                && method.name.equals("repair") && method.desc.startsWith("(L" + STACK + ";")) {
                boolean damagePatched = false;
                for (AbstractInsnNode insn : method.instructions.toArray()) {
                    if (!damagePatched && insn instanceof MethodInsnNode) {
                        MethodInsnNode call = (MethodInsnNode) insn;
                        if (call.owner.equals(STACK) && (call.name.equals("getItemDamage") || call.name.equals("func_77952_i")) && call.desc.equals("()I")) {
                            call.owner = HOOK; call.name = "repairDamage"; call.desc = "(L" + STACK + ";)I";
                            call.itf = false; call.setOpcode(Opcodes.INVOKESTATIC); damagePatched = true;
                        }
                    }
                    if (insn.getOpcode() == Opcodes.ARETURN) {
                        InsnList hook = new InsnList();
                        hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, "afterRepair", "(L" + STACK + ";L" + STACK + ";)L" + STACK + ";", false));
                        method.instructions.insertBefore(insn, hook);
                    }
                }
                if (!damagePatched) throw new IllegalStateException("TT2 cannot locate the consumable repair check in " + node.name);
                changed = true;
            }
            if (node.name.equals(BASE_MOD) && method.name.equals("apply") && method.desc.equals("(L" + NBT + ";)V")) {
                wrappers.add(wrapApplication(method));
                changed = true;
            }
        }
        if (!changed) return bytes;
        node.methods.addAll(wrappers);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static MethodNode wrapApplication(MethodNode original) {
        MethodNode wrapper = new MethodNode(original.access, original.name, original.desc, original.signature,
            original.exceptions.toArray(new String[0]));
        original.name = "tt2$consumable$apply";
        InsnList code = wrapper.instructions;
        code.add(new VarInsnNode(Opcodes.ALOAD, 1));
        code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "slimeknights/tconstruct/library/utils/TagUtil",
            "getBaseModifiersUsed", "(L" + NBT + ";)I", false));
        code.add(new VarInsnNode(Opcodes.ISTORE, 2));
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new VarInsnNode(Opcodes.ALOAD, 1));
        code.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, BASE_MOD, original.name, original.desc, false));
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new VarInsnNode(Opcodes.ALOAD, 1));
        code.add(new VarInsnNode(Opcodes.ILOAD, 2));
        code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, API, "recordApplication",
            "(L" + MOD + ";L" + NBT + ";I)V", false));
        code.add(new InsnNode(Opcodes.RETURN));
        return wrapper;
    }

    private static int itemStackArgument(MethodNode method) {
        int slot = (method.access & Opcodes.ACC_STATIC) == 0 ? 1 : 0;
        for (Type arg : Type.getArgumentTypes(method.desc)) {
            if (arg.getDescriptor().equals("L" + STACK + ";")) return slot;
            slot += arg.getSize();
        }
        return -1;
    }

    private static MethodNode wrapAction(String owner, MethodNode original) {
        String name = original.name;
        original.name = "tt2$consumable$" + name;
        MethodNode wrapper = new MethodNode(original.access, name, original.desc, original.signature,
            original.exceptions.toArray(new String[0]));
        wrapper.visibleAnnotations = original.visibleAnnotations;
        original.visibleAnnotations = null;
        Type[] args = Type.getArgumentTypes(original.desc);
        Type result = Type.getReturnType(original.desc);
        boolean isStatic = (original.access & Opcodes.ACC_STATIC) != 0;
        List<Object> locals = new ArrayList<>();
        int local = isStatic ? 0 : 1;
        if (!isStatic) locals.add(owner);
        for (Type arg : args) { local += arg.getSize(); locals.add(frameType(arg)); }
        int action = local;
        locals.add(API + "$Action");
        int value = action + 1;
        int error = value + result.getSize();
        InsnList code = wrapper.instructions;
        code.add(new VarInsnNode(Opcodes.ALOAD, itemStackArgument(original)));
        code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, API, "beginAttack", "(L" + STACK + ";)L" + API + "$Action;", false));
        code.add(new VarInsnNode(Opcodes.ASTORE, action));
        LabelNode start = new LabelNode(), end = new LabelNode(), handler = new LabelNode();
        code.add(start);
        int slot = 0;
        if (!isStatic) code.add(new VarInsnNode(Opcodes.ALOAD, slot++));
        for (Type arg : args) { code.add(new VarInsnNode(arg.getOpcode(Opcodes.ILOAD), slot)); slot += arg.getSize(); }
        code.add(new MethodInsnNode(isStatic ? Opcodes.INVOKESTATIC : Opcodes.INVOKESPECIAL, owner, original.name, original.desc, false));
        if (result.getSort() != Type.VOID) code.add(new VarInsnNode(result.getOpcode(Opcodes.ISTORE), value));
        code.add(end);
        code.add(new VarInsnNode(Opcodes.ALOAD, action));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, API + "$Action", "close", "()V", false));
        if (result.getSort() != Type.VOID) code.add(new VarInsnNode(result.getOpcode(Opcodes.ILOAD), value));
        code.add(new InsnNode(result.getOpcode(Opcodes.IRETURN)));
        code.add(handler);
        code.add(new FrameNode(Opcodes.F_FULL, locals.size(), locals.toArray(), 1, new Object[]{"java/lang/Throwable"}));
        code.add(new VarInsnNode(Opcodes.ASTORE, error));
        code.add(new VarInsnNode(Opcodes.ALOAD, action));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, API + "$Action", "close", "()V", false));
        code.add(new VarInsnNode(Opcodes.ALOAD, error)); code.add(new InsnNode(Opcodes.ATHROW));
        wrapper.tryCatchBlocks.add(new TryCatchBlockNode(start, end, handler, null));
        return wrapper;
    }

    private static Object frameType(Type type) {
        switch (type.getSort()) {
            case Type.OBJECT: return type.getInternalName();
            case Type.ARRAY: return type.getDescriptor();
            case Type.FLOAT: return Opcodes.FLOAT;
            case Type.DOUBLE: return Opcodes.DOUBLE;
            case Type.LONG: return Opcodes.LONG;
            default: return Opcodes.INTEGER;
        }
    }
}
