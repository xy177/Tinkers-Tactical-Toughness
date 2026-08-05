package xy177.tt2.modifiers;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import slimeknights.tconstruct.library.modifiers.ModifierAspect;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import slimeknights.tconstruct.tools.modifiers.ToolModifier;
import xy177.tt2.config.TT2Config;
import xy177.tt2.init.TT2Items;

public class ModCraftsmanStaffTemplate extends ToolModifier {

    public enum Type {
        FARMING("tt2_craftsman_farming", 0xFFAA00),
        COMBAT("tt2_craftsman_combat", 0xAAAAAA),
        MINING("tt2_craftsman_mining", 0xFFFF55),
        EXCAVATION("tt2_craftsman_excavation", 0x5555FF),
        FELLING("tt2_craftsman_felling", 0x00AAAA),
        SHEARING("tt2_craftsman_shearing", 0x55FFFF),
        NATURE("tt2_craftsman_nature", 0x55AA55),
        INSIGHT("tt2_craftsman_insight", 0xAA55AA),
        RESEARCH("tt2_craftsman_research", 0xAA5500),
        FORESTRY("tt2_craftsman_forestry", 0x55AA55);

        public final String id;
        private final int color;

        Type(String id, int color) {
            this.id = id;
            this.color = color;
        }
    }

    private static final float BASE_DAMAGE_COEFFICIENT = 0.2F;
    private static final float COMBAT_ATTACK_SPEED_MULTIPLIER = 1.5F;
    private static final float FELLING_ATTACK_SPEED_MULTIPLIER = 0.8F;
    private static final float COMBAT_FELLING_ATTACK_SPEED_MULTIPLIER = 1.2F;

    private static ModCraftsmanStaffTemplate forestryModifier;

    private final Type type;

    public ModCraftsmanStaffTemplate(Type type, Item template) {
        super(type.id, type.color);
        this.type = type;
        addAspects(new ModifierAspect.SingleAspect(this), new ModifierAspect.DataAspect(this));
        if (template != null) {
            addItem(template, 1, 1);
        }
        if (type == Type.FORESTRY) {
            forestryModifier = this;
        }
    }

    @Override
    protected boolean canApplyCustom(ItemStack stack) {
        if (stack == null || stack.isEmpty() || TT2Items.CRAFTSMAN_STAFF == null
            || stack.getItem() != TT2Items.CRAFTSMAN_STAFF) {
            return false;
        }
        if (!isIntegration(type)) {
            return true;
        }
        if (type == Type.FORESTRY) {
            return has(stack, Type.RESEARCH) && !has(stack, Type.FORESTRY);
        }
        return !hasAnyIntegration(stack, type);
    }

    @Override
    public void applyEffect(NBTTagCompound rootCompound, NBTTagCompound modifierTag) {
        if (type == Type.RESEARCH && forestryModifier != null
            && TinkerUtil.getIndexInCompoundList(TagUtil.getModifiersTagList(rootCompound), Type.FORESTRY.id) < 0
            && TinkerUtil.getIndexInList(TagUtil.getBaseModifiersTagList(rootCompound), Type.FORESTRY.id) < 0) {
            forestryModifier.apply(rootCompound);
        }

        ToolNBT original = TagUtil.getOriginalToolStats(rootCompound);
        ToolNBT current = TagUtil.getToolStats(rootCompound);

        boolean previousCombat = hasApplied(rootCompound, Type.COMBAT, type);
        boolean previousFelling = hasApplied(rootCompound, Type.FELLING, type);
        float previousCoefficient = damageCoefficient(previousCombat, previousFelling);
        float targetCoefficient = damageCoefficient(
            previousCombat || type == Type.COMBAT,
            previousFelling || type == Type.FELLING
        );

        float previousBaseAttack = original.attack * previousCoefficient / BASE_DAMAGE_COEFFICIENT;
        float targetBaseAttack = original.attack * targetCoefficient / BASE_DAMAGE_COEFFICIENT;
        float otherAttackChanges = current.attack - previousBaseAttack;
        current.attack = targetBaseAttack + otherAttackChanges;

        float previousSpeedMultiplier = attackSpeedMultiplier(previousCombat, previousFelling);
        float targetSpeedMultiplier = attackSpeedMultiplier(
            previousCombat || type == Type.COMBAT,
            previousFelling || type == Type.FELLING
        );
        current.attackSpeedMultiplier = current.attackSpeedMultiplier
            / previousSpeedMultiplier * targetSpeedMultiplier;

        TagUtil.setToolTag(rootCompound, current.get());
    }

    public static boolean has(ItemStack stack, Type type) {
        return stack != null && !stack.isEmpty() && stack.hasTagCompound()
            && TinkerUtil.hasModifier(TagUtil.getTagSafe(stack), type.id);
    }

    public static boolean isIntegration(Type type) {
        return type == Type.NATURE || type == Type.INSIGHT || type == Type.RESEARCH
            || type == Type.FORESTRY;
    }

    public static boolean hasAnyIntegration(ItemStack stack, Type except) {
        for (Type candidate : new Type[]{Type.NATURE, Type.INSIGHT, Type.RESEARCH}) {
            if (candidate != except && has(stack, candidate)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasApplied(NBTTagCompound root, Type wanted, Type currentlyApplying) {
        if (wanted == currentlyApplying) {
            return false;
        }
        NBTTagList modifiers = TagUtil.getModifiersTagList(root);
        return TinkerUtil.getIndexInCompoundList(modifiers, wanted.id) >= 0;
    }

    private static float damageCoefficient(boolean combat, boolean felling) {
        if (combat && felling) {
            return (float) TT2Config.craftsmanStaffCombatFellingDamageCoefficient;
        }
        if (felling) {
            return (float) TT2Config.craftsmanStaffFellingDamageCoefficient;
        }
        return combat ? (float) TT2Config.craftsmanStaffCombatDamageCoefficient
            : BASE_DAMAGE_COEFFICIENT;
    }

    private static float attackSpeedMultiplier(boolean combat, boolean felling) {
        if (combat && felling) {
            return COMBAT_FELLING_ATTACK_SPEED_MULTIPLIER;
        }
        if (combat) {
            return COMBAT_ATTACK_SPEED_MULTIPLIER;
        }
        return felling ? FELLING_ATTACK_SPEED_MULTIPLIER : 1.0F;
    }

}
