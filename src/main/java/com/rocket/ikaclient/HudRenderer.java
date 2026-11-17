package com.rocket.ikaclient;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

public final class HudRenderer {

    private static final int GREEN = 0xFF55FF55;
    private static final int RED = 0xFFFF5555;
    private static final int GRAY = 0xFFAAAAAA;
    private static final int WHITE = 0xFFFFFFFF;

    private HudRenderer() {}

    public static void register() {
        HudElementRegistry.addLast(
                Identifier.of(MainClient.MOD_ID, "status_hud"),
                HudRenderer::render
        );
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player == null || mc.world == null) return;

        KillAura aura = MainClient.aura;
        if (aura == null) return;

        boolean enabled = aura.isEnabled();
        boolean critReady = aura.isCritReady(mc);

        context.drawText(mc.textRenderer, enabled ? "KillAura: ON" : "KillAura: OFF", 8, 8, enabled ? GREEN : RED, true);
        context.drawText(mc.textRenderer, critReady ? "CRIT READY" : "NOT READY", 8, 20, critReady ? GREEN : GRAY, true);

        if (enabled && aura.getTarget() != null) {
            context.drawText(mc.textRenderer, "Target: " + aura.getTarget().getName().getString(), 8, 32, WHITE, true);
        }
    }
}
