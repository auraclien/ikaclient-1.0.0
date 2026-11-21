package com.rocket.ikaclient.utils;

import net.minecraft.client.gui.DrawContext;

public class RenderUtils {
    
    // Рисуем прямоугольник с закругленными углами
    public static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        context.fill(x + radius, y, x + width - radius, y + height, color);
        context.fill(x, y + radius, x + width, y + height - radius, color);
        
        // Углы (рисуем круги по краям)
        context.fillEllipse(x + radius, y + radius, radius, radius, color);
        context.fillEllipse(x + width - radius, y + radius, radius, radius, color);
        context.fillEllipse(x + radius, y + height - radius, radius, radius, color);
        context.fillEllipse(x + width - radius, y + height - radius, radius, radius, color);
    }

    // Рисуем тот самый переключатель (Switch) как на скрине
    public static void drawSwitch(DrawContext context, int x, int y, boolean enabled) {
        int width = 40;
        int height = 20;
        int radius = 10;

        // Цвета
        int bgColor = enabled ? 0xFF2ECC71 : 0xFF3A3A3A; // Зеленый или Темно-серый
        int circleColor = 0xFFFFFFFF; // Белый кружок

        // Фон переключателя
        drawRoundedRect(context, x, y, width, height, radius, bgColor);

        // Кружок (анимация позиции)
        int circleX = enabled ? (x + width - radius - 2) : (x + 2);
        context.fillEllipse(circleX, y + radius, 8, 8, circleColor);
    }
}
