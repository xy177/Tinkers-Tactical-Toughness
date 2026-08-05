package xy177.tt2.compat;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.traits.ITrait;
import xy177.tt2.TT2;
import xy177.tt2.config.TT2Config;
import xy177.tt2.modifiers.ModCraftsmanStaffTemplate;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/** Optional-mod behavior for the Tinker's Staff. No optional mod class is referenced directly here. */
public final class CraftsmanStaffCompat {

    public static final CraftsmanStaffCompat EVENTS = new CraftsmanStaffCompat();
    private static final Map<EntityPlayer, AnalyzerBlockInteraction> ANALYZER_BLOCK_INTERACTIONS = new WeakHashMap<>();
    private static final Map<EntityPlayer, ItemStack> ACTIVE_EXTERNAL_CASTS = new WeakHashMap<>();
    private static final Map<EntityLivingBase, PendingNatureHit> PENDING_NATURE_HITS = new WeakHashMap<>();
    private static final Map<EntityLivingBase, PendingThaumcraftHit> PENDING_THAUMCRAFT_HITS =
        new WeakHashMap<>();
    private static final Map<Class<?>, Method> FORESTRY_LEAF_DROP_METHODS = new WeakHashMap<>();
    private static final Map<Object, StoredFocusCast> THAUMCRAFT_FOCUS_OBJECT_CONTEXTS =
        new WeakHashMap<>();
    private static final int MAX_THAUMCRAFT_FOCUS_CONTEXTS = 1024;
    private static final Map<FocusCastKey, StoredFocusCast> THAUMCRAFT_FOCUS_CONTEXTS =
        new LinkedHashMap<FocusCastKey, StoredFocusCast>(128, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<FocusCastKey, StoredFocusCast> eldest) {
                return size() > MAX_THAUMCRAFT_FOCUS_CONTEXTS;
            }
        };
    private static final ThreadLocal<ActiveFocusCast> ACTIVE_THAUMCRAFT_FOCUS = new ThreadLocal<>();
    private static final ThreadLocal<Integer> EXTERNAL_TRAIT_CALLBACK_DEPTH =
        new ThreadLocal<Integer>() {
            @Override
            protected Integer initialValue() {
                return 0;
            }
        };
    private static volatile ThaumcraftFocusAccess thaumcraftFocusAccess;
    private static volatile boolean thaumcraftFocusUnavailable;

    private static final String TAG_COMPAT = "TT2Compat";
    private static final String TAG_PROXY_ITEM_DAMAGE = "TT2ProxyItemDamage";
    private static final String TAG_EXTERNAL_DAMAGE_MULTIPLIER = "TT2ExternalDamageMultiplier";
    private static final String TAG_EXTERNAL_STAFF = "TT2ExternalStaff";
    private static final String TAG_EXTERNAL_CASTER_MOST = "TT2ExternalCasterMost";
    private static final String TAG_EXTERNAL_CASTER_LEAST = "TT2ExternalCasterLeast";
    private static final String TAG_NATURE_MODE = "TT2NatureMode";
    private static final String ITEM_BOTANIA_WAND = "botania:twigwand";
    private static final String ITEM_BOTANIA_GUN = "botania:managun";
    private static final String ITEM_THAUMCRAFT_GAUNTLET = "thaumcraft:caster_basic";
    private static final String ITEM_THAUMCRAFT_GAUNTLET_LEGACY = "thaumcraft:caster_gauntlet";
    private static final String ITEM_THAUMCRAFT_SALT = "thaumcraft:salis_mundus";
    private static final String TAG_THAUMCRAFT_GAUNTLET = "thaumcraft_gauntlet";
    private static final String TAG_THAUMCRAFT_FOCUS = "focus";
    private static final String ITEM_FORESTRY_WRENCH = "forestry:wrench";
    private static final String ITEM_FORESTRY_SCOOP = "forestry:scoop";
    private static final String ITEM_FORESTRY_SMOKER = "forestry:smoker";
    private static final String ITEM_FORESTRY_GRAFTER = "forestry:grafter";
    private static final String ITEM_FORESTRY_ALYZER = "forestry:portable_alyzer";
    private static final String ITEM_AE_NETWORK_TOOL = "appliedenergistics2:network_tool";
    private static final String ITEM_MEKANISM_CONFIGURATOR = "mekanism:configurator";

    public static final int NATURE_MODE_FUNCTION = 0;
    public static final int NATURE_MODE_BIND = 1;
    public static final int NATURE_MODE_MANA_BLASTER = 2;

    private CraftsmanStaffCompat() {
    }

    public interface Action<T> {
        T run(ItemStack staff);
    }

    public static final class ResearchTool {
        public final String modId;
        public final String modNameKey;
        public final String itemId;
        public final String fallbackToolName;

        private ResearchTool(String modId, String modNameKey, String itemId, String fallbackToolName) {
            this.modId = modId;
            this.modNameKey = modNameKey;
            this.itemId = itemId;
            this.fallbackToolName = fallbackToolName;
        }
    }

    public static boolean isLoaded(String modId) {
        return Loader.isModLoaded(modId);
    }

    public static boolean isNatureAvailable() {
        return isLoaded("botania");
    }

    public static boolean isInsightAvailable() {
        return isLoaded("thaumcraft");
    }

    public static boolean isResearchAvailable() {
        return isLoaded("forestry") || isLoaded("thermalfoundation") || isLoaded("appliedenergistics2")
            || isLoaded("mekanism") || isLoaded("actuallyadditions") || isLoaded("extrautils2")
            || isLoaded("enderio");
    }

    @Nullable
    public static Item getItem(String id) {
        if (id == null) {
            return null;
        }
        Item item = Item.getByNameOrId(id);
        return item == null || item.getRegistryName() == null
            || "minecraft:air".equals(item.getRegistryName().toString()) ? null : item;
    }

    public static List<ResearchTool> getResearchTools() {
        List<ResearchTool> tools = new ArrayList<>();
        addResearchTool(tools, "forestry", "tooltip.tt2.forging_template.research.mod.forestry",
            ITEM_FORESTRY_WRENCH, "扳手");
        addResearchTool(tools, "thermalfoundation", "tooltip.tt2.forging_template.research.mod.thermalfoundation",
            "thermalfoundation:wrench", "新月锤");
        addResearchTool(tools, "appliedenergistics2",
            "tooltip.tt2.forging_template.research.mod.appliedenergistics2",
            "appliedenergistics2:network_tool", "网络工具");
        addResearchTool(tools, "mekanism", "tooltip.tt2.forging_template.research.mod.mekanism",
            "mekanism:configurator", "调整器");
        addResearchTool(tools, "actuallyadditions",
            "tooltip.tt2.forging_template.research.mod.actuallyadditions",
            "actuallyadditions:item_laser_wrench", "中继器扳手");
        addResearchTool(tools, "extrautils2", "tooltip.tt2.forging_template.research.mod.extrautils2",
            "extrautils2:wrench", "扳手");
        addResearchTool(tools, "enderio", "tooltip.tt2.forging_template.research.mod.enderio",
            "enderio:item_yeta_wrench", "以太扳手");
        return Collections.unmodifiableList(tools);
    }

    private static void addResearchTool(List<ResearchTool> tools, String modId, String modNameKey,
                                        String itemId, String fallbackToolName) {
        if (isLoaded(modId) && getItem(itemId) != null) {
            tools.add(new ResearchTool(modId, modNameKey, itemId, fallbackToolName));
        }
    }

    public static List<ResearchTool> getResearchToolsForTarget(World world, BlockPos pos) {
        List<ResearchTool> tools = new ArrayList<>(getResearchTools());
        if (world == null || pos == null) {
            return tools;
        }
        ResourceLocation registryName = world.getBlockState(pos).getBlock().getRegistryName();
        if (registryName == null) {
            return tools;
        }
        String namespace = registryName.getNamespace();
        List<ResearchTool> ordered = new ArrayList<>(tools.size());
        for (ResearchTool tool : tools) {
            if (ownsTarget(tool.modId, namespace)) {
                ordered.add(tool);
            }
        }
        for (ResearchTool tool : tools) {
            if (!ownsTarget(tool.modId, namespace)) {
                ordered.add(tool);
            }
        }
        return ordered;
    }

    private static boolean ownsTarget(String modId, String namespace) {
        return modId.equals(namespace)
            || "thermalfoundation".equals(modId) && namespace.startsWith("thermal");
    }

    public static void addResearchTooltip(List<String> tooltip) {
        for (ResearchTool tool : getResearchTools()) {
            Item item = getItem(tool.itemId);
            String itemName = item == null ? tool.fallbackToolName : new ItemStack(item).getDisplayName();
            if (ITEM_FORESTRY_WRENCH.equals(tool.itemId)) {
                itemName = itemName + " / " + getItemName(ITEM_FORESTRY_SCOOP, "捕蜂网")
                    + " / " + getItemName(ITEM_FORESTRY_SMOKER, "烟熏工具")
                    + " / " + getItemName(ITEM_FORESTRY_GRAFTER, "剪枝器")
                    + " / " + getItemName(ITEM_FORESTRY_ALYZER, "便携分析仪");
            }
            String modName = net.minecraft.util.text.translation.I18n.translateToLocal(tool.modNameKey);
            if (modName.equals(tool.modNameKey)) {
                modName = tool.modNameKey.substring(tool.modNameKey.lastIndexOf('.') + 1);
            }
            tooltip.add(net.minecraft.util.text.TextFormatting.GRAY + modName + ": " + itemName);
        }
    }

    private static String getItemName(String itemId, String fallback) {
        Item item = getItem(itemId);
        return item == null ? fallback : new ItemStack(item).getDisplayName();
    }

    public static Item getBotaniaWand() {
        return getFirstItem(ITEM_BOTANIA_WAND, "botania:twig_wand");
    }

    public static Item getBotaniaGun() {
        return getFirstItem(ITEM_BOTANIA_GUN, "botania:mana_gun");
    }

    public static int getNatureMode(ItemStack staff) {
        if (staff == null || staff.isEmpty() || !staff.hasTagCompound()) {
            return NATURE_MODE_FUNCTION;
        }
        int mode = staff.getTagCompound().getInteger(TAG_NATURE_MODE);
        return mode >= NATURE_MODE_FUNCTION && mode <= NATURE_MODE_MANA_BLASTER
            ? mode : NATURE_MODE_FUNCTION;
    }

    public static EnumActionResult cycleNatureMode(EntityPlayer player, ItemStack staff) {
        int mode = (getNatureMode(staff) + 1) % 3;
        if (!staff.hasTagCompound()) {
            staff.setTagCompound(new NBTTagCompound());
        }
        staff.getTagCompound().setInteger(TAG_NATURE_MODE, mode);
        if (player.world.isRemote) {
            TT2.proxy.showCraftsmanStaffNatureMode(mode);
        }
        return EnumActionResult.SUCCESS;
    }

    public static EnumActionResult onNatureWandUse(EntityPlayer player, WorldAccess worldAccess, ItemStack staff,
                                                    EnumHand hand, BlockPos pos, EnumFacing facing,
                                                    float hitX, float hitY, float hitZ) {
        Item wand = getBotaniaWand();
        if (wand == null) {
            return EnumActionResult.PASS;
        }
        int mode = getNatureMode(staff);
        return delegate(player, staff, "botania_wand", wand, workingStaff -> {
            setBotaniaBindMode(workingStaff, mode == NATURE_MODE_BIND);
            return wand.onItemUse(player, worldAccess.world(), pos, hand, facing, hitX, hitY, hitZ);
        });
    }

    public static ActionResult<ItemStack> onNatureWandRightClick(EntityPlayer player, WorldAccess worldAccess,
                                                                 ItemStack staff, EnumHand hand) {
        Item wand = getBotaniaWand();
        if (wand == null) {
            return new ActionResult<>(EnumActionResult.PASS, staff);
        }
        int mode = getNatureMode(staff);
        ActionResult<ItemStack> result = delegate(player, staff, "botania_wand", wand, workingStaff -> {
            setBotaniaBindMode(workingStaff, mode == NATURE_MODE_BIND);
            return wand.onItemRightClick(worldAccess.world(), player, hand);
        });
        return new ActionResult<>(result == null ? EnumActionResult.PASS : result.getType(), staff);
    }

    public static ActionResult<ItemStack> onNatureBlasterRightClick(EntityPlayer player, WorldAccess worldAccess,
                                                                    ItemStack staff, EnumHand hand) {
        return onItemRightClick(player, worldAccess, staff, hand, "botania_gun", getBotaniaGun());
    }

    private static void setBotaniaBindMode(ItemStack workingStaff, boolean bindMode) {
        if (!workingStaff.hasTagCompound()) {
            workingStaff.setTagCompound(new NBTTagCompound());
        }
        workingStaff.getTagCompound().setBoolean("bindMode", bindMode);
    }

    @Nullable
    private static Item getFirstItem(String... ids) {
        for (String id : ids) {
            Item item = getItem(id);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    public static Item getThaumcraftGauntlet() {
        return getFirstItem(ITEM_THAUMCRAFT_GAUNTLET, ITEM_THAUMCRAFT_GAUNTLET_LEGACY);
    }

    public static Item getThaumcraftSalt() {
        return getItem(ITEM_THAUMCRAFT_SALT);
    }

    /** Reads the installed focus without exposing Thaumcraft state at the staff's root NBT. */
    @Nullable
    public static ItemStack getThaumcraftFocusStack(ItemStack staff) {
        NBTTagCompound state = getSavedTag(staff, TAG_THAUMCRAFT_GAUNTLET);
        if (state == null || !state.hasKey(TAG_THAUMCRAFT_FOCUS, 10)) {
            return null;
        }
        ItemStack focus = new ItemStack(state.getCompoundTag(TAG_THAUMCRAFT_FOCUS));
        return focus.isEmpty() ? null : focus;
    }

    /** Writes the installed focus transactionally into TT2Compat -> thaumcraft_gauntlet. */
    public static void setThaumcraftFocusStack(ItemStack staff, @Nullable ItemStack focus) {
        if (staff == null || staff.isEmpty()) {
            return;
        }
        NBTTagCompound compat = ensureCompatRoot(staff);
        NBTTagCompound state = compat.hasKey(TAG_THAUMCRAFT_GAUNTLET, 10)
            ? compat.getCompoundTag(TAG_THAUMCRAFT_GAUNTLET).copy() : new NBTTagCompound();
        if (focus == null || focus.isEmpty()) {
            state.removeTag(TAG_THAUMCRAFT_FOCUS);
        } else {
            state.setTag(TAG_THAUMCRAFT_FOCUS, focus.writeToNBT(new NBTTagCompound()));
        }
        if (state.getKeySet().isEmpty()) {
            compat.removeTag(TAG_THAUMCRAFT_GAUNTLET);
        } else {
            compat.setTag(TAG_THAUMCRAFT_GAUNTLET, state);
        }
    }

    /** Invokes a Thaumcraft caster interface method while keeping its state on the staff. */
    @Nullable
    public static Object invokeThaumcraftGauntlet(ItemStack staff, Method method,
                                                   @Nullable Object[] arguments) {
        Item gauntlet = getThaumcraftGauntlet();
        if (staff == null || staff.isEmpty() || method == null || gauntlet == null) {
            return null;
        }
        EntityPlayer player = findPlayer(arguments);
        return delegate(player, staff, TAG_THAUMCRAFT_GAUNTLET, gauntlet, workingStaff -> {
            Object[] forwarded = arguments == null ? new Object[0] : arguments.clone();
            if (forwarded.length > 0 && forwarded[0] instanceof ItemStack) {
                forwarded[0] = workingStaff;
            }
            try {
                return method.invoke(gauntlet, forwarded);
            } catch (IllegalAccessException failure) {
                throw new IllegalStateException("Could not access Thaumcraft caster method " + method, failure);
            } catch (InvocationTargetException failure) {
                Throwable cause = failure.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
                if (cause instanceof Error) {
                    throw (Error) cause;
                }
                throw new IllegalStateException("Thaumcraft caster method failed " + method, cause);
            }
        });
    }

    @Nullable
    private static EntityPlayer findPlayer(@Nullable Object[] arguments) {
        if (arguments != null) {
            for (Object argument : arguments) {
                if (argument instanceof EntityPlayer) {
                    return (EntityPlayer) argument;
                }
            }
        }
        return null;
    }

    public static Item getForestryWrench() {
        return getItem(ITEM_FORESTRY_WRENCH);
    }

    public static Item getForestryScoop() {
        return getItem(ITEM_FORESTRY_SCOOP);
    }

    public static Item getForestrySmoker() {
        return getItem(ITEM_FORESTRY_SMOKER);
    }

    public static Item getForestryGrafter() {
        return getItem(ITEM_FORESTRY_GRAFTER);
    }

    public static Item getForestryAlyzer() {
        return getItem(ITEM_FORESTRY_ALYZER);
    }

    public static void markAnalyzerBlockInteraction(EntityPlayer player, EnumHand hand) {
        if (player == null || hand == null || !player.isSneaking()) {
            return;
        }
        synchronized (ANALYZER_BLOCK_INTERACTIONS) {
            ANALYZER_BLOCK_INTERACTIONS.put(player,
                new AnalyzerBlockInteraction(hand, player.world.getTotalWorldTime()));
        }
    }

    public static boolean consumeAnalyzerBlockInteraction(EntityPlayer player, EnumHand hand) {
        if (player == null || hand == null) {
            return false;
        }
        AnalyzerBlockInteraction interaction;
        synchronized (ANALYZER_BLOCK_INTERACTIONS) {
            interaction = ANALYZER_BLOCK_INTERACTIONS.remove(player);
        }
        return interaction != null && interaction.hand == hand
            && interaction.tick == player.world.getTotalWorldTime();
    }


    public static EnumActionResult onItemUseFirst(EntityPlayer player, WorldAccess worldAccess, ItemStack staff,
                                                   EnumHand hand, BlockPos pos, EnumFacing facing,
                                                   float hitX, float hitY, float hitZ, String key, Item item) {
        if (item == null) {
            return EnumActionResult.PASS;
        }
        if (isItem(item, ITEM_FORESTRY_SMOKER) && !player.isSneaking()) {
            if (!isForestryHive(worldAccess.world(), pos)) {
                return EnumActionResult.PASS;
            }
            delegate(player, staff, key, item, working -> item.onItemUseFirst(
                player, worldAccess.world(), pos, facing, hitX, hitY, hitZ, hand));
            player.setActiveHand(hand);
            return EnumActionResult.SUCCESS;
        }
        if (isItem(item, ITEM_AE_NETWORK_TOOL)) {
            return useAeNetworkTool(player, worldAccess.world(), staff, hand, pos, facing,
                hitX, hitY, hitZ, key);
        }
        return delegate(player, staff, key, item, workingStaff -> item.onItemUseFirst(
            player, worldAccess.world(), pos, facing, hitX, hitY, hitZ, hand));
    }

    public static EnumActionResult onItemUse(EntityPlayer player, WorldAccess worldAccess, ItemStack staff,
                                              EnumHand hand, BlockPos pos, EnumFacing facing,
                                              float hitX, float hitY, float hitZ, String key, Item item) {
        if (item == null) {
            return EnumActionResult.PASS;
        }
        return delegate(player, staff, key, item, workingStaff -> item.onItemUse(
            player, worldAccess.world(), pos, hand, facing, hitX, hitY, hitZ));
    }

    public static ActionResult<ItemStack> onItemRightClick(EntityPlayer player, WorldAccess worldAccess,
                                                            ItemStack staff, EnumHand hand, String key, Item item) {
        if (item == null) {
            return new ActionResult<>(EnumActionResult.PASS, staff);
        }
        if (isItem(item, ITEM_AE_NETWORK_TOOL)) {
            if (!worldAccess.world().isRemote) {
                player.openGui(TT2.instance, xy177.tt2.init.TT2Blocks.GUI_CRAFTSMAN_STAFF_AE_TOOL,
                    worldAccess.world(), hand == EnumHand.MAIN_HAND ? 0 : 1, 0, 0);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, staff);
        }
        ActionResult<ItemStack> result = delegate(player, staff, key, item,
            isExternalCaster(item),
            workingStaff -> {
                if (isBotaniaGun(item)) {
                    ensureNatureLens(workingStaff);
                }
                return item.onItemRightClick(worldAccess.world(), player, hand);
        });
        return new ActionResult<>(result == null ? EnumActionResult.PASS : result.getType(),
            staff);
    }

    public static ActionResult<ItemStack> onContinuousRightClick(EntityPlayer player, WorldAccess worldAccess,
                                                                 ItemStack staff, EnumHand hand, String key, Item item) {
        if (item == null || !canUseForestrySmoker(player, worldAccess.world())) {
            return new ActionResult<>(EnumActionResult.PASS, staff);
        }
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, staff);
    }

    public static ActionResult<ItemStack> openCraftsmanEye(EntityPlayer player, WorldAccess worldAccess,
                                                            ItemStack staff, EnumHand hand) {
        if (!CraftsmanEyeGenetics.isAvailable()) {
            return new ActionResult<>(EnumActionResult.PASS, staff);
        }
        if (!worldAccess.world().isRemote) {
            player.openGui(TT2.instance, xy177.tt2.init.TT2Blocks.GUI_CRAFTSMAN_STAFF_ALYZER,
                worldAccess.world(), hand == EnumHand.MAIN_HAND ? 0 : 1, 0, 0);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, staff);
    }
    public static boolean itemInteractionForEntity(EntityPlayer player, ItemStack staff, EnumHand hand,
                                                   EntityLivingBase target, String key, Item item) {
        if (item == null) {
            return false;
        }
        return delegate(player, staff, key, item,
            workingStaff -> item.itemInteractionForEntity(workingStaff, player, target, hand));
    }

    public static boolean tryForestryScoopEntityInteraction(EntityPlayer player, ItemStack staff,
                                                             EnumHand hand, EntityLivingBase target) {
        if (getForestryScoop() == null || target == null
            || !target.getClass().getName().equals("forestry.lepidopterology.entities.EntityButterfly")) {
            return false;
        }
        try {
            Method getButterfly = target.getClass().getMethod("getButterfly");
            Object butterfly = getButterfly.invoke(target);
            if (butterfly == null) {
                return false;
            }
            if (!player.world.isRemote) {
                Object genome = invokeNoArgs(butterfly, "getGenome");
                Object primary = invokeNoArgs(genome, "getPrimary");
                Object root = invokeNoArgs(primary, "getRoot");
                Object copy = invokeNoArgs(butterfly, "copy");
                Object type = enumConstant("forestry.api.lepidopterology.EnumFlutterType", "BUTTERFLY");
                ItemStack caught = (ItemStack) invokeByName(root, "getMemberStack", copy, type);
                Object tracker = invokeByName(root, "getBreedingTracker", player.world, player.getGameProfile());
                invokeByName(tracker, "registerCatch", butterfly);
                if (caught != null && !caught.isEmpty()) {
                    target.entityDropItem(caught, 0.0F);
                }
                target.setDead();
            } else {
                player.swingArm(hand);
            }
            return true;
        } catch (ReflectiveOperationException failure) {
            logCompatFailure("Forestry scoop butterfly interaction", failure);
            return false;
        }
    }

    public static boolean tryForestryScoopBlockInteraction(EntityPlayer player, WorldAccess worldAccess,
                                                            ItemStack staff, EnumHand hand, BlockPos pos,
                                                            IBlockState state, EnumFacing facing,
                                                            float hitX, float hitY, float hitZ) {
        if (getForestryScoop() == null || state == null
            || !state.getBlock().getClass().getName()
                .equals("forestry.arboriculture.blocks.BlockForestryLeaves")) {
            return false;
        }
        try {
            Object tile = worldAccess.world().getTileEntity(pos);
            if (tile == null || !tile.getClass().getName().equals("forestry.arboriculture.tiles.TileLeaves")) {
                return false;
            }
            Object caterpillar = invokeNoArgs(tile, "getCaterpillar");
            if (caterpillar == null) {
                return false;
            }
            if (!worldAccess.world().isRemote) {
                Class<?> manager = Class.forName("forestry.api.lepidopterology.ButterflyManager");
                Object root = manager.getField("butterflyRoot").get(null);
                Object type = enumConstant("forestry.api.lepidopterology.EnumFlutterType", "CATERPILLAR");
                ItemStack caught = (ItemStack) invokeByName(root, "getMemberStack", caterpillar, type);
                if (caught != null && !caught.isEmpty()) {
                    EntityItem drop = new EntityItem(worldAccess.world(), pos.getX() + 0.5D,
                        pos.getY() + 0.5D, pos.getZ() + 0.5D, caught);
                    worldAccess.world().spawnEntity(drop);
                }
                invokeByName(tile, "setCaterpillar", new Object[]{null});
            }
            player.swingArm(hand);
            return true;
        } catch (ReflectiveOperationException failure) {
            logCompatFailure("Forestry scoop caterpillar interaction", failure);
            return false;
        }
    }

    public static boolean onBlockDestroyed(EntityLivingBase player, WorldAccess worldAccess, ItemStack staff,
                                           IBlockState state, BlockPos pos, String key, Item item) {
        if (item == null) {
            return false;
        }
        if (!(player instanceof EntityPlayer)) {
            return item.onBlockDestroyed(staff, worldAccess.world(), state, pos, player);
        }
        EnumHand hand = findHand((EntityPlayer) player, staff);
        if (hand == null) {
            return item.onBlockDestroyed(staff, worldAccess.world(), state, pos, player);
        }
        return delegate((EntityPlayer) player, staff, key, item,
            workingStaff -> item.onBlockDestroyed(workingStaff, worldAccess.world(), state, pos, player));
    }

    public static boolean canHarvestBlock(EntityPlayer player, ItemStack staff, IBlockState state,
                                          String key, Item item) {
        if (item == null) {
            return false;
        }
        EnumHand hand = findHand(player, staff);
        if (hand == null) {
            return item.canHarvestBlock(state, staff);
        }
        return delegate(player, staff, key, item, workingStaff -> item.canHarvestBlock(state, workingStaff));
    }

    public static float getDestroySpeed(EntityPlayer player, ItemStack staff, IBlockState state,
                                        String key, Item item) {
        if (item == null) {
            return 1.0F;
        }
        EnumHand hand = findHand(player, staff);
        if (hand == null) {
            return item.getDestroySpeed(staff, state);
        }
        return delegate(player, staff, key, item, workingStaff -> item.getDestroySpeed(workingStaff, state));
    }

    public static void onUpdate(EntityPlayer player, ItemStack staff, WorldAccess worldAccess,
                                int slot, boolean selected, String key, Item item) {
        if (item == null) {
            return;
        }
        EnumHand hand = findHand(player, staff);
        if (hand == null) {
            return;
        }
        delegate(player, staff, key, item,
            workingStaff -> {
                item.onUpdate(workingStaff, worldAccess.world(), player, slot, selected);
                return null;
            });
    }

    public static void onUsingTick(EntityLivingBase entity, ItemStack staff, int count,
                                   String key, Item item) {
        if (item == null || !(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        if (isItem(item, ITEM_FORESTRY_SMOKER) && !canUseForestrySmoker(player, player.world)) {
            player.stopActiveHand();
            return;
        }
        EnumHand hand = findHand(player, staff);
        if (hand == null) {
            return;
        }
        delegate(player, staff, key, item,
            workingStaff -> {
                item.onUsingTick(workingStaff, entity, count);
                return null;
            });
    }

    public static void onPlayerStoppedUsing(EntityLivingBase entity, ItemStack staff, int count,
                                            String key, Item item) {
        if (item == null || !(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        EnumHand hand = findHand(player, staff);
        if (hand == null) {
            return;
        }
        delegate(player, staff, key, item,
            workingStaff -> {
                item.onPlayerStoppedUsing(workingStaff, player.world, entity, count);
                return null;
            });
    }

    public static void ensureNatureLens(ItemStack staff) {
        if (staff == null || staff.isEmpty() || staff.hasTagCompound()
            && staff.getTagCompound().hasKey("lens", 10)) {
            return;
        }
        try {
            Class<?> gun = Class.forName("vazkii.botania.common.item.ItemManaGun");
            Method setLens = gun.getMethod("setLens", ItemStack.class, ItemStack.class);
            Item lens = getItem("botania:lens");
            if (lens != null) {
                setLens.invoke(null, staff, new ItemStack(lens, 1, 8));
            }
        } catch (ReflectiveOperationException ignored) {
            // An older Botania build without the lens helper simply keeps the gun unmodified.
        }
    }

    /** Installs a Thaumcraft focus held in the other hand onto the delegated caster. */
    public static boolean tryInstallThaumcraftFocus(EntityPlayer player, EnumHand staffHand,
                                                    ItemStack staff) {
        if (player == null || staff == null || getThaumcraftGauntlet() == null
            || !isLoaded("thaumcraft")) {
            return false;
        }
        EnumHand focusHand = staffHand == EnumHand.MAIN_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
        ItemStack focus = player.getHeldItem(focusHand);
        if (focus == null || focus.isEmpty()
            || !focus.getItem().getClass().getName().startsWith("thaumcraft.common.items.casters.ItemFocus")) {
            return false;
        }
        final boolean[] installed = new boolean[]{false};
        final ItemStack[] replaced = new ItemStack[]{ItemStack.EMPTY};
        delegate(player, staff, TAG_THAUMCRAFT_GAUNTLET, getThaumcraftGauntlet(), workingStaff -> {
            try {
                Item gauntlet = getThaumcraftGauntlet();
                Method getFocus = gauntlet.getClass().getMethod("getFocusStack", ItemStack.class);
                Method setFocus = gauntlet.getClass().getMethod("setFocus", ItemStack.class, ItemStack.class);
                Object current = getFocus.invoke(gauntlet, workingStaff);
                if (current instanceof ItemStack && !((ItemStack) current).isEmpty()) {
                    replaced[0] = ((ItemStack) current).copy();
                }
                setFocus.invoke(gauntlet, workingStaff, focus.copy());
                installed[0] = true;
            } catch (ReflectiveOperationException ignored) {
                installed[0] = false;
            }
            return null;
        });
        if (installed[0] && !player.world.isRemote) {
            if (!player.capabilities.isCreativeMode) {
                focus.shrink(1);
            }
            if (!replaced[0].isEmpty() && !player.inventory.addItemStackToInventory(replaced[0])) {
                player.dropItem(replaced[0], false);
            }
            player.sendStatusMessage(new TextComponentTranslation(
                "message.tt2.craftsman_staff.focus_installed"), true);
        }
        return installed[0];
    }

    /** Records the casting staff before a Thaumcraft medium defers the remaining focus package. */
    public static void captureThaumcraftFocusPackage(@Nullable Object focusPackage) {
        if (focusPackage == null) {
            return;
        }
        ThaumcraftFocusAccess access = getThaumcraftFocusAccess();
        if (access == null || !access.isFocusPackage(focusPackage)) {
            return;
        }
        try {
            StoredFocusCast cast = findOrCreateStoredFocusCast(focusPackage, access);
            if (cast != null) {
                registerFocusPackageTree(focusPackage, cast, access,
                    new IdentityHashMap<Object, Boolean>());
            }
        } catch (ReflectiveOperationException | RuntimeException failure) {
            logCompatFailure("Thaumcraft focus cast capture", failure);
        }
    }

    /** Invokes the original focus effect while exposing its casting staff to the damage hook. */
    public static boolean executeThaumcraftFocusEffect(Object effect, Object target, Object trajectory,
                                                       float power, int targetIndex) {
        ThaumcraftFocusAccess access = getThaumcraftFocusAccess();
        ActiveFocusCast previous = ACTIVE_THAUMCRAFT_FOCUS.get();
        ActiveFocusCast current = null;
        if (access != null && effect != null && access.isFocusEffect(effect)) {
            try {
                Object focusPackage = access.getPackage(effect);
                StoredFocusCast stored = findOrCreateStoredFocusCast(focusPackage, access);
                EntityLivingBase caster = access.getCaster(focusPackage);
                if (stored != null && caster != null) {
                    current = new ActiveFocusCast(stored.staff.copy(), caster);
                }
            } catch (ReflectiveOperationException | RuntimeException failure) {
                logCompatFailure("Thaumcraft focus execution context", failure);
            }
        }

        ActiveFocusCast effective = current == null ? previous : current;
        if (effective == null) {
            ACTIVE_THAUMCRAFT_FOCUS.remove();
        } else {
            ACTIVE_THAUMCRAFT_FOCUS.set(effective);
        }
        try {
            Object result;
            if (access != null && effect != null && access.isFocusEffect(effect)) {
                result = access.execute(effect, target, trajectory, power, targetIndex);
            } else {
                result = invokeByName(effect, "execute", target, trajectory, power, targetIndex);
            }
            return Boolean.TRUE.equals(result);
        } catch (InvocationTargetException failure) {
            throw propagateFocusFailure(failure.getCause());
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Could not invoke Thaumcraft focus effect", failure);
        } finally {
            if (previous == null) {
                ACTIVE_THAUMCRAFT_FOCUS.remove();
            } else {
                ACTIVE_THAUMCRAFT_FOCUS.set(previous);
            }
        }
    }

    /** Applies Tinkers' damage traits to a direct Thaumcraft focus hit. */
    public static boolean attackThaumcraftFocusTarget(Entity target, DamageSource source, float amount) {
        ActiveFocusCast cast = resolveThaumcraftFocusCast(target, source);
        if (!(target instanceof EntityLivingBase) || target.world.isRemote || cast == null
            || !isInsightStaff(cast.staff) || isApplyingExternalTraitCallbacks()) {
            return target.attackEntityFrom(source, amount);
        }

        EntityLivingBase livingTarget = (EntityLivingBase) target;
        List<ITrait> traits = getTraits(cast.staff);
        float baseDamage = getThaumcraftTraitBaseDamage(amount, cast.staff, cast.caster);
        float finalDamage = baseDamage;

        beginExternalTraitCallbacks();
        try {
            for (ITrait trait : traits) {
                finalDamage = trait.damage(cast.staff, cast.caster, livingTarget,
                    baseDamage, finalDamage, false);
            }

            float healthBefore = livingTarget.getHealth();
            int originalHurtResistantTime = livingTarget.hurtResistantTime;
            for (ITrait trait : traits) {
                try {
                    trait.onHit(cast.staff, cast.caster, livingTarget, finalDamage, false);
                } finally {
                    livingTarget.hurtResistantTime = originalHurtResistantTime;
                }
            }

            boolean hit = target.attackEntityFrom(source, finalDamage);
            if (hit) {
                float damageDealt = Math.max(0.0F, healthBefore - livingTarget.getHealth());
                for (ITrait trait : traits) {
                    trait.afterHit(cast.staff, cast.caster, livingTarget,
                        damageDealt, false, true);
                }
            }
            return hit;
        } finally {
            endExternalTraitCallbacks();
        }
    }

    private static RuntimeException propagateFocusFailure(@Nullable Throwable failure) {
        if (failure instanceof RuntimeException) {
            return (RuntimeException) failure;
        }
        if (failure instanceof Error) {
            throw (Error) failure;
        }
        return new IllegalStateException("Thaumcraft focus effect failed", failure);
    }

    /** Applies staff damage scaling when an optional-mod attack bypasses the direct damage hook. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingHurt(LivingHurtEvent event) {
        synchronized (PENDING_NATURE_HITS) {
            PENDING_NATURE_HITS.remove(event.getEntityLiving());
        }
        if (isApplyingExternalTraitCallbacks()) {
            return;
        }

        synchronized (PENDING_THAUMCRAFT_HITS) {
            PENDING_THAUMCRAFT_HITS.remove(event.getEntityLiving());
        }

        ActiveFocusCast focus = resolveThaumcraftFocusCast(event.getEntityLiving(), event.getSource());
        if (focus != null && isInsightStaff(focus.staff)
            && isThaumcraftFocusDamageSource(event.getSource(), focus)) {
            applyExternalDamageTraits(event, focus.staff, focus.caster);
            return;
        }

        NatureBurstContext nature = findNatureBurst(event.getEntityLiving(), event.getSource());
        if (nature != null) {
            float baseDamage = event.getAmount() * getExternalDamageMultiplier(nature.staff);
            float finalDamage = baseDamage;
            for (ITrait trait : getTraits(nature.staff)) {
                finalDamage = trait.damage(nature.staff, nature.caster, event.getEntityLiving(),
                    baseDamage, finalDamage, false);
            }
            event.setAmount(finalDamage);
            synchronized (PENDING_NATURE_HITS) {
                PENDING_NATURE_HITS.put(event.getEntityLiving(),
                    new PendingNatureHit(nature.staff, nature.caster, finalDamage,
                        event.getEntityLiving().world.getTotalWorldTime()));
            }
            return;
        }

        Entity source = event.getSource().getImmediateSource();
        if (source != null && source.getEntityData().hasKey(TAG_EXTERNAL_DAMAGE_MULTIPLIER, 5)) {
            event.setAmount(event.getAmount()
                * source.getEntityData().getFloat(TAG_EXTERNAL_DAMAGE_MULTIPLIER));
            return;
        }
        Entity attacker = event.getSource().getTrueSource();
        if (!(attacker instanceof EntityPlayer) || source == null) {
            return;
        }
        EntityPlayer player = (EntityPlayer) attacker;
        ItemStack activeCast;
        synchronized (ACTIVE_EXTERNAL_CASTS) {
            activeCast = ACTIVE_EXTERNAL_CASTS.get(player);
        }
        ItemStack staff = activeCast == null ? findStaff(player) : activeCast;
        if (staff == null) {
            return;
        }
        String className = source.getClass().getName();
        boolean natureDamage = className.startsWith("vazkii.botania.common.entity.EntityManaBurst")
            && ModCraftsmanStaffTemplate.has(staff, ModCraftsmanStaffTemplate.Type.NATURE);
        if (natureDamage) {
            event.setAmount(event.getAmount() * getExternalDamageMultiplier(staff));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDamage(LivingDamageEvent event) {
        if (isApplyingExternalTraitCallbacks()) {
            synchronized (PENDING_NATURE_HITS) {
                PENDING_NATURE_HITS.remove(event.getEntityLiving());
            }
            synchronized (PENDING_THAUMCRAFT_HITS) {
                PENDING_THAUMCRAFT_HITS.remove(event.getEntityLiving());
            }
            return;
        }

        PendingThaumcraftHit thaumcraft;
        synchronized (PENDING_THAUMCRAFT_HITS) {
            thaumcraft = PENDING_THAUMCRAFT_HITS.remove(event.getEntityLiving());
        }
        if (thaumcraft != null
            && thaumcraft.tick == event.getEntityLiving().world.getTotalWorldTime()) {
            beginExternalTraitCallbacks();
            try {
                for (ITrait trait : getTraits(thaumcraft.staff)) {
                    trait.afterHit(thaumcraft.staff, thaumcraft.caster, event.getEntityLiving(),
                        event.getAmount(), false, true);
                }
            } finally {
                endExternalTraitCallbacks();
            }
        }

        PendingNatureHit pending;
        synchronized (PENDING_NATURE_HITS) {
            pending = PENDING_NATURE_HITS.remove(event.getEntityLiving());
        }
        if (pending == null || pending.tick != event.getEntityLiving().world.getTotalWorldTime()) {
            return;
        }
        beginExternalTraitCallbacks();
        try {
            for (ITrait trait : getTraits(pending.staff)) {
                trait.onHit(pending.staff, pending.caster, event.getEntityLiving(), pending.damage, false);
                trait.afterHit(pending.staff, pending.caster, event.getEntityLiving(), pending.damage, false, true);
            }
        } finally {
            endExternalTraitCallbacks();
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        String className = event.getEntity().getClass().getName();
        boolean botania = className.startsWith("vazkii.botania.common.entity.EntityManaBurst");
        boolean thaumcraft = isThaumcraftFocusCarrier(className);
        if (!botania && !thaumcraft) {
            return;
        }
        if (thaumcraft) {
            if (!event.getWorld().isRemote) {
                captureThaumcraftFocusCarrier(event.getEntity());
            }
            return;
        }
        synchronized (ACTIVE_EXTERNAL_CASTS) {
            for (Map.Entry<EntityPlayer, ItemStack> cast : ACTIVE_EXTERNAL_CASTS.entrySet()) {
                EntityPlayer caster = cast.getKey();
                ItemStack staff = cast.getValue();
                if (!ModCraftsmanStaffTemplate.has(staff, ModCraftsmanStaffTemplate.Type.NATURE)) {
                    continue;
                }
                event.getEntity().getEntityData().setFloat(
                    TAG_EXTERNAL_DAMAGE_MULTIPLIER, getExternalDamageMultiplier(staff));
                NBTTagCompound snapshot = new NBTTagCompound();
                staff.copy().writeToNBT(snapshot);
                event.getEntity().getEntityData().setTag(TAG_EXTERNAL_STAFF, snapshot);
                event.getEntity().getEntityData().setLong(TAG_EXTERNAL_CASTER_MOST,
                    caster.getUniqueID().getMostSignificantBits());
                event.getEntity().getEntityData().setLong(TAG_EXTERNAL_CASTER_LEAST,
                    caster.getUniqueID().getLeastSignificantBits());
                break;
            }
        }
    }

    @Nullable
    private static NatureBurstContext findNatureBurst(EntityLivingBase target, DamageSource source) {
        if (target == null || target.world.isRemote) {
            return null;
        }
        NatureBurstContext direct = natureContextFromBurst(source.getImmediateSource(), target);
        if (direct != null) {
            return direct;
        }
        if (source != DamageSource.MAGIC) {
            return null;
        }
        Entity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Entity candidate : target.world.loadedEntityList) {
            if (natureContextFromBurst(candidate, target) == null) {
                continue;
            }
            AxisAlignedBB flightPath = new AxisAlignedBB(candidate.posX, candidate.posY, candidate.posZ,
                candidate.prevPosX, candidate.prevPosY, candidate.prevPosZ).grow(1.0D);
            if (!flightPath.intersects(target.getEntityBoundingBox())) {
                continue;
            }
            double distance = candidate.getDistanceSq(target);
            if (distance < bestDistance) {
                best = candidate;
                bestDistance = distance;
            }
        }
        if (best == null) {
            return null;
        }
        return natureContextFromBurst(best, target);
    }

    @Nullable
    private static NatureBurstContext natureContextFromBurst(@Nullable Entity burst, EntityLivingBase target) {
        if (burst == null
            || !burst.getClass().getName().startsWith("vazkii.botania.common.entity.EntityManaBurst")) {
            return null;
        }
        NBTTagCompound data = burst.getEntityData();
        if (!data.hasKey(TAG_EXTERNAL_STAFF, 10)
            || !data.hasKey(TAG_EXTERNAL_CASTER_MOST, 4)
            || !data.hasKey(TAG_EXTERNAL_CASTER_LEAST, 4)) {
            return null;
        }
        ItemStack staff = new ItemStack(data.getCompoundTag(TAG_EXTERNAL_STAFF));
        if (staff.isEmpty() || !(staff.getItem() instanceof xy177.tt2.tools.CraftsmanStaff)
            || !ModCraftsmanStaffTemplate.has(staff, ModCraftsmanStaffTemplate.Type.NATURE)) {
            return null;
        }
        java.util.UUID casterId = new java.util.UUID(data.getLong(TAG_EXTERNAL_CASTER_MOST),
            data.getLong(TAG_EXTERNAL_CASTER_LEAST));
        EntityPlayer caster = target.world.getPlayerEntityByUUID(casterId);
        return caster == null ? null : new NatureBurstContext(staff, caster);
    }

    private static List<ITrait> getTraits(ItemStack staff) {
        return TinkerUtil.getTraitsOrdered(staff);
    }

    private static void applyExternalDamageTraits(LivingHurtEvent event, ItemStack staff,
                                                   EntityLivingBase caster) {
        float baseDamage = getThaumcraftTraitBaseDamage(event.getAmount(), staff, caster);
        float finalDamage = baseDamage;
        List<ITrait> traits = getTraits(staff);
        beginExternalTraitCallbacks();
        try {
            for (ITrait trait : traits) {
                finalDamage = trait.damage(staff, caster, event.getEntityLiving(),
                    baseDamage, finalDamage, false);
            }

            int originalHurtResistantTime = event.getEntityLiving().hurtResistantTime;
            for (ITrait trait : traits) {
                try {
                    trait.onHit(staff, caster, event.getEntityLiving(), finalDamage, false);
                } finally {
                    event.getEntityLiving().hurtResistantTime = originalHurtResistantTime;
                }
            }
            event.setAmount(finalDamage);
        } finally {
            endExternalTraitCallbacks();
        }
        synchronized (PENDING_THAUMCRAFT_HITS) {
            PENDING_THAUMCRAFT_HITS.put(event.getEntityLiving(),
                new PendingThaumcraftHit(staff, caster,
                    event.getEntityLiving().world.getTotalWorldTime()));
        }
    }

    private static final class NatureBurstContext {
        private final ItemStack staff;
        private final EntityPlayer caster;

        private NatureBurstContext(ItemStack staff, EntityPlayer caster) {
            this.staff = staff;
            this.caster = caster;
        }
    }

    private static final class PendingNatureHit {
        private final ItemStack staff;
        private final EntityPlayer caster;
        private final float damage;
        private final long tick;

        private PendingNatureHit(ItemStack staff, EntityPlayer caster, float damage, long tick) {
            this.staff = staff;
            this.caster = caster;
            this.damage = damage;
            this.tick = tick;
        }
    }

    private static final class PendingThaumcraftHit {
        private final ItemStack staff;
        private final EntityLivingBase caster;
        private final long tick;

        private PendingThaumcraftHit(ItemStack staff, EntityLivingBase caster, long tick) {
            this.staff = staff.copy();
            this.caster = caster;
            this.tick = tick;
        }
    }

    @Nullable
    private static ThaumcraftFocusAccess getThaumcraftFocusAccess() {
        ThaumcraftFocusAccess current = thaumcraftFocusAccess;
        if (current != null || thaumcraftFocusUnavailable) {
            return current;
        }
        synchronized (CraftsmanStaffCompat.class) {
            current = thaumcraftFocusAccess;
            if (current != null || thaumcraftFocusUnavailable) {
                return current;
            }
            try {
                current = new ThaumcraftFocusAccess(CraftsmanStaffCompat.class.getClassLoader());
                thaumcraftFocusAccess = current;
                return current;
            } catch (ReflectiveOperationException | LinkageError | RuntimeException failure) {
                thaumcraftFocusUnavailable = true;
                logCompatFailure("Thaumcraft focus runtime", failure);
                return null;
            }
        }
    }

    @Nullable
    private static StoredFocusCast findOrCreateStoredFocusCast(@Nullable Object focusPackage,
                                                                ThaumcraftFocusAccess access)
        throws ReflectiveOperationException {
        if (focusPackage == null || !access.isFocusPackage(focusPackage)) {
            return null;
        }
        UUID packageId = access.getUniqueId(focusPackage);
        UUID casterId = access.getCasterId(focusPackage);
        EntityLivingBase caster = access.getCaster(focusPackage);
        if (casterId == null && caster != null) {
            casterId = caster.getUniqueID();
        }

        StoredFocusCast stored;
        synchronized (THAUMCRAFT_FOCUS_OBJECT_CONTEXTS) {
            stored = THAUMCRAFT_FOCUS_OBJECT_CONTEXTS.get(focusPackage);
        }
        if (stored == null) {
            stored = getStoredFocusCast(casterId, packageId);
        }
        if (stored != null) {
            return stored;
        }

        ItemStack activeStaff = getActiveInsightStaff(caster, casterId);
        if (activeStaff == null) {
            return null;
        }
        if (casterId == null && caster != null) {
            casterId = caster.getUniqueID();
        }
        stored = new StoredFocusCast(activeStaff, casterId);
        if (packageId != null) {
            putStoredFocusCast(casterId, packageId, stored);
        }
        return stored;
    }

    @Nullable
    private static ItemStack getActiveInsightStaff(@Nullable EntityLivingBase caster,
                                                    @Nullable UUID casterId) {
        synchronized (ACTIVE_EXTERNAL_CASTS) {
            if (caster instanceof EntityPlayer) {
                ItemStack direct = ACTIVE_EXTERNAL_CASTS.get(caster);
                if (isInsightStaff(direct)) {
                    return direct;
                }
            }
            if (casterId != null) {
                for (Map.Entry<EntityPlayer, ItemStack> entry : ACTIVE_EXTERNAL_CASTS.entrySet()) {
                    if (casterId.equals(entry.getKey().getUniqueID()) && isInsightStaff(entry.getValue())) {
                        return entry.getValue();
                    }
                }
            }
        }
        return null;
    }

    private static boolean isInsightStaff(@Nullable ItemStack staff) {
        return staff != null && !staff.isEmpty()
            && staff.getItem() instanceof xy177.tt2.tools.CraftsmanStaff
            && ModCraftsmanStaffTemplate.has(staff, ModCraftsmanStaffTemplate.Type.INSIGHT);
    }

    @Nullable
    private static ActiveFocusCast resolveThaumcraftFocusCast(Entity target,
                                                               @Nullable DamageSource source) {
        ActiveFocusCast active = ACTIVE_THAUMCRAFT_FOCUS.get();
        if (active != null && isInsightStaff(active.staff)) {
            return active;
        }
        if (target == null || source == null) {
            return null;
        }

        Entity carrier = source.getImmediateSource();
        if (carrier == null || !isThaumcraftFocusCarrier(carrier.getClass().getName())) {
            return null;
        }
        StoredFocusCast stored = readStoredFocusCast(carrier);
        if (stored == null) {
            captureThaumcraftFocusCarrier(carrier);
            stored = readStoredFocusCast(carrier);
        }
        if (stored == null || !isInsightStaff(stored.staff)) {
            return null;
        }

        Entity trueSource = source.getTrueSource();
        EntityLivingBase caster = trueSource instanceof EntityLivingBase
            ? (EntityLivingBase) trueSource : null;
        if (caster != null && stored.casterId != null
            && !stored.casterId.equals(caster.getUniqueID())) {
            caster = null;
        }
        if (caster == null && stored.casterId != null && target.world != null) {
            caster = target.world.getPlayerEntityByUUID(stored.casterId);
        }
        return caster == null ? null : new ActiveFocusCast(stored.staff.copy(), caster);
    }

    private static boolean isThaumcraftFocusDamageSource(DamageSource source, ActiveFocusCast cast) {
        Entity trueSource = source.getTrueSource();
        if (trueSource != null) {
            return trueSource == cast.caster
                || trueSource.getUniqueID().equals(cast.caster.getUniqueID());
        }
        Entity immediateSource = source.getImmediateSource();
        if (immediateSource == null
            || !isThaumcraftFocusCarrier(immediateSource.getClass().getName())) {
            return false;
        }
        StoredFocusCast stored = readStoredFocusCast(immediateSource);
        return stored != null && isInsightStaff(stored.staff)
            && (stored.casterId == null || stored.casterId.equals(cast.caster.getUniqueID()));
    }

    @Nullable
    private static StoredFocusCast getStoredFocusCast(@Nullable UUID casterId,
                                                       @Nullable UUID packageId) {
        if (packageId == null) {
            return null;
        }
        synchronized (THAUMCRAFT_FOCUS_CONTEXTS) {
            StoredFocusCast exact = THAUMCRAFT_FOCUS_CONTEXTS.get(
                new FocusCastKey(casterId, packageId));
            if (exact != null) {
                return exact;
            }
            for (Map.Entry<FocusCastKey, StoredFocusCast> entry
                : THAUMCRAFT_FOCUS_CONTEXTS.entrySet()) {
                FocusCastKey key = entry.getKey();
                if (packageId.equals(key.packageId)
                    && (casterId == null || casterId.equals(key.casterId))) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private static void putStoredFocusCast(@Nullable UUID casterId, UUID packageId,
                                           StoredFocusCast cast) {
        synchronized (THAUMCRAFT_FOCUS_CONTEXTS) {
            THAUMCRAFT_FOCUS_CONTEXTS.put(new FocusCastKey(casterId, packageId), cast);
        }
    }

    private static void registerFocusPackageTree(Object focusPackage, StoredFocusCast cast,
                                                 ThaumcraftFocusAccess access,
                                                 IdentityHashMap<Object, Boolean> visited)
        throws ReflectiveOperationException {
        if (focusPackage == null || !access.isFocusPackage(focusPackage)
            || visited.put(focusPackage, Boolean.TRUE) != null) {
            return;
        }
        synchronized (THAUMCRAFT_FOCUS_OBJECT_CONTEXTS) {
            THAUMCRAFT_FOCUS_OBJECT_CONTEXTS.put(focusPackage, cast);
        }

        UUID packageId = access.getUniqueId(focusPackage);
        UUID casterId = access.getCasterId(focusPackage);
        if (packageId != null) {
            putStoredFocusCast(casterId == null ? cast.casterId : casterId, packageId, cast);
        }

        for (Object node : access.getNodes(focusPackage)) {
            if (access.isFocusPackage(node)) {
                registerFocusPackageTree(node, cast, access, visited);
            } else if (access.isSplitNode(node)) {
                for (Object splitPackage : access.getSplitPackages(node)) {
                    registerFocusPackageTree(splitPackage, cast, access, visited);
                }
            }
        }
    }

    private static boolean isThaumcraftFocusCarrier(String className) {
        return className.startsWith("thaumcraft.common.entities.projectile.EntityFocus")
            || className.startsWith("thaumcraft.common.entities.monster.EntitySpellBat");
    }

    private static void captureThaumcraftFocusCarrier(Entity carrier) {
        ThaumcraftFocusAccess access = getThaumcraftFocusAccess();
        if (access == null) {
            return;
        }
        try {
            Object focusPackage = getFocusPackageFromCarrier(carrier);
            if (focusPackage == null || !access.isFocusPackage(focusPackage)) {
                return;
            }
            StoredFocusCast stored = findOrCreateStoredFocusCast(focusPackage, access);
            if (stored == null) {
                stored = readStoredFocusCast(carrier);
            }
            if (stored == null) {
                stored = getStoredFocusCast(access.getCasterId(focusPackage),
                    access.getUniqueId(focusPackage));
            }
            if (stored == null) {
                return;
            }
            registerFocusPackageTree(focusPackage, stored, access,
                new IdentityHashMap<Object, Boolean>());
            writeStoredFocusCast(carrier, stored);
        } catch (ReflectiveOperationException | RuntimeException failure) {
            logCompatFailure("Thaumcraft delayed focus capture", failure);
        }
    }

    @Nullable
    private static Object getFocusPackageFromCarrier(Entity carrier)
        throws ReflectiveOperationException {
        for (Class<?> type = carrier.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField("focusPackage");
                field.setAccessible(true);
                return field.get(carrier);
            } catch (NoSuchFieldException ignored) {
                // Continue through possible compatibility subclasses.
            }
        }
        throw new NoSuchFieldException(carrier.getClass().getName() + ".focusPackage");
    }

    @Nullable
    private static StoredFocusCast readStoredFocusCast(Entity carrier) {
        NBTTagCompound data = carrier.getEntityData();
        if (!data.hasKey(TAG_EXTERNAL_STAFF, 10)
            || !data.hasKey(TAG_EXTERNAL_CASTER_MOST, 4)
            || !data.hasKey(TAG_EXTERNAL_CASTER_LEAST, 4)) {
            return null;
        }
        ItemStack staff = new ItemStack(data.getCompoundTag(TAG_EXTERNAL_STAFF));
        if (!isInsightStaff(staff)) {
            return null;
        }
        UUID casterId = new UUID(data.getLong(TAG_EXTERNAL_CASTER_MOST),
            data.getLong(TAG_EXTERNAL_CASTER_LEAST));
        return new StoredFocusCast(staff, casterId);
    }

    private static void writeStoredFocusCast(Entity carrier, StoredFocusCast cast) {
        NBTTagCompound data = carrier.getEntityData();
        NBTTagCompound snapshot = new NBTTagCompound();
        cast.staff.copy().writeToNBT(snapshot);
        data.setTag(TAG_EXTERNAL_STAFF, snapshot);
        data.setFloat(TAG_EXTERNAL_DAMAGE_MULTIPLIER, getExternalDamageMultiplier(cast.staff));
        if (cast.casterId != null) {
            data.setLong(TAG_EXTERNAL_CASTER_MOST, cast.casterId.getMostSignificantBits());
            data.setLong(TAG_EXTERNAL_CASTER_LEAST, cast.casterId.getLeastSignificantBits());
        }
    }

    private static boolean isApplyingExternalTraitCallbacks() {
        return EXTERNAL_TRAIT_CALLBACK_DEPTH.get() > 0;
    }

    private static void beginExternalTraitCallbacks() {
        EXTERNAL_TRAIT_CALLBACK_DEPTH.set(EXTERNAL_TRAIT_CALLBACK_DEPTH.get() + 1);
    }

    private static void endExternalTraitCallbacks() {
        int depth = EXTERNAL_TRAIT_CALLBACK_DEPTH.get();
        if (depth <= 1) {
            EXTERNAL_TRAIT_CALLBACK_DEPTH.remove();
        } else {
            EXTERNAL_TRAIT_CALLBACK_DEPTH.set(depth - 1);
        }
    }

    private static final class FocusCastKey {
        @Nullable
        private final UUID casterId;
        private final UUID packageId;

        private FocusCastKey(@Nullable UUID casterId, UUID packageId) {
            this.casterId = casterId;
            this.packageId = packageId;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof FocusCastKey)) {
                return false;
            }
            FocusCastKey key = (FocusCastKey) other;
            return packageId.equals(key.packageId)
                && (casterId == null ? key.casterId == null : casterId.equals(key.casterId));
        }

        @Override
        public int hashCode() {
            return 31 * (casterId == null ? 0 : casterId.hashCode()) + packageId.hashCode();
        }
    }

    private static final class StoredFocusCast {
        private final ItemStack staff;
        @Nullable
        private final UUID casterId;

        private StoredFocusCast(ItemStack staff, @Nullable UUID casterId) {
            this.staff = staff.copy();
            this.casterId = casterId;
        }
    }

    private static final class ActiveFocusCast {
        private final ItemStack staff;
        private final EntityLivingBase caster;

        private ActiveFocusCast(ItemStack staff, EntityLivingBase caster) {
            this.staff = staff;
            this.caster = caster;
        }
    }

    private static final class ThaumcraftFocusAccess {
        private final Class<?> focusEffectClass;
        private final Class<?> focusPackageClass;
        private final Class<?> splitNodeClass;
        private final Method execute;
        private final Method getPackage;
        private final Method getCaster;
        private final Method getCasterId;
        private final Method getUniqueId;
        private final Method getSplitPackages;
        private final Field nodes;

        private ThaumcraftFocusAccess(ClassLoader loader) throws ReflectiveOperationException {
            focusEffectClass = Class.forName("thaumcraft.api.casters.FocusEffect", false, loader);
            Class<?> focusNodeClass = Class.forName("thaumcraft.api.casters.FocusNode", false, loader);
            focusPackageClass = Class.forName("thaumcraft.api.casters.FocusPackage", false, loader);
            splitNodeClass = Class.forName("thaumcraft.api.casters.FocusModSplit", false, loader);
            Class<?> trajectoryClass = Class.forName("thaumcraft.api.casters.Trajectory", false, loader);
            execute = focusEffectClass.getMethod("execute", RayTraceResult.class,
                trajectoryClass, float.class, int.class);
            getPackage = focusNodeClass.getMethod("getPackage");
            getCaster = focusPackageClass.getMethod("getCaster");
            getCasterId = focusPackageClass.getMethod("getCasterUUID");
            getUniqueId = focusPackageClass.getMethod("getUniqueID");
            getSplitPackages = splitNodeClass.getMethod("getSplitPackages");
            nodes = focusPackageClass.getField("nodes");
        }

        private boolean isFocusEffect(Object value) {
            return focusEffectClass.isInstance(value);
        }

        private boolean isFocusPackage(Object value) {
            return focusPackageClass.isInstance(value);
        }

        private boolean isSplitNode(Object value) {
            return splitNodeClass.isInstance(value);
        }

        private Object execute(Object effect, Object target, Object trajectory,
                               float power, int targetIndex) throws ReflectiveOperationException {
            return execute.invoke(effect, target, trajectory, power, targetIndex);
        }

        @Nullable
        private Object getPackage(Object effect) throws ReflectiveOperationException {
            return effect == null ? null : getPackage.invoke(effect);
        }

        @Nullable
        private EntityLivingBase getCaster(@Nullable Object focusPackage)
            throws ReflectiveOperationException {
            if (!isFocusPackage(focusPackage)) {
                return null;
            }
            Object value = getCaster.invoke(focusPackage);
            return value instanceof EntityLivingBase ? (EntityLivingBase) value : null;
        }

        @Nullable
        private UUID getCasterId(@Nullable Object focusPackage) throws ReflectiveOperationException {
            if (!isFocusPackage(focusPackage)) {
                return null;
            }
            Object value = getCasterId.invoke(focusPackage);
            return value instanceof UUID ? (UUID) value : null;
        }

        @Nullable
        private UUID getUniqueId(@Nullable Object focusPackage) throws ReflectiveOperationException {
            if (!isFocusPackage(focusPackage)) {
                return null;
            }
            Object value = getUniqueId.invoke(focusPackage);
            return value instanceof UUID ? (UUID) value : null;
        }

        private List<?> getNodes(Object focusPackage) throws ReflectiveOperationException {
            Object value = nodes.get(focusPackage);
            return value instanceof List ? (List<?>) value : Collections.emptyList();
        }

        private List<?> getSplitPackages(Object splitNode) throws ReflectiveOperationException {
            Object value = getSplitPackages.invoke(splitNode);
            return value instanceof List ? (List<?>) value : Collections.emptyList();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onHarvestDrops(HarvestDropsEvent event) {
        EntityPlayer player = event.getHarvester();
        if (player == null || event.getWorld().isRemote) {
            return;
        }
        ItemStack staff = player.getHeldItemMainhand();
        if (!(staff.getItem() instanceof xy177.tt2.tools.CraftsmanStaff)
            || ToolHelper.isBroken(staff)
            || !ModCraftsmanStaffTemplate.has(staff, ModCraftsmanStaffTemplate.Type.FORESTRY)) {
            return;
        }

        Object block = event.getState().getBlock();
        if (!isForestryLeafBlock(block.getClass())) {
            return;
        }
        Method getLeafDrop = findForestryLeafDropMethod(block.getClass());
        if (getLeafDrop == null) {
            return;
        }

        NonNullList<ItemStack> drops = NonNullList.create();
        try {
            GameProfile profile = player.getGameProfile();
            getLeafDrop.invoke(block, drops, event.getWorld(), profile, event.getPos(),
                100.0F, event.getFortuneLevel());
        } catch (ReflectiveOperationException failure) {
            if (TT2.logger != null) {
                TT2.logger.warn("Could not apply the Forestry grafter drop multiplier", failure);
            }
            return;
        }

        event.getDrops().clear();
        event.getDrops().addAll(drops);
        ToolHelper.damageTool(staff, 1, player);
    }

    private static boolean isForestryLeafBlock(Class<?> type) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            if (current.getName().equals("forestry.arboriculture.blocks.BlockAbstractLeaves")) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static Method findForestryLeafDropMethod(Class<?> type) {
        synchronized (FORESTRY_LEAF_DROP_METHODS) {
            Method cached = FORESTRY_LEAF_DROP_METHODS.get(type);
            if (cached != null) {
                return cached;
            }
        }
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Method method = current.getDeclaredMethod("getLeafDrop", NonNullList.class,
                    World.class, GameProfile.class, BlockPos.class, float.class, int.class);
                method.setAccessible(true);
                synchronized (FORESTRY_LEAF_DROP_METHODS) {
                    FORESTRY_LEAF_DROP_METHODS.put(type, method);
                }
                return method;
            } catch (NoSuchMethodException ignored) {
                // Continue through the Forestry leaf hierarchy.
            }
        }
        return null;
    }

    private static EnumActionResult useAeNetworkTool(EntityPlayer player, World world, ItemStack staff,
                                                      EnumHand hand, BlockPos pos, @Nullable EnumFacing facing,
                                                      float hitX, float hitY, float hitZ, String key) {
        Item item = getItem(ITEM_AE_NETWORK_TOOL);
        if (item == null) {
            return EnumActionResult.PASS;
        }
        if (facing == null) {
            return EnumActionResult.PASS;
        }
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        boolean handled = false;
        try {
            Method method = item.getClass().getMethod("serverSideToolLogic", ItemStack.class,
                EntityPlayer.class, EnumHand.class, World.class, BlockPos.class, EnumFacing.class,
                float.class, float.class, float.class);
            Object result = method.invoke(item, staff, player, hand, world, pos, facing,
                hitX, hitY, hitZ);
            handled = Boolean.TRUE.equals(result);
        } catch (ReflectiveOperationException failure) {
            if (TT2.logger != null) {
                TT2.logger.warn("Could not invoke the Applied Energistics network tool", failure);
            }
        }

        return handled ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
    }
    @Nullable
    public static Object createAeNetworkToolGui(EntityPlayer player, ItemStack staff, boolean client) {
        if (player == null || staff == null || staff.isEmpty() || !isLoaded("appliedenergistics2")) {
            return null;
        }
        try {
            Class<?> gridHostClass = Class.forName("appeng.api.networking.IGridHost");
            Class<?> viewerClass = Class.forName("appeng.items.contents.NetworkToolViewer");
            Object viewer = viewerClass.getConstructor(ItemStack.class, gridHostClass)
                .newInstance(staff, null);
            Class<?> interfaceClass = Class.forName("appeng.api.implementations.guiobjects.INetworkTool");
            if (client) {
                Class<?> guiClass = Class.forName("appeng.client.gui.implementations.GuiNetworkTool");
                return guiClass.getConstructor(net.minecraft.entity.player.InventoryPlayer.class, interfaceClass)
                    .newInstance(player.inventory, viewer);
            }
            Class<?> containerClass = Class.forName("appeng.container.implementations.ContainerNetworkTool");
            return containerClass.getConstructor(net.minecraft.entity.player.InventoryPlayer.class, interfaceClass)
                .newInstance(player.inventory, viewer);
        } catch (ReflectiveOperationException failure) {
            logCompatFailure("Applied Energistics network tool GUI", failure);
            return null;
        }
    }

    private static boolean canUseForestrySmoker(EntityPlayer player, World world) {
        if (player == null || world == null || !isLoaded("forestry")) {
            return false;
        }
        RayTraceResult hit = player.rayTrace(5.0D, 1.0F);
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            return false;
        }
        return isForestryHive(world, hit.getBlockPos());
    }

    private static boolean isForestryHive(World world, BlockPos pos) {
        Object tile = world.getTileEntity(pos);
        if (tile == null) {
            return false;
        }
        try {
            return Class.forName("forestry.api.apiculture.IHiveTile").isInstance(tile);
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private static Object invokeNoArgs(Object target, String name) throws ReflectiveOperationException {
        return invokeByName(target, name);
    }

    private static Object invokeByName(Object target, String name, Object... arguments)
        throws ReflectiveOperationException {
        if (target == null) {
            throw new NoSuchMethodException(name + " on null");
        }
        for (Method method : target.getClass().getMethods()) {
            if (method.getName().equals(name) && parametersAccept(method.getParameterTypes(), arguments)) {
                return method.invoke(target, arguments);
            }
        }
        throw new NoSuchMethodException(target.getClass().getName() + "." + name);
    }

    private static boolean parametersAccept(Class<?>[] parameterTypes, Object[] arguments) {
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

    private static Object enumConstant(String className, String constant) throws ReflectiveOperationException {
        Class<?> type = Class.forName(className);
        @SuppressWarnings({"rawtypes", "unchecked"})
        Object value = Enum.valueOf((Class<? extends Enum>) type.asSubclass(Enum.class), constant);
        return value;
    }

    private static void logCompatFailure(String behavior, Throwable failure) {
        if (TT2.logger != null) {
            TT2.logger.warn("Could not apply optional behavior: {}", behavior, failure);
        }
    }


    private static final class AnalyzerBlockInteraction {
        private final EnumHand hand;
        private final long tick;

        private AnalyzerBlockInteraction(EnumHand hand, long tick) {
            this.hand = hand;
            this.tick = tick;
        }
    }

    public static float getExternalDamageMultiplier(ItemStack staff) {
        if (staff == null || staff.isEmpty()) {
            return 1.0F;
        }
        try {
            ToolNBT original = TagUtil.getOriginalToolStats(staff);
            float base = original == null ? 0.0F : original.attack;
            float current = ToolHelper.getAttackStat(staff);
            if (base <= 0.0F) {
                return 1.0F;
            }
            float multiplier = current / base;
            return Float.isNaN(multiplier) || Float.isInfinite(multiplier)
                ? 1.0F : Math.max(0.0F, multiplier);
        } catch (RuntimeException ignored) {
            return 1.0F;
        }
    }

    private static float getThaumcraftTraitBaseDamage(float originalDamage, ItemStack staff,
                                                       EntityLivingBase caster) {
        if (!isInsightStaff(staff)) {
            return originalDamage;
        }
        try {
            double adjusted = originalDamage + ToolHelper.getActualDamage(staff, caster)
                * TT2Config.craftsmanStaffInsightSpellDamageCoefficient;
            adjusted *= getExternalDamageMultiplier(staff);
            if (Double.isNaN(adjusted) || Double.isInfinite(adjusted)) {
                return originalDamage;
            }
            return (float) Math.max(0.0D, Math.min(Float.MAX_VALUE, adjusted));
        } catch (RuntimeException ignored) {
            return originalDamage;
        }
    }

    private static <T> T delegate(EntityPlayer player, ItemStack staff, String key, Item item,
                                  Action<T> action) {
        return delegate(player, staff, key, item, false, action);
    }

    private static <T> T delegate(EntityPlayer player, ItemStack staff, String key, Item item,
                                  boolean externalCast, Action<T> action) {
        int originalDamage = staff.getItemDamage();
        int originalCount = staff.getCount();
        NBTTagCompound originalTag = staff.hasTagCompound() ? staff.getTagCompound().copy() : null;
        NBTTagCompound saved = getSavedTag(staff, key);
        int externalDamage = saved != null && saved.hasKey(TAG_PROXY_ITEM_DAMAGE, 3)
            ? saved.getInteger(TAG_PROXY_ITEM_DAMAGE) : 0;
        if (saved != null) {
            saved.removeTag(TAG_PROXY_ITEM_DAMAGE);
            NBTTagCompound working = staff.hasTagCompound()
                ? staff.getTagCompound() : new NBTTagCompound();
            for (String stateKey : saved.getKeySet()) {
                if (!TAG_COMPAT.equals(stateKey)) {
                    working.setTag(stateKey, saved.getTag(stateKey).copy());
                }
            }
            staff.setTagCompound(working);
        }
        staff.setItemDamage(externalDamage);
        prepareStaffForUse(item, staff);
        int beforeDamage = staff.getItemDamage();
        int beforeCount = staff.getCount();
        T result = null;
        NBTTagCompound externalState = new NBTTagCompound();
        int resultingExternalDamage = beforeDamage;
        try {
            if (externalCast) {
                synchronized (ACTIVE_EXTERNAL_CASTS) {
                    ACTIVE_EXTERNAL_CASTS.put(player, staff);
                }
            }
            result = action.run(staff);
        } catch (Throwable failure) {
            if (TT2.logger != null) {
                TT2.logger.warn("Optional item behavior failed for {}", item.getRegistryName(), failure);
            }
        } finally {
            if (externalCast) {
                synchronized (ACTIVE_EXTERNAL_CASTS) {
                    ACTIVE_EXTERNAL_CASTS.remove(player);
                }
            }
            ItemStack resultStack = result instanceof ActionResult
                ? ((ActionResult<?>) result).getResult() instanceof ItemStack
                    ? (ItemStack) ((ActionResult<?>) result).getResult() : staff
                : staff;
            int damage = transferableDamage(item, beforeDamage, beforeCount, resultStack);
            resultingExternalDamage = resultStack.getItemDamage();
            collectExternalState(originalTag, saved, resultStack, externalState);
            staff.setCount(originalCount);
            staff.setItemDamage(originalDamage);
            staff.setTagCompound(originalTag == null ? null : originalTag.copy());
            saveExternalState(staff, key, externalState, resultingExternalDamage);
            if (damage > 0 && !ToolHelper.isBroken(staff)) {
                ToolHelper.damageTool(staff, damage, player);
            }
        }
        return result;
    }

    private static int transferableDamage(Item item, int beforeDamage, int beforeCount, ItemStack resultStack) {
        String itemId = item == null || item.getRegistryName() == null
            ? "" : item.getRegistryName().toString();
        int damage = 0;
        if (ITEM_FORESTRY_SCOOP.equals(itemId) || ITEM_FORESTRY_GRAFTER.equals(itemId)) {
            damage += Math.max(0, resultStack.getItemDamage() - beforeDamage);
            resultStack.setItemDamage(0);
        }
        if (ITEM_THAUMCRAFT_SALT.equals(itemId)) {
            damage += Math.max(0, beforeCount - resultStack.getCount());
        }
        return damage;
    }

    private static boolean isExternalCaster(Item item) {
        String itemId = item == null || item.getRegistryName() == null
            ? "" : item.getRegistryName().toString();
        return ITEM_THAUMCRAFT_GAUNTLET.equals(itemId)
            || ITEM_THAUMCRAFT_GAUNTLET_LEGACY.equals(itemId) || isBotaniaGun(item);
    }

    @Nullable
    private static NBTTagCompound getSavedTag(ItemStack staff, String key) {
        if (staff == null || !staff.hasTagCompound() || key == null) {
            return null;
        }
        NBTTagCompound root = staff.getTagCompound();
        if (!root.hasKey(TAG_COMPAT, 10)) {
            return null;
        }
        NBTTagCompound compat = root.getCompoundTag(TAG_COMPAT);
        return compat.hasKey(key, 10) ? compat.getCompoundTag(key).copy() : null;
    }

    private static void prepareStaffForUse(Item item, ItemStack staff) {
        if (!isItem(item, ITEM_MEKANISM_CONFIGURATOR)) {
            return;
        }
        try {
            Class<?> modeClass = Class.forName(
                "mekanism.common.item.ItemConfigurator$ConfiguratorMode", false,
                item.getClass().getClassLoader());
            @SuppressWarnings({"rawtypes", "unchecked"})
            Object wrenchMode = Enum.valueOf((Class<? extends Enum>) modeClass.asSubclass(Enum.class), "WRENCH");
            Method setState = item.getClass().getMethod("setState", ItemStack.class, modeClass);
            setState.invoke(item, staff, wrenchMode);
        } catch (ReflectiveOperationException failure) {
            if (TT2.logger != null) {
                TT2.logger.warn("Could not select the Mekanism configurator wrench mode", failure);
            }
        }
    }

    private static boolean isItem(Item item, String itemId) {
        return item != null && item.getRegistryName() != null
            && itemId.equals(item.getRegistryName().toString());
    }

    private static boolean isBotaniaGun(Item item) {
        if (item == null || item.getRegistryName() == null) {
            return false;
        }
        String id = item.getRegistryName().toString();
        return ITEM_BOTANIA_GUN.equals(id) || "botania:mana_gun".equals(id);
    }

    private static void collectExternalState(@Nullable NBTTagCompound original, @Nullable NBTTagCompound previous,
                                             ItemStack workingStack, NBTTagCompound output) {
        if (!workingStack.hasTagCompound()) {
            return;
        }
        NBTTagCompound working = workingStack.getTagCompound();
        for (String stateKey : working.getKeySet()) {
            if (TAG_COMPAT.equals(stateKey)) {
                continue;
            }
            boolean previouslyExternal = previous != null && previous.hasKey(stateKey);
            boolean belongsToOriginal = original != null && original.hasKey(stateKey);
            if (previouslyExternal || !belongsToOriginal) {
                output.setTag(stateKey, working.getTag(stateKey).copy());
            }
        }
    }

    private static void saveExternalState(ItemStack staff, String key, NBTTagCompound state, int itemDamage) {
        if (staff == null || key == null) {
            return;
        }
        NBTTagCompound compat = ensureCompatRoot(staff);
        if (itemDamage != 0) {
            state.setInteger(TAG_PROXY_ITEM_DAMAGE, itemDamage);
        }
        if (state.getKeySet().isEmpty()) {
            compat.removeTag(key);
        } else {
            compat.setTag(key, state);
        }
    }

    private static NBTTagCompound ensureCompatRoot(ItemStack staff) {
        if (!staff.hasTagCompound()) {
            staff.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound root = staff.getTagCompound();
        if (!root.hasKey(TAG_COMPAT, 10)) {
            root.setTag(TAG_COMPAT, new NBTTagCompound());
        }
        return root.getCompoundTag(TAG_COMPAT);
    }

    @Nullable
    private static EnumHand findHand(EntityPlayer player, ItemStack staff) {
        if (player == null) {
            return null;
        }
        if (player.getHeldItemMainhand() == staff || ItemStack.areItemStacksEqual(player.getHeldItemMainhand(), staff)) {
            return EnumHand.MAIN_HAND;
        }
        if (player.getHeldItemOffhand() == staff || ItemStack.areItemStacksEqual(player.getHeldItemOffhand(), staff)) {
            return EnumHand.OFF_HAND;
        }
        return null;
    }

    @Nullable
    private static ItemStack findStaff(EntityPlayer player) {
        if (player == null) {
            return null;
        }
        ItemStack main = player.getHeldItemMainhand();
        if (main != null && main.getItem() instanceof xy177.tt2.tools.CraftsmanStaff) {
            return main;
        }
        ItemStack off = player.getHeldItemOffhand();
        return off != null && off.getItem() instanceof xy177.tt2.tools.CraftsmanStaff ? off : null;
    }

    public interface WorldAccess {
        net.minecraft.world.World world();
    }
}
