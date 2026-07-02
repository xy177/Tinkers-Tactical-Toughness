package xy177.tt2.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.modifiers.IModifier;
import c4.conarm.lib.ArmoryRegistry;

import javax.annotation.Nullable;
import java.util.List;

public class ItemModifierCrystal extends Item {

    public static final String TAG_MODIFIER = "Modifier";
    public static final String TAG_LEVEL = "Level";
    public static final String TAG_COLOR = "Color";
    public static final String TAG_EMBOSS = "Emboss";
    public static final String TAG_EMBOSS_LEVEL = "EmbossLevel";
    public static final String TAG_VALUE = "Value";
    public static final String TAG_MAX_VALUE = "MaxValue";
    public static final String TAG_PARTIAL = "Partial";

    public ItemModifierCrystal() {
        setTranslationKey("tt2.modifier_crystal");
        setCreativeTab(TinkerRegistry.tabGeneral);
        setMaxStackSize(64);
    }

    public static ItemStack withModifier(String modifier, int level, int color, boolean emboss) {
        return withModifier(modifier, level, color, emboss, 1, 0);
    }

    public static ItemStack withModifier(String modifier, int level, int color, boolean emboss, int value, int maxValue) {
        ItemStack stack = new ItemStack(xy177.tt2.init.TT2Items.MODIFIER_CRYSTAL);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString(TAG_MODIFIER, modifier);
        tag.setInteger(TAG_LEVEL, Math.max(1, level));
        tag.setInteger(TAG_COLOR, color);
        tag.setBoolean(TAG_EMBOSS, emboss);
        if (emboss) {
            tag.setInteger(TAG_EMBOSS_LEVEL, embossLevel(modifier));
        }
        tag.setInteger(TAG_VALUE, Math.max(1, value));
        if (maxValue > 0) {
            tag.setInteger(TAG_MAX_VALUE, maxValue);
            tag.setBoolean(TAG_PARTIAL, value < maxValue);
        }
        stack.setTagCompound(tag);
        return stack;
    }

    public static int getValue(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return 1;
        }
        return Math.max(1, stack.getTagCompound().getInteger(TAG_VALUE));
    }

    public static int getColor(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return 0xFFFFFF;
        }
        return stack.getTagCompound().hasKey(TAG_COLOR) ? stack.getTagCompound().getInteger(TAG_COLOR) : 0xFFFFFF;
    }

    public static int getEmbossLevel(ItemStack stack) {
        if (!stack.hasTagCompound() || !stack.getTagCompound().getBoolean(TAG_EMBOSS)) {
            return 0;
        }
        int level = stack.getTagCompound().getInteger(TAG_EMBOSS_LEVEL);
        return level <= 0 ? embossLevel(stack.getTagCompound().getString(TAG_MODIFIER)) : level;
    }

    public static int embossLevel(String modifier) {
        return modifier.startsWith("moretcon.extratrait2") || modifier.startsWith("extratrait2") ? 2 : 1;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String name = super.getItemStackDisplayName(stack);
        if (!stack.hasTagCompound()) {
            return name;
        }
        String id = stack.getTagCompound().getString(TAG_MODIFIER);
        IModifier modifier = TinkerRegistry.getModifier(id);
        if (modifier == null) {
            modifier = ArmoryRegistry.getArmorModifier(id);
        }
        String modifierName = modifier == null ? id : modifier.getLocalizedName();
        if (stack.getTagCompound().getBoolean(TAG_EMBOSS)) {
            String key = getEmbossLevel(stack) == 2
                ? "item.tt2.modifier_crystal.emboss2.name"
                : "item.tt2.modifier_crystal.emboss.name";
            return I18n.translateToLocalFormatted(key, materialName(modifierName));
        }
        if (id.startsWith("fortify")) {
            return I18n.translateToLocalFormatted("item.tt2.modifier_crystal.fortify.name", materialName(modifierName));
        }
        if (id.startsWith("polished_armor")) {
            return I18n.translateToLocalFormatted("item.tt2.modifier_crystal.polished.name", materialName(modifierName));
        }
        String key = stack.getTagCompound().getBoolean(TAG_PARTIAL)
            ? "item.tt2.modifier_crystal.partial.name"
            : "item.tt2.modifier_crystal.modifier.name";
        return I18n.translateToLocalFormatted(key, modifierName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (!stack.hasTagCompound()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        tooltip.add(TextFormatting.DARK_GRAY + tag.getString(TAG_MODIFIER));
        if (tag.getInteger(TAG_LEVEL) > 1) {
            tooltip.add(TextFormatting.GRAY + "Level: " + tag.getInteger(TAG_LEVEL));
        }
        if (tag.hasKey(TAG_MAX_VALUE)) {
            tooltip.add(TextFormatting.GRAY.toString() + tag.getInteger(TAG_VALUE) + " / " + tag.getInteger(TAG_MAX_VALUE));
        }
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
        return true;
    }

    private static String materialName(String name) {
        int start = name.indexOf('(');
        int end = name.lastIndexOf(')');
        return start >= 0 && end > start ? name.substring(start + 1, end) : name;
    }
}
