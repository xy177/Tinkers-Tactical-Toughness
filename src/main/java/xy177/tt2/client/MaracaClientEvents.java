package xy177.tt2.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import xy177.tt2.network.PacketMaracaAction;
import xy177.tt2.network.PacketMaracaUiState;
import xy177.tt2.network.TT2Network;
import xy177.tt2.tools.Maraca;

import java.lang.reflect.Field;

public class MaracaClientEvents {

    private static final String[] HOVERED_SLOT_FIELDS = {"hoveredSlot", "field_147006_u"};
    private static Field hoveredSlotField;
    private boolean uiOpen;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        boolean open = mc.currentScreen != null;
        if (uiOpen != open) {
            uiOpen = open;
            if (mc.player != null) {
                TT2Network.CHANNEL.sendToServer(new PacketMaracaUiState(open));
            }
        }
    }

    @SubscribeEvent
    public void onGuiMouse(GuiScreenEvent.MouseInputEvent.Pre event) {
        int button = Mouse.getEventButton();
        if (!(event.getGui() instanceof GuiContainer) || button < 0 || !Mouse.getEventButtonState()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        if (player == null) {
            return;
        }

        Slot hovered = getHoveredSlot((GuiContainer) event.getGui());
        if (hovered != null && button == 1 && Maraca.canUseAsPrimary(hovered.getStack())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiKeyboard(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!(event.getGui() instanceof GuiContainer) || !Keyboard.getEventKeyState()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        if (player == null) {
            return;
        }

        Slot hovered = getHoveredSlot((GuiContainer) event.getGui());
        ItemStack hoveredStack = hovered == null ? ItemStack.EMPTY : hovered.getStack();
        boolean dropKey = Keyboard.getEventKey() == mc.gameSettings.keyBindDrop.getKeyCode();
        if (Maraca.isTemporaryCopy(player.inventory.getItemStack())
                || Maraca.isTemporaryCopy(hoveredStack)
                || dropKey && Maraca.canUseAsPrimary(hoveredStack)) {
            event.setCanceled(true);
        }
    }

    private Slot getHoveredSlot(GuiContainer gui) {
        try {
            Field field = hoveredSlotField;
            if (field == null) {
                for (String name : HOVERED_SLOT_FIELDS) {
                    try {
                        field = GuiContainer.class.getDeclaredField(name);
                        field.setAccessible(true);
                        hoveredSlotField = field;
                        break;
                    } catch (NoSuchFieldException ignored) {
                    }
                }
            }
            return field == null ? null : (Slot) field.get(gui);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @SubscribeEvent
    public void onMouse(net.minecraftforge.client.event.MouseEvent event) {
        if (!event.isButtonstate()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        if (player == null || mc.currentScreen != null || !(player.getHeldItemMainhand().getItem() instanceof Maraca)) {
            return;
        }

        ItemStack mainhand = player.getHeldItemMainhand();
        if (!Maraca.canUseAsPrimary(mainhand) || !Maraca.canAct(player, mainhand)) {
            event.setCanceled(true);
            return;
        }

        if (event.getButton() == 0) {
            event.setCanceled(true);
            Entity target = mc.objectMouseOver == null ? null : mc.objectMouseOver.entityHit;
            if (target instanceof EntityLivingBase && target != player) {
                TT2Network.CHANNEL.sendToServer(new PacketMaracaAction(PacketMaracaAction.Action.QUICK, target.getEntityId()));
                mc.playerController.attackEntity(player, target);
                player.isSwingInProgress = false;
            } else {
                TT2Network.CHANNEL.sendToServer(new PacketMaracaAction(PacketMaracaAction.Action.QUICK));
                player.swingArm(EnumHand.MAIN_HAND);
                player.resetCooldown();
            }
        } else if (event.getButton() == 1) {
            event.setCanceled(true);
            if (!Maraca.hasTemporaryOffhandCopy(player)) {
                return;
            }
            int targetId = -1;
            if (mc.objectMouseOver != null) {
                Entity target = mc.objectMouseOver.entityHit;
                if (target instanceof EntityLivingBase && target != player) {
                    targetId = target.getEntityId();
                }
            }
            TT2Network.CHANNEL.sendToServer(new PacketMaracaAction(PacketMaracaAction.Action.SLOW, targetId));
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        if (player == null) {
            return;
        }

        if (isHoldingMaraca(player)) {
            if (mc.gameSettings.keyBindDrop.isPressed()) {
                ItemStack mainhand = player.getHeldItemMainhand();
                if (mainhand.getItem() instanceof Maraca && Maraca.canUseAsPrimary(mainhand)) {
                    TT2Network.CHANNEL.sendToServer(new PacketMaracaAction(PacketMaracaAction.Action.PERFORM));
                    return;
                }
            }

            if (mc.gameSettings.keyBindSwapHands.isPressed()) {
                ItemStack mainhand = player.getHeldItemMainhand();
                if (Maraca.canUseAsPrimary(mainhand) && Maraca.canAct(player, mainhand)
                        && Maraca.hasTemporaryOffhandCopy(player)) {
                    TT2Network.CHANNEL.sendToServer(new PacketMaracaAction(PacketMaracaAction.Action.RESONANCE));
                }
            }
        }
    }

    private boolean isHoldingMaraca(EntityPlayerSP player) {
        return player.getHeldItemMainhand().getItem() instanceof Maraca
            || player.getHeldItemOffhand().getItem() instanceof Maraca;
    }
}

