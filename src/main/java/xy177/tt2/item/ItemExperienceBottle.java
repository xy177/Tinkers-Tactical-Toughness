package xy177.tt2.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.TinkerRegistry;
import xy177.tt2.init.TT2Items;

import javax.annotation.Nullable;
import java.util.List;

public class ItemExperienceBottle extends Item {

    public static final String TAG_EXPERIENCE = "Experience";

    public ItemExperienceBottle() {
        setTranslationKey("tt2.experience_bottle");
        setCreativeTab(TinkerRegistry.tabGeneral);
        setMaxStackSize(64);
    }

    public static ItemStack withExperience(int experience) {
        ItemStack stack = new ItemStack(TT2Items.EXPERIENCE_BOTTLE);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger(TAG_EXPERIENCE, Math.max(0, experience));
        stack.setTagCompound(tag);
        return stack;
    }

    public static int getExperience(ItemStack stack) {
        return stack.hasTagCompound() ? Math.max(0, stack.getTagCompound().getInteger(TAG_EXPERIENCE)) : 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.GRAY + I18n.translateToLocalFormatted("tooltip.tt2.experience_bottle.xp",
            getExperience(stack)));
    }
}
