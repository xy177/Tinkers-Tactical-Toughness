package xy177.tt2.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;
import xy177.tt2.TT2;
import xy177.tt2.compat.ThaumcraftInsightHooks;

import java.lang.reflect.Field;

/** Extends ThaumcraftFix's caster-only key contexts to insight-upgraded staffs. */
public final class ThaumcraftFixKeybindCompat {

    private static final String KEY_HANDLER = "thaumcraft.common.lib.events.KeyHandler";
    private static boolean installed;

    private ThaumcraftFixKeybindCompat() {
    }

    public static void install() {
        if (installed) {
            return;
        }
        try {
            ClassLoader loader = ThaumcraftFixKeybindCompat.class.getClassLoader();
            Class<?> keyHandler = Class.forName(KEY_HANDLER, true, loader);
            KeyBinding focusKey = getKeyBinding(keyHandler, "keyF");
            KeyBinding toggleKey = getKeyBinding(keyHandler, "keyG");

            focusKey.setKeyConflictContext(wrap(focusKey.getKeyConflictContext(), false));
            toggleKey.setKeyConflictContext(wrap(toggleKey.getKeyConflictContext(), true));
            installed = true;
            TT2.logger.info("TT2 extended ThaumcraftFix caster key contexts for the Craftsman's Staff.");
        } catch (ReflectiveOperationException | LinkageError | RuntimeException failure) {
            TT2.logger.warn("TT2 could not extend ThaumcraftFix caster key contexts; "
                + "staff focus selection may be unavailable.", failure);
        }
    }

    private static KeyBinding getKeyBinding(Class<?> keyHandler, String fieldName)
        throws ReflectiveOperationException {
        Field field = keyHandler.getField(fieldName);
        Object value = field.get(null);
        if (!(value instanceof KeyBinding)) {
            throw new IllegalStateException(KEY_HANDLER + "." + fieldName + " is not a KeyBinding");
        }
        return (KeyBinding) value;
    }

    private static IKeyConflictContext wrap(IKeyConflictContext original, boolean requireGameFocus) {
        return original instanceof CompatContext
            ? original : new CompatContext(original, requireGameFocus);
    }

    private static final class CompatContext implements IKeyConflictContext {
        private final IKeyConflictContext original;
        private final boolean requireGameFocus;

        private CompatContext(IKeyConflictContext original, boolean requireGameFocus) {
            if (original == null) {
                throw new IllegalArgumentException("original key conflict context cannot be null");
            }
            this.original = original;
            this.requireGameFocus = requireGameFocus;
        }

        @Override
        public boolean isActive() {
            return original.isActive() || isCompatCasterActive();
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return original.conflicts(unwrap(other));
        }

        private static IKeyConflictContext unwrap(IKeyConflictContext context) {
            return context instanceof CompatContext ? ((CompatContext) context).original : context;
        }

        private boolean isCompatCasterActive() {
            Minecraft minecraft = Minecraft.getMinecraft();
            EntityPlayerSP player = minecraft.player;
            return player != null && (!requireGameFocus || minecraft.inGameHasFocus)
                && ThaumcraftInsightHooks.isHoldingCompatCaster(player);
        }
    }
}
