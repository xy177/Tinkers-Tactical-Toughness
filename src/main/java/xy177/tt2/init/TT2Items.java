package xy177.tt2.init;

import net.minecraft.item.Item;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xy177.tt2.TT2;
import xy177.tt2.armor.ScoutBoots;
import xy177.tt2.armor.ScoutChestplate;
import xy177.tt2.armor.ScoutHelmet;
import xy177.tt2.armor.ScoutLeggings;
import xy177.tt2.config.TT2Config;
import xy177.tt2.compat.CraftsmanStaffCompat;
import xy177.tt2.item.ItemExperienceBottle;
import xy177.tt2.item.ItemForgingTemplate;
import xy177.tt2.item.ItemModifierCrystal;
import xy177.tt2.tools.Doppelhander;
import xy177.tt2.tools.CraftsmanStaff;
import xy177.tt2.tools.HeavyShield;
import xy177.tt2.tools.Maraca;
import xy177.tt2.tools.Spear;
import xy177.tt2.tools.SwiftShield;
import xy177.tt2.tools.TinkerNunchaku;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = TT2.MOD_ID)
public class TT2Items {

    public static SwiftShield SWIFT_SHIELD;
    public static HeavyShield HEAVY_SHIELD;
    public static TinkerNunchaku NUNCHAKU;
    public static Doppelhander DOPPELHANDER;
    public static Maraca MARACA;
    public static Spear SPEAR;
    public static CraftsmanStaff CRAFTSMAN_STAFF;
    public static ItemModifierCrystal MODIFIER_CRYSTAL;
    public static ItemExperienceBottle EXPERIENCE_BOTTLE;
    public static ScoutHelmet SCOUT_HELMET;
    public static ScoutChestplate SCOUT_CHESTPLATE;
    public static ScoutLeggings SCOUT_LEGGINGS;
    public static ScoutBoots SCOUT_BOOTS;
    public static ItemForgingTemplate FORGING_TEMPLATE_FARMING;
    public static ItemForgingTemplate FORGING_TEMPLATE_COMBAT;
    public static ItemForgingTemplate FORGING_TEMPLATE_MINING;
    public static ItemForgingTemplate FORGING_TEMPLATE_EXCAVATION;
    public static ItemForgingTemplate FORGING_TEMPLATE_FELLING;
    public static ItemForgingTemplate FORGING_TEMPLATE_INSIGHT;
    public static ItemForgingTemplate FORGING_TEMPLATE_NATURE;
    public static ItemForgingTemplate FORGING_TEMPLATE_SHEARING;
    public static ItemForgingTemplate FORGING_TEMPLATE_RESEARCH;
    public static ItemForgingTemplate[] FORGING_TEMPLATES = new ItemForgingTemplate[0];

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        if (TT2Config.enableSwiftShield) {
            SWIFT_SHIELD = new SwiftShield();
            SWIFT_SHIELD.setRegistryName(TT2.MOD_ID, "swift_shield");
            event.getRegistry().register(SWIFT_SHIELD);
        }

        if (TT2Config.enableHeavyShield) {
            HEAVY_SHIELD = new HeavyShield();
            HEAVY_SHIELD.setRegistryName(TT2.MOD_ID, "heavy_shield");
            event.getRegistry().register(HEAVY_SHIELD);
        }

        if (TT2Config.enableNunchaku) {
            NUNCHAKU = new TinkerNunchaku();
            NUNCHAKU.setRegistryName(TT2.MOD_ID, "nunchaku");
            event.getRegistry().register(NUNCHAKU);
        }

        if (TT2Config.enableDoppelhander) {
            DOPPELHANDER = new Doppelhander();
            DOPPELHANDER.setRegistryName(TT2.MOD_ID, "doppelhander");
            event.getRegistry().register(DOPPELHANDER);
        }

        if (TT2Config.enableMaraca) {
            MARACA = new Maraca();
            MARACA.setRegistryName(TT2.MOD_ID, "maraca");
            event.getRegistry().register(MARACA);
        }

        if (TT2Config.enableSpear) {
            SPEAR = new Spear();
            SPEAR.setRegistryName(TT2.MOD_ID, "spear");
            event.getRegistry().register(SPEAR);
        }

        CRAFTSMAN_STAFF = new CraftsmanStaff();
        CRAFTSMAN_STAFF.setRegistryName(TT2.MOD_ID, "craftsman_staff");
        event.getRegistry().register(CRAFTSMAN_STAFF);

