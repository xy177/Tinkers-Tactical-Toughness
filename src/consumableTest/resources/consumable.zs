#loader contenttweaker
import mods.contenttweaker.tconstruct.TraitBuilder;
import mods.contenttweaker.conarm.ArmorTraitBuilder;
import mods.tt2.ConsumableUses;

val charged = TraitBuilder.create("tt2_test_script");
charged.color = 0x55CCEE;
charged.localizedName = "Script charge";
charged.maxLevel = 1;
charged.countPerLevel = 1;
charged.setConsumable(2, "modifier");
charged.addItem(<item:minecraft:record_13>, 1, 1);
charged.calcDamage = function(trait, tool, attacker, target, baseDamage, currentDamage, critical) {
    return currentDamage + 4.0;
};
charged.afterHit = function(trait, tool, attacker, target, damage, critical, hit) {
    if (hit) ConsumableUses.tryConsume(tool, "tt2_test_script", 1);
};
charged.register();

val materialCharge = TraitBuilder.create("tt2_test_script_material");
materialCharge.color = 0x66CC66;
materialCharge.localizedName = "Script material charge";
materialCharge.setConsumable(5);
materialCharge.calcDamage = function(trait, tool, attacker, target, baseDamage, currentDamage, critical) {
    return currentDamage + 1.0;
};
materialCharge.afterBlockBreak = function(trait, tool, world, block, pos, miner, effective) {
    if (effective) {
        ConsumableUses.tryUse(tool, "tt2_test_script_material", 1, function(currentTool) {
            return true;
        });
    }
};
materialCharge.register();
val armorCharge = ArmorTraitBuilder.create("tt2_test_script_armor");
armorCharge.setConsumable(4);
armorCharge.register();
print("TT2 consumable test traits registered");
