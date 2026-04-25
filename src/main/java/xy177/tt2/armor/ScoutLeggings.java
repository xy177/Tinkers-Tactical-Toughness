package xy177.tt2.armor;

import c4.conarm.common.ConstructsRegistry;
import c4.conarm.lib.materials.ArmorMaterialType;
import net.minecraft.inventory.EntityEquipmentSlot;

public class ScoutLeggings extends ScoutArmorCore {

    public ScoutLeggings() {
        super(EntityEquipmentSlot.LEGS, "scout", ArmorMaterialType.core(ConstructsRegistry.leggingsCore));
    }
}
