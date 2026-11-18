package com.rocket.ikaclient;

import com.rocket.ikaclient.modules.KillAura;
import com.rocket.ikaclient.modules.Module;
import com.rocket.ikaclient.modules.ModuleManager;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

public class HudRenderer {
    private static final int BG_COLOR = 0x90000000;
    private static final int ACCENT_COLOR = 0xFF00AAFF;

    public static void register() {
        HudElementRegistry.addLast(
                Identifier.of(MainClient.MOD_ID, "hud"),
                HudRenderer::render
        );
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        int x = 6;
        int y = 6;
        int width = 140;
        
        int activeModules = 0;
        for (Module m : ModuleManager.modules) {
            if (m.enabled) activeModules++;
        }
        int height = 20 + (activeModules * 12);
        if (height < 35) height = 35; // Минимальная высота

        context.fill(x, y, x + width, y + height, BG_COLOR);
        context.fill(x, y, x + 3, y + height, ACCENT_COLOR);

        context.drawText(mc.textRenderer, "§lIKA", x + 8, y + 5, ACCENT_COLOR, true);
        context.drawText(mc.textRenderer, "§7CLIENT", x + 32, y + 5, 0xFFAAAAAA, true);
        
        context.fill(x + 5, y + 16, x + width - 5, y + 17, 0x40FFFFFF);

        int modY = y + 20;
        for (Module module : ModuleManager.modules) {
            if (module.enabled) {
                context.fill(x + 5, modY, x + 7, modY + 8, 0xFFFFFFFF);
                context.drawText(mc.textRenderer, module.name, x + 12, modY, 0xFFFFFFFF, true);
                modY += 12;
            }
        }

        KillAura aura = (KillAura) ModuleManager.getModule("KillAura");
        if (aura != null && aura.enabled) {
            boolean crit = aura.isCritReady();
            String critText = crit ? "CRIT" : "WAIT";
            int color = crit ? 0xFF55FF55 : 0xFFFFAA00;
            context.drawText(mc.textRenderer, critText, x + width - 35, y + height - 12, color, true);
        }
    }
}
