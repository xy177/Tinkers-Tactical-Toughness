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

public class ASMTinkerAnimate implements IClassTransformer {
    public static boolean loaded = false;

    private static final String ITEM_STACK = "net/minecraft/item/ItemStack";
    private static final String ICASTER = "thaumcraft/api/casters/ICaster";
    private static final String IARCHITECT = "thaumcraft/api/items/IArchitect";
    private static final String ITEM_CASTER = "thaumcraft/common/items/casters/ItemCaster";
    private static final String FOCUS_ENGINE = "thaumcraft.api.casters.FocusEngine";
    private static final String FOCUS_EFFECT = "thaumcraft/api/casters/FocusEffect";
    private static final String FOCUS_EFFECT_CLASS_PREFIX =
        "thaumcraft.common.items.casters.foci.FocusEffect";
    private static final String FOCUS_EXECUTE_DESC =
        "(Lnet/minecraft/util/math/RayTraceResult;Lthaumcraft/api/casters/Trajectory;FI)Z";
    private static final String FOCUS_RUN_DESC =
        "(Lthaumcraft/api/casters/FocusPackage;[Lthaumcraft/api/casters/Trajectory;"
            + "[Lnet/minecraft/util/math/RayTraceResult;)V";
    private static final String FOCUS_EFFECT_EXCHANGE =
        "thaumcraft/common/items/casters/foci/FocusEffectExchange";
    private static final String THAUMCRAFT_FIX_CORE_RESOURCE =
        "thecodex6824/thaumcraftfix/core/ThaumcraftFixCore.class";
    private static final String THAUMCRAFT_HOOKS = "xy177/tt2/compat/ThaumcraftInsightHooks";
    private static Boolean thaumcraftFixPresent;
    private static final Set<String> THAUMCRAFT_CASTER_CLASSES = new HashSet<>(Arrays.asList(
        "thaumcraft.client.lib.events.HudHandler",
        "thaumcraft.client.lib.events.RenderEventHandler",
        "thaumcraft.client.lib.events.WandRenderingHandler",
        "thaumcraft.common.blocks.crafting.BlockCrucible",
        "thaumcraft.common.blocks.essentia.BlockTube",
        "thaumcraft.common.items.casters.CasterManager",
        "thaumcraft.common.items.casters.ItemCaster",
        "thaumcraft.common.items.casters.foci.FocusEffectExchange",
        "thaumcraft.common.items.casters.foci.FocusMediumPlan",
        "thaumcraft.common.lib.events.KeyHandler",
        "thaumcraft.common.lib.network.misc.PacketFocusChangeToServer$1",
        "thaumcraft.common.lib.network.misc.PacketItemKeyToServer$1"
    ));
    private static final Set<String> THAUMCRAFT_DAMAGE_FOCUS_CLASSES = new HashSet<>(Arrays.asList(
        "thaumcraft.common.items.casters.foci.FocusEffectAir",
        "thaumcraft.common.items.casters.foci.FocusEffectCurse",
        "thaumcraft.common.items.casters.foci.FocusEffectEarth",
        "thaumcraft.common.items.casters.foci.FocusEffectFire",
        "thaumcraft.common.items.casters.foci.FocusEffectFlux",
        "thaumcraft.common.items.casters.foci.FocusEffectFrost",
        "thaumcraft.common.items.casters.foci.FocusEffectHeal"
    ));
    private static final Set<String> THAUMCRAFT_NON_DAMAGE_FOCUS_CLASSES = new HashSet<>(Arrays.asList(
        "thaumcraft.common.items.casters.foci.FocusEffectBreak",
        "thaumcraft.common.items.casters.foci.FocusEffectExchange",
        "thaumcraft.common.items.casters.foci.FocusEffectRift"
    ));

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
        if(transformedName.equals("net.minecraft.client.model.ModelBiped")) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

            if(!tryTransformModelBiped(classNode)) return basicClass;

