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
    private static final int BG_COLOR = 0x60000000; // Полупрозрачный темный
    private static final int ACCENT_COLOR = 0xFF00AAFF; // Голубой акцент

    public static void register() {
        HudElementRegistry.addLast(
                Identifier.of(MainClient.MOD_ID, "hud"),
                HudRenderer::render
        );
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        // 1. Рисуем красивую панель клиента
        int x = 5;
        int y = 5;
        int width = 130;
        int height = 15 + (ModuleManager.modules.size() * 12);

        context.fill(x, y, x + width, y + height, BG_COLOR);
        context.fill(x, y, x + 2, y + height, ACCENT_COLOR); // Полоска слева

        context.drawText(mc.textRenderer, "§lIKA CLIENT", x + 8, y + 5, ACCENT_COLOR, true);
        context.drawText(mc.textRenderer, "§7Right Shift for menu", x + 8, y + height - 12, 0xFFAAAAAA, false);

        // 2. Выводим список включенных модулей
        int modY = y + 20;
        for (Module module : ModuleManager.modules) {
            if (module.enabled) {
                context.drawText(mc.textRenderer, module.name, x + 8, modY, 0xFFFFFFFF, true);
                modY += 12;
            }
        }

        // 3. Индикатор крита для KillAura
        KillAura aura = (KillAura) ModuleManager.modules.stream().filter(m -> m.name.equals("KillAura")).findFirst().orElse(null);
        if (aura != null && aura.enabled) {
            boolean crit = aura.isCritReady();
            String critText = crit ? "CRIT READY" : "READY";
            int color = crit ? 0xFF55FF55 : 0xFFFFAA00;
            context.drawText(mc.textRenderer, "[" + critText + "]", x + 8, modY, color, true);
        }
    }
}
