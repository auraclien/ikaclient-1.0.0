package com.rocket.ikaclient.ui;

import com.rocket.ikaclient.modules.Module;
import com.rocket.ikaclient.modules.ModuleManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ClickGUI extends Screen {
    private int currentTab = 0;
    private final String[] tabs = {"Combat", "Movement", "Render", "Misc"};

    public ClickGUI() {
        super(Text.literal("Ika Client Menu"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Затемняем фон
        context.fill(0, 0, this.width, this.height, 0x80000000);
        
        int startX = this.width / 2 - 200;
        int startY = this.height / 2 - 120;
        int width = 400;
        int height = 240;

        // Фон меню
        context.fill(startX, startY, startX + width, startY + height, 0xFF1E1E1E);
        
        // Верхняя панель
        context.fill(startX, startY, startX + width, startY + 25, 0xFF2A2A2A);
        context.drawText(textRenderer, "§lIka Client", startX + 10, startY + 8, 0xFFFFFFFF, true);
        
        context.fill(startX, startY + 25, startX + width, startY + 26, 0xFF333333);

        // Вкладки слева
        int tabY = startY + 35;
        for (int i = 0; i < tabs.length; i++) {
            boolean selected = (currentTab == i);
            int tabColor = selected ? 0xFF00AAFF : 0xFF2A2A2A;
            
            context.fill(startX + 5, tabY, startX + 90, tabY + 20, tabColor);
            context.drawText(textRenderer, tabs[i], startX + 10, tabY + 6, 0xFFFFFFFF, true);
            tabY += 25;
        }

        context.fill(startX + 95, startY + 26, startX + 96, startY + height, 0xFF333333);

        // Модули справа
        int modX = startX + 105;
        int modY = startY + 35;
        int col = 0;

        for (Module module : ModuleManager.modules) {
            if (currentTab == 0 && !module.name.equals("KillAura")) continue;
            if (currentTab == 1 && !module.name.equals("FullBridge") && !module.name.equals("AutoSprint")) continue;
            if (currentTab == 2 && !module.name.equals("ESP")) continue;
            if (currentTab == 3) continue;

            // Плашка модуля
            context.fill(modX, modY, modX + 135, modY + 30, 0xFF2A2A2A);
            context.drawText(textRenderer, module.name, modX + 8, modY + 11, 0xFFFFFFFF, true);
            
            // Рисуем овальную кнопку (тумблер)
            drawToggle(context, modX + 100, modY + 9, module.enabled);

            modY += 40;
            col++;
            if (col == 2) {
                modX += 145;
                modY = startY + 35;
                col = 0;
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    // Метод для рисования овальных кнопок (без вылетов)
    private void drawToggle(DrawContext context, int x, int y, boolean enabled) {
        int bgColor = enabled ? 0xFF00FF00 : 0xFF555555;
        int circleColor = 0xFFFFFFFF;
        int width = 26;
        int height = 14;
        int radius = height / 2;

        // Фон кнопки (овал)
        context.fill(x + radius, y, x + width - radius, y + height, bgColor);
        drawCircle(context, x + radius, y + radius, radius, bgColor);
        drawCircle(context, x + width - radius, y + radius, radius, bgColor);

        // Кружок внутри
        int circleX = enabled ? x + width - radius : x + radius;
        drawCircle(context, circleX, y + radius, radius - 2, circleColor);
    }

    // Безопасный круг
    private void drawCircle(DrawContext context, int centerX, int centerY, int radius, int color) {
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                if (i * i + j * j <= radius * radius) {
                    context.fill(centerX + i, centerY + j, centerX + i + 1, centerY + j + 1, color);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int startX = this.width / 2 - 200;
        int startY = this.height / 2 - 120;

        // Клики по вкладкам
        int tabY = startY + 35;
        for (int i = 0; i < tabs.length; i++) {
            if (mouseX >= startX + 5 && mouseX <= startX + 90 && mouseY >= tabY && mouseY <= tabY + 20) {
                currentTab = i;
                return true;
            }
            tabY += 25;
        }

        // Клики по модулям
        int modX = startX + 105;
        int modY = startY + 35;
        int col = 0;

        for (Module module : ModuleManager.modules) {
            if (currentTab == 0 && !module.name.equals("KillAura")) continue;
            if (currentTab == 1 && !module.name.equals("FullBridge") && !module.name.equals("AutoSprint")) continue;
            if (currentTab == 2 && !module.name.equals("ESP")) continue;
            if (currentTab == 3) continue;

            if (mouseX >= modX && mouseX <= modX + 135 && mouseY >= modY && mouseY <= modY + 30) {
                module.toggle();
                return true;
            }

            modY += 40;
            col++;
            if (col == 2) {
                modX += 145;
                modY = startY + 35;
                col = 0;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
