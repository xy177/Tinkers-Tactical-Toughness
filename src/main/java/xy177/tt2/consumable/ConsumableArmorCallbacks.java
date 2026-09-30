package xy177.tt2.consumable;

import c4.conarm.lib.armor.ArmorModifications;
import c4.conarm.lib.traits.IArmorTrait;
import c4.conarm.lib.traits.IArmorAbility;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import slimeknights.tconstruct.library.modifiers.ModifierNBT;
import slimeknights.tconstruct.library.utils.TinkerUtil;
import xy177.tt2.api.consumable.ConsumableUses;

import static xy177.tt2.consumable.ConsumableCallbacks.call;
import static xy177.tt2.consumable.ConsumableCallbacks.run;

public final class ConsumableArmorCallbacks {
    private ConsumableArmorCallbacks() {}
    public static ArmorModifications getModifications(IArmorTrait t, EntityPlayer p, ArmorModifications m, ItemStack s, DamageSource d, double amount, int slot) {
        return call(t, s, () -> t.getModifications(p, m, s, d, amount, slot), m);
    }
    public static void onItemPickup(IArmorTrait t, ItemStack s, EntityItem item, EntityItemPickupEvent e) {
        run(t, s, () -> t.onItemPickup(s, item, e));
    }
    public static float onHeal(IArmorTrait t, ItemStack s, EntityPlayer p, float original, float current, LivingHealEvent e) {
        return call(t, s, () -> t.onHeal(s, p, original, current, e), current);
    }
    public static float onHurt(IArmorTrait t, ItemStack s, EntityPlayer p, DamageSource d, float original, float current, LivingHurtEvent e) {
        return call(t, s, () -> t.onHurt(s, p, d, original, current, e), current);
    }
    public static float onDamaged(IArmorTrait t, ItemStack s, EntityPlayer p, DamageSource d, float original, float current, LivingDamageEvent e) {
        return call(t, s, () -> t.onDamaged(s, p, d, original, current, e), current);
    }
    public static void onKnockback(IArmorTrait t, ItemStack s, EntityPlayer p, LivingKnockBackEvent e) {
        run(t, s, () -> t.onKnockback(s, p, e));
    }
    public static void onFalling(IArmorTrait t, ItemStack s, EntityPlayer p, LivingFallEvent e) {
        run(t, s, () -> t.onFalling(s, p, e));
    }
    public static void onJumping(IArmorTrait t, ItemStack s, EntityPlayer p, LivingEvent.LivingJumpEvent e) {
        run(t, s, () -> t.onJumping(s, p, e));
    }
    public static void onArmorEquipped(IArmorTrait t, ItemStack s, EntityPlayer p, int slot) {
        run(t, s, () -> t.onArmorEquipped(s, p, slot));
    }
    public static void onArmorRemoved(IArmorTrait t, ItemStack s, EntityPlayer p, int slot) {
        // Removal is cleanup, not a new effect; it must still undo already-applied equipment bonuses.
        t.onArmorRemoved(s, p, slot);
    }
    public static int onArmorDamage(IArmorTrait t, ItemStack s, DamageSource d, int original, int current, EntityPlayer p, int slot) {
        return call(t, s, () -> t.onArmorDamage(s, d, original, current, p, slot), current);
    }
    public static int onArmorHeal(IArmorTrait t, ItemStack s, DamageSource d, int original, int current, EntityPlayer p, int slot) {
        return call(t, s, () -> t.onArmorHeal(s, d, original, current, p, slot), current);
    }
    public static boolean disableRendering(IArmorTrait t, ItemStack s, EntityLivingBase p) {
        return call(t, s, () -> t.disableRendering(s, p), false);
    }
    public static void onAbilityTick(IArmorTrait t, int level, World w, EntityPlayer p) {
        if (ConsumableUses.definition(t.getIdentifier()) == null) { t.onAbilityTick(level, w, p); return; }
        int active = 0;
        for (ItemStack s : p.getArmorInventoryList()) {
            if (ConsumableUses.isPresent(s, t.getIdentifier()) && ConsumableUses.isActive(s, t.getIdentifier())) {
                ModifierNBT data = ModifierNBT.readTag(TinkerUtil.getModifierTag(s, t.getIdentifier()));
                active += t instanceof IArmorAbility ? Math.max(0, ((IArmorAbility) t).getAbilityLevel(data)) : Math.max(1, data.level);
            }
        }
        if (active > 0) t.onAbilityTick(Math.min(level, active), w, p);
    }
}
