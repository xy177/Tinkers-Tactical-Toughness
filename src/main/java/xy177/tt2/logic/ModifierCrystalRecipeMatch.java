package xy177.tt2.logic;

import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import slimeknights.mantle.util.RecipeMatch;
import slimeknights.tconstruct.library.modifiers.ModifierAspect;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.modifiers.TinkerGuiException;
import slimeknights.tconstruct.library.utils.TagUtil;
import xy177.tt2.init.TT2Items;
import xy177.tt2.item.ItemModifierCrystal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ModifierCrystalRecipeMatch extends RecipeMatch {

    private final String modifier;
    private final boolean emboss;
    private final int embossLevel;

    public ModifierCrystalRecipeMatch(String modifier, boolean emboss) {
        this(modifier, emboss, emboss ? ItemModifierCrystal.embossLevel(modifier) : 0);
    }

    public ModifierCrystalRecipeMatch(String modifier, boolean emboss, int embossLevel) {
        super(1, 1);
        this.modifier = modifier;
        this.emboss = emboss;
        this.embossLevel = embossLevel;
    }

    @Override
    public List<ItemStack> getInputs() {
        return ImmutableList.of();
    }

    @Override
    public Optional<Match> matches(NonNullList<ItemStack> stacks) {
        List<ItemStack> matched = new ArrayList<>();
        int amount = 0;
        for (ItemStack stack : stacks) {
            if (isCrystalFor(stack)) {
                ItemStack copy = stack.copy();
                copy.setCount(stack.getCount());
                matched.add(copy);
                amount += ItemModifierCrystal.getValue(copy) * copy.getCount();
            }
        }
        return amount > 0 ? Optional.of(new Match(matched, amount)) : Optional.empty();
    }

    private boolean isCrystalFor(ItemStack stack) {
        return !stack.isEmpty()
            && stack.getItem() == TT2Items.MODIFIER_CRYSTAL
            && stack.hasTagCompound()
            && modifier.equals(stack.getTagCompound().getString(ItemModifierCrystal.TAG_MODIFIER))
            && stack.getTagCompound().getBoolean(ItemModifierCrystal.TAG_EMBOSS) == emboss
            && (!emboss || ItemModifierCrystal.getEmbossLevel(stack) == embossLevel);
    }

    public static class UniqueEmbossAspect extends ModifierAspect {

        private final int embossLevel;

        public UniqueEmbossAspect(int embossLevel) {
            this.embossLevel = embossLevel;
        }

        @Override
        public boolean canApply(ItemStack tool, ItemStack original) throws TinkerGuiException {
            NBTTagList modifiers = TagUtil.getModifiersTagList(tool);
            for (int i = 0; i < modifiers.tagCount(); i++) {
                String id = ModifierNBT.readTag(modifiers.getCompoundTagAt(i)).identifier;
                if (isEmbossLevel(id, embossLevel)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void updateNBT(net.minecraft.nbt.NBTTagCompound root, net.minecraft.nbt.NBTTagCompound modifierTag) {
        }
    }

    public static boolean isEmboss(String id) {
        return isEmbossLevel(id, 1) || isEmbossLevel(id, 2);
    }

    public static boolean isEmbossLevel(String id, int level) {
        boolean second = id.startsWith("moretcon.extratrait2") || id.startsWith("extratrait2");
        if (level == 2) {
            return second;
        }
        return !second && (id.startsWith("extratrait") || id.startsWith("extratrait_armor"));
    }
}
