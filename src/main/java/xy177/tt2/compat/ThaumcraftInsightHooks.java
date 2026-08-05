package xy177.tt2.compat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import xy177.tt2.modifiers.ModCraftsmanStaffTemplate;
import xy177.tt2.tools.CraftsmanStaff;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/** Runtime-only Thaumcraft interface adapter for insight-upgraded Tinker's Staffs. */
public final class ThaumcraftInsightHooks {

    private static final String CASTER_INTERFACE = "thaumcraft.api.casters.ICaster";
    private static final String ARCHITECT_INTERFACE = "thaumcraft.api.items.IArchitect";
    private static volatile RuntimeAdapter runtimeAdapter;
    private static volatile boolean runtimeUnavailable;

    private ThaumcraftInsightHooks() {
    }

    public static Object getCompatItem(ItemStack stack) {
        if (stack == null) {
            return null;
        }
        Item item = stack.getItem();
        if (!(item instanceof CraftsmanStaff)
            || !ModCraftsmanStaffTemplate.has(stack, ModCraftsmanStaffTemplate.Type.INSIGHT)) {
            return item;
        }
        Item gauntlet = CraftsmanStaffCompat.getThaumcraftGauntlet();
        if (gauntlet == null) {
            return item;
        }
        RuntimeAdapter adapter = getRuntimeAdapter();
        return adapter == null || !adapter.caster.isInstance(gauntlet) ? item : adapter.proxy;
    }

    public static void captureFocusPackage(Object focusPackage) {
        CraftsmanStaffCompat.captureThaumcraftFocusPackage(focusPackage);
    }

    public static boolean executeFocusEffect(Object effect, Object target, Object trajectory,
                                             float power, int targetIndex) {
        return CraftsmanStaffCompat.executeThaumcraftFocusEffect(
            effect, target, trajectory, power, targetIndex);
    }

    public static boolean attackFocusTarget(Entity target, DamageSource source, float amount) {
        return CraftsmanStaffCompat.attackThaumcraftFocusTarget(target, source, amount);
    }

    @Nullable
    public static EntityEquipmentSlot isHoldingCaster(EntityPlayer player, Class<?> casterClass) {
        if (player == null || casterClass == null) {
            return null;
        }
        if (casterClass.isInstance(getCompatItem(player.getHeldItemMainhand()))) {
            return EntityEquipmentSlot.MAINHAND;
        }
        if (casterClass.isInstance(getCompatItem(player.getHeldItemOffhand()))) {
            return EntityEquipmentSlot.OFFHAND;
        }
        return null;
    }

    public static boolean isHoldingCompatCaster(EntityPlayer player) {
        RuntimeAdapter adapter = getRuntimeAdapter();
        return adapter != null && isHoldingCaster(player, adapter.caster) != null;
    }

    @Nullable
    private static RuntimeAdapter getRuntimeAdapter() {
        RuntimeAdapter current = runtimeAdapter;
        if (current != null || runtimeUnavailable) {
            return current;
        }
        synchronized (ThaumcraftInsightHooks.class) {
            current = runtimeAdapter;
            if (current != null || runtimeUnavailable) {
                return current;
            }
            try {
                ClassLoader loader = ThaumcraftInsightHooks.class.getClassLoader();
                Class<?> caster = Class.forName(CASTER_INTERFACE, false, loader);
                Class<?> architect = Class.forName(ARCHITECT_INTERFACE, false, loader);
                current = new RuntimeAdapter(caster, architect);
                runtimeAdapter = current;
                return current;
            } catch (ClassNotFoundException | LinkageError | RuntimeException unavailable) {
                runtimeUnavailable = true;
                return null;
            }
        }
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive() || type == void.class) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0.0F;
        }
        return 0.0D;
    }

    private static final class RuntimeAdapter implements InvocationHandler {
        private final Class<?> caster;
        private final Class<?> focus;
        private final Object proxy;

        private RuntimeAdapter(Class<?> caster, Class<?> architect) throws ClassNotFoundException {
            this.caster = caster;
            this.focus = Class.forName("thaumcraft.common.items.casters.ItemFocus", false,
                caster.getClassLoader());
            this.proxy = Proxy.newProxyInstance(caster.getClassLoader(),
                new Class<?>[]{caster, architect}, this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] arguments) {
            if (method.getDeclaringClass() == Object.class) {
                switch (method.getName()) {
                    case "equals":
                        return arguments != null && arguments.length == 1 && proxy == arguments[0];
                    case "hashCode":
                        return System.identityHashCode(proxy);
                    case "toString":
                        return "TT2 Thaumcraft insight caster adapter";
                    default:
                        return null;
                }
            }
            ItemStack staff = arguments != null && arguments.length > 0
                && arguments[0] instanceof ItemStack ? (ItemStack) arguments[0] : ItemStack.EMPTY;
            if (staff.isEmpty() || !(staff.getItem() instanceof CraftsmanStaff)
                || !ModCraftsmanStaffTemplate.has(staff, ModCraftsmanStaffTemplate.Type.INSIGHT)) {
                return defaultValue(method.getReturnType());
            }
            int argumentCount = arguments == null ? 0 : arguments.length;
            if ("getFocusStack".equals(method.getName()) && argumentCount == 1) {
                return CraftsmanStaffCompat.getThaumcraftFocusStack(staff);
            }
            if ("getFocus".equals(method.getName()) && argumentCount == 1) {
                ItemStack focusStack = CraftsmanStaffCompat.getThaumcraftFocusStack(staff);
                return focusStack != null && !focusStack.isEmpty()
                    && focus.isInstance(focusStack.getItem()) ? focusStack.getItem() : null;
            }
            if ("setFocus".equals(method.getName()) && argumentCount == 2
                && (arguments[1] == null || arguments[1] instanceof ItemStack)) {
                CraftsmanStaffCompat.setThaumcraftFocusStack(staff, (ItemStack) arguments[1]);
                return null;
            }
            Object result = CraftsmanStaffCompat.invokeThaumcraftGauntlet(staff, method, arguments);
            return result != null ? result : defaultValue(method.getReturnType());
        }
    }
}
