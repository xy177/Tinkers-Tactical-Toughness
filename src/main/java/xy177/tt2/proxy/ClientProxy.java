package xy177.tt2.proxy;

import c4.conarm.lib.ArmoryRegistryClient;
import c4.conarm.lib.book.ArmoryBook;
import c4.conarm.lib.client.ArmorBuildGuiInfo;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.tconstruct.library.TinkerRegistryClient;
import slimeknights.tconstruct.library.book.TinkerBook;
import slimeknights.tconstruct.library.client.CustomTextureCreator;
import slimeknights.tconstruct.library.client.ToolBuildGuiInfo;
import xy177.tt2.TT2;
import xy177.tt2.client.NunchakuClientHandler;
import xy177.tt2.client.ScoutArmorClientEvents;
import xy177.tt2.client.MaracaClientEvents;
import xy177.tt2.client.book.TT2ArmorySectionTransformer;
import xy177.tt2.client.book.TT2ToolSectionTransformer;
import xy177.tt2.client.book.content.TT2ContentScoutArmor;
import xy177.tt2.client.texture.ScoutArmorLayerSprite;
import xy177.tt2.config.TT2Config;
import xy177.tt2.events.HeavyShieldClientEvents;
import xy177.tt2.init.TT2Items;
import xy177.tt2.item.ItemModifierCrystal;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        BookLoader.registerPageType(TT2ContentScoutArmor.ID, TT2ContentScoutArmor.class);
        ArmoryBook.INSTANCE.addTransformer(new TT2ArmorySectionTransformer());

        if (TT2Config.enableScoutArmor) {
            MinecraftForge.EVENT_BUS.register(this);
            registerScoutArmorTextures();
        }
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        if (TT2Config.enableHeavyShield) {
            MinecraftForge.EVENT_BUS.register(new HeavyShieldClientEvents());
        }

        if (TT2Config.enableNunchaku) {
            MinecraftForge.EVENT_BUS.register(new NunchakuClientHandler());
        }

        if (TT2Items.MARACA != null) {
            MinecraftForge.EVENT_BUS.register(new MaracaClientEvents());
        }

        if (TT2Config.enableScoutArmor) {
            MinecraftForge.EVENT_BUS.register(new ScoutArmorClientEvents());
            if (TT2Items.SCOUT_HELMET != null) {
                ArmoryRegistryClient.addArmorBuilding(ArmorBuildGuiInfo.default3Part(TT2Items.SCOUT_HELMET));
            }
            if (TT2Items.SCOUT_CHESTPLATE != null) {
                ArmoryRegistryClient.addArmorBuilding(ArmorBuildGuiInfo.default3Part(TT2Items.SCOUT_CHESTPLATE));
            }
            if (TT2Items.SCOUT_LEGGINGS != null) {
                ArmoryRegistryClient.addArmorBuilding(ArmorBuildGuiInfo.default3Part(TT2Items.SCOUT_LEGGINGS));
            }
            if (TT2Items.SCOUT_BOOTS != null) {
                ArmoryRegistryClient.addArmorBuilding(ArmorBuildGuiInfo.default3Part(TT2Items.SCOUT_BOOTS));
            }
        }
        if (TT2Items.MODIFIER_CRYSTAL != null) {
            Minecraft.getMinecraft().getItemColors().registerItemColorHandler(
                (stack, tintIndex) -> tintIndex == 0 ? ItemModifierCrystal.getColor(stack) : 0xFFFFFF,
                TT2Items.MODIFIER_CRYSTAL
            );
        }
        TinkerBook.INSTANCE.addTransformer(new TT2ToolSectionTransformer());
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

        if (TT2Config.enableSwiftShield && TT2Items.SWIFT_SHIELD != null) {
            ToolBuildGuiInfo info = new ToolBuildGuiInfo(TT2Items.SWIFT_SHIELD);
            info.addSlotPosition(24, 30);
            info.addSlotPosition(48, 52);
            TinkerRegistryClient.addToolBuilding(info);
        }

        if (TT2Config.enableHeavyShield && TT2Items.HEAVY_SHIELD != null) {
            TinkerRegistryClient.addToolBuilding(ToolBuildGuiInfo.default3Part(TT2Items.HEAVY_SHIELD));
        }

        if (TT2Config.enableNunchaku && TT2Items.NUNCHAKU != null) {
            ToolBuildGuiInfo info = new ToolBuildGuiInfo(TT2Items.NUNCHAKU);
            info.addSlotPosition(16, 44);
            info.addSlotPosition(48, 56);
            info.addSlotPosition(48, 32);
            TinkerRegistryClient.addToolBuilding(info);
        }

        if (TT2Config.enableDoppelhander && TT2Items.DOPPELHANDER != null) {
            ToolBuildGuiInfo info = new ToolBuildGuiInfo(TT2Items.DOPPELHANDER);
            info.addSlotPosition(56, 16);
            info.addSlotPosition(24, 48);
            info.addSlotPosition(40, 32);
            info.addSlotPosition(8, 64);
            TinkerRegistryClient.addToolBuilding(info);
        }

        if (TT2Items.MARACA != null) {
            ToolBuildGuiInfo info = new ToolBuildGuiInfo(TT2Items.MARACA);
            info.addSlotPosition(53, 22);
            info.addSlotPosition(33, 42);
            info.addSlotPosition(13, 62);
            TinkerRegistryClient.addToolBuilding(info);
        }
    }

    private void registerScoutArmorTextures() {
        for (String layer : SCOUT_ARMOR_TEXTURE_LAYERS) {
            CustomTextureCreator.registerTexture(new net.minecraft.util.ResourceLocation(TT2.MOD_ID, layer));
        }
    }

    private void registerScoutArmorLayerSprites(TextureStitchEvent.Pre event) {
        for (String layer : SCOUT_ARMOR_TEXTURE_LAYERS) {
            net.minecraft.util.ResourceLocation location = new net.minecraft.util.ResourceLocation(TT2.MOD_ID, layer);
            event.getMap().setTextureEntry(new ScoutArmorLayerSprite(location));
        }
    }

    @SubscribeEvent
    public void onTextureStitchPre(TextureStitchEvent.Pre event) {
        if (TT2Config.enableScoutArmor) {
            registerScoutArmorTextures();
            registerScoutArmorLayerSprites(event);
        }
    }

    private static final String[] SCOUT_ARMOR_TEXTURE_LAYERS = {
        "models/armor/scout/armor_full_core",
        "models/armor/scout/armor_full_core_broken",
        "models/armor/scout/armor_full_plate1",
        "models/armor/scout/armor_full_plate2",
        "models/armor/scout/armor_full_leg_core",
        "models/armor/scout/armor_full_leg_plate1",
        "models/armor/scout/armor_full_leg_plate2"
    };
}

