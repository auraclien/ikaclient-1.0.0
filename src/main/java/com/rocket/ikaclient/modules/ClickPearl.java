package com.rocket.ikaclient.modules;

import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class ClickPearl extends Module {
    
    public ClickPearl() {
        super("ClickPearl", GLFW.GLFW_KEY_G);
    }

    @Override
    public void onTick() {
        // Логика вызывается из MainClient по бинду
    }

    public void throwPearl() {
        if (mc.player == null || mc.interactionManager == null || mc.getNetworkHandler() == null) return;

        // Ищем слот с эндер-жемчугом в хотбаре (0-8)
        int pearlSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.ENDER_PEARL) {
                pearlSlot = i;
                break;
            }
        }

        if (pearlSlot != -1) {
            // Запоминаем текущий слот
            int previousSlot = mc.player.getInventory().selectedSlot;
            
            // Отправляем пакет, что мы переключились на жемчуг
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(pearlSlot));
            
            // Синхронизируем локально (чтобы игра поняла, что мы держим)
            mc.player.getInventory().selectedSlot = pearlSlot;
            
            // Кидаем
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.swingHand(Hand.MAIN_HAND);
            
            // Отправляем пакет, что вернулись обратно
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
            mc.player.getInventory().selectedSlot = previousSlot;
        }
    }
}
