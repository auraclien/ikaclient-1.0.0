package com.rocket.ikaclient.modules;

import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class ClickPearl extends Module {
    private int previousSlot = -1;

    public ClickPearl() {
        super("ClickPearl", GLFW.GLFW_KEY_G); // По умолчанию на G
    }

    @Override
    public void onTick() {
        // Модуль работает по бинду, поэтому в onTick мы ничего не делаем.
        // Вся логика будет в MainClient при нажатии кнопки.
    }

    // Этот метод вызывается из MainClient, когда нажата клавиша бинда
    public void throwPearl() {
        if (mc.player == null || mc.interactionManager == null) return;

        // Ищем слот с эндер-жемчугом (в хотбаре)
        int pearlSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.ENDER_PEARL) {
                pearlSlot = i;
                break;
            }
        }

        if (pearlSlot != -1) {
            // Запоминаем текущий слот
            previousSlot = mc.player.getInventory().selectedSlot;
            
            // Переключаемся на жемчуг
            mc.player.getInventory().selectedSlot = pearlSlot;
            
            // Кидаем
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.swingHand(Hand.MAIN_HAND);
            
            // Возвращаем обратно
            mc.player.getInventory().selectedSlot = previousSlot;
        }
    }
}
