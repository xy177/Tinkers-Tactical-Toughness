package xy177.tt2test;

import c4.conarm.lib.ArmoryRegistry;
import c4.conarm.lib.armor.ArmorCore;
import c4.conarm.lib.materials.ArmorMaterials;
import c4.conarm.lib.traits.AbstractArmorTrait;
import c4.conarm.lib.traits.IArmorTrait;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.modifiers.ModifierAspect;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.traits.AbstractTrait;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.library.utils.ToolBuilder;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.tools.melee.TinkerMeleeWeapons;
import slimeknights.tconstruct.tools.common.RepairRecipe;
import slimeknights.tconstruct.tools.common.inventory.ContainerToolStation;
import slimeknights.tconstruct.tools.common.tileentity.TileToolStation;
import slimeknights.tconstruct.tools.common.tileentity.TileToolForge;
import slimeknights.tconstruct.tools.modifiers.ModExtraTrait;
import xy177.tt2.api.consumable.*;
import xy177.tt2.config.TT2Config;
import xy177.tt2.inventory.ContainerModifierWorktable;
import xy177.tt2.logic.ModifierWorktableLogic;
import xy177.tt2.tile.TileModifierWorktable;
import slimeknights.tconstruct.shared.TinkerCommons;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/** Runs only with -PconsumableTest. Never included in the release JAR. */
@Mod(modid = "tt2_consumable_test", name = "TT2 consumable runtime tests", version = "1", dependencies = "required-after:tt2;")
public final class ConsumableRuntimeTest {
    private TestTrait material;
    private TestModifier modifier;
    private TestModifier free;
    private TestModifier multi;
    private ModExtraTrait emboss;
    private TestArmorTrait armorTrait;
    private final ITrait control = new AbstractTrait("tt2_test_control", 0xFFFFFF) {
        @Override public float damage(ItemStack s, EntityLivingBase a, EntityLivingBase t, float d, float c, boolean crit) { return c + 7; }
    };
    private int checks;
    private final List<String> results = new ArrayList<>();
    private int consumedEvents;
    private boolean cancelConsumption;

    @Mod.EventHandler public void preInit(FMLPreInitializationEvent event) {
        material = new TestTrait();
        armorTrait = new TestArmorTrait();
        modifier = new TestModifier("tt2_test_modifier", 1, false);
        free = new TestModifier("tt2_test_free", 1, true);
        multi = new TestModifier("tt2_test_multi", 2, false);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler public void init(FMLInitializationEvent event) {
        TinkerRegistry.getMaterial("wood").addTrait(material);
        ArmorMaterials.addArmorTrait(TinkerRegistry.getMaterial("wood"), armorTrait);
        emboss = new ModExtraTrait(TinkerRegistry.getMaterial("wood"), Arrays.asList(material), "tt2_test_emboss");
        emboss.toolCores.add(TinkerMeleeWeapons.broadSword);
    }

    @SubscribeEvent public void consumed(ConsumableUseEvent.Consumed event) { consumedEvents++; }
    @SubscribeEvent public void beforeConsume(ConsumableUseEvent.Before event) { if (cancelConsumption) event.setCanceled(true); }

    @Mod.EventHandler public void run(FMLServerStartedEvent event) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        try {
            runTests(server.getWorld(0));
            results.add("PASS: " + checks + " assertions; CraftTweaker=" + Loader.isModLoaded("crafttweaker"));
        } catch (Throwable failure) {
            failure.printStackTrace();
            results.add("FAIL: " + failure);
        } finally {
            try { Files.write(new File("consumable-test-results.txt").toPath(), results, StandardCharsets.UTF_8); }
            catch (Exception failure) { throw new RuntimeException(failure); }
            for (String result : results) System.out.println("[TT2-TEST] " + result);
            server.initiateShutdown();
        }
    }

    private ItemStack tool() {
        return tool("wood");
    }

    private ItemStack tool(String name) {
        Material material = TinkerRegistry.getMaterial(name);
        return TinkerMeleeWeapons.broadSword.buildItem(Arrays.asList(material, material, material));
    }