        MODIFIER_CRYSTAL = new ItemModifierCrystal();
        MODIFIER_CRYSTAL.setRegistryName(TT2.MOD_ID, "modifier_crystal");
        event.getRegistry().register(MODIFIER_CRYSTAL);

        EXPERIENCE_BOTTLE = new ItemExperienceBottle();
        EXPERIENCE_BOTTLE.setRegistryName(TT2.MOD_ID, "experience_bottle");
        event.getRegistry().register(EXPERIENCE_BOTTLE);

        FORGING_TEMPLATE_FARMING = forgingTemplate("farming", TextFormatting.GOLD);
        FORGING_TEMPLATE_COMBAT = forgingTemplate("combat", TextFormatting.GRAY);
        FORGING_TEMPLATE_MINING = forgingTemplate("mining", TextFormatting.YELLOW);
        FORGING_TEMPLATE_EXCAVATION = forgingTemplate("excavation", TextFormatting.BLUE);
        FORGING_TEMPLATE_FELLING = forgingTemplate("felling", TextFormatting.DARK_AQUA);
        FORGING_TEMPLATE_INSIGHT = CraftsmanStaffCompat.isInsightAvailable()
            ? forgingTemplate("insight", TextFormatting.DARK_PURPLE) : null;
        FORGING_TEMPLATE_NATURE = CraftsmanStaffCompat.isNatureAvailable()
            ? forgingTemplate("nature", TextFormatting.GREEN) : null;
        FORGING_TEMPLATE_SHEARING = forgingTemplate("shearing", TextFormatting.AQUA);
        FORGING_TEMPLATE_RESEARCH = CraftsmanStaffCompat.isResearchAvailable()
            ? forgingTemplate("research", TextFormatting.DARK_RED, true) : null;

        List<ItemForgingTemplate> templates = new ArrayList<>();
        templates.add(FORGING_TEMPLATE_FARMING);
        templates.add(FORGING_TEMPLATE_COMBAT);
        templates.add(FORGING_TEMPLATE_MINING);
        templates.add(FORGING_TEMPLATE_EXCAVATION);
        templates.add(FORGING_TEMPLATE_FELLING);
        templates.add(FORGING_TEMPLATE_SHEARING);
        if (FORGING_TEMPLATE_INSIGHT != null) templates.add(FORGING_TEMPLATE_INSIGHT);
        if (FORGING_TEMPLATE_NATURE != null) templates.add(FORGING_TEMPLATE_NATURE);
        if (FORGING_TEMPLATE_RESEARCH != null) templates.add(FORGING_TEMPLATE_RESEARCH);
        FORGING_TEMPLATES = templates.toArray(new ItemForgingTemplate[templates.size()]);
        event.getRegistry().registerAll(FORGING_TEMPLATES);

        if (TT2Config.enableScoutArmor) {
            SCOUT_HELMET = new ScoutHelmet();
            SCOUT_HELMET.setRegistryName(TT2.MOD_ID, "scout_helmet");
            event.getRegistry().register(SCOUT_HELMET);

            SCOUT_CHESTPLATE = new ScoutChestplate();
            SCOUT_CHESTPLATE.setRegistryName(TT2.MOD_ID, "scout_chestplate");
            event.getRegistry().register(SCOUT_CHESTPLATE);

            SCOUT_LEGGINGS = new ScoutLeggings();
            SCOUT_LEGGINGS.setRegistryName(TT2.MOD_ID, "scout_leggings");
            event.getRegistry().register(SCOUT_LEGGINGS);

            SCOUT_BOOTS = new ScoutBoots();
            SCOUT_BOOTS.setRegistryName(TT2.MOD_ID, "scout_boots");
            event.getRegistry().register(SCOUT_BOOTS);
        }
    }

    private static ItemForgingTemplate forgingTemplate(String variant, TextFormatting tooltipColor) {
        ItemForgingTemplate template = new ItemForgingTemplate(variant, tooltipColor);
        template.setRegistryName(TT2.MOD_ID, "forging_template_" + variant);
        return template;
    }

    private static ItemForgingTemplate forgingTemplate(String variant, TextFormatting tooltipColor,
                                                       boolean researchTemplate) {
        ItemForgingTemplate template = new ItemForgingTemplate(variant, tooltipColor, researchTemplate);
        template.setRegistryName(TT2.MOD_ID, "forging_template_" + variant);
        return template;
    }
}

