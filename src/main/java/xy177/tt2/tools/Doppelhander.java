package xy177.tt2.tools;

import c4.conarm.common.ConstructsRegistry;
import c4.conarm.common.armor.utils.ArmorHelper;
import c4.conarm.lib.materials.ArmorMaterialType;
import c4.conarm.lib.materials.CoreMaterialStats;
import c4.conarm.lib.materials.PlatesMaterialStats;
import c4.conarm.lib.traits.IArmorTrait;
import com.google.common.collect.Multimap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.materials.ExtraMaterialStats;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.materials.MaterialTypes;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.TinkerToolCore;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.ToolBuilder;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.library.utils.TooltipBuilder;
import slimeknights.tconstruct.tools.TinkerTools;
import xy177.tt2.config.TT2Config;
import xy177.tt2.potion.TT2Potions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Doppelhander extends TinkerToolCore {

    public static final float ATTACK_COEFF = 0.8f;
    public static final double BLOCK_HALF_ANGLE_DEG = 60.0;
    public static final double AOE_HALF_ANGLE_DEG = 60.0;
    public static final String TAG_BLOCK_BONUS = "doppelhander_block_bonus";

    private static final Set<UUID> AREA_ATTACKING = new HashSet<>();

    public Doppelhander() {
        super(
            PartMaterialType.head(TinkerTools.swordBlade),
            new PartMaterialType(TinkerTools.toughBinding, MaterialTypes.HANDLE),
            ArmorMaterialType.plating(ConstructsRegistry.armorPlate),
            PartMaterialType.handle(TinkerTools.toughToolRod)
        );
        this.setTranslationKey("tt2.doppelhander");
        addCategory(Category.WEAPON);

        this.addPropertyOverride(new ResourceLocation("blocking"), new IItemPropertyGetter() {
            @Override
            @SideOnly(Side.CLIENT)
            public float apply(@Nonnull ItemStack stack, @Nullable World worldIn,
                               @Nullable EntityLivingBase entityIn) {
                return entityIn != null
                    && entityIn.isHandActive()
                    && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
            }
        });
    }

    @Override
    public void addMaterialTraits(NBTTagCompound root, List<Material> materials) {
        int size = Math.min(materials.size(), requiredComponents.length);
        for (int i = 0; i < size; i++) {
            if (i == 2) {
                continue;
            }

            PartMaterialType required = requiredComponents[i];
            Material material = materials.get(i);
            for (ITrait trait : required.getApplicableTraitsForMaterial(material)) {
                ToolBuilder.addTrait(root, trait, material.materialTextColor);
            }
        }
    }

    @Override
    protected ToolNBT buildTagData(List<Material> materials) {
        ToolNBT data = new ToolNBT();

        HeadMaterialStats blade = materials.get(0).getStatsOrUnknown(MaterialTypes.HEAD);
        HandleMaterialStats binding = materials.get(1).getStatsOrUnknown(MaterialTypes.HANDLE);
        ExtraMaterialStats plate = materials.get(2).getStatsOrUnknown(MaterialTypes.EXTRA);
        HandleMaterialStats handle = materials.get(3).getStatsOrUnknown(MaterialTypes.HANDLE);

        data.head(blade);
        data.extra(plate);
        data.handle(handle);
        data.durability = Math.max(1, Math.round(data.durability * Math.max(0.1f, binding.modifier)));
        data.modifiers = DEFAULT_MODIFIERS;
        return data;
    }

    @Override
    public float damagePotential() {
        return ATTACK_COEFF;
    }

    @Override
    public double attackSpeed() {
        return 0.8d;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
        preventSlowDown(entity, 0.8f);
        super.onUpdate(stack, world, entity, slot, isSelected);
        updateArmorTraits(stack, world, entity, slot, isSelected);
    }

    private void updateArmorTraits(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
        if (!isSelected) {
            return;
        }

        for (IArmorTrait trait : getPlateArmorTraits(stack)) {
            trait.onUpdate(stack, world, entity, slot, true);
        }
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (hand != EnumHand.MAIN_HAND) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        if (!ToolHelper.isBroken(stack)) {
            player.setActiveHand(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }

    @Nonnull
    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BLOCK;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public boolean canDestroyBlockInCreative(World world, net.minecraft.util.math.BlockPos pos,
                                             ItemStack stack, EntityPlayer player) {
        return false;
    }

    @Override
    public int[] getRepairParts() {
        return new int[]{0};
    }

    @Nonnull
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);

        if (slot == EntityEquipmentSlot.MAINHAND && !ToolHelper.isBroken(stack)) {
            multimap.put(
                SharedMonsterAttributes.ARMOR.getName(),
                new AttributeModifier(UUID.fromString("1e1d7593-b0bd-4c97-84fc-b70e514a5f7c"),
                    "tt2_doppelhander_item_armor", getProvidedArmor(stack, false), 0)
            );
            multimap.put(
                SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(),
                new AttributeModifier(UUID.fromString("f84525bd-c7d4-49cb-97d2-56a6f0f1c8fa"),
                    "tt2_doppelhander_item_toughness", getProvidedToughness(stack, false), 0)
            );
        }

        return multimap;
    }

    @Override
    public void getTooltip(ItemStack stack, List<String> tooltips) {
        super.getTooltip(stack, tooltips);

        Material plateMaterial = getPlateMaterial(stack);
        if (plateMaterial != null) {
            for (IArmorTrait trait : getPlateArmorTraits(stack)) {
                tooltips.add(plateMaterial.getTextColor() + trait.getLocalizedName());
            }
        }
    }

    @Override
    public List<String> getInformation(ItemStack stack, boolean detailed) {
        TooltipBuilder info = new TooltipBuilder(stack);

        info.addDurability(!detailed);
        info.addAttack();
        info.add(formatProvidedArmor(stack, false));
        info.add(formatProvidedToughness(stack, false));

        if (ToolHelper.getFreeModifiers(stack) > 0) {
            info.addFreeModifiers();
        }

        if (detailed) {
            info.addModifierInfo();
        }

        return info.getTooltip();
    }

    private String formatProvidedArmor(ItemStack stack, boolean defensiveStance) {
        return Util.translate("tooltip.tt2.doppelhander.armor") + ": "
            + TextFormatting.AQUA + Util.df.format(getProvidedArmor(stack, defensiveStance));
    }

    private String formatProvidedToughness(ItemStack stack, boolean defensiveStance) {
        return Util.translate("tooltip.tt2.doppelhander.toughness") + ": "
            + TextFormatting.AQUA + Util.df.format(getProvidedToughness(stack, defensiveStance));
    }

    @Override
    public boolean dealDamage(ItemStack stack, EntityLivingBase attacker, Entity target, float damage) {
        float adjustedDamage = damage;
        if (attacker instanceof EntityPlayer
                && attacker.isPotionActive(TT2Potions.DEFENSIVE_STANCE)) {
            adjustedDamage *= 1.0f + getDefensiveDamageBonusPercent(stack, true) / 100.0f;
        }

        boolean hit = super.dealDamage(stack, attacker, target, adjustedDamage);

        if (hit
                && attacker instanceof EntityPlayer
                && target instanceof EntityLivingBase
                && attacker.isPotionActive(TT2Potions.DEFENSIVE_STANCE)) {
            UUID attackerId = attacker.getUniqueID();
            if (!AREA_ATTACKING.contains(attackerId)) {
                AREA_ATTACKING.add(attackerId);
                try {
                    performAreaAttack(stack, (EntityPlayer) attacker, (EntityLivingBase) target, adjustedDamage);
                } finally {
                    AREA_ATTACKING.remove(attackerId);
                }
            }
        }

        return hit;
    }

    private void performAreaAttack(ItemStack stack, EntityPlayer attacker, EntityLivingBase primaryTarget, float damage) {
        double radius = TT2Config.doppelhanderAoeRadius;
        List<EntityLivingBase> nearbyTargets = attacker.world.getEntitiesWithinAABB(
            EntityLivingBase.class,
            attacker.getEntityBoundingBox().grow(radius, 1.5D, radius)
        );

        for (EntityLivingBase target : nearbyTargets) {
            if (target == attacker || target == primaryTarget) {
                continue;
            }
            if (!target.canBeAttackedWithItem() || attacker.isOnSameTeam(target)) {
                continue;
            }
            if (attacker.getDistanceSq(target) > radius * radius) {
                continue;
            }
            if (!isWithinArc(attacker, target, AOE_HALF_ANGLE_DEG)) {
                continue;
            }

            attackSecondaryTarget(stack, attacker, target, damage);
        }
    }

    private void attackSecondaryTarget(ItemStack stack, EntityPlayer attacker, EntityLivingBase target, float damage) {
        float finalDamage = damage;
        List<ITrait> traits = getToolTraits(stack);

        for (ITrait trait : traits) {
            finalDamage = trait.damage(stack, attacker, target, damage, finalDamage, false);
        }

        boolean wasHit = target.attackEntityFrom(net.minecraft.util.DamageSource.causePlayerDamage(attacker), finalDamage);

        for (ITrait trait : traits) {
            trait.onHit(stack, attacker, target, finalDamage, false);
            trait.afterHit(stack, attacker, target, finalDamage, false, wasHit);
        }

        if (wasHit) {
            reduceDurabilityOnHit(stack, attacker, finalDamage);
        }
    }

    private List<ITrait> getToolTraits(ItemStack stack) {
        List<ITrait> traits = new ArrayList<>();
        NBTTagList traitTags = TagUtil.getTraitsTagList(stack);
        for (int i = 0; i < traitTags.tagCount(); i++) {
            ITrait trait = TinkerRegistry.getTrait(traitTags.getStringTagAt(i));
            if (trait != null) {
                traits.add(trait);
            }
        }
        return traits;
    }

    public static boolean isWithinArc(EntityLivingBase attacker, Entity target, double halfAngleDeg) {
        double dx = target.posX - attacker.posX;
        double dz = target.posZ - attacker.posZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 0.01D) return true;

        dx /= dist;
        dz /= dist;

        float yawRad = attacker.rotationYaw * ((float) Math.PI / 180.0f);
        double facingX = -Math.sin(yawRad);
        double facingZ = Math.cos(yawRad);
        return (facingX * dx + facingZ * dz) >= Math.cos(Math.toRadians(halfAngleDeg));
    }

    public static float getBlockBonus(ItemStack stack) {
        return TagUtil.getExtraTag(stack).getFloat(TAG_BLOCK_BONUS);
    }

    public static void setBlockBonus(ItemStack stack, float value) {
        NBTTagCompound extra = TagUtil.getExtraTag(stack);
        extra.setFloat(TAG_BLOCK_BONUS, Math.max(0.0f, Math.min(1.0f, value)));
        TagUtil.setExtraTag(stack, extra);
    }

    public static float getBlockGain(ItemStack stack) {
        HandleMaterialStats handleStats = getHandleStats(stack);
        if (handleStats == null) {
            return 0.0f;
        }
        return (float) (handleStats.modifier * TT2Config.doppelhanderBlockGainPerHandleModifier / 100.0);
    }

    @Nullable
    public static Material getPlateMaterial(ItemStack stack) {
        NBTTagList matList = TagUtil.getBaseMaterialsTagList(stack);
        if (matList == null || matList.tagCount() < 3) {
            return null;
        }

        Material material = TinkerRegistry.getMaterial(matList.getStringTagAt(2));
        return material == Material.UNKNOWN ? null : material;
    }

    @Nullable
    private static HandleMaterialStats getHandleStats(ItemStack stack) {
        NBTTagList matList = TagUtil.getBaseMaterialsTagList(stack);
        if (matList == null || matList.tagCount() < 4) {
            return null;
        }

        Material material = TinkerRegistry.getMaterial(matList.getStringTagAt(3));
        if (material == null || material == Material.UNKNOWN) {
            return null;
        }
        return material.getStats(MaterialTypes.HANDLE);
    }

    public static float getProvidedArmor(ItemStack stack, boolean defensiveStance) {
        Material material = getPlateMaterial(stack);
        if (material == null) {
            return 0.0f;
        }

        CoreMaterialStats coreStats = material.getStats(ArmorMaterialType.CORE);
        if (coreStats == null) {
            return 0.0f;
        }

        int slot = defensiveStance ? EntityEquipmentSlot.CHEST.getIndex() : EntityEquipmentSlot.HEAD.getIndex();
        float armor = coreStats.defense * ArmorHelper.defenseMultipliers[slot];
        return ((int) (armor * 100.0f + 0.5f)) / 100.0f;
    }

    public static float getProvidedToughness(ItemStack stack, boolean defensiveStance) {
        Material material = getPlateMaterial(stack);
        if (material == null) {
            return 0.0f;
        }

        PlatesMaterialStats plateStats = material.getStats(ArmorMaterialType.PLATES);
        if (plateStats == null) {
            return 0.0f;
        }

        return plateStats.toughness;
    }

    public static float getDefensiveDamageBonusPercent(ItemStack stack, boolean defensiveStance) {
        float armor = getProvidedArmor(stack, defensiveStance);
        float scaled = (float) (armor * TT2Config.doppelhanderDamageBonusArmorCoefficient);
        return Math.max((float) TT2Config.doppelhanderDamageBonusMinPercent, scaled);
    }

    public static List<IArmorTrait> getPlateArmorTraits(ItemStack stack) {
        List<IArmorTrait> traits = new ArrayList<>();
        Material material = getPlateMaterial(stack);
        if (material == null) {
            return traits;
        }

        for (ITrait trait : ArmorMaterialType.plating(ConstructsRegistry.armorPlate).getApplicableTraitsForMaterial(material)) {
            if (trait instanceof IArmorTrait) {
                traits.add((IArmorTrait) trait);
            }
        }
        return traits;
    }
}
