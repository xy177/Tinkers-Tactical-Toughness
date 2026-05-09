package xy177.tt2.tools;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.materials.MaterialTypes;
import slimeknights.tconstruct.library.tinkering.Category;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.SwordCore;
import slimeknights.tconstruct.library.tools.ToolNBT;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.library.utils.ToolHelper;
import slimeknights.tconstruct.tools.TinkerTools;
import xy177.tt2.TT2;
import xy177.tt2.events.MaracaEvents;

import javax.annotation.Nonnull;
import java.util.List;

public class Maraca extends SwordCore {

    public static final String TAG_TEMP_COPY = "tt2_maraca_temp_copy";
    public static final String TAG_RECORD = "tt2_maraca_record";

    public Maraca() {
        super(
            PartMaterialType.extra(TinkerTools.largePlate),
            new PartMaterialType(TinkerTools.toughBinding, MaterialTypes.HANDLE),
            PartMaterialType.handle(TinkerTools.toolRod)
        );
        addCategory(Category.WEAPON);
        setTranslationKey("tt2.maraca");
    }

    @Override
    public float damagePotential() {
        return 0.75f;
    }

    @Override
    public double attackSpeed() {
        return 1.6d;
    }

    @Override
    protected ToolNBT buildTagData(List<Material> materials) {
        ToolNBT data = buildDefaultTag(materials);
        HandleMaterialStats binding = materials.get(1).getStatsOrUnknown(MaterialTypes.HANDLE);
        data.durability = Math.max(1, Math.round(data.durability * Math.max(0.1f, binding.modifier)));
        data.modifiers = DEFAULT_MODIFIERS;
        return data;
    }

