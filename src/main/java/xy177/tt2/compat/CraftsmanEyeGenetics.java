package xy177.tt2.compat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;
import xy177.tt2.TT2;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Reflection-only bridge to Forestry's genetic data API for the Craftsman's Eye. */
public final class CraftsmanEyeGenetics {

    private CraftsmanEyeGenetics() {
    }

    public static boolean isAvailable() {
        return Loader.isModLoaded("forestry");
    }

    public static ItemStack normalize(ItemStack specimen) {
        if (!isAvailable() || specimen == null || specimen.isEmpty()) {
            return specimen;
        }
        try {
            Class<?> utilityClass = Class.forName("forestry.core.utils.GeneticsUtil");
            Method convert = utilityClass.getMethod("convertToGeneticEquivalent", ItemStack.class);
            Object converted = convert.invoke(null, specimen);
            return converted instanceof ItemStack ? (ItemStack) converted : specimen;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return specimen;
        }
    }

    public static boolean isAnalyzable(ItemStack specimen) {
        return getIndividual(specimen) != null;
    }

    public static boolean isAnalyzed(ItemStack specimen) {
        Object individual = getIndividual(specimen);
        return individual != null && booleanValue(invokeQuietly(individual, "isAnalyzed"));
    }

    public static boolean analyze(EntityPlayer player, ItemStack specimen) {
        if (player == null || specimen == null || specimen.isEmpty()) {
            return false;
        }
        Object root = getSpeciesRoot(specimen);
        Object individual = getIndividual(root, specimen);
        if (root == null || individual == null) {
            return false;
        }
        if (booleanValue(invokeQuietly(individual, "isAnalyzed"))) {
            return true;
        }
        Object analyzeResult = invokeQuietly(individual, "analyze");
        if (!(analyzeResult instanceof Boolean) || !((Boolean) analyzeResult)) {
            return false;
        }

        // Persist the analyzed flag before touching optional tracker bookkeeping.
        NBTTagCompound tag = new NBTTagCompound();
        try {
            Object result = invoke(individual, "writeToNBT", tag);
            specimen.setTagCompound(result instanceof NBTTagCompound ? (NBTTagCompound) result : tag);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError failure) {
            if (TT2.logger != null) {
                TT2.logger.warn("Could not persist a Craftsman's Eye analysis", failure);
            }
            return false;
        }

        registerSpeciesBestEffort(root, individual, player);
        return true;
    }

