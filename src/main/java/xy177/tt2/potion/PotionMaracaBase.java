package xy177.tt2.potion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import slimeknights.tconstruct.library.materials.Material;
import xy177.tt2.init.TT2Items;

import java.util.Arrays;

public abstract class PotionMaracaBase extends Potion {

    private final Material headMaterial;
    private final Material bindingMaterial;
    private final Material handleMaterial;
    private ItemStack iconStack = ItemStack.EMPTY;

    protected PotionMaracaBase(String name, int color, Material headMaterial, Material bindingMaterial, Material handleMaterial) {
        super(false, color);
        setPotionName(name);
        this.headMaterial = headMaterial;
        this.bindingMaterial = bindingMaterial;
        this.handleMaterial = handleMaterial;
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }

    @Override
    public boolean hasStatusIcon() {
        return false;
    }

    @Override
    public void renderInventoryEffect(PotionEffect effect, Gui gui, int x, int y, float z) {
        renderMaracaIcon(x + 6, y + 7, 1.125F, 1.0F);
    }

    @Override
    public void renderHUDEffect(PotionEffect effect, Gui gui, int x, int y, float z, float alpha) {
        renderMaracaIcon(x + 3, y + 3, 1.125F, alpha);
    }

    private ItemStack getIconStack() {
        if (iconStack.isEmpty() && TT2Items.MARACA != null) {
            iconStack = TT2Items.MARACA.buildItemForRendering(Arrays.asList(headMaterial, bindingMaterial, handleMaterial));
        }
        return iconStack;
    }

    private void renderMaracaIcon(int x, int y, float scale, float alpha) {
        ItemStack stack = getIconStack();
        if (stack.isEmpty()) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        GlStateManager.translate(x, y, 0.0F);
        GlStateManager.scale(scale, scale, 1.0F);
        RenderHelper.enableGUIStandardItemLighting();
        Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}

