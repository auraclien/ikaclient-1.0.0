package com.rocket.ikaclient.modules;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;

import java.util.IdentityHashMap;
import java.util.Map;

public class ESP extends Module {

    private final Map<PlayerEntity, Boolean> previousGlow = new IdentityHashMap<>();
    private ClientWorld lastWorld;

    public ESP() {
        super("ESP", -1);
    }

    @Override
    public void onEnable() {
        lastWorld = mc.world;
    }

    @Override
    public void onTick() {
        if (!enabled || mc.player == null || mc.world == null) {
            clearGlow();
            lastWorld = null;
            return;
        }

        if (lastWorld != mc.world) {
            clearGlow();
            lastWorld = mc.world;
        }

        for (PlayerEntity entity : mc.world.getPlayers()) {
            if (entity == mc.player || !entity.isAlive() || entity.isRemoved()) continue;

            previousGlow.putIfAbsent(entity, entity.isGlowing());
            entity.setGlowing(true);
        }
    }

    @Override
    public void onDisable() {
        clearGlow();
        lastWorld = null;
    }

    private void clearGlow() {
        for (Map.Entry<PlayerEntity, Boolean> entry : previousGlow.entrySet()) {
            PlayerEntity entity = entry.getKey();
            if (entity != null) {
                entity.setGlowing(entry.getValue());
            }
        }
        previousGlow.clear();
    }
}
