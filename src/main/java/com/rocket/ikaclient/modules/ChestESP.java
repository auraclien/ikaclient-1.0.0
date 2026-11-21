package com.rocket.ikaclient.modules;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashSet;
import java.util.Set;

public class ChestESP extends Module {
    private final Set<BlockPos> highlightedBlocks = new HashSet<>();

    public ChestESP() { super("ChestESP", -1); }

    @Override
    public void onTick() {
        if (!enabled || mc.player == null || mc.world == null) {
            highlightedBlocks.clear();
            return;
        }
        if (mc.player.age % 20 == 0) { 
            highlightedBlocks.clear();
            int radius = 16;
            int playerChunkX = mc.player.getChunkPos().x;
            int playerChunkZ = mc.player.getChunkPos().z;

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    WorldChunk chunk = mc.world.getChunk(playerChunkX + x, playerChunkZ + z);
                    if (chunk == null) continue;

                    for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                        if (blockEntity instanceof ChestBlockEntity || blockEntity instanceof BarrelBlockEntity || 
                            blockEntity instanceof ShulkerBoxBlockEntity || blockEntity instanceof EnderChestBlockEntity) {
                            highlightedBlocks.add(blockEntity.getPos());
                        }
                    }
                }
            }
        }
    }

    public Set<BlockPos> getHighlightedBlocks() {
        return highlightedBlocks;
    }
}
