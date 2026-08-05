package xy177.tt2.client;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import c4.conarm.client.utils.ArmorModelUtils;
import slimeknights.tconstruct.common.ModelRegisterUtil;
import xy177.tt2.TT2;
import xy177.tt2.config.TT2Config;
import xy177.tt2.init.TT2Items;
import xy177.tt2.init.TT2Blocks;
import xy177.tt2.item.ItemForgingTemplate;

@Mod.EventBusSubscriber(modid = TT2.MOD_ID, value = Side.CLIENT)
public class ClientRegistration {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        if (TT2Config.enableSwiftShield && TT2Items.SWIFT_SHIELD != null) {
            ModelRegisterUtil.registerToolModel(TT2Items.SWIFT_SHIELD);
        }
        if (TT2Config.enableHeavyShield && TT2Items.HEAVY_SHIELD != null) {
            ModelRegisterUtil.registerToolModel(TT2Items.HEAVY_SHIELD);
        }
        if (TT2Config.enableNunchaku && TT2Items.NUNCHAKU != null) {
            ModelRegisterUtil.registerToolModel(TT2Items.NUNCHAKU);
        }
        if (TT2Config.enableDoppelhander && TT2Items.DOPPELHANDER != null) {
            ModelRegisterUtil.registerToolModel(TT2Items.DOPPELHANDER);
        }
        if (TT2Config.enableMaraca && TT2Items.MARACA != null) {
            ModelRegisterUtil.registerToolModel(TT2Items.MARACA);
        }
        if (TT2Config.enableSpear && TT2Items.SPEAR != null) {
            ModelRegisterUtil.registerToolModel(TT2Items.SPEAR);
        }
        if (TT2Items.CRAFTSMAN_STAFF != null) {
            ModelRegisterUtil.registerToolModel(TT2Items.CRAFTSMAN_STAFF);
        }
        if (TT2Items.MODIFIER_CRYSTAL != null) {
            ModelLoader.setCustomModelResourceLocation(TT2Items.MODIFIER_CRYSTAL, 0,
                new ModelResourceLocation(TT2.MOD_ID + ":modifier_crystal", "inventory"));
        }
        if (TT2Items.EXPERIENCE_BOTTLE != null) {
            ModelLoader.setCustomModelResourceLocation(TT2Items.EXPERIENCE_BOTTLE, 0,
                new ModelResourceLocation(TT2.MOD_ID + ":experience_bottle", "inventory"));
        }
        for (ItemForgingTemplate template : TT2Items.FORGING_TEMPLATES) {
            if (template != null && template.getRegistryName() != null) {
                ModelLoader.setCustomModelResourceLocation(template, 0,
                    new ModelResourceLocation(template.getRegistryName(), "inventory"));
            }
        }
        if (TT2Blocks.MODIFIER_WORKTABLE != null) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(TT2Blocks.MODIFIER_WORKTABLE), 0,
                new ModelResourceLocation(TT2.MOD_ID + ":modifier_worktable", "inventory"));
        }
        if (TT2Config.enableScoutArmor) {
            if (TT2Items.SCOUT_HELMET != null) {
                ArmorModelUtils.registerArmorModel(TT2Items.SCOUT_HELMET);
            }
            if (TT2Items.SCOUT_CHESTPLATE != null) {
                ArmorModelUtils.registerArmorModel(TT2Items.SCOUT_CHESTPLATE);
            }
            if (TT2Items.SCOUT_LEGGINGS != null) {
                ArmorModelUtils.registerArmorModel(TT2Items.SCOUT_LEGGINGS);
            }
            if (TT2Items.SCOUT_BOOTS != null) {
                ArmorModelUtils.registerArmorModel(TT2Items.SCOUT_BOOTS);
            }
        }
    }
}