    private void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        checks++;
        results.add("OK " + label);
    }

    private void runTests(WorldServer world) throws Exception {
        FakePlayer player = FakePlayerFactory.getMinecraft(world);
        ItemStack stack = tool();
        check(ConsumableUses.remaining(stack, material.identifier) == 3, "material starts at three uses");
        check(tooltip(material, stack).endsWith("[3]"), "tooltip shows remaining uses");
        player.setHeldItem(EnumHand.MAIN_HAND, stack);
        for (int i = 0; i < 4; i++) {
            EntityZombie target = new EntityZombie(world);
            target.setHealth(20);
            ToolHelper.attackEntity(stack, TinkerMeleeWeapons.broadSword, player, target, null, false);
        }
        check(material.damageCalls == 3, "real ToolHelper attack gates fourth damage callback");
        check(material.afterCalls == 3, "last use completes afterHit but fourth attack does not");
        check(ConsumableUses.remaining(stack, material.identifier) == 0, "material remains at zero");
        check(ConsumableUses.isPresent(stack, material.identifier), "exhausted material trait remains present");
        check(tooltip(material, stack).startsWith(TextFormatting.GRAY.toString()), "exhausted tooltip is grey");
        check(tooltip(material, stack).endsWith("[0]"), "exhausted tooltip shows zero");
        ToolHelper.healTool(stack, 500, player);
        check(ConsumableUses.remaining(stack, material.identifier) == 0, "passive healing does not replenish uses");
        ToolBuilder.rebuildTool(stack.getTagCompound(), TinkerMeleeWeapons.broadSword);
        check(ConsumableUses.remaining(stack, material.identifier) == 0, "rebuild does not replenish uses");
        File saved = new File("consumable-tool.dat");
        CompressedStreamTools.safeWrite(stack.writeToNBT(new NBTTagCompound()), saved);
        stack = new ItemStack(CompressedStreamTools.read(saved));
        check(ConsumableUses.remaining(stack, material.identifier) == 0, "disk NBT save/load preserves exhaustion");
        stack.setItemDamage(0);
        NonNullList<ItemStack> repair = NonNullList.from(ItemStack.EMPTY, new ItemStack(net.minecraft.init.Blocks.PLANKS, 2));
        ItemStack preview = ToolBuilder.tryRepairTool(repair, stack, false);
        check(!preview.isEmpty() && ConsumableUses.remaining(preview, material.identifier) == 3, "full durability repair preview replenishes uses");
        check(repair.get(0).getCount() == 2 && ConsumableUses.remaining(stack, material.identifier) == 0, "repair preview changes neither input nor source");
        ItemStack repaired = ToolBuilder.tryRepairTool(repair, stack, true);
        check(!repaired.isEmpty() && repair.get(0).getCount() == 1, "full durability repair consumes one valid material");
        check(ToolBuilder.tryRepairTool(repair, repaired, false).isEmpty(), "fully restored tool cannot be repaired for nothing");
        ConsumableUses.tryConsume(repaired, material.identifier, 1);
        check(ConsumableUses.remaining(repaired, material.identifier) == 2, "partial use consumed");
        ItemStack partial = ToolBuilder.tryRepairTool(repair, repaired, false);
        check(ConsumableUses.remaining(partial, material.identifier) == 3, "repair refills partial pools too");
        check(ToolBuilder.tryRepairTool(NonNullList.from(ItemStack.EMPTY, new ItemStack(Items.APPLE)), repaired, false).isEmpty(), "wrong repair material rejected");
        InventoryCrafting crafting = new InventoryCrafting(new Container() {
            @Override public boolean canInteractWith(EntityPlayer player) { return true; }
        }, 3, 3);
        crafting.setInventorySlotContents(0, repaired);
        crafting.setInventorySlotContents(1, TinkerTools.sharpeningKit.getItemstackWithMaterial(TinkerRegistry.getMaterial("wood")));
        ItemStack kitResult = new RepairRecipe().getCraftingResult(crafting);
        check(!kitResult.isEmpty() && ConsumableUses.remaining(kitResult, material.identifier) == 3, "real sharpening-kit recipe refills full-durability tool");

        int beforeEvents = consumedEvents;
        ItemStack rollback = tool();
        check(!ConsumableUses.tryUse(rollback, material.identifier, 2, () -> false), "failed effect returns false");
        check(ConsumableUses.remaining(rollback, material.identifier) == 3 && consumedEvents == beforeEvents, "failed effect refunds and emits no consumption event");
        try { ConsumableUses.tryUse(rollback, material.identifier, 2, () -> { throw new IllegalStateException("expected"); }); }
        catch (IllegalStateException expected) { }
        check(ConsumableUses.remaining(rollback, material.identifier) == 3 && consumedEvents == beforeEvents, "throwing effect refunds reservation");
        check(!ConsumableUses.tryConsume(rollback, material.identifier, 4), "insufficient uses reject atomically");
        cancelConsumption = true;
        check(!ConsumableUses.tryConsume(rollback, material.identifier, 1) && ConsumableUses.remaining(rollback, material.identifier) == 3,
            "cancelled Before event changes no uses");
        cancelConsumption = false;
        check(control.damage(rollback, player, new EntityZombie(world), 1, 1, false) == 8, "unregistered trait callback is unchanged");
        testModifier(modifier, 1, 1);
        testModifier(free, 1, 0);
        testModifier(multi, 2, 2);
        testExtraction(modifier, 1);
        testExtraction(free, 1);
        testExtraction(multi, 2);
        testDeferredAndSnapshots(player);
        testStation(player, world, new TileToolStation(), "station");
        testStation(player, world, new TileToolForge(), "forge");
        testHidden();
        testEmboss();
        testNestedAndPassive();
        testArmor(player);
        testWorktableBlacklist(player, world);
        if (Loader.isModLoaded("contenttweaker")) testScript(player, world);
    }

    private String tooltip(IModifier trait, ItemStack stack) {
        return trait.getTooltip(TinkerUtil.getModifierTag(stack, trait.getIdentifier()), false);
    }

    private void testModifier(TestModifier mod, int levels, int expectedSlots) throws Exception {
        ItemStack tool = tool();
        int before = TagUtil.getBaseModifiersUsed(tool.getTagCompound());
        for (int i = 0; i < levels; i++) mod.apply(tool);
        ConsumableUses.rebuild(tool);
        check(ConsumableUses.paidSlots(tool, mod.identifier) == expectedSlots, mod.identifier + " records actual slots");
        check(ConsumableUses.tryConsume(tool, mod.identifier, 3), mod.identifier + " consumes final uses");
        check(!ConsumableUses.isPresent(tool, mod.identifier), mod.identifier + " removed at zero");
        check(TagUtil.getBaseModifiersUsed(tool.getTagCompound()) == before, mod.identifier + " returns exact slots");
        mod.apply(tool);
        check(ConsumableUses.remaining(tool, mod.identifier) == 3, mod.identifier + " reapplies with fresh uses");
    }

    private void testStation(FakePlayer player, WorldServer world, TileToolStation tile, String label) {
        ItemStack source = tool();
        ConsumableUses.tryConsume(source, material.identifier, 1);
        source.setItemDamage(0);
        tile.setWorld(world);
        tile.setPos(new BlockPos(0, 80, 0));
        IInventory inventory = tile;
        inventory.setInventorySlotContents(0, source);
        inventory.setInventorySlotContents(1, new ItemStack(net.minecraft.init.Blocks.PLANKS, 2));
        ContainerToolStation container = new ContainerToolStation(player.inventory, tile);
        container.setToolSelection(null, inventory.getSizeInventory());
        container.onCraftMatrixChanged(tile);
        ItemStack result = container.getResult().copy();
        check(!result.isEmpty() && ConsumableUses.remaining(result, material.identifier) == 3, label + " container previews full-durability repair");
        check(ConsumableUses.remaining(source, material.identifier) == 2 && inventory.getStackInSlot(1).getCount() == 2,
            label + " preview leaves inputs untouched");
        container.onResultTaken(player, result);
        check(inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(1).getCount() == 1,
            label + " taking result consumes exactly one repair material");
    }

    private ItemStack hide(ItemStack source, String id) {
        TileModifierWorktable table = new TileModifierWorktable();
        table.setInventorySlotContents(TileModifierWorktable.SLOT_TOOL, source);
        table.setInventorySlotContents(TileModifierWorktable.SLOT_INPUT_1,
            PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.INVISIBILITY));
        return ModifierWorktableLogic.getResult(table, id, ModifierWorktableLogic.TYPE_HIDE);
    }

    private TileModifierWorktable extractionTable(ItemStack tool, ItemStack first, ItemStack second) {
        TileModifierWorktable table = new TileModifierWorktable();
        table.setInventorySlotContents(TileModifierWorktable.SLOT_TOOL, tool);
        table.setInventorySlotContents(TileModifierWorktable.SLOT_INPUT_1, first);
        table.setInventorySlotContents(TileModifierWorktable.SLOT_INPUT_2, second);
        return table;
    }

    private void checkBlockedExtraction(TileModifierWorktable table, String id, int action, String label) {
        NBTTagCompound before = table.writeToNBT(new NBTTagCompound());
        check(ModifierWorktableLogic.isExtractionBlocked(id, action), label + " reports blacklist warning");
        check(!ModifierWorktableLogic.canApplyAction(table.getStackInSlot(0), id, action), label + " rejects eligibility");
        check(ModifierWorktableLogic.getResult(table, id, action).isEmpty(), label + " rejects selected preview");
        check(ModifierWorktableLogic.getResult(table, id).isEmpty(), label + " rejects automatic preview");
        ModifierWorktableLogic.apply(table, id, action);
        ModifierWorktableLogic.apply(table, id);
        check(before.equals(table.writeToNBT(new NBTTagCompound())), label + " leaves tool, uses, slots and inputs unchanged");
    }

    private void testWorktableBlacklist(FakePlayer player, WorldServer world) throws Exception {
        String[] original = TT2Config.modifierWorktableExtractionBlacklist;
        File configFile = File.createTempFile("tt2-worktable-blacklist-", ".cfg");
        try {
            check(original.length == 0, "extraction blacklist defaults to empty");
            ItemStack source = tool();
            modifier.apply(source);
            free.apply(source);
            ConsumableUses.tryConsume(source, modifier.identifier, 1);
            TileModifierWorktable table = extractionTable(source, TinkerCommons.matSlimeCrystalGreen.copy(),
                new ItemStack(Items.DYE, 3, 4));
            check(!ModifierWorktableLogic.getResult(table, modifier.identifier).isEmpty(), "empty blacklist allows extraction");
            List<String> candidates = ModifierWorktableLogic.getSelectableModifiers(table, ModifierWorktableLogic.TYPE_EXTRACT);

            Configuration cfg = new Configuration(configFile);
            cfg.get(Configuration.CATEGORY_GENERAL, "modifierWorktableExtractionBlacklist",
                new String[] { " " + modifier.identifier + " ", "", "unknown_test_id" });
            cfg.save();
            TT2Config.init(configFile);
            check(TT2Config.modifierWorktableExtractionBlacklist.length == 3, "blacklist loads from actual Forge string-list config");
            checkBlockedExtraction(table, modifier.identifier, ModifierWorktableLogic.TYPE_EXTRACT, "consumable modifier blacklist");
            check(candidates.equals(ModifierWorktableLogic.getSelectableModifiers(table, ModifierWorktableLogic.TYPE_EXTRACT)),
                "blacklist does not shift client/server selection indices");
            check(!ModifierWorktableLogic.getResult(table, free.identifier).isEmpty(), "unlisted modifier remains extractable");
            TileModifierWorktable containerTable = extractionTable(source.copy(), TinkerCommons.matSlimeCrystalGreen.copy(),
                new ItemStack(Items.DYE, 3, 4));
            containerTable.setWorld(world);
            ContainerModifierWorktable container = new ContainerModifierWorktable(player.inventory, containerTable);
            check(!container.isExtractionBlocked(), "action overview has no blacklist warning");
            container.enchantItem(player, container.getActions().indexOf(ModifierWorktableLogic.TYPE_EXTRACT));
            container.enchantItem(player, container.getModifiers().indexOf(modifier.identifier) + 1);
            check(containerTable.getStackInSlot(TileModifierWorktable.SLOT_OUTPUT).isEmpty(), "real worktable container rejects blacklisted output");
            check(container.isExtractionBlocked(), "selected blacklisted modifier reports warning");
            WindowPropertyRecorder listener = new WindowPropertyRecorder();
            container.addListener(listener);
            check(listener.extractionBlocked == 1, "opening container synchronizes blacklist warning");
            String[] serverBlacklist = TT2Config.modifierWorktableExtractionBlacklist;
            TT2Config.modifierWorktableExtractionBlacklist = new String[0];
            container.updateProgressBar(3, 1);
            check(container.isExtractionBlocked(), "received warning does not consult local blacklist");
            TT2Config.modifierWorktableExtractionBlacklist = serverBlacklist;
            container.updateProgressBar(3, 0);
            check(!container.isExtractionBlocked(), "received clear removes blacklist warning");
            container.detectAndSendChanges();
            check(container.isExtractionBlocked() && listener.extractionBlocked == 1, "server refresh synchronizes authoritative warning");
            container.enchantItem(player, container.getModifiers().indexOf(free.identifier) + 1);
            container.detectAndSendChanges();
            check(!container.isExtractionBlocked() && listener.extractionBlocked == 0, "switching to unlisted modifier clears synced warning");
            containerTable.setInventorySlotContents(TileModifierWorktable.SLOT_INPUT_1, ItemStack.EMPTY);
            container.detectAndSendChanges();
            check(!container.isExtractionBlocked() && containerTable.getStackInSlot(TileModifierWorktable.SLOT_OUTPUT).isEmpty(),
                "missing ingredients do not show blacklist warning");
            containerTable.setInventorySlotContents(TileModifierWorktable.SLOT_INPUT_1, TinkerCommons.matSlimeCrystalGreen.copy());
            container.detectAndSendChanges();
            ItemStack allowedOutput = containerTable.getStackInSlot(TileModifierWorktable.SLOT_OUTPUT).copy();
            check(!allowedOutput.isEmpty(), "real worktable container previews unlisted output");
            container.onOutputTaken(player, allowedOutput);
            check(!ConsumableUses.isPresent(containerTable.getStackInSlot(0), free.identifier)
                && ConsumableUses.isPresent(containerTable.getStackInSlot(0), modifier.identifier),
                "real worktable container extracts only the selected unlisted modifier");
            container.enchantItem(player, 0);
            container.enchantItem(player, container.getActions().indexOf(ModifierWorktableLogic.TYPE_REMOVE));
            container.enchantItem(player, container.getModifiers().indexOf(modifier.identifier) + 1);
            container.detectAndSendChanges();
            check(!container.isExtractionBlocked() && listener.extractionBlocked == 0, "non-extraction action has no blacklist warning");
            check(ModifierWorktableLogic.canApplyAction(source, modifier.identifier, ModifierWorktableLogic.TYPE_REMOVE),
                "blacklist does not block removal");
            check(ModifierWorktableLogic.canApplyAction(source, modifier.identifier, ModifierWorktableLogic.TYPE_SORT),
                "blacklist does not block sorting");
            ItemStack hidden = hide(source, modifier.identifier);
            check(!hidden.isEmpty() && ModifierWorktableLogic.isHidden(hidden, modifier.identifier), "blacklist does not block hiding");
            check(ModifierWorktableLogic.canApplyAction(hidden, modifier.identifier, ModifierWorktableLogic.TYPE_UNHIDE),
                "blacklist does not block unhiding");
            TileModifierWorktable hiddenTable = extractionTable(hidden, TinkerCommons.matSlimeCrystalGreen.copy(),
                new ItemStack(Items.DYE, 3, 4));
            checkBlockedExtraction(hiddenTable, modifier.identifier, ModifierWorktableLogic.TYPE_EXTRACT, "hidden modifier blacklist");

            TT2Config.modifierWorktableExtractionBlacklist = new String[] { modifier.identifier + "_other", "", "unknown_test_id" };
            check(!ModifierWorktableLogic.getResult(table, modifier.identifier).isEmpty(), "blacklist matches exact IDs only");
            TT2Config.modifierWorktableExtractionBlacklist = new String[0];
            ModifierWorktableLogic.apply(table, modifier.identifier);
            check(!ConsumableUses.isPresent(source, modifier.identifier), "clearing blacklist restores actual extraction");

            source = tool("iron");
            IModifier diamond = TinkerRegistry.getModifier("diamond");
            diamond.apply(source);
            table = extractionTable(source, TinkerCommons.matSlimeCrystalGreen.copy(), new ItemStack(Items.DYE, 3, 4));
            check(!ModifierWorktableLogic.getResult(table, "diamond").isEmpty(), "ordinary diamond modifier is extractable");
            TT2Config.modifierWorktableExtractionBlacklist = new String[] { "diamond" };
            checkBlockedExtraction(table, "diamond", ModifierWorktableLogic.TYPE_EXTRACT, "ordinary modifier blacklist");

            TT2Config.modifierWorktableExtractionBlacklist = new String[0];
            source = tool("iron");
            emboss.apply(source);
            table = extractionTable(source, TinkerCommons.matSlimeCrystalMagma.copy(), new ItemStack(Items.DIAMOND));
            check(!ModifierWorktableLogic.getResult(table, emboss.getIdentifier()).isEmpty(), "unlisted embossment is extractable");
            TT2Config.modifierWorktableExtractionBlacklist = new String[] { emboss.getIdentifier() };
            checkBlockedExtraction(table, emboss.getIdentifier(), ModifierWorktableLogic.TYPE_EXTRACT_EMBOSS, "embossment ID blacklist");
            TT2Config.modifierWorktableExtractionBlacklist = new String[] { material.identifier };
            checkBlockedExtraction(table, emboss.getIdentifier(), ModifierWorktableLogic.TYPE_EXTRACT_EMBOSS, "embedded material trait blacklist");

            for (String id : new String[] { "fortifyiron", "polished_armoriron", "toolleveling", "leveling" }) {
                TT2Config.modifierWorktableExtractionBlacklist = new String[0];
                if (id.equals("polished_armoriron") || id.equals("leveling")) {
                    ArmorCore item = ArmoryRegistry.getArmor().iterator().next();
                    source = item.buildItem(java.util.Collections.nCopies(item.getRequiredComponents().size(), TinkerRegistry.getMaterial("wood")));
                } else {
                    source = tool("iron");
                }
                NBTTagCompound tag = new NBTTagCompound();
                ModifierNBT data = new ModifierNBT();
                data.identifier = id;
                data.level = 1;
                data.color = 0xFFFFFF;
                data.write(tag);
                tag.setInteger("xp", 50);
                NBTTagList modifiers = TagUtil.getModifiersTagList(source);
                modifiers.appendTag(tag);
                TagUtil.setModifiersTagList(source, modifiers);
                boolean experience = id.equals("toolleveling") || id.equals("leveling");
                int action = experience ? ModifierWorktableLogic.TYPE_EXTRACT_EXPERIENCE : ModifierWorktableLogic.TYPE_EXTRACT_FORTIFY;
                table = extractionTable(source,
                    (experience ? TinkerCommons.matMendingMoss : TinkerCommons.matSlimeCrystalBlue).copy(),
                    new ItemStack(experience ? Items.EXPERIENCE_BOTTLE : Items.GOLD_INGOT));
                check(!ModifierWorktableLogic.getResult(table, id, action).isEmpty(), id + " fixture is extractable");
                TT2Config.modifierWorktableExtractionBlacklist = new String[] { id };
                checkBlockedExtraction(table, id, action, id + " blacklist");
            }
        } finally {
            TT2Config.init(new File("config/tt2.cfg"));
            TT2Config.modifierWorktableExtractionBlacklist = original;
            Files.deleteIfExists(configFile.toPath());
        }
    }

    private void testHidden() {
        ItemStack source = tool();
        modifier.apply(source);
        free.apply(source);
        source = hide(source, modifier.identifier);
        check(!source.isEmpty() && ModifierWorktableLogic.isHidden(source, modifier.identifier), "worktable hides consumable modifier");
        ConsumableUses.tryConsume(source, free.identifier, 3);
        check(ConsumableUses.isPresent(source, modifier.identifier) && ModifierWorktableLogic.isHidden(source, modifier.identifier),
            "another modifier exhaustion preserves hidden modifier");
        ConsumableUses.tryConsume(source, modifier.identifier, 3);
        check(!ConsumableUses.isPresent(source, modifier.identifier) && !ModifierWorktableLogic.isHidden(source, modifier.identifier),
            "hidden consumable expires without resurrection");
        check(TagUtil.getBaseModifiersUsed(source.getTagCompound()) == 0, "hidden exhaustion returns exact paid slot");
    }

    private ItemStack extractEmboss(ItemStack source) {
        TileModifierWorktable table = new TileModifierWorktable();
        table.setInventorySlotContents(TileModifierWorktable.SLOT_TOOL, source);
        table.setInventorySlotContents(TileModifierWorktable.SLOT_INPUT_1, TinkerCommons.matSlimeCrystalMagma.copy());
        table.setInventorySlotContents(TileModifierWorktable.SLOT_INPUT_2, new ItemStack(Items.DIAMOND));
        ItemStack crystal = ModifierWorktableLogic.getResult(table, emboss.getIdentifier(), ModifierWorktableLogic.TYPE_EXTRACT_EMBOSS);
        ModifierWorktableLogic.apply(table, emboss.getIdentifier(), ModifierWorktableLogic.TYPE_EXTRACT_EMBOSS);
        return crystal;
    }

    private void testEmboss() throws Exception {
        ItemStack source = tool("iron");
        modifier.apply(source);
        emboss.apply(source);
        ConsumableUses.rebuild(source);
        ConsumableUses.tryConsume(source, material.identifier, 1);
        ItemStack crystal = extractEmboss(source);
        check(!crystal.isEmpty() && !ConsumableUses.isPresent(source, material.identifier), "emboss extraction removes only emboss-provided trait");
        check(TagUtil.getBaseModifiersUsed(source.getTagCompound()) == 1, "zero-slot emboss cannot refund unrelated paid slot");
        ItemStack dest = ToolBuilder.tryModifyTool(NonNullList.from(ItemStack.EMPTY, crystal), tool("iron"), true);
        check(!dest.isEmpty() && ConsumableUses.remaining(dest, material.identifier) == 2, "emboss crystal transfers remaining pool");
        emboss.apply(source);
        ConsumableUses.rebuild(source);
        check(ConsumableUses.remaining(source, material.identifier) == 0, "re-embossing source does not duplicate transferred pool");
        ItemStack restored = ToolBuilder.tryRepairTool(NonNullList.from(ItemStack.EMPTY, new ItemStack(Items.IRON_INGOT)), source, true);
        check(!restored.isEmpty() && ConsumableUses.remaining(restored, material.identifier) == 3, "real repair replenishes re-embossed pool");
        ItemStack shared = tool();
        emboss.apply(shared);
        ConsumableUses.tryConsume(shared, material.identifier, 1);
        crystal = extractEmboss(shared);
        check(ConsumableUses.remaining(shared, material.identifier) == 2, "extracting redundant emboss keeps base-material pool");
        dest = ToolBuilder.tryModifyTool(NonNullList.from(ItemStack.EMPTY, crystal), tool("iron"), true);
        check(!dest.isEmpty() && ConsumableUses.remaining(dest, material.identifier) == 0, "redundant emboss cannot clone base-material uses");
    }

    private void testNestedAndPassive() {
        ItemStack source = tool();
        float charged = TagUtil.getToolStats(source).attack;
        ConsumableUses.tryConsume(source, material.identifier, 2);
        try (ConsumableUses.Action outer = ConsumableUses.beginAttack(source)) {
            ConsumableUses.tryConsume(source, material.identifier, 1);
            try (ConsumableUses.Action inner = ConsumableUses.beginAttack(source)) {
                check(!ConsumableUses.isActive(source, material.identifier), "nested target cannot reuse final charge");
            }
            check(ConsumableUses.isActive(source, material.identifier), "outer last action still finishes after nested target");
        }
        check(TagUtil.getToolStats(source).attack == charged - 2, "exhaustion removes passive stat effect");
        ConsumableUses.restore(source, material.identifier, 1);
        check(TagUtil.getToolStats(source).attack == charged, "explicit restoration reapplies passive stats once");
        ConsumableUses.rebuild(source);
        check(TagUtil.getToolStats(source).attack == charged, "repeated rebuild does not multiply passive stats");
    }

    private void testExtraction(TestModifier mod, int levels) throws Exception {
        ItemStack source = tool();
        for (int i = 0; i < levels; i++) mod.apply(source);
        ConsumableUses.tryConsume(source, mod.identifier, 1);
        TileModifierWorktable table = new TileModifierWorktable();
        table.setInventorySlotContents(TileModifierWorktable.SLOT_TOOL, source);
        table.setInventorySlotContents(TileModifierWorktable.SLOT_INPUT_1, TinkerCommons.matSlimeCrystalGreen.copy());
        table.setInventorySlotContents(TileModifierWorktable.SLOT_INPUT_2, new ItemStack(Items.DYE, 3, 4));
        ItemStack crystal = ModifierWorktableLogic.getResult(table, mod.identifier, ModifierWorktableLogic.TYPE_EXTRACT);
        check(!crystal.isEmpty(), mod.identifier + " worktable creates a consumable crystal");
        ModifierWorktableLogic.apply(table, mod.identifier, ModifierWorktableLogic.TYPE_EXTRACT);
        check(!ConsumableUses.isPresent(source, mod.identifier), mod.identifier + " extraction removes source modifier");
        check(TagUtil.getBaseModifiersUsed(source.getTagCompound()) == 0, mod.identifier + " extraction returns only paid slots");
        NonNullList<ItemStack> inputs = NonNullList.from(ItemStack.EMPTY, crystal.copy());
        ItemStack dest = ToolBuilder.tryModifyTool(inputs, tool(), false);
        check(!dest.isEmpty() && ConsumableUses.remaining(dest, mod.identifier) == 2, mod.identifier + " crystal preview preserves depleted uses");
        check(inputs.get(0).getCount() == 1, "crystal preview does not consume input");
        dest = ToolBuilder.tryModifyTool(inputs, tool(), true);
        check(inputs.get(0).isEmpty() && ConsumableUses.remaining(dest, mod.identifier) == 2, mod.identifier + " crystal application consumes crystal and transfers exact uses");
        check(slimeknights.tconstruct.library.modifiers.ModifierNBT.readTag(TinkerUtil.getModifierTag(dest, mod.identifier)).level == levels,
            mod.identifier + " crystal preserves complete modifier level");
    }

    private void testArmor(FakePlayer player) {
        ArmorCore item = ArmoryRegistry.getArmor().iterator().next();
        ItemStack armor = item.buildItem(java.util.Collections.nCopies(item.getRequiredComponents().size(), TinkerRegistry.getMaterial("wood")));
        check(ConsumableUses.remaining(armor, armorTrait.identifier) == 2, "armor material starts with registered pool");
        IArmorTrait trait = armorTrait;
        for (int i = 0; i < 3; i++) {
            float value = trait.onHurt(armor, player, DamageSource.GENERIC, 10, 10, new LivingHurtEvent(player, DamageSource.GENERIC, 10));
            check(value == (i < 2 ? 8 : 10), "armor hurt callback gating " + i);
        }
        armor.setItemDamage(0);
        NonNullList<ItemStack> inputs = NonNullList.from(ItemStack.EMPTY, new ItemStack(net.minecraft.init.Blocks.PLANKS));
        ItemStack result = ToolBuilder.tryRepairTool(inputs, armor, true);
        check(!result.isEmpty() && inputs.get(0).isEmpty() && ConsumableUses.remaining(result, armorTrait.identifier) == 2,
            "real full-durability armor repair refills pool and consumes material");
    }

    private void testDeferredAndSnapshots(FakePlayer player) {
        ItemStack tool = tool();
        modifier.apply(tool);
        ConsumableUses.Action pending = ConsumableUses.beginAttack(tool);
        ConsumableUses.tryConsume(tool, modifier.identifier, 3);
        pending.suspend();
        check(ConsumableUses.isPresent(tool, modifier.identifier), "split hit defers modifier removal");
        check(!ConsumableUses.isActive(tool, modifier.identifier), "unrelated action cannot borrow reserved final use");
        try (ConsumableUses.Action ignored = pending.resume()) {
            check(ConsumableUses.isActive(tool, modifier.identifier), "split hit resumes final afterHit");
        }
        check(!ConsumableUses.isPresent(tool, modifier.identifier), "split hit finalizes removal");
        modifier.apply(tool);
        pending = ConsumableUses.beginAttack(tool);
        ConsumableUses.tryConsume(tool, modifier.identifier, 3);
        pending.suspend().close();
        check(!ConsumableUses.isPresent(tool, modifier.identifier), "cancelled split hit closes cleanly");
        ConsumableUses.prepareSnapshot(tool);
        player.setHeldItem(EnumHand.MAIN_HAND, tool);
        ItemStack snapshot = tool.copy();
        check(ConsumableUses.resolveLiveTool(snapshot, player) == tool, "spell snapshot resolves actual carried item");
        ConsumableUses.tryConsume(ConsumableUses.resolveLiveTool(snapshot, player), material.identifier, 1);
        check(ConsumableUses.remaining(tool, material.identifier) == 2, "spell consumption updates original tool");
        player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
        check(ConsumableUses.remaining(ConsumableUses.resolveLiveTool(snapshot, player), material.identifier) == 0,
            "missing original cannot give snapshots free uses");
    }

    private void testScript(FakePlayer player, WorldServer world) throws Exception {
        String id = "tt2_test_script";
        IModifier mod = TinkerRegistry.getModifier(id);
        check(mod != null && ConsumableUses.definition(id) != null, "ContentTweaker builder expansion registered modifier");
        check(ConsumableUses.definition("tt2_test_script_material").kind == ConsumableUses.Kind.MATERIAL,
            "ContentTweaker two-argument expansion defaults to material");
        NonNullList<ItemStack> ingredients = NonNullList.from(ItemStack.EMPTY, new ItemStack(Items.RECORD_13));
        ItemStack stack = ToolBuilder.tryModifyTool(ingredients, tool(), true);
        check(!stack.isEmpty() && ingredients.get(0).isEmpty(), "script modifier applies through real station recipe");
        EntityZombie target = new EntityZombie(world);
        ITrait trait = TinkerRegistry.getTrait(id);
        for (int i = 0; i < 2; i++) {
            check(trait.damage(stack, player, target, 3, 3, false) == 7, "script damage callback is active " + i);
            trait.afterHit(stack, player, target, 7, false, true);
        }
        check(!ConsumableUses.isPresent(stack, id), "script-selected afterHit consumption removes modifier");
        check(trait.damage(stack, player, target, 3, 3, false) == 3, "exhausted script callback is gated");
        check(TagUtil.getBaseModifiersUsed(stack.getTagCompound()) == 0, "script modifier refunds slot");
        String materialId = "tt2_test_script_material";
        ItemStack scriptTool = tool("iron");
        ITrait materialTrait = TinkerRegistry.getTrait(materialId);
        ToolBuilder.addTrait(scriptTool.getTagCompound(), materialTrait, 0x66CC66);
        materialTrait.afterBlockBreak(scriptTool, world, net.minecraft.init.Blocks.STONE.getDefaultState(), BlockPos.ORIGIN, player, false);
        check(ConsumableUses.remaining(scriptTool, materialId) == 5, "script controls unsuccessful custom trigger without spending");
        materialTrait.afterBlockBreak(scriptTool, world, net.minecraft.init.Blocks.STONE.getDefaultState(), BlockPos.ORIGIN, player, true);
        check(ConsumableUses.remaining(scriptTool, materialId) == 4, "script tryUse functional callback spends on original tool");
        check(ConsumableUses.definition("tt2_test_script_armor") != null, "ContentTweaker armor builder expansion registers");
    }

    private static final class WindowPropertyRecorder implements IContainerListener {
        int extractionBlocked = -1;
        @Override public void sendAllContents(Container container, NonNullList<ItemStack> items) { }
        @Override public void sendSlotContents(Container container, int slot, ItemStack stack) { }
        @Override public void sendWindowProperty(Container container, int id, int value) {
            if (id == 3) extractionBlocked = value;
        }
        @Override public void sendAllWindowProperties(Container container, IInventory inventory) { }
    }

    private static final class TestTrait extends ConsumableTrait {
        int damageCalls;
        int afterCalls;
        TestTrait() { super("tt2_test_material", 0x33AAEE, 3); }
        @Override public void applyEffect(NBTTagCompound root, NBTTagCompound modifierTag) {
            if (TinkerUtil.hasTrait(root, identifier)) return;
            super.applyEffect(root, modifierTag);
            ToolNBT stats = TagUtil.getToolStats(root);
            stats.attack += 2;
            TagUtil.setToolTag(root, stats.get());
        }
        @Override public float damage(ItemStack tool, EntityLivingBase attacker, EntityLivingBase target, float damage, float current, boolean critical) {
            damageCalls++;
            ConsumableUses.tryConsume(tool, identifier, 1);
            return current + 2;
        }
        @Override public void afterHit(ItemStack tool, EntityLivingBase attacker, EntityLivingBase target, float damage, boolean critical, boolean hit) { afterCalls++; }
    }

    private static final class TestArmorTrait extends AbstractArmorTrait {
        TestArmorTrait() {
            super("tt2_test_armor", 0xBBBBBB);
            ConsumableUses.register(identifier, 2, ConsumableUses.Kind.MATERIAL);
        }
        @Override public float onHurt(ItemStack armor, EntityPlayer player, DamageSource source, float original, float current, LivingHurtEvent event) {
            ConsumableUses.tryConsume(armor, identifier, 1);
            return current - 2;
        }
    }

    private static final class TestModifier extends ConsumableModifier {
        TestModifier(String id, int levels, boolean free) {
            super(id, 0xCC8833, levels, 1, 3);
            if (free) {
                aspects.clear();
                addAspects(new ModifierAspect.DataAspect(this, 0xCC8833));
            }
            addItem(Items.NETHER_STAR);
        }
    }
}