    @Override
    public int[] getRepairParts() {
        return new int[]{0};
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            MaracaEvents events = TT2.proxy.getMaracaEvents();
            if (events != null) {
                events.performSlowFromItem(player, stack, null);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target, EnumHand hand) {
        if (!player.world.isRemote) {
            MaracaEvents events = TT2.proxy.getMaracaEvents();
            if (events != null) {
                events.performSlowFromItem(player, stack, target);
            }
        }
        return true;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.NONE;
    }

    public static boolean isTemporaryCopy(ItemStack stack) {
        return !stack.isEmpty()
            && stack.getItem() instanceof Maraca
            && stack.hasTagCompound()
            && stack.getTagCompound().getBoolean(TAG_TEMP_COPY);
    }

    public static void markTemporaryCopy(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof Maraca)) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        tag.setBoolean(TAG_TEMP_COPY, true);
        stack.setTagCompound(tag);
    }

    public static void clearTemporaryCopy(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof Maraca) || !stack.hasTagCompound()) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        tag.removeTag(TAG_TEMP_COPY);
        if (tag.isEmpty()) {
            stack.setTagCompound(null);
        }
    }

    public static ItemStack createTemporaryCopy(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof Maraca)) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        markTemporaryCopy(copy);
        copy.setCount(1);
        TagUtil.setNoRenameFlag(copy, true);
        return copy;
    }

    public static boolean canUseAsPrimary(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof Maraca && !isTemporaryCopy(stack);
    }

    public static void syncTemporaryOffhand(EntityPlayer player) {
        ItemStack mainhand = player.getHeldItemMainhand();
        ItemStack offhand = player.getHeldItemOffhand();
        if (canUseAsPrimary(mainhand)) {
            if (offhand.isEmpty() || isTemporaryCopy(offhand)) {
                ItemStack updated = createTemporaryCopy(mainhand);
                if (!ItemStack.areItemStacksEqual(updated, offhand)
                    || updated.getItemDamage() != offhand.getItemDamage()
                    || !isTemporaryCopy(offhand)) {
                    player.setHeldItem(EnumHand.OFF_HAND, updated);
                }
            }
        } else if (isTemporaryCopy(offhand)) {
            player.setHeldItem(EnumHand.OFF_HAND, ItemStack.EMPTY);
        }
    }

    public static void onMainhandUpdated(EntityPlayer player) {
        syncTemporaryOffhand(player);
    }

    public static boolean hasTemporaryOffhandCopy(EntityPlayer player) {
        return isTemporaryCopy(player.getHeldItemOffhand());
    }

    public static void clearTemporaryOffhandIfNeeded(EntityPlayer player) {
        if (isTemporaryCopy(player.getHeldItemOffhand()) && !canUseAsPrimary(player.getHeldItemMainhand())) {
            player.setHeldItem(EnumHand.OFF_HAND, ItemStack.EMPTY);
        }
    }

    public static boolean purgeTemporaryCopies(EntityPlayer player) {
        boolean changed = false;
        if (player.openContainer != null) {
            for (Slot slot : player.openContainer.inventorySlots) {
                if (isTemporaryCopy(slot.getStack()) && !isOffhandSlot(player, slot)) {
                    slot.putStack(ItemStack.EMPTY);
                    slot.onSlotChanged();
                    changed = true;
                }
            }
        }

        ItemStack cursor = player.inventory.getItemStack();
        if (isTemporaryCopy(cursor)) {
            player.inventory.setItemStack(ItemStack.EMPTY);
            changed = true;
        }
        return changed;
    }

    private static boolean isOffhandSlot(EntityPlayer player, Slot slot) {
        return slot != null && slot.inventory == player.inventory && slot.getSlotIndex() == 40;
    }

    public static boolean canAct(EntityPlayer player, ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof Maraca)) {
            return false;
        }
        return !ToolHelper.isBroken(stack)
            && player.getCooledAttackStrength(0.5f) >= 1.0f
            && !player.getCooldownTracker().hasCooldown(stack.getItem());
    }

    public static String getRecord(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return "";
        }
        return stack.getTagCompound().getString(TAG_RECORD);
    }

    public static void appendRecord(ItemStack stack, char action) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        String record = tag.getString(TAG_RECORD) + action;
        if (record.length() > 8) {
            record = record.substring(record.length() - 8);
        }
        tag.setString(TAG_RECORD, record);
        stack.setTagCompound(tag);
    }

    public static void clearRecord(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return;
        }
        stack.getTagCompound().setString(TAG_RECORD, "");
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        return false;
    }

    @Override
    public boolean dealDamage(ItemStack stack, EntityLivingBase attacker, Entity target, float damage) {
        if (attacker instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) attacker;
            if (!MaracaEvents.isSlowAttack(player) && !canAct(player, stack)) {
                return false;
            }
        }

        boolean hit = super.dealDamage(stack, attacker, target, damage);
        if (hit && attacker instanceof EntityPlayer && !MaracaEvents.isSlowAttack((EntityPlayer) attacker)) {
            MaracaEvents.recordQuick((EntityPlayer) attacker, stack);
            syncTemporaryOffhand((EntityPlayer) attacker);
        }
        return hit;
    }

    public boolean performSpecialAttack(ItemStack stack, EntityPlayer attacker, EntityLivingBase target,
                                        float damageMultiplier, boolean critical) {
        float baseDamage = ToolHelper.getActualAttack(stack) * damageMultiplier;
        if (baseDamage <= 0.0f) {
            return false;
        }

        float finalDamage = baseDamage;
        NBTTagList traits = TagUtil.getTraitsTagList(stack);
        for (int i = 0; i < traits.tagCount(); i++) {
            ITrait trait = TinkerRegistry.getTrait(traits.getStringTagAt(i));
            if (trait != null) {
                finalDamage = trait.damage(stack, attacker, target, baseDamage, finalDamage, critical);
            }
        }

        boolean hit = target.attackEntityFrom(DamageSource.causePlayerDamage(attacker), finalDamage);
        for (int i = 0; i < traits.tagCount(); i++) {
            ITrait trait = TinkerRegistry.getTrait(traits.getStringTagAt(i));
            if (trait != null) {
                trait.onHit(stack, attacker, target, finalDamage, critical);
                trait.afterHit(stack, attacker, target, finalDamage, critical, hit);
            }
        }

        if (hit) {
            reduceDurabilityOnHit(stack, attacker, finalDamage);
            if (attacker instanceof EntityPlayer) {
                syncTemporaryOffhand((EntityPlayer) attacker);
            }
        }
        return hit;
    }
}
