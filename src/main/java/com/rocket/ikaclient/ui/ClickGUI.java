package com.rocket.ikaclient.ui;

import com.rocket.ikaclient.modules.Module;
import com.rocket.ikaclient.modules.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ClickGUI extends Screen {
    public ClickGUI() {
        super(Text.literal("Ika Client Menu"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Темный фон
        this.renderBackground(context, mouseX, mouseY, delta);
        
        int startX = 50;
        int startY = 50;

        // Рисуем заголовок
        context.drawText(textRenderer, "§l§bIka Client", startX, startY - 20, 0xFFFFFF, true);

        int yOffset = startY;
        for (Module module : ModuleManager.modules) {
            // Красивая плашка под модуль
            int bgColor = module.enabled ? 0x8000FF00 : 0x80000000; // Зеленый если вкл, черный если выкл
            context.fill(startX, yOffset, startX + 120, yOffset + 15, bgColor);
            
            // Текст модуля
            String text = module.name + (module.enabled ? " [ON]" : " [OFF]");
            context.drawText(textRenderer, text, startX + 5, yOffset + 4, 0xFFFFFF, true);

            yOffset += 20;
        }
        
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startX = 50;
        int startY = 50;
        int yOffset = startY;

        for (Module module : ModuleManager.modules) {
            if (mouseX >= startX && mouseX <= startX + 120 && mouseY >= yOffset && mouseY <= yOffset + 15) {
                module.toggle();
                return true;
            }
            yOffset += 20;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false; // Игра не останавливается, когда открыто меню
    }
}
