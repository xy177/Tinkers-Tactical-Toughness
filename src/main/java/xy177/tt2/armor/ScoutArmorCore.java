package xy177.tt2.armor;

import c4.conarm.common.ConstructsRegistry;
import c4.conarm.common.armor.utils.ArmorHelper;
import c4.conarm.lib.armor.ArmorCore;
import c4.conarm.lib.armor.ArmorNBT;
import c4.conarm.lib.materials.ArmorMaterialType;
import c4.conarm.lib.materials.CoreMaterialStats;
import c4.conarm.lib.materials.PlatesMaterialStats;
import com.google.common.collect.Multimap;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.utils.TooltipBuilder;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;
import c4.conarm.lib.tinkering.ArmorTooltipBuilder;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import slimeknights.tconstruct.library.Util;
import xy177.tt2.TT2;
import xy177.tt2.client.model.ScoutArmorModel;
import xy177.tt2.config.TT2Config;
import xy177.tt2.events.ScoutArmorEvents;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class ScoutArmorCore extends ArmorCore {

    private final Map<EntityEquipmentSlot, ModelBiped> scoutModels = new EnumMap<>(EntityEquipmentSlot.class);

    private static final UUID[] SPEED_MODIFIERS = new UUID[]{
        UUID.fromString("7c6ae507-1ea7-4308-a2d3-3a7c60ba4fd1"),
        UUID.fromString("8d4f28f3-76d5-44a1-9b14-e65838f0ad0f"),
        UUID.fromString("164730c8-847b-4b1d-a91e-59caa0fe5011"),
        UUID.fromString("2df2f93c-95c0-46a3-a642-3bba3f0aeaa5")
    };

    protected ScoutArmorCore(EntityEquipmentSlot slotIn, String appearanceName, PartMaterialType core) {
        super(slotIn, appearanceName, core);
        this.requiredComponents[2] = ArmorMaterialType.plating(ConstructsRegistry.armorPlate);
        this.setTranslationKey("tt2." + getRegistrySuffix(slotIn));
    }

    private static String getRegistrySuffix(EntityEquipmentSlot slot) {
        switch (slot) {
            case HEAD:
                return "scout_helmet";
            case CHEST:
                return "scout_chestplate";
            case LEGS:
                return "scout_leggings";
            default:
                return "scout_boots";
        }
    }

    @Override
    protected ArmorNBT buildTagData(List<Material> materials) {
        ArmorNBT data = new ArmorNBT();
        int slot = armorType.getIndex();

        CoreMaterialStats core = materials.get(0).getStatsOrUnknown(ArmorMaterialType.CORE);
        PlatesMaterialStats plate1 = materials.get(1).getStatsOrUnknown(ArmorMaterialType.PLATES);
        PlatesMaterialStats plate2 = materials.get(2).getStatsOrUnknown(ArmorMaterialType.PLATES);

        data.core(slot, core);
        data.defense *= 0.5f;

        float avgModifier = Math.max(0.1f, (plate1.modifier + plate2.modifier) / 2f);
        int avgDurability = Math.round(((plate1.durability + plate2.durability) / 2f) * ArmorHelper.durabilityMultipliers[slot]);
        data.durability = Math.max(1, Math.round(data.durability * avgModifier) + avgDurability);
        data.toughness = 0f;
        data.modifiers = DEFAULT_MODIFIERS;
        return data;
    }

    @Override
    public List<String> getInformation(ItemStack stack, boolean detailed) {
        TooltipBuilder info = new TooltipBuilder(stack);

        info.addDurability(!detailed);
        ArmorTooltipBuilder.addDefense(info, stack);
        ArmorTooltipBuilder.addToughness(info, stack);
        info.add(formatDodgeChance(stack));
        info.add(formatRangedDamageBonus(stack));
        info.add(formatFallDamageReduction());
        info.add(formatEnvironmentalDamageReduction());
        String setBonus = getCurrentSetBonusTooltip();
        if (setBonus != null) {
            info.add(setBonus);
        }

        if (ToolHelper.getFreeModifiers(stack) > 0) {
            info.addFreeModifiers();
        }

        if (detailed) {
            info.addModifierInfo();
        }

        return info.getTooltip();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getArmorModelTexture(ItemStack stack, String type) {
        if (ToolHelper.isBroken(stack) && type.equals(ArmorMaterialType.CORE)) {
            return new ResourceLocation(TT2.MOD_ID, "models/armor/scout/armor_core_broken").toString();
        }
        String leg = armorType == EntityEquipmentSlot.LEGS ? "leg_" : "";
        if (type.equals(ArmorMaterialType.CORE)) {
            return new ResourceLocation(TT2.MOD_ID, "models/armor/scout/armor_" + leg + "core").toString();
        }
        if (type.equals(ArmorMaterialType.PLATES)) {
            return new ResourceLocation(TT2.MOD_ID, "models/armor/scout/armor_" + leg + "plate1").toString();
        }
        if (type.equals(ArmorMaterialType.TRIM)) {
            return new ResourceLocation(TT2.MOD_ID, "models/armor/scout/armor_" + leg + "plate2").toString();
        }
        return super.getArmorModelTexture(stack, type);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack,
                                    EntityEquipmentSlot armorSlot, ModelBiped defaultModel) {
        return scoutModels.computeIfAbsent(armorSlot, ScoutArmorModel::new);
    }

    private String formatDodgeChance(ItemStack stack) {
        double percent = getDodgeChanceContribution(
            stack,
            xy177.tt2.config.TT2Config.scoutDodgeChanceCoefficient,
            xy177.tt2.config.TT2Config.scoutDodgeChanceCap
        ) * 100.0;
        return Util.translate("tooltip.tt2.scout.dodge") + ": "
            + TextFormatting.AQUA + Util.df.format(percent) + "%";
    }

    private String formatRangedDamageBonus(ItemStack stack) {
        double percent = getRangedDamageBonusPercent(
            stack,
            xy177.tt2.config.TT2Config.scoutRangedDamageCoefficient
        );
        return Util.translate("tooltip.tt2.scout.ranged_bonus") + ": "
            + TextFormatting.AQUA + Util.df.format(percent) + "%";
    }

    private String formatFallDamageReduction() {
        double percent = getFallDamageReductionContribution() * 100.0;
        return Util.translate("tooltip.tt2.scout.fall_reduction") + ": "
            + TextFormatting.AQUA + Util.df.format(percent) + "%";
    }

    private String formatEnvironmentalDamageReduction() {
        double percent = getEnvironmentalDamageReductionContribution() * 100.0;
        return Util.translate("tooltip.tt2.scout.environment_reduction") + ": "
            + TextFormatting.AQUA + Util.df.format(percent) + "%";
    }

    private String getCurrentSetBonusTooltip() {
        if (!FMLCommonHandler.instance().getSide().isClient()) {
            return null;
        }
        return buildCurrentSetBonusTooltip();
    }

    @SideOnly(Side.CLIENT)
    private String buildCurrentSetBonusTooltip() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) {
            return null;
        }

        int pieces = ScoutArmorEvents.getScoutPieceCount(player);
        if (pieces <= 0) {
            return null;
        }

        int displayPieces = Math.min(pieces, 4);
        return Util.translate("tooltip.tt2.scout.set_bonus_" + displayPieces);
    }

    @Nonnull
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull EntityEquipmentSlot slot, ItemStack armor) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, armor);
        if (!ToolHelper.isBroken(armor) && slot == this.armorType) {
            multimap.put(
                SharedMonsterAttributes.MOVEMENT_SPEED.getName(),
                new AttributeModifier(SPEED_MODIFIERS[slot.getIndex()], "Scout speed bonus", getSpeedBonus(), 2)
            );
        }
        return multimap;
    }

    public float getSpeedBonus() {
        return 0.30f * getPartRatio(armorType);
    }

    public float getAggroRangeContribution() {
        return 12f * getPartRatio(armorType);
    }

    public float getDodgeChanceContribution(ItemStack stack, double coefficient, double cap) {
        float armorValue = ArmorHelper.getArmor(stack, armorType.getIndex());
        float rawChance = armorValue * 0.04f * (float) coefficient;
        float partCap = (float) (cap * getPartRatio(armorType));
        return Math.min(rawChance, partCap);
    }

    public float getRangedDamageBonusPercent(ItemStack stack, double coefficient) {
        return getAveragePlateToughness(stack) * (float) coefficient;
    }

    public float getFallDamageReductionContribution() {
        return (float) TT2Config.scoutFallDamageReduction * getPartRatio(armorType);
    }

    public float getEnvironmentalDamageReductionContribution() {
        return (float) TT2Config.scoutEnvironmentalDamageReduction * getPartRatio(armorType);
    }

    public float getAveragePlateToughness(ItemStack stack) {
        List<Material> materials = TinkerUtil.getMaterialsFromTagList(TagUtil.getBaseMaterialsTagList(stack));
        if (materials.size() < 3) {
            return 0f;
        }
        PlatesMaterialStats plate1 = materials.get(1).getStats(ArmorMaterialType.PLATES);
        PlatesMaterialStats plate2 = materials.get(2).getStats(ArmorMaterialType.PLATES);
        if (plate1 == null || plate2 == null) {
            return 0f;
        }
        return (plate1.toughness + plate2.toughness) / 2f;
    }

    public static boolean isScoutArmor(ItemStack stack) {
        return stack.getItem() instanceof ScoutArmorCore;
    }

    public static float getPartRatio(EntityEquipmentSlot slot) {
        switch (slot) {
            case HEAD:
                return 0.15f;
            case CHEST:
                return 0.40f;
            case LEGS:
                return 0.30f;
            case FEET:
                return 0.15f;
            default:
                return 0f;
        }
    }
}
