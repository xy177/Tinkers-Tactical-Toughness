package xy177.tt2.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import xy177.tt2.TT2;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class SpearHeldItemLayer extends LayerHeldItem {

    private static final Field LAYER_RENDERERS = findLayerField();
    private static final Set<RenderPlayer> INSTALLED = Collections.newSetFromMap(
        new IdentityHashMap<RenderPlayer, Boolean>()
    );
    private static boolean warned;

    public SpearHeldItemLayer(RenderLivingBase<?> renderer) {
        super(renderer);
    }

    public static void install(RenderPlayer renderer) {
        if (renderer == null || INSTALLED.contains(renderer)) {
            return;
        }
        if (LAYER_RENDERERS == null) {
            warnOnce("Unable to access the player layer list; third-person Spear item animation is disabled.");
            INSTALLED.add(renderer);
            return;
        }

        try {
            List<?> layers = (List<?>) LAYER_RENDERERS.get(renderer);
            boolean replaced = false;
            for (int i = 0; i < layers.size(); i++) {
                Object layer = layers.get(i);
                if (layer != null && layer.getClass() == LayerHeldItem.class) {
                    @SuppressWarnings("unchecked")
                    List<LayerRenderer<?>> mutable = (List<LayerRenderer<?>>) (List<?>) layers;
                    mutable.set(i, new SpearHeldItemLayer(renderer));
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                warnOnce("The player renderer uses a custom held-item layer; "
                    + "third-person Spear item animation is disabled for that renderer.");
            }
            INSTALLED.add(renderer);
        } catch (IllegalAccessException | RuntimeException exception) {
            warnOnce("Unable to replace the player held-item layer; third-person Spear item animation is disabled.");
            INSTALLED.add(renderer);
        }
    }

    @Override
    public void doRenderLayer(EntityLivingBase living, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw,
                              float headPitch, float scale) {
        boolean rightPrimary = living.getPrimaryHand() == EnumHandSide.RIGHT;
        ItemStack left = rightPrimary ? living.getHeldItemOffhand() : living.getHeldItemMainhand();
        ItemStack right = rightPrimary ? living.getHeldItemMainhand() : living.getHeldItemOffhand();
        if (left.isEmpty() && right.isEmpty()) {
            return;
        }

        GlStateManager.pushMatrix();
        try {
            if (livingEntityRenderer.getMainModel().isChild) {
                GlStateManager.translate(0.0F, 0.75F, 0.0F);
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
            }
            renderHeldItem(living, right, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND,
                EnumHandSide.RIGHT, partialTicks);
            renderHeldItem(living, left, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND,
                EnumHandSide.LEFT, partialTicks);
        } finally {
            GlStateManager.popMatrix();
        }
    }

    private void renderHeldItem(EntityLivingBase living, ItemStack stack,
                                ItemCameraTransforms.TransformType transform,
                                EnumHandSide side, float partialTicks) {
        if (stack.isEmpty()) {
            return;
        }
        GlStateManager.pushMatrix();
        try {
            if (living.isSneaking()) {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }
            translateToHand(side);
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            boolean left = side == EnumHandSide.LEFT;
            GlStateManager.translate((left ? -1.0F : 1.0F) / 16.0F, 0.125F, -0.625F);
            SpearAnimationHooks.applyThirdPersonItem(living, stack, side, partialTicks);
            boolean spear = SpearAnimationHooks.isSpear(stack);
            if (spear) {
                SpearRenderContext.begin();
            }
            try {
                Minecraft.getMinecraft().getItemRenderer().renderItemSide(living, stack, transform, left);
            } finally {
                if (spear) {
                    SpearRenderContext.end();
                }
            }
        } finally {
            GlStateManager.popMatrix();
        }
    }

    private static Field findLayerField() {
        try {
            return ReflectionHelper.findField(RenderLivingBase.class,
                "layerRenderers", "field_177097_h");
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static void warnOnce(String message) {
        if (!warned) {
            warned = true;
            if (TT2.logger != null) {
                TT2.logger.warn(message);
            }
        }
    }
}
