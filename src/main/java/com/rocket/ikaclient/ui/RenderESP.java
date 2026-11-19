package com.rocket.ikaclient.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.rocket.ikaclient.modules.ChestESP;
import com.rocket.ikaclient.modules.Module;
import com.rocket.ikaclient.modules.ModuleManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;

public class RenderESP {

    public static void register() {
        WorldRenderEvents.LAST.register(RenderESP::onRender);
    }

    private static void onRender(WorldRenderContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        // Получаем наш модуль ChestESP
        Module module = ModuleManager.getModule("ChestESP");
        if (!(module instanceof ChestESP chestEsp) || !module.enabled) return;

        MatrixStack matrices = context.matrixStack();
        if (matrices == null) return;

        // Настройка рендера (отключаем глубину, чтобы видеть сквозь стены)
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.lineWidth(2.0f);

        Vec3d cameraPos = context.camera().getPos();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        for (BlockPos pos : chestEsp.getHighlightedBlocks()) {
            // Вычисляем дистанцию от игрока до сундука
            double dist = mc.player.getPos().distanceTo(Vec3d.ofCenter(pos));
            if (dist > 256) continue; // Рендерим только в радиусе 256 блоков

            // Создаем рамку вокруг блока
            Box box = new Box(pos).expand(0.01); // Чуть больше блока, чтобы линии не сливались
            
            // Сдвигаем координаты относительно камеры
            float minX = (float) (box.minX - cameraPos.x);
            float minY = (float) (box.minY - cameraPos.y);
            float minZ = (float) (box.minZ - cameraPos.z);
            float maxX = (float) (box.maxX - cameraPos.x);
            float maxY = (float) (box.maxY - cameraPos.y);
            float maxZ = (float) (box.maxZ - cameraPos.z);

            // Цвет (Желтый: R=1, G=1, B=0, A=1)
            float r = 1.0f, g = 1.0f, b = 0.0f, a = 1.0f;

            Matrix4f matrix = matrices.peek().getPositionMatrix();

            // Рисуем линии куба (12 ребер)
            buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
            buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);

            buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
            buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);

            buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
            buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);

            buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
            buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);

            // Верхние линии
            buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
            buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);

            buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
            buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);

            buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
            buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);

            buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
            buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);

            // Вертикальные линии
            buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
            buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);

            buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
            buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);

            buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
            buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);

            buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
            buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        }

        // Отрисовка
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        
        // Возвращаем настройки обратно
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
