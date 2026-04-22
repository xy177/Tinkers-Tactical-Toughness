package xy177.tt2.tools.traits;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.materials.HandleMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.materials.MaterialTypes;
import slimeknights.tconstruct.library.potion.TinkerPotion;
import slimeknights.tconstruct.library.traits.AbstractTrait;
import slimeknights.tconstruct.library.utils.TagUtil;
import xy177.tt2.TT2;
import xy177.tt2.config.TT2Config;

/**
 * 匠魂双节棍内置词条：连击
 *
 * 效果：
 *   · 每次命中：连击层数 +1，每层提升 COMBO_GAIN_PER_HIT 伤害
 *   · 层数上限：floor(handle.modifier × COMBO_CAP_BINDING_MULTIPLIER / COMBO_GAIN_PER_HIT)
 *   · 每次命中刷新持续时间为 COMBO_DECAY_DELAY tick，停止攻击后自然消退
 */
public class TraitNunchakuCombo extends AbstractTrait {

    public static final TinkerPotion comboPotion =
        new TinkerPotion(new net.minecraft.util.ResourceLocation(TT2.MOD_ID, "nunchaku_combo"), false, false);

    public TraitNunchakuCombo() {
        super("nunchaku_combo", TextFormatting.GOLD);
    }

    @Override
    public float damage(ItemStack tool, EntityLivingBase attacker,
                        EntityLivingBase target, float damage,
                        float newDamage, boolean isCritical) {
        int level = comboPotion.getLevel(attacker);
        float bonus = level * TT2Config.nunchakuComboGainPerHit;
        return newDamage * (1.0f + bonus);
    }

    @Override
    public void afterHit(ItemStack tool, EntityLivingBase attacker,
                         EntityLivingBase target, float damageDealt,
                         boolean wasCritical, boolean wasHit) {
        if (!wasHit || attacker.world.isRemote) return;

        int maxLevel = calcMaxLevel(tool);
        int current  = comboPotion.getLevel(attacker);
        int next     = Math.min(current + 1, maxLevel);

        comboPotion.apply(attacker, TT2Config.nunchakuComboDecayDelay, next);

        if (attacker instanceof EntityPlayer) {
            int percent = Math.round(next * TT2Config.nunchakuComboGainPerHit * 100f);
            float multiplier = 1f + next * TT2Config.nunchakuComboGainPerHit;
            ((EntityPlayer) attacker).sendStatusMessage(
                new TextComponentString("连击 " + next + "/" + maxLevel
                    + "  (+" + percent + "%)  伤害x"
                    + String.format("%.2f", multiplier))
                    .setStyle(new Style().setColor(TextFormatting.GOLD)),
                true
            );
        }
    }

    private int calcMaxLevel(ItemStack tool) {
        float handleMod = 2f;
        try {
            net.minecraft.nbt.NBTTagList mats = TagUtil.getBaseMaterialsTagList(tool);
            if (mats.tagCount() >= 3) {
                String handleId = mats.getStringTagAt(2);
                Material mat = TinkerRegistry.getMaterial(handleId);
                if (mat != null && mat != Material.UNKNOWN) {
                    HandleMaterialStats handle = mat.getStats(MaterialTypes.HANDLE);
                    if (handle != null && handle.modifier > 0f) {
                        handleMod = handle.modifier;
                    }
                }
            }
        } catch (Exception ignored) {}

        float gain = TT2Config.nunchakuComboGainPerHit;
        if (gain <= 0f) gain = 0.1f;
        return Math.max(1, (int)(handleMod
            * TT2Config.nunchakuComboCapBindingMultiplier
            / gain));
    }
}
