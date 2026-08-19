package xy177.tt2.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.utils.ToolHelper;
import xy177.tt2.compat.CriticalHitEventCompat;

@Mixin(value = ToolHelper.class, remap = false)
public abstract class ToolHelperMixin {

    @ModifyConstant(
        method = "attackEntity(Lnet/minecraft/item/ItemStack;Lslimeknights/tconstruct/library/tools/ToolCore;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;Z)Z",
        constant = @Constant(floatValue = 1.5F),
        require = 0,
        expect = 1,
        remap = false
    )
    private static float tt2$fireForgeCriticalHitEvent(
        float tinkerMultiplier,
        ItemStack tool,
        ToolCore toolCore,
        EntityLivingBase attacker,
        Entity target
    ) {
        return CriticalHitEventCompat.resolveDamageModifier(tinkerMultiplier, attacker, target);
    }
}
