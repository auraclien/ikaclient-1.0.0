package com.rocket.ikaclient.utils;

import net.minecraft.client.gui.DrawContext;

public class RenderUtils {

    // Закруглённый прямоугольник (рисуем построчно, без шейдеров)
    public static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        if (radius < 0) radius = 0;
        if (radius > width / 2) radius = width / 2;
        if (radius > height / 2) radius = height / 2;

        // Тело во всю ширину (между дугами)
        context.fill(x, y + radius, x + width, y + height - radius, color);

        // Верхняя дуга
        for (int i = 0; i < radius; i++) {
            int dy = radius - i;
            int inset = radius - (int) Math.round(Math.sqrt(radius * radius - dy * dy));
            context.fill(x + inset, y + i, x + width - inset, y + i + 1, color);
        }
        // Нижняя дуга
        for (int i = 0; i < radius; i++) {
            int dy = radius - i;
            int inset = radius - (int) Math.round(Math.sqrt(radius * radius - dy * dy));
            context.fill(x + inset, y + height - 1 - i, x + width - inset, y + height - i, color);
        }
    }

    // Круг построчно (для шарика переключателя)
    public static void drawCircle(DrawContext context, int cx, int cy, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            int dx = (int) Math.round(Math.sqrt(radius * radius - dy * dy));
            context.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

    // Тот самый переключатель ON/OFF
    public static void drawSwitch(DrawContext context, int x, int y, boolean enabled) {
        int width = 40;
        int height = 20;
        int radius = 10;

        int bgColor = enabled ? 0xFF2ECC71 : 0xFF3A3A3A;
        int circleColor = 0xFFFFFFFF;

        drawRoundedRect(context, x, y, width, height, radius, bgColor);

        int circleCx = enabled ? (x + width - radius) : (x + radius);
        int circleCy = y + height / 2;
        drawCircle(context, circleCx, circleCy, radius - 2, circleColor);
    }
}
