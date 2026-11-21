package com.rocket.ikaclient;

import com.rocket.ikaclient.modules.ChestESP;
import com.rocket.ikaclient.modules.KillAura;
import com.rocket.ikaclient.modules.Module;
import com.rocket.ikaclient.modules.ModuleManager;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

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

        // 1. Левая панель
        int x = 6; int y = 6; int width = 140;
        int activeModules = 0;
        for (Module m : ModuleManager.modules) if (m.enabled) activeModules++;
        int height = Math.max(20 + (activeModules * 12), 35);

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
            context.drawText(mc.textRenderer, crit ? "CRIT" : "WAIT", x + width - 35, y + height - 12, crit ? 0xFF55FF55 : 0xFFFFAA00, true);
        }

        // 2. Правая панель (ChestESP)
        ChestESP chestEsp = (ChestESP) ModuleManager.getModule("ChestESP");
        if (chestEsp != null && chestEsp.enabled) {
            int rightX = mc.getWindow().getScaledWidth() - 160;
            int rightY = 6;
            
            context.fill(rightX, rightY, rightX + 150, rightY + 18, BG_COLOR);
            context.fill(rightX, rightY, rightX + 150, rightY + 2, 0xFFFFFF00);
            context.drawText(mc.textRenderer, "§eChestESP", rightX + 5, rightY + 5, 0xFFFFFFFF, true);

            int count = 0;
            double px = mc.player.getX(), py = mc.player.getY(), pz = mc.player.getZ();

            for (BlockPos pos : chestEsp.getHighlightedBlocks()) {
                if (count >= 10) break;
                double dist = Math.sqrt(Math.pow(pos.getX() + 0.5 - px, 2) + Math.pow(pos.getY() + 0.5 - py, 2) + Math.pow(pos.getZ() + 0.5 - pz, 2));
                String text = String.format("%.0fм [%d, %d, %d]", dist, pos.getX(), pos.getY(), pos.getZ());
                rightY += 12;
                context.drawText(mc.textRenderer, text, rightX + 5, rightY, 0xFFFFFF00, true);
                count++;
            }
            context.fill(rightX - 1, rightY + 14 - (count * 12) - 1, rightX + 151, rightY + 15, 0xFFFFFF00);
        }
    }
}