    private static void registerSpeciesBestEffort(Object root, Object individual, EntityPlayer player) {
        try {
            Object genome = invoke(individual, "getGenome");
            Object tracker = invoke(root, "getBreedingTracker", player.world, player.getGameProfile());
            if (genome == null || tracker == null) {
                return;
            }
            Object primary = invoke(genome, "getPrimary");
            Object secondary = invoke(genome, "getSecondary");
            if (primary != null) {
                invoke(tracker, "registerSpecies", primary);
            }
            if (secondary != null) {
                invoke(tracker, "registerSpecies", secondary);
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError failure) {
            // Discovery bookkeeping is optional; the analyzed NBT is already persisted.
            if (TT2.logger != null) {
                TT2.logger.debug("Could not update the Forestry breeding tracker", failure);
            }
        }
    }

    public static SpecimenInfo describe(ItemStack specimen) {
        Object root = getSpeciesRoot(specimen);
        Object individual = getIndividual(root, specimen);
        if (root == null || individual == null) {
            return SpecimenInfo.EMPTY;
        }

        String displayName = stringValue(invokeQuietly(individual, "getDisplayName"));
        if (displayName.isEmpty() && specimen != null && !specimen.isEmpty()) {
            displayName = specimen.getDisplayName();
        }
        String identifier = stringValue(invokeQuietly(individual, "getIdent"));
        String rootId = stringValue(invokeQuietly(root, "getUID"));
        boolean analyzed = booleanValue(invokeQuietly(individual, "isAnalyzed"));
        Object genome = invokeQuietly(individual, "getGenome");
        String primary = alleleName(invokeQuietly(genome, "getPrimary"));
        String secondary = alleleName(invokeQuietly(genome, "getSecondary"));
        List<ChromosomeInfo> chromosomes = describeChromosomes(root, genome);
        List<String> tooltip = new ArrayList<>();
        try {
            invoke(individual, "addTooltip", tooltip);
        } catch (ReflectiveOperationException ignored) {
            // Summary pages remain useful when an add-on supplies no tooltip data.
        }
        return new SpecimenInfo(true, analyzed, displayName, identifier, rootId, primary, secondary,
            chromosomes, tooltip);
    }

    private static List<ChromosomeInfo> describeChromosomes(Object root, Object genome) {
        if (root == null || genome == null) {
            return Collections.emptyList();
        }
        Object chromosomeArray = invokeQuietly(genome, "getChromosomes");
        if (chromosomeArray == null || !chromosomeArray.getClass().isArray()) {
            return Collections.emptyList();
        }
        Object typeArray = invokeQuietly(root, "getKaryotype");
        int length = Array.getLength(chromosomeArray);
        List<ChromosomeInfo> result = new ArrayList<>(length);
        for (int index = 0; index < length; index++) {
            Object chromosome = Array.get(chromosomeArray, index);
            Object type = typeArray != null && typeArray.getClass().isArray() && index < Array.getLength(typeArray)
                ? Array.get(typeArray, index) : null;
            String typeName = stringValue(invokeQuietly(type, "getName"));
            if (typeName.isEmpty()) {
                typeName = "#" + (index + 1);
            }
            String active = alleleName(invokeQuietly(chromosome, "getActiveAllele"));
            String inactive = alleleName(invokeQuietly(chromosome, "getInactiveAllele"));
            result.add(new ChromosomeInfo(typeName, active, inactive));
        }
        return result;
    }

    private static Object getSpeciesRoot(ItemStack specimen) {
        if (!isAvailable() || specimen == null || specimen.isEmpty()) {
            return null;
        }
        try {
            Class<?> managerClass = Class.forName("forestry.api.genetics.AlleleManager");
            Field registryField = managerClass.getField("alleleRegistry");
            Object registry = registryField.get(null);
            return registry == null ? null : invoke(registry, "getSpeciesRoot", specimen);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private static Object getIndividual(ItemStack specimen) {
        return getIndividual(getSpeciesRoot(specimen), specimen);
    }

    private static Object getIndividual(Object root, ItemStack specimen) {
        if (root == null || specimen == null || specimen.isEmpty()) {
            return null;
        }
        return invokeQuietly(root, "getMember", specimen);
    }

    private static String alleleName(Object allele) {
        String name = stringValue(invokeQuietly(allele, "getAlleleName"));
        if (name.isEmpty()) {
            name = stringValue(invokeQuietly(allele, "getName"));
        }
        if (name.isEmpty()) {
            name = stringValue(invokeQuietly(allele, "getUID"));
        }
        return name;
    }

    private static Object invokeQuietly(Object target, String name, Object... arguments) {
        try {
            return invoke(target, name, arguments);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    private static Object invoke(Object target, String name, Object... arguments)
        throws ReflectiveOperationException {
        if (target == null) {
            throw new NoSuchMethodException(name + " on null");
        }
        for (Method method : target.getClass().getMethods()) {
            if (method.getName().equals(name) && accepts(method.getParameterTypes(), arguments)) {
                return method.invoke(target, arguments);
            }
        }
        throw new NoSuchMethodException(target.getClass().getName() + "." + name);
    }

    private static boolean accepts(Class<?>[] parameterTypes, Object[] arguments) {
        if (parameterTypes.length != arguments.length) {
            return false;
        }
        for (int index = 0; index < parameterTypes.length; index++) {
            if (arguments[index] != null && !boxed(parameterTypes[index]).isInstance(arguments[index])) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> boxed(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == char.class) return Character.class;
        return type;
    }

    private static boolean booleanValue(Object value) {
        return value instanceof Boolean && (Boolean) value;
    }

    private static String stringValue(Object value) {
        return value instanceof String ? (String) value : "";
    }

    public static final class SpecimenInfo {
        public static final SpecimenInfo EMPTY = new SpecimenInfo(false, false, "", "", "", "", "",
            Collections.emptyList(), Collections.emptyList());

        public final boolean analyzable;
        public final boolean analyzed;
        public final String displayName;
        public final String identifier;
        public final String rootId;
        public final String primary;
        public final String secondary;
        public final List<ChromosomeInfo> chromosomes;
        public final List<String> tooltip;

        private SpecimenInfo(boolean analyzable, boolean analyzed, String displayName, String identifier,
                             String rootId, String primary, String secondary,
                             List<ChromosomeInfo> chromosomes, List<String> tooltip) {
            this.analyzable = analyzable;
            this.analyzed = analyzed;
            this.displayName = displayName;
            this.identifier = identifier;
            this.rootId = rootId;
            this.primary = primary;
            this.secondary = secondary;
            this.chromosomes = chromosomes;
            this.tooltip = tooltip;
        }
    }

    public static final class ChromosomeInfo {
        public final String name;
        public final String active;
        public final String inactive;

        private ChromosomeInfo(String name, String active, String inactive) {
            this.name = name;
            this.active = active;
            this.inactive = inactive;
        }
    }
}
