package xy177.tt2.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import c4.conarm.lib.ArmoryRegistry;
import c4.conarm.common.ConstructsRegistry;
import slimeknights.tconstruct.library.TinkerRegistry;
import xy177.tt2.config.TT2Config;
import xy177.tt2.compat.CraftsmanStaffCompat;
import xy177.tt2.events.ConstructArmorExtraModifierEvents;
import xy177.tt2.events.ConstructArmorSetBonusEvents;
import xy177.tt2.events.DefenseDamageEvents;
import xy177.tt2.events.DoppelhanderEvents;
import xy177.tt2.events.HeavyShieldEvents;
import xy177.tt2.events.HiddenModifierEvents;
import xy177.tt2.events.ScoutArmorEvents;
import xy177.tt2.events.SpearEvents;
import xy177.tt2.events.MaracaEvents;
import xy177.tt2.events.ShieldEvents;
import xy177.tt2.init.TT2Items;
import xy177.tt2.init.TT2Blocks;
import xy177.tt2.init.TT2Recipes;
import xy177.tt2.init.TT2ModifierCrystalRecipes;
import xy177.tt2.modifiers.ModExperienceTransfer;
import xy177.tt2.modifiers.ModSpearLunge;
import xy177.tt2.modifiers.ModCraftsmanStaffTemplate;
import xy177.tt2.network.TT2Network;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CommonProxy {

    protected ScoutArmorEvents scoutArmorEvents;
    protected MaracaEvents maracaEvents;
    public static ModExperienceTransfer experienceTransfer;
    public static ModSpearLunge spearLunge;
    protected ModCraftsmanStaffTemplate[] craftsmanStaffTemplates;

    public void preInit(FMLPreInitializationEvent event) {
        TT2Config.init(new File(event.getModConfigurationDirectory(), "tt2.cfg"));
        TT2Network.init();
        TT2Blocks.preInit();

        if (TT2Config.enableNunchaku) {
            TinkerRegistry.addTrait(xy177.tt2.tools.TinkerNunchaku.COMBO_TRAIT);
        }
        experienceTransfer = new ModExperienceTransfer();
        if (TT2Config.enableSpear) {
            spearLunge = new ModSpearLunge();
        }
    }

    public void init(FMLInitializationEvent event) {
        if (CraftsmanStaffCompat.isNatureAvailable() || CraftsmanStaffCompat.isInsightAvailable()
            || CraftsmanStaffCompat.isResearchAvailable()) {
            MinecraftForge.EVENT_BUS.register(CraftsmanStaffCompat.EVENTS);
        }
        List<ModCraftsmanStaffTemplate> staffTemplateList = new ArrayList<>();
        addCraftsmanStaffTemplate(staffTemplateList, ModCraftsmanStaffTemplate.Type.FARMING,
            TT2Items.FORGING_TEMPLATE_FARMING);
        addCraftsmanStaffTemplate(staffTemplateList, ModCraftsmanStaffTemplate.Type.COMBAT,
            TT2Items.FORGING_TEMPLATE_COMBAT);
        addCraftsmanStaffTemplate(staffTemplateList, ModCraftsmanStaffTemplate.Type.MINING,
            TT2Items.FORGING_TEMPLATE_MINING);
        addCraftsmanStaffTemplate(staffTemplateList, ModCraftsmanStaffTemplate.Type.EXCAVATION,
            TT2Items.FORGING_TEMPLATE_EXCAVATION);
        addCraftsmanStaffTemplate(staffTemplateList, ModCraftsmanStaffTemplate.Type.FELLING,
            TT2Items.FORGING_TEMPLATE_FELLING);
        addCraftsmanStaffTemplate(staffTemplateList, ModCraftsmanStaffTemplate.Type.SHEARING,
            TT2Items.FORGING_TEMPLATE_SHEARING);
        addCraftsmanStaffTemplate(staffTemplateList, ModCraftsmanStaffTemplate.Type.NATURE,
            TT2Items.FORGING_TEMPLATE_NATURE);
        addCraftsmanStaffTemplate(staffTemplateList, ModCraftsmanStaffTemplate.Type.INSIGHT,
            TT2Items.FORGING_TEMPLATE_INSIGHT);
        addCraftsmanStaffTemplate(staffTemplateList, ModCraftsmanStaffTemplate.Type.RESEARCH,
            TT2Items.FORGING_TEMPLATE_RESEARCH);
        if (CraftsmanStaffCompat.isLoaded("forestry")) {
            addCraftsmanStaffTemplate(staffTemplateList, ModCraftsmanStaffTemplate.Type.FORESTRY, null);
        }
        craftsmanStaffTemplates = staffTemplateList.toArray(new ModCraftsmanStaffTemplate[staffTemplateList.size()]);

        if (ConstructsRegistry.book != null && ConstructsRegistry.book.getMaxDamage() != 100) {
            ConstructsRegistry.book.setMaxDamage(100);
        }

        if (TT2Config.enableDefenseDamage) {
            MinecraftForge.EVENT_BUS.register(new DefenseDamageEvents());
        }
        MinecraftForge.EVENT_BUS.register(new ConstructArmorExtraModifierEvents());
        MinecraftForge.EVENT_BUS.register(new ConstructArmorSetBonusEvents());
        MinecraftForge.EVENT_BUS.register(new HiddenModifierEvents());

        if (TT2Config.enableSwiftShield && TT2Items.SWIFT_SHIELD != null) {
            TinkerRegistry.registerToolCrafting(TT2Items.SWIFT_SHIELD);
            MinecraftForge.EVENT_BUS.register(new ShieldEvents());
        }

        if (TT2Config.enableHeavyShield && TT2Items.HEAVY_SHIELD != null) {
            TinkerRegistry.registerToolForgeCrafting(TT2Items.HEAVY_SHIELD);
            MinecraftForge.EVENT_BUS.register(new HeavyShieldEvents());
        }

        if (TT2Config.enableNunchaku && TT2Items.NUNCHAKU != null) {
            TinkerRegistry.registerToolCrafting(TT2Items.NUNCHAKU);
        }

        if (TT2Config.enableDoppelhander && TT2Items.DOPPELHANDER != null) {
            TinkerRegistry.registerToolForgeCrafting(TT2Items.DOPPELHANDER);
            MinecraftForge.EVENT_BUS.register(new DoppelhanderEvents());
        }

        if (TT2Config.enableMaraca && TT2Items.MARACA != null) {
            TinkerRegistry.registerToolForgeCrafting(TT2Items.MARACA);
            maracaEvents = new MaracaEvents();
            MinecraftForge.EVENT_BUS.register(maracaEvents);
        }

        if (TT2Config.enableSpear && TT2Items.SPEAR != null) {
            TinkerRegistry.registerToolCrafting(TT2Items.SPEAR);
            MinecraftForge.EVENT_BUS.register(new SpearEvents());
        }

        if (TT2Items.CRAFTSMAN_STAFF != null) {
            TinkerRegistry.registerToolCrafting(TT2Items.CRAFTSMAN_STAFF);
            TinkerRegistry.registerToolForgeCrafting(TT2Items.CRAFTSMAN_STAFF);
        }

        if (TT2Config.enableScoutArmor) {
            if (TT2Items.SCOUT_HELMET != null) {
                ArmoryRegistry.registerArmorForging(TT2Items.SCOUT_HELMET);
            }
            if (TT2Items.SCOUT_CHESTPLATE != null) {
                ArmoryRegistry.registerArmorForging(TT2Items.SCOUT_CHESTPLATE);
            }
            if (TT2Items.SCOUT_LEGGINGS != null) {
                ArmoryRegistry.registerArmorForging(TT2Items.SCOUT_LEGGINGS);
            }
            if (TT2Items.SCOUT_BOOTS != null) {
                ArmoryRegistry.registerArmorForging(TT2Items.SCOUT_BOOTS);
            }
            scoutArmorEvents = new ScoutArmorEvents();
            MinecraftForge.EVENT_BUS.register(scoutArmorEvents);
        }
    }

    public void postInit(FMLPostInitializationEvent event) {
        TT2Recipes.registerCastingRecipes();
        TT2ModifierCrystalRecipes.register();
    }

    public ScoutArmorEvents getScoutArmorEvents() {
        return scoutArmorEvents;
    }

    public MaracaEvents getMaracaEvents() {
        return maracaEvents;
    }

    public void requestSpearStab(EntityPlayer player, boolean cooldownAlreadyReset) {
    }

    public void handleSpearAnimation(int entityId, EnumHand hand, int requestSequence,
                                     int animationSequence, int durationTicks) {
    }

    public boolean isRenderingSpearInHand() {
        return false;
    }

    public void showCraftsmanStaffNatureMode(int mode) {
    }

    private static void addCraftsmanStaffTemplate(List<ModCraftsmanStaffTemplate> templates,
                                                  ModCraftsmanStaffTemplate.Type type,
                                                  net.minecraft.item.Item template) {
        if (template != null || type == ModCraftsmanStaffTemplate.Type.FORESTRY) {
            templates.add(new ModCraftsmanStaffTemplate(type, template));
        }
    }
}


