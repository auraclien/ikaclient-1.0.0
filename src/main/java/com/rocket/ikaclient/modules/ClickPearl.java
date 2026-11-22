package com.rocket.ikaclient.modules;

import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

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
            // Получаем текущий слот через Reflection (так как он private)
            int previousSlot = getSelectedSlot();
            
            // Отправляем пакет, что мы переключились на жемчуг
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(pearlSlot));
            
            // Меняем слот локально через Reflection
            setSelectedSlot(pearlSlot);
            
            // Кидаем
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.swingHand(Hand.MAIN_HAND);
            
            // Возвращаем обратно
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
            setSelectedSlot(previousSlot);
        }
    }

    // Метод для получения selectedSlot через Reflection
    private int getSelectedSlot() {
        try {
            Field field = mc.player.getInventory().getClass().getField("selectedSlot");
            field.setAccessible(true);
            return field.getInt(mc.player.getInventory());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Метод для установки selectedSlot через Reflection
    private void setSelectedSlot(int slot) {
        try {
            Field field = mc.player.getInventory().getClass().getField("selectedSlot");
            field.setAccessible(true);
            field.setInt(mc.player.getInventory(), slot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
