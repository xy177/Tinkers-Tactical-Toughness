package xy177.tt2.modifiers;

import c4.conarm.lib.tinkering.TinkersArmor;
import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Loader;
import slimeknights.mantle.util.RecipeMatch;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import xy177.tt2.init.TT2Items;
import xy177.tt2.item.ItemExperienceBottle;

import java.util.ArrayList;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ModExperienceTransfer extends Modifier {

    public static final String ID = "tt2_experience_transfer";
    public static final String TOOL_LEVELING = "toolleveling";
    public static final String ARMOR_LEVELING = "leveling";
    private static final ThreadLocal<Integer> APPLYING_EXPERIENCE = new ThreadLocal<>();

    public ModExperienceTransfer() {
        super(ID);
        addRecipeMatch(new ExperienceRecipeMatch());
    }

    public static boolean isLevelingModifier(String modifier) {
        return TOOL_LEVELING.equals(modifier) || ARMOR_LEVELING.equals(modifier);
    }

    public static boolean hasLeveling(ItemStack stack) {
        return levelingModifier(stack) != null;
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public List<String> getExtraInfo(ItemStack stack, NBTTagCompound modifierTag) {
        return Collections.emptyList();
    }

    @Override
    protected boolean canApplyCustom(ItemStack stack) {
        return hasLeveling(stack) && currentExperience() > 0;
    }

    @Override
    public void apply(ItemStack stack) {
        int experience = currentExperience();
        APPLYING_EXPERIENCE.remove();
        if (experience > 0) {
            addExperience(stack, experience);
        }
    }

    @Override
    public void apply(NBTTagCompound root) {
        // ToolBuilder calls apply(ItemStack); keep the internal modifier from writing itself into tools.
    }

    @Override
    public void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag) {
    }

    public static int totalExperience(ItemStack stack, String modifier) {
        return (int) Math.min(Integer.MAX_VALUE, totalExperienceLong(stack, modifier));
    }

    public static void extractExperience(ItemStack stack, String modifier, int amount) {
        if (amount <= 0) {
            return;
        }
        setTotalExperience(stack, modifier, Math.max(0L, totalExperienceLong(stack, modifier) - amount));
    }

    private static long totalExperienceLong(ItemStack stack, String modifier) {
        NBTTagCompound tag = TinkerUtil.getModifierTag(stack, modifier);
        if (tag.getKeySet().isEmpty()) {
            return 0;
        }
        int level = Math.max(1, ModifierNBT.readTag(tag).level);
        long total = Math.max(0, tag.getInteger("xp"));
        for (int i = 1; i < level; i++) {
            total = saturatedAdd(total, safeXpForLevel(stack, modifier, i));
            if (total == Long.MAX_VALUE) {
                return total;
            }
        }
        return total;
    }

    public static void resetExperience(ItemStack stack, String modifier) {
        NBTTagCompound tag = TinkerUtil.getModifierTag(stack, modifier);
        if (tag.getKeySet().isEmpty()) {
            return;
        }
        ModifierNBT data = ModifierNBT.readTag(tag);
        data.identifier = modifier;
        data.color = 0xFFFFFF;
        data.level = 1;
        data.write(tag);
        tag.setInteger("xp", 0);
        tag.setInteger("bonus_modifiers", 0);
        writeModifierTag(stack, modifier, tag);
        rebuild(stack);
    }

    public static ItemStack transferExperience(ItemStack stack, int experience) {
        if (experience <= 0 || !hasLeveling(stack)) {
            return ItemStack.EMPTY;
        }
        ItemStack result = stack.copy();
        addExperience(result, experience);
        return result;
    }

    private static void addExperience(ItemStack stack, int experience) {
        String modifier = levelingModifier(stack);
        if (modifier == null || experience <= 0) {
            return;
        }
        NBTTagCompound tag = TinkerUtil.getModifierTag(stack, modifier);
        if (tag.getKeySet().isEmpty()) {
            return;
        }
        ModifierNBT data = ModifierNBT.readTag(tag);
        long xp = Math.max(0, tag.getInteger("xp")) + (long) experience;
        int level = Math.max(1, data.level);
        int bonus = Math.max(0, tag.getInteger("bonus_modifiers"));
        while (canLevelUp(modifier, level)) {
            int required = safeXpForLevel(stack, modifier, level);
            if (required <= 0 || xp < required) {
                break;
            }
            xp -= required;
            level++;
            bonus++;
        }
        data.identifier = modifier;
        data.color = 0xFFFFFF;
        data.level = level;
        data.write(tag);
        tag.setInteger("xp", (int) Math.min(Integer.MAX_VALUE, xp));
        tag.setInteger("bonus_modifiers", bonus);
        writeModifierTag(stack, modifier, tag);
        rebuild(stack);
    }

    private static int currentExperience() {
        Integer experience = APPLYING_EXPERIENCE.get();
        return experience == null ? 0 : experience;
    }

    private static void setTotalExperience(ItemStack stack, String modifier, long experience) {
        NBTTagCompound tag = TinkerUtil.getModifierTag(stack, modifier);
        if (tag.getKeySet().isEmpty()) {
            return;
        }
        ModifierNBT data = ModifierNBT.readTag(tag);
        int level = 1;
        int bonus = 0;
        long xp = Math.max(0L, experience);
        while (canLevelUp(modifier, level)) {
            int required = safeXpForLevel(stack, modifier, level);
            if (required <= 0 || xp < required) {
                break;
            }
            xp -= required;
            level++;
            bonus++;
        }
        data.identifier = modifier;
        data.color = 0xFFFFFF;
        data.level = level;
        data.write(tag);
        tag.setInteger("xp", (int) Math.min(Integer.MAX_VALUE, xp));
        tag.setInteger("bonus_modifiers", bonus);
        writeModifierTag(stack, modifier, tag);
        rebuild(stack);
    }

    private static String levelingModifier(ItemStack stack) {
        if (hasModifier(stack, TOOL_LEVELING)) {
            return TOOL_LEVELING;
        }
        return hasModifier(stack, ARMOR_LEVELING) ? ARMOR_LEVELING : null;
    }

    private static boolean hasModifier(ItemStack stack, String modifier) {
        return TinkerUtil.getIndexInCompoundList(TagUtil.getModifiersTagList(stack), modifier) >= 0;
    }

    private static void writeModifierTag(ItemStack stack, String modifier, NBTTagCompound tag) {
        NBTTagList modifiers = TagUtil.getModifiersTagList(stack);
        int index = TinkerUtil.getIndexInCompoundList(modifiers, modifier);
        if (index >= 0) {
            modifiers.set(index, tag);
            TagUtil.setModifiersTagList(stack, modifiers);
        }
    }

    private static int xpForLevel(ItemStack stack, String modifier, int level) {
        try {
            if (TOOL_LEVELING.equals(modifier) && Loader.isModLoaded("tinkertoolleveling")) {
                Class<?> config = Class.forName("slimeknights.toolleveling.config.Config");
                if (level <= 1) {
                    Method base = config.getMethod("getBaseXpForTool", net.minecraft.item.Item.class);
                    return (Integer) base.invoke(null, stack.getItem());
                }
                Method multiplier = config.getMethod("getLevelMultiplier");
                return (int) (xpForLevel(stack, modifier, level - 1) * (Float) multiplier.invoke(null));
            }
            if (ARMOR_LEVELING.equals(modifier)) {
                Class<?> armor = Class.forName("c4.conarm.integrations.tinkertoolleveling.ModArmorLeveling");
                if (level <= 1) {
                    return (Integer) armor.getMethod("getBaseXp").invoke(null);
                }
                Method multiplier = armor.getMethod("getLevelMultiplier");
                return (int) (xpForLevel(stack, modifier, level - 1) * (Float) multiplier.invoke(null));
            }
        } catch (Exception ignored) {
        }
        return level <= 1 ? 500 : xpForLevel(stack, modifier, level - 1) * 2;
    }

    private static int safeXpForLevel(ItemStack stack, String modifier, int level) {
        int xp = xpForLevel(stack, modifier, level);
        return xp > 0 ? xp : Integer.MAX_VALUE;
    }

    private static long saturatedAdd(long a, long b) {
        if (Long.MAX_VALUE - a < b) {
            return Long.MAX_VALUE;
        }
        return a + b;
    }

    private static boolean canLevelUp(String modifier, int level) {
        try {
            if (TOOL_LEVELING.equals(modifier) && Loader.isModLoaded("tinkertoolleveling")) {
                Class<?> config = Class.forName("slimeknights.toolleveling.config.Config");
                return (Boolean) config.getMethod("canLevelUp", int.class).invoke(null, level);
            }
            if (ARMOR_LEVELING.equals(modifier)) {
                Class<?> armor = Class.forName("c4.conarm.integrations.tinkertoolleveling.ModArmorLeveling");
                return (Boolean) armor.getMethod("canLevelUp", int.class).invoke(null, level);
            }
        } catch (Exception ignored) {
        }
        return true;
    }

    private static void rebuild(ItemStack stack) {
        try {
            if (stack.getItem() instanceof ToolCore) {
                slimeknights.tconstruct.library.utils.ToolBuilder.rebuildTool(stack.getTagCompound(), (ToolCore) stack.getItem());
            } else if (stack.getItem() instanceof TinkersArmor) {
                c4.conarm.lib.tinkering.ArmorBuilder.rebuildArmor(stack.getTagCompound(), (TinkersArmor) stack.getItem());
            }
        } catch (Exception ignored) {
        }
    }

    private static class ExperienceRecipeMatch extends RecipeMatch {

        ExperienceRecipeMatch() {
            super(1, 1);
        }

        @Override
        public List<ItemStack> getInputs() {
            return ImmutableList.of();
        }

        @Override
        public Optional<Match> matches(NonNullList<ItemStack> stacks) {
            for (ItemStack stack : stacks) {
                if (!stack.isEmpty() && stack.getItem() == TT2Items.EXPERIENCE_BOTTLE) {
                    int experience = ItemExperienceBottle.getExperience(stack);
                    if (experience > 0) {
                        ItemStack matched = stack.copy();
                        matched.setCount(1);
                        List<ItemStack> inputs = new ArrayList<>();
                        inputs.add(matched);
                        APPLYING_EXPERIENCE.set(experience);
                        return Optional.of(new Match(inputs, 1));
                    }
                }
            }
            APPLYING_EXPERIENCE.remove();
            return Optional.empty();
        }
    }
}
