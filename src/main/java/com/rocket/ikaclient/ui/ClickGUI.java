package com.rocket.ikaclient.ui;

import com.rocket.ikaclient.modules.Module;
import com.rocket.ikaclient.modules.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ClickGUI extends Screen {
    private int currentTab = 0;
    private final String[] tabs = {"Combat", "Movement", "Render", "Misc"};
    private Module waitingForBind = null;

    public ClickGUI() {
        super(Text.literal("Ika Client Menu"));
    }

    @Override
    protected void init() {
        super.init();
        
        int startX = this.width / 2 - 200;
        int startY = this.height / 2 - 120;

        // Добавляем кнопки вкладок (слева)
        for (int i = 0; i < tabs.length; i++) {
            final int index = i;
            this.addDrawableChild(ButtonWidget.builder(Text.literal(tabs[i]), button -> {
                currentTab = index;
                this.clearAndInit(); // Перерисовываем меню
            }).dimensions(startX + 5, startY + 35 + (i * 25), 85, 20).build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Рисуем темный фон
        context.fill(0, 0, this.width, this.height, 0x80000000);
        
        int startX = this.width / 2 - 200;
        int startY = this.height / 2 - 120;
        int width = 400;
        int height = 240;

        // Фон меню
        context.fill(startX, startY, startX + width, startY + height, 0xFF1E1E1E);
        context.fill(startX, startY, startX + width, startY + 25, 0xFF2A2A2A);
        context.drawText(textRenderer, "§lIka Client", startX + 10, startY + 8, 0xFFFFFFFF, true);
        context.fill(startX, startY + 25, startX + width, startY + 26, 0xFF333333);
        context.fill(startX + 95, startY + 26, startX + 96, startY + height, 0xFF333333);

        // Отрисовка модулей
        int modX = startX + 105;
        int modY = startY + 35;
        int col = 0;

        for (Module module : ModuleManager.modules) {
            // Фильтр по вкладкам
            if (currentTab == 0 && !module.name.equals("KillAura")) continue;
            if (currentTab == 1 && !module.name.equals("FullBright") && !module.name.equals("AutoSprint")) continue;
            if (currentTab == 2 && !module.name.equals("ESP")) continue;
            if (currentTab == 3) continue;

            // Если модуль ждет бинда
            if (waitingForBind == module) {
                context.fill(modX, modY, modX + 135, modY + 30, 0xFF555555);
                context.drawText(textRenderer, "§ePress key...", modX + 8, modY + 11, 0xFFFFFFFF, true);
            } else {
                // Фон модуля
                int bgColor = module.enabled ? 0xFF2E7D32 : 0xFF2A2A2A; // Зеленый если вкл
                context.fill(modX, modY, modX + 135, modY + 30, bgColor);
                
                // Название и бинд
                String bindText = module.key != -1 ? " §7[" + GLFW.glfwGetKeyName(module.key, 0) + "]" : "";
                context.drawText(textRenderer, module.name + bindText, modX + 8, modY + 11, 0xFFFFFFFF, true);
            }

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

    // Обработка кликов
    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        
        int startX = this.width / 2 - 200;
        int startY = this.height / 2 - 120;
        int modX = startX + 105;
        int modY = startY + 35;
        int col = 0;

        for (Module module : ModuleManager.modules) {
            if (currentTab == 0 && !module.name.equals("KillAura")) continue;
            if (currentTab == 1 && !module.name.equals("FullBright") && !module.name.equals("AutoSprint")) continue;
            if (currentTab == 2 && !module.name.equals("ESP")) continue;
            if (currentTab == 3) continue;

            if (mouseX >= modX && mouseX <= modX + 135 && mouseY >= modY && mouseY <= modY + 30) {
                if (button == 0) { // ЛКМ - вкл/выкл
                    module.toggle();
                    return true;
                } else if (button == 2) { // СКМ - бинд
                    waitingForBind = module;
                    return true;
                }
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waitingForBind != null) {
            waitingForBind.key = keyCode;
            waitingForBind = null;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
