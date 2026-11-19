package com.rocket.ikaclient.modules;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.WorldChunk;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class ChestESP extends Module {
    private final Set<BlockPos> highlightedBlocks = new HashSet<>();

    public ChestESP() {
        super("ChestESP", -1);
    }

    @Override
    public void onTick() {
        if (!enabled || mc.player == null || mc.world == null) {
            clearHighlights();
            return;
        }

        // Если игрок сдвинулся, сканируем заново (раз в тик)
        if (mc.player.age % 20 == 0) { // Раз в секунду для оптимизации
            scanChunks();
        }
    }

    private void scanChunks() {
        clearHighlights();
        
        // Сканируем чанки в радиусе 256 блоков (16 чанков во все стороны)
        int radius = 16;
        int playerChunkX = mc.player.getChunkPos().x;
        int playerChunkZ = mc.player.getChunkPos().z;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                WorldChunk chunk = mc.world.getChunk(playerChunkX + x, playerChunkZ + z);
                if (chunk == null) continue;

                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof ChestBlockEntity || 
                        blockEntity instanceof BarrelBlockEntity || 
                        blockEntity instanceof ShulkerBoxBlockEntity || 
                        blockEntity instanceof EnderChestBlockEntity) {
                        
                        BlockPos pos = blockEntity.getPos();
                        highlightedBlocks.add(pos);
                        // В 1.21.11 нельзя просто так поставить Glowing на блок, 
                        // поэтому мы запоминаем позиции для рендера (см. HudRenderer/RenderEvent)
                    }
                }
            }
        }
    }

    private void clearHighlights() {
        highlightedBlocks.clear();
    }

    public Set<BlockPos> getHighlightedBlocks() {
        return highlightedBlocks;
    }
}
