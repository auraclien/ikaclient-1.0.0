package com.rocket.ikaclient.ui;

import com.rocket.ikaclient.modules.Module;
import com.rocket.ikaclient.modules.ModuleManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput; // Новый импорт для 1.21.11
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x80000000);
        
        int startX = this.width / 2 - 200;
        int startY = this.height / 2 - 120;
        int width = 400;
        int height = 240;

        context.fill(startX, startY, startX + width, startY + height, 0xFF1E1E1E);
        context.fill(startX, startY, startX + width, startY + 25, 0xFF2A2A2A);
        context.drawText(textRenderer, "§lIka Client", startX + 10, startY + 8, 0xFFFFFFFF, true);
        context.fill(startX, startY + 25, startX + width, startY + 26, 0xFF333333);

        int tabY = startY + 35;
        for (int i = 0; i < tabs.length; i++) {
            boolean selected = (currentTab == i);
            int tabColor = selected ? 0xFF00AAFF : 0xFF2A2A2A;
            context.fill(startX + 5, tabY, startX + 90, tabY + 20, tabColor);
            context.drawText(textRenderer, tabs[i], startX + 10, tabY + 6, 0xFFFFFFFF, true);
            tabY += 25;
        }
        context.fill(startX + 95, startY + 26, startX + 96, startY + height, 0xFF333333);

        int modX = startX + 105;
        int modY = startY + 35;
        int col = 0;

        for (Module module : ModuleManager.modules) {
            if (currentTab == 0 && !module.name.equals("KillAura")) continue;
            if (currentTab == 1 && !module.name.equals("FullBright") && !module.name.equals("AutoSprint")) continue;
            if (currentTab == 2 && !module.name.equals("ESP")) continue;
            if (currentTab == 3) continue;

            if (waitingForBind == module) {
                context.fill(modX, modY, modX + 135, modY + 30, 0xFF555555);
                context.drawText(textRenderer, "§ePress key...", modX + 8, modY + 11, 0xFFFFFFFF, true);
            } else {
                int bgColor = module.enabled ? 0xFF2E7D32 : 0xFF2A2A2A;
                context.fill(modX, modY, modX + 135, modY + 30, bgColor);
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

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        
        int startX = this.width / 2 - 200;
        int startY = this.height / 2 - 120;

        int tabY = startY + 35;
        for (int i = 0; i < tabs.length; i++) {
            if (mouseX >= startX + 5 && mouseX <= startX + 90 && mouseY >= tabY && mouseY <= tabY + 20) {
                currentTab = i;
                return true;
            }
            tabY += 25;
        }

        int modX = startX + 105;
        int modY = startY + 35;
        int col = 0;

        for (Module module : ModuleManager.modules) {
            if (currentTab == 0 && !module.name.equals("KillAura")) continue;
            if (currentTab == 1 && !module.name.equals("FullBright") && !module.name.equals("AutoSprint")) continue;
            if (currentTab == 2 && !module.name.equals("ESP")) continue;
            if (currentTab == 3) continue;

            if (mouseX >= modX && mouseX <= modX + 135 && mouseY >= modY && mouseY <= modY + 30) {
                if (button == 0) { 
                    module.toggle();
                    return true;
                } else if (button == 2) { 
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

    // ИСПРАВЛЕНИЕ: Используем KeyInput вместо int keyCode
    @Override
    public boolean keyPressed(KeyInput input) {
        if (waitingForBind != null) {
            waitingForBind.key = input.key(); // Сохраняем код клавиши
            waitingForBind = null;
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
