package xy177.tt2.armor;

import c4.conarm.common.ConstructsRegistry;
import c4.conarm.lib.materials.ArmorMaterialType;
import net.minecraft.inventory.EntityEquipmentSlot;

public class ScoutHelmet extends ScoutArmorCore {

    public ScoutHelmet() {
        super(EntityEquipmentSlot.HEAD, "scout", ArmorMaterialType.core(ConstructsRegistry.helmetCore));
    }
}