            ClassWriter writer = new ClassWriter(
                classReader,
                ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS
            );
            classNode.accept(writer);
            return writer.toByteArray();
        }
        if(transformedName.equals("net.minecraft.client.renderer.entity.RenderLivingBase")) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            if(!tryTransformLivingRendererOutlineColor(classNode)) return basicClass;

            ClassWriter writer = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return writer.toByteArray();
        }
        if(transformedName.equals("net.minecraft.client.renderer.RenderGlobal")) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            if(!tryTransformOutlineSelection(classNode)) return basicClass;

            ClassWriter writer = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return writer.toByteArray();
        }
        if(transformedName.equals("slimeknights.tconstruct.library.utils.ToolBuilder")) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            if(!tryTransformToolBuilder(classNode)) return basicClass;

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            return writer.toByteArray();
        }
        boolean thaumcraftCasterHooks = THAUMCRAFT_CASTER_CLASSES.contains(transformedName);
        boolean thaumcraftFocusEngine = FOCUS_ENGINE.equals(transformedName);
        boolean thaumcraftFocusEffect = transformedName.startsWith(FOCUS_EFFECT_CLASS_PREFIX);
        if (thaumcraftCasterHooks || thaumcraftFocusEngine || thaumcraftFocusEffect) {
            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            int injected = 0;
            if (thaumcraftCasterHooks) {
                injected += tryTransformThaumcraftCasterChecks(classNode);
            }
            if (thaumcraftFocusEngine) {
                injected += tryTransformThaumcraftFocusExecution(classNode);
            }
            if (thaumcraftFocusEffect) {
                int damageHooks = tryTransformThaumcraftFocusDamage(classNode);
                int expectedDamageHooks = expectedThaumcraftDamageHooks(transformedName);
                if (expectedDamageHooks >= 0 && damageHooks != expectedDamageHooks) {
                    System.out.println("TT2 found " + damageHooks + " direct damage calls in "
                        + transformedName + ", expected " + expectedDamageHooks
                        + "; leaving the class unchanged.");
                    return basicClass;
                }
                injected += damageHooks;
            }
            if (injected == 0) {
                return basicClass;
            }

            ClassWriter writer = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            System.out.println("TT2 installed " + injected + " conditional Thaumcraft hooks in "
                + transformedName + ".");
            return writer.toByteArray();
        }
        return basicClass;
    }

    private static int expectedThaumcraftDamageHooks(String transformedName) {
        if (THAUMCRAFT_DAMAGE_FOCUS_CLASSES.contains(transformedName)) {
            return 1;
        }
        if (THAUMCRAFT_NON_DAMAGE_FOCUS_CLASSES.contains(transformedName)) {
            return 0;
        }
        return -1;
    }

    protected int tryTransformThaumcraftCasterChecks(ClassNode classNode) {
        if (FOCUS_EFFECT_EXCHANGE.equals(classNode.name)) {
            return tryTransformThaumcraftExchangeCasterChecks(classNode);
        }
        int injected = 0;
        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode instruction = method.instructions.getFirst();
                 instruction != null; instruction = instruction.getNext()) {
                if (!(instruction instanceof MethodInsnNode)) {
                    continue;
                }
                MethodInsnNode call = (MethodInsnNode) instruction;
                AbstractInsnNode next = nextCodeInstruction(instruction);
                if (isItemLookup(call) && isCasterTypeCheck(next)) {
                    transformItemLookup(call);
                    injected++;
                    continue;
                }
                if (isHoldingItemLookup(call) && loadsCasterClass(previousCodeInstruction(instruction))) {
                    call.owner = THAUMCRAFT_HOOKS;
                    call.name = "isHoldingCaster";
                    call.itf = false;
                    injected++;
                }
            }
        }
        return injected;
    }

    protected int tryTransformThaumcraftExchangeCasterChecks(ClassNode classNode) {
        MethodNode execute = null;
        for (MethodNode method : classNode.methods) {
            if ("execute".equals(method.name) && FOCUS_EXECUTE_DESC.equals(method.desc)) {
                execute = method;
                break;
            }
        }
        if (execute == null) {
            System.out.println("TT2 could not find FocusEffectExchange#execute; staff exchange support is disabled.");
            return 0;
        }

        List<ExchangeCasterCheck> checks = new ArrayList<>();
        for (AbstractInsnNode instruction = execute.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode lookup = (MethodInsnNode) instruction;
            AbstractInsnNode next = nextCodeInstruction(instruction);
            if (!isItemLookup(lookup) || !isExchangeCasterTypeCheck(next)) {
                continue;
            }
            TypeInsnNode typeCheck = (TypeInsnNode) next;
            MethodInsnNode pickedBlockCall = null;
            if (typeCheck.getOpcode() == Opcodes.CHECKCAST) {
                pickedBlockCall = findPickedBlockCall(typeCheck);
                if (pickedBlockCall == null) {
                    System.out.println("TT2 found an unsupported FocusEffectExchange caster access; "
                        + "staff exchange support is disabled.");
                    return 0;
                }
            }
            checks.add(new ExchangeCasterCheck(lookup, typeCheck, pickedBlockCall));
        }
        if (checks.size() != 5) {
            System.out.println("TT2 found " + checks.size() + " FocusEffectExchange caster checks, expected 5; "
                + "staff exchange support is disabled.");
            return 0;
        }

        boolean deferInterfaceConversion = isThaumcraftFixPresent();
        for (ExchangeCasterCheck check : checks) {
            transformItemLookup(check.lookup);
            if (!deferInterfaceConversion && ITEM_CASTER.equals(check.typeCheck.desc)) {
                check.typeCheck.desc = ICASTER;
                transformPickedBlockCall(check.pickedBlockCall);
            }
        }
        if (deferInterfaceConversion) {
            System.out.println("TT2 deferred FocusEffectExchange interface conversion to ThaumcraftFix.");
        }
        return checks.size();
    }

    protected int tryTransformThaumcraftFocusExecution(ClassNode classNode) {
        MethodNode runFocusPackage = null;
        for (MethodNode method : classNode.methods) {
            if ("runFocusPackage".equals(method.name) && FOCUS_RUN_DESC.equals(method.desc)) {
                runFocusPackage = method;
                break;
            }
        }
        if (runFocusPackage == null) {
            System.out.println("TT2 could not find Thaumcraft FocusEngine#runFocusPackage.");
            return 0;
        }

        MethodInsnNode executeCall = null;
        int executeCalls = 0;
        for (AbstractInsnNode instruction = runFocusPackage.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (call.getOpcode() == Opcodes.INVOKEVIRTUAL
                && FOCUS_EFFECT.equals(call.owner)
                && "execute".equals(call.name)
                && FOCUS_EXECUTE_DESC.equals(call.desc)) {
                executeCall = call;
                executeCalls++;
            }
        }
        if (executeCalls != 1) {
            System.out.println("TT2 found an unexpected number of Thaumcraft focus effect calls: "
                + executeCalls + ".");
            return 0;
        }

        InsnList capture = new InsnList();
        capture.add(new VarInsnNode(Opcodes.ALOAD, 0));
        capture.add(new MethodInsnNode(
            Opcodes.INVOKESTATIC,
            THAUMCRAFT_HOOKS,
            "captureFocusPackage",
            "(Ljava/lang/Object;)V",
            false
        ));
        runFocusPackage.instructions.insertBefore(runFocusPackage.instructions.getFirst(), capture);

        executeCall.setOpcode(Opcodes.INVOKESTATIC);
        executeCall.owner = THAUMCRAFT_HOOKS;
        executeCall.name = "executeFocusEffect";
        executeCall.desc = "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;FI)Z";
        executeCall.itf = false;
        return 2;
    }

    protected int tryTransformThaumcraftFocusDamage(ClassNode classNode) {
        int injected = 0;
        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode instruction = method.instructions.getFirst();
                 instruction != null; instruction = instruction.getNext()) {
                if (!(instruction instanceof MethodInsnNode)) {
                    continue;
                }
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (call.getOpcode() != Opcodes.INVOKEVIRTUAL
                    || !("attackEntityFrom".equals(call.name) || "func_70097_a".equals(call.name))
                    || !"(Lnet/minecraft/util/DamageSource;F)Z".equals(call.desc)
                    || !call.owner.startsWith("net/minecraft/entity/")) {
                    continue;
                }
                call.setOpcode(Opcodes.INVOKESTATIC);
                call.owner = THAUMCRAFT_HOOKS;
                call.name = "attackFocusTarget";
                call.desc = "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/DamageSource;F)Z";
                call.itf = false;
                injected++;
            }
        }
        return injected;
    }

    private static void transformItemLookup(MethodInsnNode call) {
        call.setOpcode(Opcodes.INVOKESTATIC);
        call.owner = THAUMCRAFT_HOOKS;
        call.name = "getCompatItem";
        call.desc = "(Lnet/minecraft/item/ItemStack;)Ljava/lang/Object;";
        call.itf = false;
    }

    private static boolean isItemLookup(MethodInsnNode call) {
        return call.getOpcode() == Opcodes.INVOKEVIRTUAL
            && ITEM_STACK.equals(call.owner)
            && ("getItem".equals(call.name) || "func_77973_b".equals(call.name))
            && "()Lnet/minecraft/item/Item;".equals(call.desc);
    }

    private static boolean isCasterTypeCheck(AbstractInsnNode instruction) {
        if (!(instruction instanceof TypeInsnNode)
            || instruction.getOpcode() != Opcodes.INSTANCEOF
                && instruction.getOpcode() != Opcodes.CHECKCAST) {
            return false;
        }
        String type = ((TypeInsnNode) instruction).desc;
        return ICASTER.equals(type) || IARCHITECT.equals(type);
    }

    private static boolean isExchangeCasterTypeCheck(AbstractInsnNode instruction) {
        return instruction instanceof TypeInsnNode
            && (instruction.getOpcode() == Opcodes.INSTANCEOF
                || instruction.getOpcode() == Opcodes.CHECKCAST)
            && (ITEM_CASTER.equals(((TypeInsnNode) instruction).desc)
                || ICASTER.equals(((TypeInsnNode) instruction).desc));
    }

    private static MethodInsnNode findPickedBlockCall(TypeInsnNode typeCheck) {
        for (AbstractInsnNode instruction = nextCodeInstruction(typeCheck);
             instruction != null; instruction = nextCodeInstruction(instruction)) {
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if ((call.getOpcode() != Opcodes.INVOKEVIRTUAL
                        && call.getOpcode() != Opcodes.INVOKEINTERFACE)
                    || (!ITEM_CASTER.equals(call.owner) && !ICASTER.equals(call.owner))
                    || !"getPickedBlock".equals(call.name)
                    || !"(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;".equals(call.desc)) {
                    return null;
                }
                return call;
            }
            if (instruction instanceof JumpInsnNode || instruction.getOpcode() == Opcodes.RETURN
                || instruction.getOpcode() == Opcodes.ARETURN) {
                return null;
            }
        }
        return null;
    }

    private static void transformPickedBlockCall(MethodInsnNode call) {
        if (call == null) {
            return;
        }
        call.setOpcode(Opcodes.INVOKEINTERFACE);
        call.owner = ICASTER;
        call.itf = true;
    }

    private static boolean isThaumcraftFixPresent() {
        if (thaumcraftFixPresent == null) {
            thaumcraftFixPresent = ASMTinkerAnimate.class.getClassLoader()
                .getResource(THAUMCRAFT_FIX_CORE_RESOURCE) != null;
        }
        return thaumcraftFixPresent;
    }

    private static boolean isHoldingItemLookup(MethodInsnNode call) {
        return call.getOpcode() == Opcodes.INVOKESTATIC
            && "thaumcraft/common/lib/utils/InventoryUtils".equals(call.owner)
            && "isHoldingItem".equals(call.name)
            && "(Lnet/minecraft/entity/player/EntityPlayer;Ljava/lang/Class;)"
                .concat("Lnet/minecraft/inventory/EntityEquipmentSlot;").equals(call.desc);
    }

    private static boolean loadsCasterClass(AbstractInsnNode instruction) {
        return instruction instanceof LdcInsnNode
            && ((LdcInsnNode) instruction).cst instanceof Type
            && ICASTER.equals(((Type) ((LdcInsnNode) instruction).cst).getInternalName());
    }

    private static AbstractInsnNode nextCodeInstruction(AbstractInsnNode instruction) {
        AbstractInsnNode next = instruction.getNext();
        while (next != null && next.getOpcode() < 0) {
            next = next.getNext();
        }
        return next;
    }

    private static AbstractInsnNode previousCodeInstruction(AbstractInsnNode instruction) {
        AbstractInsnNode previous = instruction.getPrevious();
        while (previous != null && previous.getOpcode() < 0) {
            previous = previous.getPrevious();
        }
        return previous;
    }

    private static final class ExchangeCasterCheck {
        private final MethodInsnNode lookup;
        private final TypeInsnNode typeCheck;
        private final MethodInsnNode pickedBlockCall;

        private ExchangeCasterCheck(MethodInsnNode lookup, TypeInsnNode typeCheck,
                                    MethodInsnNode pickedBlockCall) {
            this.lookup = lookup;
            this.typeCheck = typeCheck;
            this.pickedBlockCall = pickedBlockCall;
        }
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

    protected boolean tryTransformModelBiped(ClassNode classNode) {
        MethodNode target = null;
        for (MethodNode node : classNode.methods) {
            if ((node.name.equals("setRotationAngles") || node.name.equals("func_78087_a"))
                && node.desc.equals("(FFFFFFLnet/minecraft/entity/Entity;)V")) {
                target = node;
                break;
            }
        }
        if (target == null) {
            System.out.println("TT2 could not find ModelBiped#setRotationAngles; Spear arm animation is disabled.");
            return false;
        }

        int injected = 0;
        for (AbstractInsnNode instruction = target.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (instruction.getOpcode() != Opcodes.RETURN) {
                continue;
            }
            InsnList hook = new InsnList();
            hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
            hook.add(new VarInsnNode(Opcodes.ALOAD, 7));
            hook.add(new VarInsnNode(Opcodes.FLOAD, 3));
            hook.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "xy177/tt2/client/SpearAnimationHooks",
                "applyThirdPersonPose",
                "(Lnet/minecraft/client/model/ModelBiped;Lnet/minecraft/entity/Entity;F)V",
                false
            ));
            target.instructions.insertBefore(instruction, hook);
            injected++;
        }
        if (injected == 0) {
            System.out.println("TT2 found ModelBiped#setRotationAngles without a return instruction.");
            return false;
        }
        System.out.println("TT2 installed the Spear third-person arm animation hook.");
        return true;
    }

    protected boolean tryTransformLivingRendererOutlineColor(ClassNode classNode) {
        int injected = 0;
        for (MethodNode node : classNode.methods) {
            if (!(node.name.equals("doRender") || node.name.equals("func_76986_a"))
                || !node.desc.equals("(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V")) {
                continue;
            }
            for (AbstractInsnNode instruction = node.instructions.getFirst();
                 instruction != null; instruction = instruction.getNext()) {
                if (!(instruction instanceof MethodInsnNode)) {
                    continue;
                }
                MethodInsnNode method = (MethodInsnNode) instruction;
                if (method.getOpcode() != Opcodes.INVOKEVIRTUAL
                    || !(method.name.equals("getTeamColor")
                        || method.name.equals("func_188298_c"))
                    || !method.desc.equals("(Lnet/minecraft/entity/Entity;)I")) {
                    continue;
                }
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                hook.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "xy177/tt2/client/ImbalanceGlowHooks",
                    "overrideOutlineColor",
                    "(ILnet/minecraft/entity/Entity;)I",
                    false
                ));
                node.instructions.insert(instruction, hook);
                injected++;
            }
        }
        if (injected == 0) {
            System.out.println("TT2 could not find RenderLivingBase's outline color call; custom status colors are disabled.");
            return false;
        }
        System.out.println("TT2 installed the lightweight status outline color hook.");
        return true;
    }

    protected boolean tryTransformOutlineSelection(ClassNode classNode) {
        int injected = 0;
        for (MethodNode node : classNode.methods) {
            if (!(node.name.equals("isOutlineActive") || node.name.equals("func_184383_a"))
                || !node.desc.equals("(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;"
                    + "Lnet/minecraft/client/renderer/culling/ICamera;)Z")) {
                continue;
            }
            for (AbstractInsnNode instruction = node.instructions.getFirst();
                 instruction != null; instruction = instruction.getNext()) {
                if (instruction.getOpcode() != Opcodes.IRETURN) {
                    continue;
                }
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                hook.add(new VarInsnNode(Opcodes.ALOAD, 2));
                hook.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "xy177/tt2/client/ImbalanceGlowHooks",
                    "overrideOutlineSelection",
                    "(ZLnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;)Z",
                    false
                ));
                node.instructions.insertBefore(instruction, hook);
                injected++;
            }
        }
        if (injected == 0) {
            System.out.println("TT2 could not find RenderGlobal's outline selection method.");
            return false;
        }
        System.out.println("TT2 installed the direct status outline selection hook.");
        return true;
    }

    protected boolean tryTransformToolBuilder(ClassNode classNode) {
        MethodNode target = null;
        for (MethodNode node : classNode.methods) {
            if (node.name.equals("tryModifyTool")
                && node.desc.equals("(Lnet/minecraft/util/NonNullList;Lnet/minecraft/item/ItemStack;Z)"
                    + "Lnet/minecraft/item/ItemStack;")) {
                target = node;
                break;
            }
        }
        if (target == null) {
            System.out.println("TT2 could not find ToolBuilder#tryModifyTool; Spear modifier priority is disabled.");
            return false;
        }

        int injected = 0;
        for (AbstractInsnNode instruction = target.instructions.getFirst();
             instruction != null; instruction = instruction.getNext()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }
            MethodInsnNode method = (MethodInsnNode) instruction;
            if (method.getOpcode() != Opcodes.INVOKESTATIC
                || !method.owner.equals("slimeknights/tconstruct/library/TinkerRegistry")
                || !method.name.equals("getAllModifiers")
                || !method.desc.equals("()Ljava/util/Collection;")) {
                continue;
            }

            InsnList hook = new InsnList();
            hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
            hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
            hook.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "xy177/tt2/modifiers/SpearModifierRecipeHooks",
                "filterModifiers",
                "(Ljava/util/Collection;Lnet/minecraft/util/NonNullList;"
                    + "Lnet/minecraft/item/ItemStack;)Ljava/util/Collection;",
                false
            ));
            target.instructions.insert(instruction, hook);
            injected++;
        }
        if (injected != 1) {
            System.out.println("TT2 found an unexpected number of ToolBuilder modifier loops: " + injected);
            return false;
        }
        System.out.println("TT2 installed the Spear modifier priority hook.");
        return true;
    }

}
