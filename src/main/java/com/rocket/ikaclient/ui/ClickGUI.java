package com.rocket.ikaclient.ui;

import com.rocket.ikaclient.modules.Module;
import com.rocket.ikaclient.modules.ModuleManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ClickGUI extends Screen {
    private int currentTab = 0; // 0=Combat, 1=Movement, 2=Render, 3=Misc
    private final String[] tabs = {"Combat", "Movement", "Render", "Misc"};

    public ClickGUI() {
        super(Text.literal("Ika Client Menu"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Темный фон на весь экран
        context.fill(0, 0, this.width, this.height, 0x80000000);
        
        int startX = this.width / 2 - 200;
        int startY = this.height / 2 - 120;
        int width = 400;
        int height = 240;

        // Фон меню
        drawRoundedRect(context, startX, startY, width, height, 8, 0xFF1E1E1E);
        // Заголовок
        context.drawText(textRenderer, "§lIka Client", startX + 10, startY + 8, 0xFFFFFFFF, true);
        context.fill(startX, startY + 25, startX + width, startY + 26, 0xFF333333);

        // Рисуем вкладки слева
        int tabY = startY + 40;
        for (int i = 0; i < tabs.length; i++) {
            boolean selected = (currentTab == i);
            int tabColor = selected ? 0xFF00AAFF : 0xFF333333;
            
            // Кнопка вкладки
            drawRoundedRect(context, startX + 10, tabY, 80, 20, 4, tabColor);
            context.drawText(textRenderer, tabs[i], startX + 15, tabY + 6, 0xFFFFFFFF, true);
            tabY += 25;
        }

        // Разделитель
        context.fill(startX + 100, startY + 26, startX + 101, startY + height, 0xFF333333);

        // Рисуем контент вкладки (сетка модулей)
        int modX = startX + 110;
        int modY = startY + 40;
        int col = 0;

        for (Module module : ModuleManager.modules) {
            // Фильтруем модули по вкладкам (простая логика)
            if (currentTab == 0 && !module.name.equals("KillAura")) continue;
            if (currentTab == 1 && !module.name.equals("FullBridge") && !module.name.equals("AutoSprint")) continue;
            if (currentTab == 2 && !module.name.equals("ESP")) continue;

            // Рисуем плашку модуля
            drawRoundedRect(context, modX, modY, 135, 30, 6, 0xFF2A2A2A);
            
            // Название
            context.drawText(textRenderer, module.name, modX + 8, modY + 6, 0xFFFFFFFF, true);
            
            // Кнопка вкл/выкл (скругленная)
            int toggleX = modX + 100;
            int toggleY = modY + 8;
            int toggleColor = module.enabled ? 0xFF00FF00 : 0xFF555555;
            drawRoundedRect(context, toggleX, toggleY, 25, 12, 6, toggleColor);
            
            // Кружок внутри кнопки (имитация переключателя)
            int circleX = module.enabled ? toggleX + 13 : toggleX + 1;
            context.fill(circleX, toggleY + 1, circleX + 10, toggleY + 11, 0xFFFFFFFF);

            modY += 40;
            col++;
            if (col == 2) { // 2 колонки
                modX += 145;
                modY = startY + 40;
                col = 0;
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int startX = this.width / 2 - 200;
        int startY = this.height / 2 - 120;

        // Клик по вкладкам
        int tabY = startY + 40;
        for (int i = 0; i < tabs.length; i++) {
            if (mouseX >= startX + 10 && mouseX <= startX + 90 && mouseY >= tabY && mouseY <= tabY + 20) {
                currentTab = i;
                return true;
            }
            tabY += 25;
        }

        // Клик по модулям
        int modX = startX + 110;
        int modY = startY + 40;
        int col = 0;

        for (Module module : ModuleManager.modules) {
            if (currentTab == 0 && !module.name.equals("KillAura")) continue;
            if (currentTab == 1 && !module.name.equals("FullBridge") && !module.name.equals("AutoSprint")) continue;
            if (currentTab == 2 && !module.name.equals("ESP")) continue;

            if (mouseX >= modX && mouseX <= modX + 135 && mouseY >= modY && mouseY <= modY + 30) {
                module.toggle();
                return true;
            }

            modY += 40;
            col++;
            if (col == 2) {
                modX += 145;
                modY = startY + 40;
                col = 0;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    // Вспомогательный метод для рисования скругленных прямоугольников
    private void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        // Рисуем центральный крест
        context.fill(x + radius, y, x + width - radius, y + height, color);
        context.fill(x, y + radius, x + width, y + height - radius, color);
        // Рисуем углы (примитивно, но работает без текстур)
        context.fill(x + radius, y + radius, x + width - radius, y + height - radius, color);
        // Закрашиваем оставшиеся пиксели (упрощенно)
        context.fill(x + 2, y + 2, x + width - 2, y + height - 2, color);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
