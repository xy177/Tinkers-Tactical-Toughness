package xy177.tt2.tools;

import xy177.tt2.TT2;
import xy177.tt2.tools.traits.TraitNunchakuCombo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.HeadMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.materials.MaterialTypes;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.SwordCore;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.tools.TinkerTools;

import javax.annotation.Nullable;
import java.util.List;


public class TinkerNunchaku extends SwordCore {

    public static final TraitNunchakuCombo COMBO_TRAIT = new TraitNunchakuCombo();

    public static final String KEY_SPINNING = "tt2_nunchaku_spinning";

    private static final float PULL_STRENGTH = 0.3F;

    public TinkerNunchaku() {
        super(
            new PartMaterialType(TinkerTools.toughToolRod, MaterialTypes.HEAD),
            new PartMaterialType(TinkerTools.toughToolRod, MaterialTypes.HEAD),
            new PartMaterialType(TinkerTools.toughBinding, MaterialTypes.HANDLE)
        );
        this.setTranslationKey("tt2.nunchaku");
        addCategory(Category.WEAPON);

        this.addPropertyOverride(
            new ResourceLocation(TT2.MOD_ID, "spinning"),
            new IItemPropertyGetter() {
                @Override
                @SideOnly(Side.CLIENT)
                public float apply(ItemStack stack, @Nullable World worldIn,
                                   @Nullable EntityLivingBase entityIn) {
                    if (entityIn instanceof EntityPlayer) {
                        NBTTagCompound nbt = entityIn.getEntityData();
                        if (nbt.getBoolean(KEY_SPINNING)) return 1.0F;
                    }
                    return 0.0F;
                }
            }
        );
    }

    @Override public float damagePotential() { return 0.45f; }
    @Override public double attackSpeed() { return xy177.tt2.config.TT2Config.nunchakuAttackSpeed; }
    @Override public float knockback() { return 0f; }

    @Override
    public boolean dealDamage(ItemStack stack, EntityLivingBase attacker,
                              Entity target, float damage) {
        boolean hit = super.dealDamage(stack, attacker, target, damage);
        if (hit && target instanceof EntityLivingBase) {
            float yawRad = attacker.rotationYaw * 0.017453292F;
            target.addVelocity(
                -MathHelper.sin(yawRad) * PULL_STRENGTH,
                0.05D,
                MathHelper.cos(yawRad) * PULL_STRENGTH
            );
            target.velocityChanged = true;
        }
        return hit;
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        return false;
    }

    @Override
    protected ToolNBT buildTagData(List<Material> materials) {
        HeadMaterialStats  head1  = materials.get(0).getStatsOrUnknown(MaterialTypes.HEAD);
        HeadMaterialStats  head2  = materials.get(1).getStatsOrUnknown(MaterialTypes.HEAD);
        HandleMaterialStats handle = materials.get(2).getStatsOrUnknown(MaterialTypes.HANDLE);
        ToolNBT data = new ToolNBT();
        data.head(head1, head2);
        data.handle(handle);
        data.modifiers = DEFAULT_MODIFIERS;
        return data;
    }

    @Override
    public void addMaterialTraits(NBTTagCompound root, List<Material> materials) {
        super.addMaterialTraits(root, materials);
        NBTTagCompound modTag = new NBTTagCompound();
        COMBO_TRAIT.updateNBTforTrait(modTag, 0xFFAA00);
        COMBO_TRAIT.applyEffect(root, modTag);
        NBTTagList modList = TagUtil.getModifiersTagList(root);
        modList.appendTag(modTag);
        TagUtil.setModifiersTagList(root, modList);
    }

    @Override
    public int[] getRepairParts() { return new int[]{0, 1}; }
}
