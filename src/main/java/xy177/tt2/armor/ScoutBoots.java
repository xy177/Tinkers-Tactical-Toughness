package xy177.tt2.armor;

import c4.conarm.common.ConstructsRegistry;
import c4.conarm.lib.materials.ArmorMaterialType;
import net.minecraft.inventory.EntityEquipmentSlot;

public class ScoutBoots extends ScoutArmorCore {

    public ScoutBoots() {
        super(EntityEquipmentSlot.FEET, "scout", ArmorMaterialType.core(ConstructsRegistry.bootsCore));
    }
}
