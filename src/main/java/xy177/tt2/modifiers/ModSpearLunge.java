package xy177.tt2.modifiers;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.oredict.OreDictionary;
import slimeknights.mantle.util.RecipeMatch;
import slimeknights.tconstruct.library.modifiers.ModifierAspect;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.tools.modifiers.ToolModifier;
import xy177.tt2.config.TT2Config;
import xy177.tt2.init.TT2Items;
import xy177.tt2.init.TT2Sounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModSpearLunge extends ToolModifier {

    public static final String ID = "tt2_lunge";
    private static final int COLOR = 0xB77CE3;

    public ModSpearLunge() {
        super(ID, COLOR);
        addAspects(new ModifierAspect.MultiAspect(
            this,
            COLOR,
            TT2Config.spearLungeMaxLevel,
            1,
            TT2Config.spearLungeModifierSlotsPerLevel
        ));
        addRecipeMatch(new LungeRecipeMatch());
    }

    @Override
    protected boolean canApplyCustom(ItemStack stack) {
        return isSpear(stack);
    }

    @Override
    public void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag) {
    }

    @Override
    public List<List<ItemStack>> getItems() {
        ImmutableList.Builder<ItemStack> recipe = ImmutableList.builder();
        addDisplayItem(recipe, new ItemStack(Blocks.PISTON), TT2Config.spearLungePistonCost);
        addDisplayItem(recipe, TinkerCommons.ingotKnightSlime, TT2Config.spearLungeKnightSlimeIngotCost);
        ImmutableList<ItemStack> recipeItems = recipe.build();
        if (recipeItems.isEmpty()) {
            return ImmutableList.of();
        }
        return ImmutableList.of(recipeItems);
    }

    public static int getLevel(ItemStack stack) {
        if (!isSpear(stack)) {
            return 0;
        }
        ModifierNBT data = ModifierNBT.readTag(TinkerUtil.getModifierTag(stack, ID));
        if (!ID.equals(data.identifier)) {
            return 0;
        }
        return Math.min(Math.max(0, data.level), Math.max(0, TT2Config.spearLungeMaxLevel));
    }

    public static boolean canTrigger(EntityPlayer player, ItemStack stack) {
        if (player == null || player.getEntityWorld().isRemote || !player.isEntityAlive()
            || player.isSpectator() || getLevel(stack) <= 0) {
            return false;
        }
        if (TT2Config.spearLungeDisallowRiding && player.isRiding()) {
            return false;
        }
        if (TT2Config.spearLungeDisallowWater && player.isInWater()) {
            return false;
        }
        if (TT2Config.spearLungeDisallowElytraFlight && player.isElytraFlying()) {
            return false;
        }
        return player.capabilities.isCreativeMode
            || player.getFoodStats().getFoodLevel() >= TT2Config.spearLungeMinFoodLevel;
    }

    public static boolean applyLunge(EntityPlayer player, ItemStack stack) {
        if (!canTrigger(player, stack)) {
            return false;
        }

        int level = getLevel(stack);
        if (!player.capabilities.isCreativeMode) {
            ToolHelper.damageTool(stack, TT2Config.spearLungeDurabilityCost, player);
            player.addExhaustion((float) (TT2Config.spearLungeExhaustionPerLevel * level));
        }

        Vec3d look = player.getLookVec();
        double forwardBoost = TT2Config.spearLungeForwardBoostPerLevel * level;
        double motionX = player.motionX + look.x * forwardBoost;
        double motionZ = player.motionZ + look.z * forwardBoost;
        double maxSpeed = TT2Config.spearLungeMaxHorizontalSpeed;
        double horizontalSpeed = Math.sqrt(motionX * motionX + motionZ * motionZ);
        if (maxSpeed > 0.0 && horizontalSpeed > maxSpeed) {
            double scale = maxSpeed / horizontalSpeed;
            motionX *= scale;
            motionZ *= scale;
        }

        player.motionX = motionX;
        player.motionZ = motionZ;
        player.velocityChanged = true;
        player.world.playSound(null, player.posX, player.posY, player.posZ,
            TT2Sounds.randomSpearLunge(player.world.rand),
            SoundCategory.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    static boolean isSpear(ItemStack stack) {
        return stack != null && !stack.isEmpty() && TT2Items.SPEAR != null
            && stack.getItem() == TT2Items.SPEAR;
    }

    public static boolean hasRecipeIngredients(NonNullList<ItemStack> stacks) {
        return findRecipeInputs(stacks) != null;
    }

    private static List<ItemStack> findRecipeInputs(NonNullList<ItemStack> stacks) {
        int pistonCost = TT2Config.spearLungePistonCost;
        int knightSlimeCost = TT2Config.spearLungeKnightSlimeIngotCost;
        if (pistonCost <= 0 && knightSlimeCost <= 0) {
            return null;
        }

        List<ItemStack> matched = new ArrayList<>();
        if (!collect(stacks, new ItemStack(Blocks.PISTON), pistonCost, matched)
            || !collect(stacks, TinkerCommons.ingotKnightSlime, knightSlimeCost, matched)) {
            return null;
        }
        return matched;
    }

    private static boolean collect(NonNullList<ItemStack> stacks, ItemStack template, int amount,
                                   List<ItemStack> matched) {
        if (amount <= 0) {
            return true;
        }
        if (template == null || template.isEmpty()) {
            return false;
        }

        int remaining = amount;
        for (ItemStack stack : stacks) {
            if (stack.isEmpty() || !OreDictionary.itemMatches(template, stack, false)) {
                continue;
            }
            ItemStack copy = stack.copy();
            copy.setCount(Math.min(stack.getCount(), remaining));
            matched.add(copy);
            remaining -= copy.getCount();
            if (remaining <= 0) {
                return true;
            }
        }
        return false;
    }

    private static void addDisplayItem(ImmutableList.Builder<ItemStack> recipe,
                                       ItemStack template, int amount) {
        if (template == null || template.isEmpty() || amount <= 0) {
            return;
        }
        ItemStack input = template.copy();
        input.setCount(amount);
        recipe.add(input);
    }

    private static class LungeRecipeMatch extends RecipeMatch {

        LungeRecipeMatch() {
            super(1, 1);
        }

        @Override
        public List<ItemStack> getInputs() {
            ImmutableList.Builder<ItemStack> inputs = ImmutableList.builder();
            addInput(inputs, new ItemStack(Blocks.PISTON), TT2Config.spearLungePistonCost);
            addInput(inputs, TinkerCommons.ingotKnightSlime, TT2Config.spearLungeKnightSlimeIngotCost);
            return inputs.build();
        }

        @Override
        public Optional<Match> matches(NonNullList<ItemStack> stacks) {
            List<ItemStack> matched = findRecipeInputs(stacks);
            if (matched == null) {
                return Optional.empty();
            }
            return Optional.of(new Match(matched, 1));
        }

        private static void addInput(ImmutableList.Builder<ItemStack> inputs, ItemStack template, int amount) {
            if (template == null || template.isEmpty() || amount <= 0) {
                return;
            }
            ItemStack input = template.copy();
            input.setCount(amount);
            inputs.add(input);
        }

    }
}
