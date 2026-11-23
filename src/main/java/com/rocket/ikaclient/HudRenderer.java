package com.rocket.ikaclient;

import com.rocket.ikaclient.modules.ChestESP;
import com.rocket.ikaclient.modules.KillAura;
import com.rocket.ikaclient.modules.Module;
import com.rocket.ikaclient.modules.ModuleManager;
import com.rocket.ikaclient.utils.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class HudRenderer {
    private static final int BG_COLOR = 0xD9000000;
    private static final int ACCENT_COLOR = 0xFF00AAFF;

    public static void register() {
        HudElementRegistry.addLast(Identifier.of(MainClient.MOD_ID, "hud"), HudRenderer::render);
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        int x = 10; 
        int y = 10; 
        int width = 280; 
        int height = 22; 
        int radius = 11; 

        RenderUtils.drawRoundedRect(context, x, y, width, height, radius, BG_COLOR);
        RenderUtils.drawRoundedRect(context, x, y, 4, height, 2, ACCENT_COLOR);

        context.drawText(mc.textRenderer, "§lIKA", x + 12, y + 7, ACCENT_COLOR, true);
        context.drawText(mc.textRenderer, "§7CLIENT", x + 38, y + 7, 0xFFAAAAAA, true);

        String fpsText = "§7FPS: §f" + mc.getCurrentFps();
        context.drawText(mc.textRenderer, fpsText, x + 105, y + 7, 0xFFFFFFFF, true);

        int px = (int) mc.player.getX();
        int py = (int) mc.player.getY();
        int pz = (int) mc.player.getZ();
        String coordsText = String.format("§7XYZ: §f%d, %d, %d", px, py, pz);
        context.drawText(mc.textRenderer, coordsText, x + 155, y + 7, 0xFFFFFFFF, true);

        // Список модулей
        int activeModules = 0;
        for (Module m : ModuleManager.modules) {
            if (m.enabled) activeModules++;
        }

        if (activeModules > 0) {
            int modY = y + height + 5;
            int modWidth = 100;
            int modHeight = 15;

            RenderUtils.drawRoundedRect(context, x, modY, modWidth, (activeModules * modHeight) + 10, 6, BG_COLOR);

            int currentY = modY + 5;
            for (Module module : ModuleManager.modules) {
                if (module.enabled) {
                    RenderUtils.drawCircle(context, x + 8, currentY + 5, 2, ACCENT_COLOR);
                    context.drawText(mc.textRenderer, module.name, x + 15, currentY, 0xFFFFFFFF, true);
                    currentY += modHeight;
                }
            }
        }

        // Индикатор цели (вместо CRIT)
        KillAura aura = (KillAura) ModuleManager.getModule("KillAura");
        if (aura != null && aura.enabled) {
            String targetText = aura.getTarget() != null ? "§cTARGET" : "§7IDLE";
            RenderUtils.drawRoundedRect(context, x + width - 45, y + 5, 35, 12, 6, 0xFF333333);
            context.drawText(mc.textRenderer, targetText, x + width - 40, y + 7, 0xFFFFFFFF, true);
        }

        // Chest ESP
        ChestESP chestEsp = (ChestESP) ModuleManager.getModule("ChestESP");
        if (chestEsp != null && chestEsp.enabled) {
            int rightX = mc.getWindow().getScaledWidth() - 160;
            int rightY = 10;
            
            context.fill(rightX, rightY, rightX + 150, rightY + 18, BG_COLOR);
            context.fill(rightX, rightY, rightX + 150, rightY + 2, 0xFFFFFF00);
            context.drawText(mc.textRenderer, "§eChestESP", rightX + 5, rightY + 5, 0xFFFFFFFF, true);

            int count = 0;
            double pxChest = mc.player.getX(), pyChest = mc.player.getY(), pzChest = mc.player.getZ();

            for (BlockPos pos : chestEsp.getHighlightedBlocks()) {
                if (count >= 10) break;
                double dist = Math.sqrt(Math.pow(pos.getX() + 0.5 - pxChest, 2) + Math.pow(pos.getY() + 0.5 - pyChest, 2) + Math.pow(pos.getZ() + 0.5 - pzChest, 2));
                String text = String.format("%.0fм [%d, %d, %d]", dist, pos.getX(), pos.getY(), pos.getZ());
                rightY += 12;
                context.fill(rightX, rightY, rightX + 150, rightY + 12, BG_COLOR);
                context.drawText(mc.textRenderer, text, rightX + 5, rightY + 2, 0xFFFFFF00, true);
                count++;
            }
            context.fill(rightX - 1, 10, rightX + 151, 11, 0xFFFFFF00); 
            context.fill(rightX - 1, rightY + 14, rightX + 151, rightY + 15, 0xFFFFFF00); 
        }
    }
}
