package xy177.tt2.consumable;

import com.google.common.collect.Multimap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.modifiers.TinkerGuiException;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.TagUtil;
import xy177.tt2.api.consumable.ConsumableUses;

import java.util.function.Supplier;

/** Calls arrive through stack-neutral interface-call redirects, not replacement registry objects. */
public final class ConsumableCallbacks {
    private ConsumableCallbacks() {}

    static <T> T call(ITrait trait, ItemStack tool, Supplier<T> effect, T fallback) {
        if (ConsumableUses.definition(trait.getIdentifier()) == null) return effect.get();
        try (ConsumableUses.Action ignored = ConsumableUses.beginAction(tool)) {
            return ConsumableUses.isActive(tool, trait.getIdentifier()) ? effect.get() : fallback;
        }
    }

    static void run(ITrait trait, ItemStack tool, Runnable effect) {
        call(trait, tool, () -> { effect.run(); return null; }, null);
    }

    public static void onUpdate(ITrait t, ItemStack s, World w, Entity e, int slot, boolean selected) {
        run(t, s, () -> t.onUpdate(s, w, e, slot, selected));
    }
    public static void onArmorTick(ITrait t, ItemStack s, World w, EntityPlayer p) {
        run(t, s, () -> t.onArmorTick(s, w, p));
    }
    public static void miningSpeed(ITrait t, ItemStack s, PlayerEvent.BreakSpeed e) {
        run(t, s, () -> t.miningSpeed(s, e));
    }
    public static void beforeBlockBreak(ITrait t, ItemStack s, BlockEvent.BreakEvent e) {
        run(t, s, () -> t.beforeBlockBreak(s, e));
    }
    public static void afterBlockBreak(ITrait t, ItemStack s, World w, IBlockState state, BlockPos pos, EntityLivingBase p, boolean effective) {
        run(t, s, () -> t.afterBlockBreak(s, w, state, pos, p, effective));
    }
    public static void blockHarvestDrops(ITrait t, ItemStack s, BlockEvent.HarvestDropsEvent e) {
        run(t, s, () -> t.blockHarvestDrops(s, e));
    }
    public static boolean isCriticalHit(ITrait t, ItemStack s, EntityLivingBase p, EntityLivingBase target) {
        return call(t, s, () -> t.isCriticalHit(s, p, target), false);
    }
    public static float damage(ITrait t, ItemStack s, EntityLivingBase p, EntityLivingBase target, float damage, float current, boolean critical) {
        return call(t, s, () -> t.damage(s, p, target, damage, current, critical), current);
    }
    public static void onHit(ITrait t, ItemStack s, EntityLivingBase p, EntityLivingBase target, float damage, boolean critical) {
        run(t, s, () -> t.onHit(s, p, target, damage, critical));
    }
    public static float knockBack(ITrait t, ItemStack s, EntityLivingBase p, EntityLivingBase target, float damage, float original, float current, boolean critical) {
        return call(t, s, () -> t.knockBack(s, p, target, damage, original, current, critical), current);
    }
    public static void afterHit(ITrait t, ItemStack s, EntityLivingBase p, EntityLivingBase target, float damage, boolean critical, boolean hit) {
        run(t, s, () -> t.afterHit(s, p, target, damage, critical, hit));
    }
    public static void onBlock(ITrait t, ItemStack s, EntityPlayer p, LivingHurtEvent e) {
        run(t, s, () -> t.onBlock(s, p, e));
    }
    public static void onPlayerHurt(ITrait t, ItemStack s, EntityPlayer p, EntityLivingBase a, LivingHurtEvent e) {
        run(t, s, () -> t.onPlayerHurt(s, p, a, e));
    }
    public static int onToolDamage(ITrait t, ItemStack s, int original, int current, EntityLivingBase e) {
        return call(t, s, () -> t.onToolDamage(s, original, current, e), current);
    }
    public static int onToolHeal(ITrait t, ItemStack s, int original, int current, EntityLivingBase e) {
        return call(t, s, () -> t.onToolHeal(s, original, current, e), current);
    }
    public static void onRepair(ITrait t, ItemStack s, int amount) {
        run(t, s, () -> t.onRepair(s, amount));
    }
    public static void getAttributeModifiers(ITrait t, EntityEquipmentSlot slot, ItemStack s, Multimap<String, AttributeModifier> map) {
        run(t, s, () -> t.getAttributeModifiers(slot, s, map));
    }

    public static void applyEffect(IModifier modifier, NBTTagCompound root, NBTTagCompound tag) {
        ConsumableUses.Definition def = ConsumableUses.definition(modifier.getIdentifier());
        if (def == null) { modifier.applyEffect(root, tag); return; }
        ConsumableUses.state(root, def);
        int remaining = ConsumableUses.remaining(root, def);
        if (remaining > 0) modifier.applyEffect(root, tag);
        else if (modifier instanceof ITrait) {
            // Keep exhausted material traits discoverable without reapplying their passive stats.
            NBTTagList traits = TagUtil.getTraitsTagList(root);
            boolean found = false;
            for (int i = 0; i < traits.tagCount(); i++) if (def.id.equals(traits.getStringTagAt(i))) found = true;
            if (!found) traits.appendTag(new NBTTagString(def.id));
            TagUtil.setTraitsTagList(root, traits);
        }
        tag.setInteger(ConsumableUses.TAG, remaining);
    }

    public static String getTooltip(IModifier modifier, NBTTagCompound tag, boolean detailed) {
        String text = modifier.getTooltip(tag, detailed);
        ConsumableUses.Definition def = ConsumableUses.definition(modifier.getIdentifier());
        if (def == null) return text;
        int count = tag.hasKey(ConsumableUses.TAG, 3) ? tag.getInteger(ConsumableUses.TAG) : def.maximum;
        text = text + "[" + Math.max(0, count) + "]";
        return count == 0 ? TextFormatting.GRAY + TextFormatting.getTextWithoutFormattingCodes(text) : text;
    }

    public static boolean canApply(IModifier modifier, ItemStack stack, ItemStack original) throws TinkerGuiException {
        ConsumableUses.Definition def = ConsumableUses.definition(modifier.getIdentifier());
        if (def != null && def.kind == ConsumableUses.Kind.MATERIAL) return false;
        return modifier.canApply(stack, original);
    }

    public static int repairDamage(ItemStack stack) {
        int damage = stack.getItemDamage();
        return damage == 0 && ConsumableUses.needsRepair(stack) ? 1 : damage;
    }

    public static ItemStack afterRepair(ItemStack result, ItemStack original) {
        if (!result.isEmpty() && TagUtil.getExtraTag(result).getInteger("RepairCount")
            > TagUtil.getExtraTag(original).getInteger("RepairCount")) {
            ConsumableUses.restoreMaterialUsesOnRepair(result);
        }
        return result;
    }
}
