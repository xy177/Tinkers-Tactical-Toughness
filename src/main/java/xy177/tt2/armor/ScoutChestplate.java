package xy177.tt2.armor;

import c4.conarm.common.ConstructsRegistry;
import c4.conarm.lib.materials.ArmorMaterialType;
import net.minecraft.inventory.EntityEquipmentSlot;

public class ScoutChestplate extends ScoutArmorCore {

    public ScoutChestplate() {
        super(EntityEquipmentSlot.CHEST, "scout", ArmorMaterialType.core(ConstructsRegistry.chestCore));
    }
}
