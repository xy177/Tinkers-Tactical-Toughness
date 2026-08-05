package xy177.tt2.tools;

import com.google.common.collect.Multimap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import slimeknights.tconstruct.library.events.TinkerToolEvent;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.materials.MaterialTypes;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.SwordCore;
import slimeknights.tconstruct.library.tools.TinkerToolCore;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.library.utils.TooltipBuilder;
import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.tools.tools.Hatchet;
import slimeknights.tconstruct.tools.tools.Kama;
import slimeknights.tconstruct.tools.tools.Pickaxe;
import slimeknights.tconstruct.tools.tools.Shovel;
import xy177.tt2.modifiers.ModCraftsmanStaffTemplate;
import xy177.tt2.config.TT2Config;
import xy177.tt2.compat.CraftsmanStaffCompat;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CraftsmanStaff extends TinkerToolCore {

    private static final float BASE_ATTACK_BONUS = 1.5F;
    private static final double BASE_REACH_BONUS = 1.5D;
    private static final double COMBAT_REACH_BONUS = 0.5D;
    private static final UUID REACH_MODIFIER = UUID.fromString("c8c06e1d-685d-46e7-ad13-8e07e214a29c");
    private static final UUID MOVEMENT_MODIFIER = UUID.fromString("ac0d8393-a335-4d30-9253-e2042419ab82");

    public CraftsmanStaff() {
        super(
            PartMaterialType.head(TinkerTools.shard),
            PartMaterialType.handle(TinkerTools.toughToolRod),
            PartMaterialType.handle(TinkerTools.toolRod)
        );
        setTranslationKey("tt2.craftsman_staff");
        addCategory(Category.WEAPON);
    }

    @Override
    public float damagePotential() {
        return 0.2F;
    }

    @Override
    public double attackSpeed() {
        return 1.0D;
    }

    @Override
    protected ToolNBT buildTagData(List<Material> materials) {
        HeadMaterialStats head = materials.get(0).getStatsOrUnknown(MaterialTypes.HEAD);
        HandleMaterialStats toughHandle = materials.get(1).getStatsOrUnknown(MaterialTypes.HANDLE);
        HandleMaterialStats handle = materials.get(2).getStatsOrUnknown(MaterialTypes.HANDLE);

        ToolNBT data = new ToolNBT();
        data.head(head);
        data.handle(toughHandle, handle);
        data.attack += BASE_ATTACK_BONUS;
        data.modifiers = DEFAULT_MODIFIERS;
        return data;
    }

    @Override
    public int[] getRepairParts() {
        return new int[]{0};
    }

    @Override
    public List<String> getInformation(ItemStack stack, boolean detailed) {
        TooltipBuilder info = new TooltipBuilder(stack);
        info.addDurability(!detailed);
        if (hasHarvestTemplate(stack)) {
            info.addHarvestLevel();
            info.addMiningSpeed();
        }
        info.addAttack();
        if (ToolHelper.getFreeModifiers(stack) > 0) {
            info.addFreeModifiers();
        }
        if (detailed) {
            info.addModifierInfo();
        }
        return info.getTooltip();
    }

    @Nonnull
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull EntityEquipmentSlot slot,
                                                                      ItemStack stack) {
        Multimap<String, AttributeModifier> attributes = super.getAttributeModifiers(slot, stack);
        if (slot != EntityEquipmentSlot.MAINHAND || ToolHelper.isBroken(stack)) {
            return attributes;
        }

        double reach = BASE_REACH_BONUS;
        if (has(stack, ModCraftsmanStaffTemplate.Type.COMBAT)) {
            reach += COMBAT_REACH_BONUS;
        }
        attributes.put(EntityPlayer.REACH_DISTANCE.getName(), new AttributeModifier(
            REACH_MODIFIER, "tt2_craftsman_staff_reach", reach, 0));

        double movement = getAverageHandleModifier(stack) * TT2Config.craftsmanStaffMovementSpeedCoefficient;
        if (movement != 0.0D) {
            attributes.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(
                MOVEMENT_MODIFIER, "tt2_craftsman_staff_movement", movement, 2));
        }
        return attributes;
    }

    @Override
    public boolean canHarvestBlock(IBlockState state, ItemStack stack) {
        return !ToolHelper.isBroken(stack) && (isEffective(stack, state)
            || hasForestryTool(stack, state));
    }

    @Override
    public int getHarvestLevel(ItemStack stack, String toolClass, EntityPlayer player, IBlockState state) {
        if ("scoop".equals(toolClass) && !ToolHelper.isBroken(stack)
            && has(stack, ModCraftsmanStaffTemplate.Type.FORESTRY)) {
            Item scoop = CraftsmanStaffCompat.getForestryScoop();
            if (scoop != null) {
                return scoop.getHarvestLevel(stack, toolClass, player, state);
            }
        }
        if (!getToolClasses(stack).contains(toolClass)) {
            return -1;
        }
        return super.getHarvestLevel(stack, toolClass, player, state);
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        Set<String> classes = new HashSet<>(super.getToolClasses(stack));
        if (has(stack, ModCraftsmanStaffTemplate.Type.COMBAT)) {
            classes.add("sword");
        }
        if (has(stack, ModCraftsmanStaffTemplate.Type.MINING)) {
            classes.add("pickaxe");
        }
        if (has(stack, ModCraftsmanStaffTemplate.Type.EXCAVATION)) {
            classes.add("shovel");
        }
        if (has(stack, ModCraftsmanStaffTemplate.Type.FELLING)) {
            classes.add("axe");
        }
        if (has(stack, ModCraftsmanStaffTemplate.Type.SHEARING)) {
            classes.add("shears");
        }
        if (has(stack, ModCraftsmanStaffTemplate.Type.FORESTRY)
            && CraftsmanStaffCompat.getForestryScoop() != null) {
            classes.add("scoop");
        }
        return classes;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        if (ToolHelper.isBroken(stack)) {
            return super.getDestroySpeed(stack, state);
        }
        if (isEffective(stack, state)) {
            float speed = ToolHelper.calcDigSpeed(stack, state);
            if (has(stack, ModCraftsmanStaffTemplate.Type.COMBAT) && state.getBlock() == Blocks.WEB) {
                speed *= 7.5F;
            }
            return speed;
        }
        if (hasForestryTool(stack, state)) {
            float speed = forestrySpeed(stack, state);
            return speed > 1.0F ? speed : super.getDestroySpeed(stack, state);
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing facing,
                                           float hitX, float hitY, float hitZ, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (ToolHelper.isBroken(stack)) {
            return EnumActionResult.FAIL;
        }
        if (has(stack, ModCraftsmanStaffTemplate.Type.FORESTRY)) {
            CraftsmanStaffCompat.markAnalyzerBlockInteraction(player, hand);
        }
        CraftsmanStaffCompat.WorldAccess access = () -> world;

        if (has(stack, ModCraftsmanStaffTemplate.Type.INSIGHT)) {
            if (CraftsmanStaffCompat.tryInstallThaumcraftFocus(player, hand, stack)) {
                return EnumActionResult.SUCCESS;
            }
            if (CraftsmanStaffCompat.getThaumcraftFocusStack(stack) == null) {
                EnumActionResult result = CraftsmanStaffCompat.onItemUseFirst(player, access, stack, hand, pos,
                    facing, hitX, hitY, hitZ, "thaumcraft_salis_mundus",
                    CraftsmanStaffCompat.getThaumcraftSalt());
                if (result != EnumActionResult.PASS) {
                    return result;
                }
            }
            EnumActionResult result = CraftsmanStaffCompat.onItemUseFirst(player, access, stack, hand, pos, facing,
                hitX, hitY, hitZ, "thaumcraft_gauntlet", CraftsmanStaffCompat.getThaumcraftGauntlet());
            if (result != EnumActionResult.PASS) {
                return result;
            }
        }
        if (has(stack, ModCraftsmanStaffTemplate.Type.FORESTRY)) {
            EnumActionResult result = CraftsmanStaffCompat.onItemUseFirst(player, access, stack, hand, pos, facing,
                hitX, hitY, hitZ, "forestry_smoker", CraftsmanStaffCompat.getForestrySmoker());
            if (result != EnumActionResult.PASS) {
                return result;
            }
        }
        if (has(stack, ModCraftsmanStaffTemplate.Type.RESEARCH)) {
            for (CraftsmanStaffCompat.ResearchTool tool
                : CraftsmanStaffCompat.getResearchToolsForTarget(world, pos)) {
                EnumActionResult result = CraftsmanStaffCompat.onItemUseFirst(player, access, stack, hand, pos, facing,
                    hitX, hitY, hitZ, "research_" + tool.itemId, CraftsmanStaffCompat.getItem(tool.itemId));
                if (result != EnumActionResult.PASS) {
                    return result;
                }
            }
        }
        return super.onItemUseFirst(player, world, pos, facing, hitX, hitY, hitZ, hand);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (ToolHelper.isBroken(stack)) {
            return EnumActionResult.FAIL;
        }
        if (has(stack, ModCraftsmanStaffTemplate.Type.FORESTRY)) {
            CraftsmanStaffCompat.markAnalyzerBlockInteraction(player, hand);
        }
        CraftsmanStaffCompat.WorldAccess access = () -> world;
        if (has(stack, ModCraftsmanStaffTemplate.Type.NATURE)
            && CraftsmanStaffCompat.getNatureMode(stack) == CraftsmanStaffCompat.NATURE_MODE_MANA_BLASTER) {
            return CraftsmanStaffCompat.onNatureBlasterRightClick(player, access, stack, hand).getType();
        }

        boolean natureWandAlreadyTried = false;
        if (has(stack, ModCraftsmanStaffTemplate.Type.NATURE)
            && CraftsmanStaffCompat.getNatureMode(stack) == CraftsmanStaffCompat.NATURE_MODE_BIND
            && player.isSneaking()) {
            natureWandAlreadyTried = true;
            EnumActionResult result = CraftsmanStaffCompat.onNatureWandUse(player, access, stack, hand, pos,
                facing, hitX, hitY, hitZ);
            if (result != EnumActionResult.PASS) {
                return result;
            }
        }

        boolean farming = has(stack, ModCraftsmanStaffTemplate.Type.FARMING);
        boolean excavation = has(stack, ModCraftsmanStaffTemplate.Type.EXCAVATION);
        if (excavation && (!farming || player.isSneaking())) {
            EnumActionResult result = useVanillaTool(Items.IRON_SHOVEL, stack, player, world, pos, hand,
                facing, hitX, hitY, hitZ);
            if (result == EnumActionResult.SUCCESS) {
                TinkerToolEvent.OnShovelMakePath.fireEvent(stack, player, world, pos);
                return result;
            }
        }
        if (farming) {
            EnumActionResult result = useVanillaTool(Items.IRON_HOE, stack, player, world, pos, hand,
                facing, hitX, hitY, hitZ);
            if (result == EnumActionResult.SUCCESS) {
                TinkerToolEvent.OnMattockHoe.fireEvent(stack, player, world, pos);
                return result;
            }
        }
        if (has(stack, ModCraftsmanStaffTemplate.Type.NATURE) && !natureWandAlreadyTried) {
            EnumActionResult result = CraftsmanStaffCompat.onNatureWandUse(player, access, stack, hand, pos,
                facing, hitX, hitY, hitZ);
            if (result != EnumActionResult.PASS) {
                return result;
            }
        }
        if (has(stack, ModCraftsmanStaffTemplate.Type.FORESTRY)) {
            if (CraftsmanStaffCompat.tryForestryScoopBlockInteraction(player, access, stack, hand,
                pos, world.getBlockState(pos), facing, hitX, hitY, hitZ)) {
                return EnumActionResult.SUCCESS;
            }
            EnumActionResult result = CraftsmanStaffCompat.onItemUse(player, access, stack, hand, pos, facing,
                hitX, hitY, hitZ, "forestry_scoop", CraftsmanStaffCompat.getForestryScoop());
            if (result != EnumActionResult.PASS) {
                return result;
            }
            result = CraftsmanStaffCompat.onItemUse(player, access, stack, hand, pos, facing,
                hitX, hitY, hitZ, "forestry_grafter", CraftsmanStaffCompat.getForestryGrafter());
            if (result != EnumActionResult.PASS) {
                return result;
            }
        }
        if (has(stack, ModCraftsmanStaffTemplate.Type.RESEARCH)) {
            for (CraftsmanStaffCompat.ResearchTool tool
                : CraftsmanStaffCompat.getResearchToolsForTarget(world, pos)) {
                EnumActionResult result = CraftsmanStaffCompat.onItemUse(player, access, stack, hand, pos, facing,
                    hitX, hitY, hitZ, "research_" + tool.itemId, CraftsmanStaffCompat.getItem(tool.itemId));
                if (result != EnumActionResult.PASS) {
                    return result;
                }
            }
        }
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (ToolHelper.isBroken(stack)) {
            return new net.minecraft.util.ActionResult<>(EnumActionResult.FAIL, stack);
        }
        CraftsmanStaffCompat.WorldAccess access = () -> world;
        if (has(stack, ModCraftsmanStaffTemplate.Type.NATURE)) {
            if (player.isSneaking()) {
                return new net.minecraft.util.ActionResult<>(
                    CraftsmanStaffCompat.cycleNatureMode(player, stack), stack);
            }
            int mode = CraftsmanStaffCompat.getNatureMode(stack);
            net.minecraft.util.ActionResult<ItemStack> result = mode == CraftsmanStaffCompat.NATURE_MODE_MANA_BLASTER
                ? CraftsmanStaffCompat.onNatureBlasterRightClick(player, access, stack, hand)
                : CraftsmanStaffCompat.onNatureWandRightClick(player, access, stack, hand);
            if (result.getType() != EnumActionResult.PASS) {
                return result;
            }
        }
        if (has(stack, ModCraftsmanStaffTemplate.Type.INSIGHT)) {
            if (CraftsmanStaffCompat.tryInstallThaumcraftFocus(player, hand, stack)) {
                return new net.minecraft.util.ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
            net.minecraft.util.ActionResult<ItemStack> result = CraftsmanStaffCompat.onItemRightClick(
                player, access, stack, hand, "thaumcraft_gauntlet", CraftsmanStaffCompat.getThaumcraftGauntlet());
            if (result.getType() != EnumActionResult.PASS) {
                return result;
            }
        }
        if (has(stack, ModCraftsmanStaffTemplate.Type.FORESTRY)) {
            if (player.isSneaking()) {
                net.minecraft.util.math.RayTraceResult hit = player.rayTrace(5.0D, 1.0F);
                if (CraftsmanStaffCompat.consumeAnalyzerBlockInteraction(player, hand)
                    || hit != null && hit.typeOfHit == net.minecraft.util.math.RayTraceResult.Type.BLOCK) {
                    return new net.minecraft.util.ActionResult<>(EnumActionResult.SUCCESS, stack);
                }
                net.minecraft.util.ActionResult<ItemStack> result = CraftsmanStaffCompat.openCraftsmanEye(player, access, stack, hand);
                if (result.getType() != EnumActionResult.PASS) {
                    return result;
                }
            } else {
                net.minecraft.util.ActionResult<ItemStack> result = CraftsmanStaffCompat.onContinuousRightClick(
                    player, access, stack, hand, "forestry_smoker", CraftsmanStaffCompat.getForestrySmoker());
                if (result.getType() != EnumActionResult.PASS) {
                    return result;
                }
            }
        }
        if (has(stack, ModCraftsmanStaffTemplate.Type.RESEARCH)) {
            for (CraftsmanStaffCompat.ResearchTool tool : CraftsmanStaffCompat.getResearchTools()) {
                net.minecraft.util.ActionResult<ItemStack> result = CraftsmanStaffCompat.onItemRightClick(
                    player, access, stack, hand, "research_" + tool.itemId,
                    CraftsmanStaffCompat.getItem(tool.itemId));
                if (result.getType() != EnumActionResult.PASS) {
                    return result;
                }
            }
        }
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    protected boolean breakBlock(ItemStack stack, BlockPos pos, EntityPlayer player) {
        if (has(stack, ModCraftsmanStaffTemplate.Type.SHEARING)
            && !ToolHelper.isBroken(stack)
            && ToolHelper.shearBlock(stack, player.world, player, pos)) {
            return true;
        }
        return super.breakBlock(stack, pos, player);
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player,
                                            EntityLivingBase target, EnumHand hand) {
        if (has(stack, ModCraftsmanStaffTemplate.Type.FORESTRY) && !ToolHelper.isBroken(stack)
            && CraftsmanStaffCompat.tryForestryScoopEntityInteraction(player, stack, hand, target)) {
            return true;
        }
        if (!has(stack, ModCraftsmanStaffTemplate.Type.SHEARING) || !(target instanceof IShearable)) {
            return super.itemInteractionForEntity(stack, player, target, hand);
        }
        int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
        if (!shearEntity(stack, player.world, player, target, fortune)) {
            return false;
        }
        player.swingArm(hand);
        player.resetCooldown();
        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, IBlockState state, BlockPos pos,
                                    EntityLivingBase entityLiving) {
        boolean result = super.onBlockDestroyed(stack, world, state, pos, entityLiving);
        if (has(stack, ModCraftsmanStaffTemplate.Type.FORESTRY)) {
            result |= CraftsmanStaffCompat.onBlockDestroyed(entityLiving, () -> world, stack, state, pos,
                "forestry_grafter", CraftsmanStaffCompat.getForestryGrafter());
        }
        return result;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        if (has(stack, ModCraftsmanStaffTemplate.Type.INSIGHT)) {
            return 72000;
        }
        if (has(stack, ModCraftsmanStaffTemplate.Type.FORESTRY)) {
            return 32;
        }
        return super.getMaxItemUseDuration(stack);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        if (has(stack, ModCraftsmanStaffTemplate.Type.INSIGHT)
            || has(stack, ModCraftsmanStaffTemplate.Type.FORESTRY)) {
            return EnumAction.BOW;
        }
        return super.getItemUseAction(stack);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase entity, int count) {
        super.onUsingTick(stack, entity, count);
        if (has(stack, ModCraftsmanStaffTemplate.Type.FORESTRY)) {
            CraftsmanStaffCompat.onUsingTick(entity, stack, count, "forestry_smoker",
                CraftsmanStaffCompat.getForestrySmoker());
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entity, int count) {
        if (has(stack, ModCraftsmanStaffTemplate.Type.FORESTRY)) {
            CraftsmanStaffCompat.onPlayerStoppedUsing(entity, stack, count, "forestry_smoker",
                CraftsmanStaffCompat.getForestrySmoker());
        }
        super.onPlayerStoppedUsing(stack, world, entity, count);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.onUpdate(stack, world, entity, slot, selected);
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        if (has(stack, ModCraftsmanStaffTemplate.Type.NATURE)) {
            CraftsmanStaffCompat.onUpdate(player, stack, () -> world, slot, selected,
                "botania_wand", CraftsmanStaffCompat.getBotaniaWand());
            if (CraftsmanStaffCompat.getNatureMode(stack) == CraftsmanStaffCompat.NATURE_MODE_MANA_BLASTER) {
                CraftsmanStaffCompat.onUpdate(player, stack, () -> world, slot, selected,
                    "botania_gun", CraftsmanStaffCompat.getBotaniaGun());
            }
        }
        if (has(stack, ModCraftsmanStaffTemplate.Type.INSIGHT)) {
            CraftsmanStaffCompat.onUpdate(player, stack, () -> world, slot, selected,
                "thaumcraft_gauntlet", CraftsmanStaffCompat.getThaumcraftGauntlet());
        }
    }

    @Override
    public boolean dealDamage(ItemStack stack, EntityLivingBase attacker, Entity target, float damage) {
        boolean hit = super.dealDamage(stack, attacker, target, damage);
        double walkDelta = attacker.distanceWalkedModified - attacker.prevDistanceWalkedModified;
        if (!hit || !has(stack, ModCraftsmanStaffTemplate.Type.COMBAT) || ToolHelper.isBroken(stack)
            || !readyForSpecialAttack(attacker) || attacker.isSprinting() || attacker.fallDistance > 0.0F
            || !attacker.onGround || walkDelta >= attacker.getAIMoveSpeed()) {
            return hit;
        }

        AxisAlignedBB area = target.getEntityBoundingBox().grow(1.0D, 0.25D, 1.0D);
        for (EntityLivingBase nearby : attacker.world.getEntitiesWithinAABB(EntityLivingBase.class, area)) {
            if (nearby == attacker || nearby == target || attacker.isOnSameTeam(nearby)
                || attacker.getDistanceSq(nearby) >= 9.0D) {
                continue;
            }
            nearby.knockBack(attacker, 0.4F,
                Math.sin(attacker.rotationYaw * 0.017453292F),
                -Math.cos(attacker.rotationYaw * 0.017453292F));
            super.dealDamage(stack, attacker, nearby, 1.0F);
        }
        attacker.world.playSound(null, attacker.posX, attacker.posY, attacker.posZ,
            SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, attacker.getSoundCategory(), 1.0F, 1.0F);
        if (attacker instanceof EntityPlayer) {
            ((EntityPlayer) attacker).resetCooldown();
        }
        return true;
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, EntityLivingBase entity,
                                    EntityLivingBase attacker) {
        return has(stack, ModCraftsmanStaffTemplate.Type.FELLING)
            || super.canDisableShield(stack, shield, entity, attacker);
    }

    private static boolean isEffective(ItemStack stack, IBlockState state) {
        net.minecraft.block.material.Material material = state.getMaterial();
        return has(stack, ModCraftsmanStaffTemplate.Type.COMBAT)
                && SwordCore.effective_materials.contains(material)
            || has(stack, ModCraftsmanStaffTemplate.Type.MINING)
                && Pickaxe.effective_materials.contains(material)
            || has(stack, ModCraftsmanStaffTemplate.Type.EXCAVATION)
                && Shovel.effective_materials.contains(material)
            || has(stack, ModCraftsmanStaffTemplate.Type.FELLING)
                && Hatchet.effective_materials.contains(material)
            || has(stack, ModCraftsmanStaffTemplate.Type.SHEARING)
                && Kama.effective_materials.contains(material);
    }

    private static boolean hasForestryTool(ItemStack stack, IBlockState state) {
        return has(stack, ModCraftsmanStaffTemplate.Type.FORESTRY)
            && (CraftsmanStaffCompat.canHarvestBlock(null, stack, state, "forestry_scoop",
                CraftsmanStaffCompat.getForestryScoop())
            || CraftsmanStaffCompat.canHarvestBlock(null, stack, state, "forestry_grafter",
                CraftsmanStaffCompat.getForestryGrafter()));
    }

    private static float forestrySpeed(ItemStack stack, IBlockState state) {
        float scoop = CraftsmanStaffCompat.getDestroySpeed(null, stack, state, "forestry_scoop",
            CraftsmanStaffCompat.getForestryScoop());
        float grafter = CraftsmanStaffCompat.getDestroySpeed(null, stack, state, "forestry_grafter",
            CraftsmanStaffCompat.getForestryGrafter());
        return Math.max(scoop, grafter);
    }

    private static boolean has(ItemStack stack, ModCraftsmanStaffTemplate.Type type) {
        return ModCraftsmanStaffTemplate.has(stack, type);
    }

    private static boolean hasHarvestTemplate(ItemStack stack) {
        return has(stack, ModCraftsmanStaffTemplate.Type.MINING)
            || has(stack, ModCraftsmanStaffTemplate.Type.EXCAVATION)
            || has(stack, ModCraftsmanStaffTemplate.Type.FELLING);
    }

    private static double getAverageHandleModifier(ItemStack stack) {
        NBTTagList materialsTag = TagUtil.getBaseMaterialsTagList(stack);
        List<Material> materials = TinkerUtil.getMaterialsFromTagList(materialsTag);
        if (materials.size() < 3) {
            return 0.0D;
        }
        HandleMaterialStats first = materials.get(1).getStatsOrUnknown(MaterialTypes.HANDLE);
        HandleMaterialStats second = materials.get(2).getStatsOrUnknown(MaterialTypes.HANDLE);
        return Math.max(0.0D, (first.modifier + second.modifier) / 2.0D);
    }

    private static EnumActionResult useVanillaTool(net.minecraft.item.Item vanillaTool, ItemStack stack,
                                                    EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        int previousDamage = stack.getItemDamage();
        EnumActionResult result = vanillaTool.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
        stack.setItemDamage(previousDamage);
        if (!world.isRemote && result == EnumActionResult.SUCCESS) {
            ToolHelper.damageTool(stack, 1, player);
        }
        return result;
    }

    private static boolean shearEntity(ItemStack stack, World world, EntityPlayer player,
                                       Entity entity, int fortune) {
        IShearable shearable = (IShearable) entity;
        BlockPos pos = entity.getPosition();
        if (!shearable.isShearable(stack, world, pos)) {
            return false;
        }
        if (!world.isRemote) {
            for (ItemStack drop : shearable.onSheared(stack, world, pos, fortune)) {
                EntityItem item = entity.entityDropItem(drop, 1.0F);
                if (item != null) {
                    item.motionY += world.rand.nextFloat() * 0.05F;
                    item.motionX += (world.rand.nextFloat() - world.rand.nextFloat()) * 0.1F;
                    item.motionZ += (world.rand.nextFloat() - world.rand.nextFloat()) * 0.1F;
                }
            }
        }
        ToolHelper.damageTool(stack, 1, player);
        return true;
    }
}
