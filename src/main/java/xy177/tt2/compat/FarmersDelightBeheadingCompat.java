package xy177.tt2.compat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import xy177.tt2.TT2;
import xy177.tt2.config.TT2Config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Bridges Tinkers' Construct tools with Beheading into Farmer's Delight's
 * existing hunting-drop manager without making Farmer's Delight a hard
 * dependency.
 */
public final class FarmersDelightBeheadingCompat {

    private static final String KNIFE_CLASS =
        "com.wdcftgg.farmersdelightlegacy.common.item.ItemKnife";
    private static final String KNIFE_API_CLASS =
        "com.wdcftgg.farmersdelightlegacy.api.knife.KnifeItemApi";
    private static final String HUNTING_MANAGER_CLASS =
        "com.wdcftgg.farmersdelightlegacy.common.recipe.manager.HuntingDropRecipeManager";

    private static Method isKnife;
    private static Method getKnifeStacks;
    private static Method addJeiDisplayStack;
    private static Method addDrops;
    private static ItemStack representativeKnife;
    private static boolean reflectionReady;
    private static boolean unavailable;
    private static boolean invocationFailureLogged;

    private FarmersDelightBeheadingCompat() {
    }

    public static void register() {
        if (!TT2Config.enableFarmersDelightBeheadingDrops || unavailable) {
            return;
        }

        try {
            initializeReflection();
            registerCleaverJeiDisplays();
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(
                new FarmersDelightBeheadingCompatEventHandler()
            );
        } catch (ReflectiveOperationException e) {
            unavailable = true;
            log("Unable to initialize Farmer's Delight hunting-drop compatibility; the bridge is disabled.", e);
        }
    }

    private static synchronized void initializeReflection() throws ReflectiveOperationException {
        if (reflectionReady) {
            return;
        }

        Class<?> knifeClass = Class.forName(KNIFE_CLASS);
        Class<?> knifeApiClass = Class.forName(KNIFE_API_CLASS);
        Class<?> huntingManagerClass = Class.forName(HUNTING_MANAGER_CLASS);

        isKnife = knifeClass.getMethod("isKnife", ItemStack.class);
        getKnifeStacks = knifeApiClass.getMethod("getHuntingAndHarvestKnifeStacks");
        addJeiDisplayStack = knifeApiClass.getMethod("addJeiDisplayStack", ItemStack.class);
        addDrops = huntingManagerClass.getMethod(
            "addDrops",
            LivingDropsEvent.class,
            EntityLivingBase.class,
            ItemStack.class
        );
        reflectionReady = true;
    }

    private static void registerCleaverJeiDisplays() {
        for (ToolCore tool : TinkerRegistry.getTools()) {
            if (!isCleaver(tool)) {
                continue;
            }

            ItemStack displayStack = findDisplayStack(tool);
            if (displayStack.isEmpty()) {
                continue;
            }

            try {
                addJeiDisplayStack.invoke(null, displayStack);
            } catch (IllegalAccessException | InvocationTargetException e) {
                // JEI display is optional; keep the actual hunting-drop bridge
                // active even if a particular display stack cannot be added.
                log("Unable to add a Tinkers' Construct cleaver to Farmer's Delight JEI knife displays.", e);
            }
        }
    }

    private static boolean isCleaver(ToolCore tool) {
        String identifier = tool.getIdentifier();
        return identifier != null
            && (identifier.equals("cleaver") || identifier.endsWith(":cleaver"));
    }

    private static ItemStack findDisplayStack(ToolCore tool) {
        NonNullList<ItemStack> candidates = NonNullList.create();
        try {
            tool.getSubItems(TinkerRegistry.tabTools, candidates);
        } catch (RuntimeException ignored) {
            // Fall back to the bare registered item below. Some addon tools
            // do not expose creative-tab variants during early initialization.
        }
        if (!candidates.isEmpty() && !candidates.get(0).isEmpty()) {
            return candidates.get(0).copy();
        }
        return new ItemStack(tool);
    }

    private static final class FarmersDelightBeheadingCompatEventHandler {

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onLivingDrops(LivingDropsEvent event) {
            if (!reflectionReady || unavailable || event.getEntityLiving().world.isRemote) {
                return;
            }

            EntityLivingBase killer = getLivingKiller(event.getSource());
            if (killer == null) {
                return;
            }

            ItemStack tool = killer.getHeldItemMainhand();
            if (tool.isEmpty() || !(tool.getItem() instanceof ToolCore)
                || !hasBeheading(tool)) {
                return;
            }

            // Farmer's Delight may already handle a registered knife. Do not
            // run its manager twice in that case.
            if (isFarmerKnife(tool)) {
                return;
            }

            ItemStack knife = getRepresentativeKnife();
            if (knife.isEmpty()) {
                return;
            }

            try {
                addDrops.invoke(null, event, killer, knife);
            } catch (IllegalAccessException | InvocationTargetException e) {
                unavailable = true;
                if (!invocationFailureLogged) {
                    invocationFailureLogged = true;
                    log("Farmer's Delight hunting-drop compatibility failed during drop handling; the bridge is disabled.", e);
                }
            }
        }
    }

    private static EntityLivingBase getLivingKiller(DamageSource source) {
        if (source == null) {
            return null;
        }
        Entity trueSource = source.getTrueSource();
        return trueSource instanceof EntityLivingBase ? (EntityLivingBase) trueSource : null;
    }

    private static boolean hasBeheading(ItemStack stack) {
        return hasIdentifier(TagUtil.getTraitsTagList(stack), "beheading")
            || hasIdentifier(TagUtil.getTraitsTagList(stack), "beheading_cleaver")
            || hasModifier(stack, "beheading")
            || hasModifier(stack, "beheading_cleaver");
    }

    private static boolean hasModifier(ItemStack stack, String identifier) {
        return TinkerUtil.getIndexInCompoundList(TagUtil.getModifiersTagList(stack), identifier) >= 0
            || TinkerUtil.getIndexInList(TagUtil.getBaseModifiersTagList(stack), identifier) >= 0;
    }

    private static boolean hasIdentifier(NBTTagList list, String identifier) {
        for (int i = 0; i < list.tagCount(); i++) {
            if (identifier.equals(list.getStringTagAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isFarmerKnife(ItemStack stack) {
        try {
            return Boolean.TRUE.equals(isKnife.invoke(null, stack));
        } catch (IllegalAccessException | InvocationTargetException e) {
            unavailable = true;
            log("Unable to check Farmer's Delight knife compatibility; the bridge is disabled.", e);
            return false;
        }
    }

    private static synchronized ItemStack getRepresentativeKnife() {
        if (representativeKnife != null && !representativeKnife.isEmpty()
            && isFarmerKnife(representativeKnife)) {
            return representativeKnife.copy();
        }

        try {
            Object result = getKnifeStacks.invoke(null);
            if (result instanceof List) {
                for (Object value : (List<?>) result) {
                    if (!(value instanceof ItemStack)) {
                        continue;
                    }
                    ItemStack candidate = (ItemStack) value;
                    if (!candidate.isEmpty() && isFarmerKnife(candidate)) {
                        representativeKnife = candidate.copy();
                        return representativeKnife.copy();
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            unavailable = true;
            log("Unable to find a Farmer's Delight knife stack for hunting-drop compatibility; the bridge is disabled.", e);
        }
        return ItemStack.EMPTY;
    }

    private static void log(String message, Exception error) {
        if (TT2.logger != null) {
            TT2.logger.warn(message, error);
        }
    }
}
