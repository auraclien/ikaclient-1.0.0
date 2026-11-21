package com.rocket.ikaclient.ui;

import com.rocket.ikaclient.modules.Module;
import com.rocket.ikaclient.modules.ModuleManager;
import com.rocket.ikaclient.utils.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ClickGUI extends Screen {
    private int currentTab = 0;
    private final String[] tabs = {"Combat", "Movement", "Render", "Misc"};
    
    // Координаты и размеры окна
    private final int windowWidth = 500;
    private final int windowHeight = 350;
    private int startX, startY;

    public ClickGUI() { 
        super(Text.literal("Ika Client")); 
    }

    @Override
    protected void init() {
        super.init();
        // Центрируем окно
        this.startX = (this.width - windowWidth) / 2;
        this.startY = (this.height - windowHeight) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Затемняем фон игры
        context.fill(0, 0, this.width, this.height, 0x90000000);

        // Рисуем основное окно (темно-серое)
        RenderUtils.drawRoundedRect(context, startX, startY, windowWidth, windowHeight, 10, 0xFF1E1E1E);
        
        // Верхняя панель (Header)
        RenderUtils.drawRoundedRect(context, startX, startY, windowWidth, 40, 10, 0xFF252525);
        // Срезаем низ хедера, чтобы он не перекрывал углы окна
        context.fill(startX + 10, startY + 30, startX + windowWidth - 10, startY + 40, 0xFF252525);
        
        // Название клиента
        context.drawTextWithShadow(textRenderer, "IKA CLIENT", startX + 20, startY + 14, 0xFFFFFFFF);

        // Рисуем вкладки (Tabs)
        int tabX = startX + 20;
        for (int i = 0; i < tabs.length; i++) {
            boolean selected = (currentTab == i);
            int color = selected ? 0xFF00AAFF : 0xFF888888;
            
            context.drawTextWithShadow(textRenderer, tabs[i], tabX, startY + 14, color);
            
            // Линия под активной вкладкой
            if (selected) {
                int textWidth = textRenderer.getWidth(tabs[i]);
                context.fill(tabX, startY + 35, tabX + textWidth, startY + 38, 0xFF00AAFF);
            }
            tabX += textRenderer.getWidth(tabs[i]) + 20;
        }

        // Разделитель
        context.fill(startX + 10, startY + 45, startX + windowWidth - 10, startY + 46, 0xFF333333);

        // Рисуем модули
        int modX = startX + 20;
        int modY = startY + 60;
        int col = 0;

        for (Module module : ModuleManager.modules) {
            // Фильтрация по вкладкам (пока простая логика, потом усложним)
            if (currentTab == 0 && !module.name.equals("KillAura")) continue;
            if (currentTab == 1 && !module.name.equals("FullBright") && !module.name.equals("AutoSprint")) continue;
            if (currentTab == 2 && !module.name.equals("ChestESP")) continue;
            if (currentTab == 3) continue;

            // Фон модуля (чуть светлее основного окна)
            RenderUtils.drawRoundedRect(context, modX, modY, 210, 30, 8, 0xFF2A2A2A);
            
            // Название модуля
            context.drawTextWithShadow(textRenderer, module.name, modX + 10, modY + 10, 0xFFFFFFFF);

            // Рисуем наш красивый переключатель справа
            RenderUtils.drawSwitch(context, modX + 170, modY + 5, module.enabled);

            // Переход на новую колонку
            modY += 40;
            col++;
            if (col == 2) { 
                modX += 225; 
                modY = startY + 60; 
                col = 0; 
            }
        }
        
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Клик по вкладкам
        int tabX = startX + 20;
        for (int i = 0; i < tabs.length; i++) {
            int w = textRenderer.getWidth(tabs[i]);
            if (mouseX >= tabX && mouseX <= tabX + w && mouseY >= startY + 10 && mouseY <= startY + 30) {
                currentTab = i;
                return true;
            }
            tabX += w + 20;
        }

        // Клик по модулям (включение/выключение)
        int modX = startX + 20;
        int modY = startY + 60;
        int col = 0;

        for (Module module : ModuleManager.modules) {
            if (currentTab == 0 && !module.name.equals("KillAura")) continue;
            if (currentTab == 1 && !module.name.equals("FullBright") && !module.name.equals("AutoSprint")) continue;
            if (currentTab == 2 && !module.name.equals("ChestESP")) continue;
            if (currentTab == 3) continue;

            // Проверяем попадание в область модуля
            if (mouseX >= modX && mouseX <= modX + 210 && mouseY >= modY && mouseY <= modY + 30) {
                module.toggle(); // Переключаем модуль
                return true;
            }

            modY += 40;
            col++;
            if (col == 2) { modX += 225; modY = startY + 60; col = 0; }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() { return false; }
}
