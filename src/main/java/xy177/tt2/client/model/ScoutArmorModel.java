package xy177.tt2.client.model;

import c4.conarm.client.models.ModelArmorBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;

public class ScoutArmorModel extends ModelArmorBase {

    private final ModelRenderer torsoCore;
    private final ModelRenderer torsoPlate1;
    private final ModelRenderer torsoPlate2;
    private final ModelRenderer leggingBelt;

    public ScoutArmorModel(EntityEquipmentSlot slot) {
        super(slot);
        this.textureWidth = 128;
        this.textureHeight = 128;

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHeadwear = new ModelRenderer(this);
        bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.mirror = true;
        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
        bipedLeftLeg.mirror = true;

        ModelRenderer hoodCore = new ModelRenderer(this, 0, 30);
        hoodCore.setRotationPoint(0.0F, 0.0F, 0.0F);
        hoodCore.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.75F);
        bipedHead.addChild(hoodCore);

        ModelRenderer hoodPlate2 = new ModelRenderer(this, 32, 12);
        hoodPlate2.setRotationPoint(0.0F, 0.0F, 0.0F);
        hoodPlate2.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 1.0F);
        bipedHead.addChild(hoodPlate2);

        ModelRenderer goggleGroup = new ModelRenderer(this);
        goggleGroup.setRotationPoint(0.0F, 24.75F, 0.0F);
        bipedHead.addChild(goggleGroup);

        ModelRenderer goggleLeft = new ModelRenderer(this, 0, 74);
        goggleLeft.setRotationPoint(0.0F, 0.0F, 0.0F);
        goggleLeft.addBox(-4.25F, -33.0F, -6.0F, 4, 4, 2, 0.0F);
        goggleGroup.addChild(goggleLeft);

        ModelRenderer goggleLeftLens = new ModelRenderer(this, 24, 50);
        goggleLeftLens.setRotationPoint(0.0F, 0.0F, 0.0F);
        goggleLeftLens.addBox(-4.25F, -33.0F, -5.5F, 4, 4, 0, 0.0F);
        goggleGroup.addChild(goggleLeftLens);

        ModelRenderer goggleRight = new ModelRenderer(this, 64, 74);
        goggleRight.setRotationPoint(0.0F, 0.0F, 0.0F);
        goggleRight.addBox(0.5F, -33.0F, -6.0F, 4, 4, 2, 0.0F);
        goggleGroup.addChild(goggleRight);

        ModelRenderer goggleRightLens = new ModelRenderer(this, 24, 46);
        goggleRightLens.setRotationPoint(0.0F, 0.0F, 0.0F);
        goggleRightLens.addBox(0.5F, -33.0F, -5.5F, 4, 4, 0, 0.0F);
        goggleGroup.addChild(goggleRightLens);

        ModelRenderer goggleBand = new ModelRenderer(this, 0, 0);
        goggleBand.setRotationPoint(0.0F, 1.0F, 1.0F);
        goggleBand.addBox(-5.0F, -33.0F, -6.0F, 10, 2, 10, 0.2F);
        goggleGroup.addChild(goggleBand);

        torsoCore = new ModelRenderer(this);
        ModelRenderer group7 = torsoCore;
        group7.setRotationPoint(0.0000F, 24.0000F, 0.0000F);
        bipedBody.addChild(group7);

        group7.setTextureOffset(32, 40);
        group7.addBox(-4.0000F, -24.0000F, -2.0000F, 8, 12, 4, 0.7500F);

        torsoPlate2 = new ModelRenderer(this);
        ModelRenderer group8 = torsoPlate2;
        group8.setRotationPoint(0.0000F, 24.0000F, 0.0000F);
        bipedBody.addChild(group8);

        ModelRenderer cube9 = new ModelRenderer(this, 0, 12);
        cube9.setRotationPoint(0.0000F, -1.0000F, 0.0000F);
        cube9.addBox(-5.0000F, -24.0000F, -3.0000F, 10, 12, 6, 0.0000F);
        group8.addChild(cube9);

        torsoPlate1 = new ModelRenderer(this);
        ModelRenderer group10 = torsoPlate1;
        group10.setRotationPoint(0.0000F, 2.6651F, -0.8840F);
        bipedBody.addChild(group10);

        ModelRenderer cube11 = new ModelRenderer(this, 32, 28);
        cube11.setRotationPoint(0.0000F, 21.8349F, 0.8840F);
        cube11.addBox(-5.0000F, -23.0000F, -3.0000F, 10, 6, 6, 0.2000F);
        group10.addChild(cube11);

        ModelRenderer cube12 = new ModelRenderer(this, 24, 56);
        cube12.setRotationPoint(0.0000F, 0.5000F, 0.0000F);
        cube12.addBox(-5.0000F, -2.5000F, -4.0000F, 10, 7, 3, 0.0100F);
        setRotationAngle(cube12, 0.1745329F, 0.0000000F, 0.0000000F);
        group10.addChild(cube12);

        leggingBelt = new ModelRenderer(this);
        leggingBelt.setRotationPoint(0.0000F, 24.0000F, 0.0000F);
        bipedBody.addChild(leggingBelt);

        ModelRenderer beltPlate1 = new ModelRenderer(this, 0, 97);
        beltPlate1.addBox(-5.0000F, -14.2500F, -3.0000F, 10, 2, 6, 0.2000F);
        leggingBelt.addChild(beltPlate1);

        ModelRenderer beltCore = new ModelRenderer(this, 8, 91);
        beltCore.addBox(-1.5000F, -14.7500F, -3.7500F, 3, 3, 1, 0.0000F);
        leggingBelt.addChild(beltCore);

        ModelRenderer rightBoot = new ModelRenderer(this);
        rightBoot.setRotationPoint(0.0000F, 0.0000F, 0.0000F);
        bipedRightLeg.addChild(rightBoot);

        ModelRenderer cube13 = new ModelRenderer(this, 66, 51);
        cube13.setRotationPoint(0.0000F, 0.0000F, 0.0000F);
        cube13.addBox(-2.0000F, 8.0000F, -2.0000F, 4, 4, 4, 0.7500F);
        rightBoot.addChild(cube13);

        ModelRenderer cube14 = new ModelRenderer(this, 56, 51);
        cube14.setRotationPoint(0.0000F, 8.2500F, -2.7500F);
        cube14.addBox(-2.0000F, -2.0000F, 0.0000F, 4, 4, 1, 0.0000F);
        setRotationAngle(cube14, 0.2181662F, 0.0000000F, 0.0000000F);
        rightBoot.addChild(cube14);

        ModelRenderer cube15 = new ModelRenderer(this, 66, 67);
        cube15.setRotationPoint(0.0000F, 0.0000F, 0.0000F);
        cube15.addBox(-2.0000F, 9.0000F, -2.0000F, 4, 3, 4, 1.0000F);
        rightBoot.addChild(cube15);

        ModelRenderer leftBoot = new ModelRenderer(this);
        leftBoot.setRotationPoint(0.0000F, 0.0000F, 0.0000F);
        bipedLeftLeg.addChild(leftBoot);

        ModelRenderer cube16 = new ModelRenderer(this, 66, 59);
        cube16.setRotationPoint(0.0000F, 0.0000F, 0.0000F);
        cube16.addBox(-2.0000F, 8.0000F, -2.0000F, 4, 4, 4, 0.7500F);
        leftBoot.addChild(cube16);

        ModelRenderer cube17 = new ModelRenderer(this, 76, 74);
        cube17.setRotationPoint(0.0000F, 8.2500F, -2.7500F);
        cube17.addBox(-2.0000F, -2.0000F, 0.0000F, 4, 4, 1, 0.0000F);
        setRotationAngle(cube17, 0.2181662F, 0.0000000F, 0.0000000F);
        leftBoot.addChild(cube17);

        ModelRenderer cube18 = new ModelRenderer(this, 48, 72);
        cube18.setRotationPoint(0.0000F, 0.0000F, 0.0000F);
        cube18.addBox(-2.0000F, 9.0000F, -2.0000F, 4, 3, 4, 1.0000F);
        leftBoot.addChild(cube18);

        // In the source BB model, left_arm is the wearer's right arm and right_arm is the wearer's left arm.
        ModelRenderer bbLeftArmOnWearerRight = new ModelRenderer(this);
        bbLeftArmOnWearerRight.setRotationPoint(-1.0000F, 4.0000F, 0.0000F);
        bipedRightArm.addChild(bbLeftArmOnWearerRight);

        bbLeftArmOnWearerRight.setTextureOffset(50, 56);
        bbLeftArmOnWearerRight.addBox(-2.0000F, -6.0000F, -2.0000F, 4, 12, 4, 0.7500F);

        bbLeftArmOnWearerRight.setTextureOffset(40, 0);
        bbLeftArmOnWearerRight.addBox(-3.0000F, 1.0000F, -3.0000F, 6, 6, 6, 0.0000F);
        setRotationAngle(bbLeftArmOnWearerRight, 0.0000000F, 3.1415927F, 0.0000000F);

        ModelRenderer bbRightArmOnWearerLeft = new ModelRenderer(this);
        bbRightArmOnWearerLeft.setRotationPoint(1.0000F, 4.0000F, 0.0000F);
        bipedLeftArm.addChild(bbRightArmOnWearerLeft);

        bbRightArmOnWearerLeft.setTextureOffset(0, 58);
        bbRightArmOnWearerLeft.addBox(-2.0000F, -6.0000F, -2.0000F, 4, 12, 4, 0.7500F);

        bbRightArmOnWearerLeft.setTextureOffset(0, 46);
        bbRightArmOnWearerLeft.addBox(-3.0000F, 1.0000F, -3.0000F, 6, 6, 6, 0.0000F);
        setRotationAngle(bbRightArmOnWearerLeft, 0.0000000F, 3.1415927F, 0.0000000F);

        ModelRenderer bbLeftShoulderOnWearerRight = new ModelRenderer(this);
        bbLeftShoulderOnWearerRight.setRotationPoint(-1.2500F, -0.7500F, 0.0000F);
        bipedRightArm.addChild(bbLeftShoulderOnWearerRight);

        ModelRenderer cube22 = new ModelRenderer(this, 56, 40);
        cube22.setRotationPoint(0.0000F, 0.0000F, 0.0000F);
        cube22.mirror = true;
        cube22.addBox(-3.0000F, -2.5000F, -3.0000F, 6, 5, 6, 0.0000F);
        setRotationAngle(cube22, 0.0000000F, 0.0000000F, -0.1308997F);
        bbLeftShoulderOnWearerRight.addChild(cube22);

        ModelRenderer cube23 = new ModelRenderer(this, 64, 32);
        cube23.setRotationPoint(0.0000F, 0.0000F, 0.0000F);
        cube23.mirror = true;
        cube23.addBox(-3.0000F, 0.5000F, -3.0000F, 6, 2, 6, 0.2000F);
        setRotationAngle(cube23, 0.0000000F, 0.0000000F, -0.1308997F);
        bbLeftShoulderOnWearerRight.addChild(cube23);

        ModelRenderer bbRightShoulderOnWearerLeft = new ModelRenderer(this);
        bbRightShoulderOnWearerLeft.setRotationPoint(1.2500F, -0.7500F, 0.0000F);
        bipedLeftArm.addChild(bbRightShoulderOnWearerLeft);

        ModelRenderer cube25 = new ModelRenderer(this, 56, 40);
        cube25.setRotationPoint(0.0000F, 0.0000F, 0.0000F);
        cube25.addBox(-3.0000F, -2.5000F, -3.0000F, 6, 5, 6, 0.0000F);
        setRotationAngle(cube25, 0.0000000F, 0.0000000F, 0.1308997F);
        bbRightShoulderOnWearerLeft.addChild(cube25);

        ModelRenderer cube26 = new ModelRenderer(this, 64, 32);
        cube26.setRotationPoint(0.0000F, 0.0000F, 0.0000F);
        cube26.addBox(-3.0000F, 0.5000F, -3.0000F, 6, 2, 6, 0.2000F);
        setRotationAngle(cube26, 0.0000000F, 0.0000000F, 0.1308997F);
        bbRightShoulderOnWearerLeft.addChild(cube26);

        ModelRenderer rightLegging = new ModelRenderer(this);
        rightLegging.setRotationPoint(0.0000F, 0.0000F, 0.0000F);
        bipedRightLeg.addChild(rightLegging);

        ModelRenderer cube27 = new ModelRenderer(this, 32, 66);
        cube27.setRotationPoint(0.0000F, 0.0000F, 0.0000F);
        cube27.addBox(-2.0000F, 0.0000F, -2.0000F, 4, 12, 4, 0.5000F);
        rightLegging.addChild(cube27);

        ModelRenderer cube28 = new ModelRenderer(this, 64, 16);
        cube28.setRotationPoint(0.0000F, 0.0000F, 0.0000F);
        cube28.addBox(-2.0000F, 0.0000F, -2.0000F, 4, 12, 4, 0.7500F);
        rightLegging.addChild(cube28);

        ModelRenderer leftLegging = new ModelRenderer(this);
        leftLegging.setRotationPoint(0.0000F, 0.0000F, 0.0000F);
        bipedLeftLeg.addChild(leftLegging);

        ModelRenderer cube29 = new ModelRenderer(this, 64, 0);
        cube29.setRotationPoint(0.0000F, 0.0000F, 0.0000F);
        cube29.addBox(-2.0000F, 0.0000F, -2.0000F, 4, 12, 4, 0.5000F);
        leftLegging.addChild(cube29);

        ModelRenderer cube30 = new ModelRenderer(this, 16, 66);
        cube30.setRotationPoint(0.0000F, 0.0000F, 0.0000F);
        cube30.addBox(-2.0000F, 0.0000F, -2.0000F, 4, 12, 4, 0.7500F);
        leftLegging.addChild(cube30);

        bipedHeadwear.showModel = false;
        bipedHead.showModel = slot == EntityEquipmentSlot.HEAD;
        bipedBody.showModel = slot == EntityEquipmentSlot.CHEST || slot == EntityEquipmentSlot.LEGS;
        bipedRightArm.showModel = slot == EntityEquipmentSlot.CHEST;
        bipedLeftArm.showModel = slot == EntityEquipmentSlot.CHEST;
        bipedRightLeg.showModel = slot == EntityEquipmentSlot.LEGS || slot == EntityEquipmentSlot.FEET;
        bipedLeftLeg.showModel = slot == EntityEquipmentSlot.LEGS || slot == EntityEquipmentSlot.FEET;
        torsoCore.showModel = slot == EntityEquipmentSlot.CHEST;
        torsoPlate1.showModel = slot == EntityEquipmentSlot.CHEST;
        torsoPlate2.showModel = slot == EntityEquipmentSlot.CHEST;
        leggingBelt.showModel = slot == EntityEquipmentSlot.LEGS;
        rightBoot.showModel = slot == EntityEquipmentSlot.FEET;
        leftBoot.showModel = slot == EntityEquipmentSlot.FEET;
        rightLegging.showModel = slot == EntityEquipmentSlot.LEGS;
        leftLegging.showModel = slot == EntityEquipmentSlot.LEGS;
    }

    private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
        bipedRightLeg.rotationPointX = -1.9F;
        bipedLeftLeg.rotationPointX = 1.9F;
    }
}
