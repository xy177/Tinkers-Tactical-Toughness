package xy177.tt2.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xy177.tt2.TT2;
import xy177.tt2.compat.CraftsmanStaffCompat;
import xy177.tt2.modifiers.ModCraftsmanStaffTemplate;
import xy177.tt2.tools.CraftsmanStaff;

import java.lang.reflect.Method;

/** Botania-style mode text and wand target HUD without linking Botania classes at load time. */
public final class CraftsmanStaffNatureClientEvents {

    private static final String[] MODE_KEYS = {
        "message.tt2.craftsman_staff.nature_mode.function",
        "message.tt2.craftsman_staff.nature_mode.bind",
        "message.tt2.craftsman_staff.nature_mode.mana_blaster"
    };

    private static int modeTicks;
    private static int shownMode;
    private static boolean wandHudReflectionFailed;

    public static void showMode(int mode) {
        shownMode = Math.max(0, Math.min(MODE_KEYS.length - 1, mode));
        modeTicks = 25;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && modeTicks > 0 && !Minecraft.getMinecraft().isGamePaused()) {
            modeTicks--;
        }
    }

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        Minecraft minecraft = Minecraft.getMinecraft();
        ItemStack staff = findNatureStaff(minecraft.player);
        if (staff == null) {
            return;
        }
        if (modeTicks > 0) {
            renderMode(minecraft, event.getResolution());
        }
        if (CraftsmanStaffCompat.getNatureMode(staff) != CraftsmanStaffCompat.NATURE_MODE_MANA_BLASTER) {
            renderWandTargetHud(minecraft, event.getResolution());
        }
    }

    private static ItemStack findNatureStaff(EntityPlayer player) {
        if (player == null) {
            return null;
        }
        ItemStack main = player.getHeldItemMainhand();
        if (isNatureStaff(main)) {
            return main;
        }
        ItemStack off = player.getHeldItemOffhand();
        return isNatureStaff(off) ? off : null;
    }

    private static boolean isNatureStaff(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof CraftsmanStaff
            && ModCraftsmanStaffTemplate.has(stack, ModCraftsmanStaffTemplate.Type.NATURE);
    }

    private static void renderMode(Minecraft minecraft, ScaledResolution resolution) {
        int alpha = Math.min(255, modeTicks * 256 / 10);
        int color = 0x00CC00 | alpha << 24;
        String text = I18n.format(MODE_KEYS[shownMode]);
        float x = (resolution.getScaledWidth() - minecraft.fontRenderer.getStringWidth(text)) / 2.0F;
        float y = resolution.getScaledHeight() - 70.0F;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        minecraft.fontRenderer.drawStringWithShadow(text, x, y, color);
        GlStateManager.disableBlend();
    }

    private static void renderWandTargetHud(Minecraft minecraft, ScaledResolution resolution) {
        RayTraceResult hit = minecraft.objectMouseOver;
        if (wandHudReflectionFailed || hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK
            || minecraft.world == null) {
            return;
        }
        Block block = minecraft.world.getBlockState(hit.getBlockPos()).getBlock();
        try {
            Class<?> wandHud = Class.forName("vazkii.botania.api.wand.IWandHUD", false,
                block.getClass().getClassLoader());
            if (!wandHud.isInstance(block)) {
                return;
            }
            Method render = wandHud.getMethod("renderHUD", Minecraft.class, ScaledResolution.class,
                net.minecraft.world.World.class, net.minecraft.util.math.BlockPos.class);
            render.invoke(block, minecraft, resolution, minecraft.world, hit.getBlockPos());
        } catch (ReflectiveOperationException failure) {
            wandHudReflectionFailed = true;
            if (TT2.logger != null) {
                TT2.logger.warn("Could not render the Botania wand HUD for the Tinker's Staff", failure);
            }
        }
    }
}
