package com.rocket.ikaclient.ui;

import com.rocket.ikaclient.modules.Module;
import com.rocket.ikaclient.modules.ModuleManager;
import com.rocket.ikaclient.utils.RenderUtils;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ClickGUI extends Screen {
    private int currentTab = 0;
    private final String[] tabs = {"Combat", "Movement", "Render", "Misc"};

    private final int windowWidth = 500;
    private final int windowHeight = 350;
    private int startX, startY;

    private Module waitingForBind = null;

    public ClickGUI() {
        super(Text.literal("Ika Client"));
    }

    @Override
    protected void init() {
        super.init();
        this.startX = (this.width - windowWidth) / 2;
        this.startY = (this.height - windowHeight) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x90000000);

        RenderUtils.drawRoundedRect(context, startX, startY, windowWidth, windowHeight, 10, 0xFF1E1E1E);

        RenderUtils.drawRoundedRect(context, startX, startY, windowWidth, 40, 10, 0xFF252525);
        context.fill(startX + 10, startY + 30, startX + windowWidth - 10, startY + 40, 0xFF252525);

        context.drawTextWithShadow(textRenderer, "IKA CLIENT", startX + 20, startY + 14, 0xFFFFFFFF);

        int tabX = startX + 20;
        for (int i = 0; i < tabs.length; i++) {
            boolean selected = (currentTab == i);
            int color = selected ? 0xFF00AAFF : 0xFF888888;

            context.drawTextWithShadow(textRenderer, tabs[i], tabX, startY + 14, color);

            if (selected) {
                int textWidth = textRenderer.getWidth(tabs[i]);
                context.fill(tabX, startY + 35, tabX + textWidth, startY + 38, 0xFF00AAFF);
            }
            tabX += textRenderer.getWidth(tabs[i]) + 20;
        }

        context.fill(startX + 10, startY + 45, startX + windowWidth - 10, startY + 46, 0xFF333333);

        int modX = startX + 20;
        int modY = startY + 60;
        int col = 0;

        for (Module module : ModuleManager.modules) {
            // ФИЛЬТР ПО ВКЛАДКАМ (Добавлен ClickPearl)
            if (currentTab == 0 && !module.name.equals("KillAura") && !module.name.equals("ClickPearl")) continue;
            if (currentTab == 1 && !module.name.equals("FullBright") && !module.name.equals("AutoSprint")) continue;
            if (currentTab == 2 && !module.name.equals("ChestESP")) continue;
            if (currentTab == 3) continue;

            if (waitingForBind == module) {
                RenderUtils.drawRoundedRect(context, modX, modY, 210, 30, 8, 0xFF555555);
                context.drawTextWithShadow(textRenderer, "§ePress key...", modX + 10, modY + 10, 0xFFFFFFFF);
            } else {
                RenderUtils.drawRoundedRect(context, modX, modY, 210, 30, 8, 0xFF2A2A2A);
                
                String bindText = module.key != -1 ? " §7[" + GLFW.glfwGetKeyName(module.key, 0) + "]" : "";
                context.drawTextWithShadow(textRenderer, module.name + bindText, modX + 10, modY + 10, 0xFFFFFFFF);
                
                RenderUtils.drawSwitch(context, modX + 170, modY + 5, module.enabled);
            }

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
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        int tabX = startX + 20;
        for (int i = 0; i < tabs.length; i++) {
            int w = textRenderer.getWidth(tabs[i]);
            if (mouseX >= tabX && mouseX <= tabX + w && mouseY >= startY + 10 && mouseY <= startY + 30) {
                currentTab = i;
                return true;
            }
            tabX += w + 20;
        }

        int modX = startX + 20;
        int modY = startY + 60;
        int col = 0;

        for (Module module : ModuleManager.modules) {
            // ТОТ ЖЕ ФИЛЬТР ДЛЯ КЛИКОВ
            if (currentTab == 0 && !module.name.equals("KillAura") && !module.name.equals("ClickPearl")) continue;
            if (currentTab == 1 && !module.name.equals("FullBright") && !module.name.equals("AutoSprint")) continue;
            if (currentTab == 2 && !module.name.equals("ChestESP")) continue;
            if (currentTab == 3) continue;

            if (mouseX >= modX && mouseX <= modX + 210 && mouseY >= modY && mouseY <= modY + 30) {
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
            if (col == 2) { modX += 225; modY = startY + 60; col = 0; }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (waitingForBind != null) {
            waitingForBind.key = input.key();
            waitingForBind = null;
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() { return false; }
}
