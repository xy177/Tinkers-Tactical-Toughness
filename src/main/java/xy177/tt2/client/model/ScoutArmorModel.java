package xy177.tt2.client.model;

import c4.conarm.client.models.ModelArmorBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.inventory.EntityEquipmentSlot;

public class ScoutArmorModel extends ModelArmorBase {

    public ScoutArmorModel(EntityEquipmentSlot slot) {
        super(slot);
        this.textureWidth = 64;
        this.textureHeight = 64;

        bipedHead = new ModelRenderer(this, 0, 32);
        bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);

        bipedBody = new ModelRenderer(this, 16, 48);
        bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, 0.25F);

        bipedRightArm = new ModelRenderer(this, 40, 48);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.25F);

        bipedLeftArm = new ModelRenderer(this, 40, 48);
        bipedLeftArm.mirror = true;
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.25F);

        bipedRightLeg = new ModelRenderer(this, 0, 48);
        bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
        bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.25F);

        bipedLeftLeg = new ModelRenderer(this, 0, 48);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
        bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, 0.25F);

        bipedHeadwear.showModel = false;
        bipedHead.showModel = slot == EntityEquipmentSlot.HEAD;
        bipedBody.showModel = slot == EntityEquipmentSlot.CHEST || slot == EntityEquipmentSlot.LEGS;
        bipedRightArm.showModel = slot == EntityEquipmentSlot.CHEST;
        bipedLeftArm.showModel = slot == EntityEquipmentSlot.CHEST;
        bipedRightLeg.showModel = slot == EntityEquipmentSlot.LEGS || slot == EntityEquipmentSlot.FEET;
        bipedLeftLeg.showModel = slot == EntityEquipmentSlot.LEGS || slot == EntityEquipmentSlot.FEET;
    }
}
