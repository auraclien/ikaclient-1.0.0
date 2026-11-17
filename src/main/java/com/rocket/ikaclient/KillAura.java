package com.rocket.ikaclient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class KillAura {

    private static final double RANGE = 2.85;
    private static final double RANGE_SQUARED = RANGE * RANGE;
    private static final float FOV = 120.0f;
    private static final float MAX_ROTATION = 25.0f;
    private static final int CPS_MIN = 6;
    private static final int CPS_MAX = 8;
    
    private boolean enabled = false;
    private PlayerEntity target = null;
    private long lastAttack = 0L;
    private long nextDelay = 0L;

    public void toggle() {
        enabled = !enabled;
        target = null;
    }

    public boolean isEnabled() { return enabled; }
    public PlayerEntity getTarget() { return target; }

    public boolean isCritReady(MinecraftClient mc) {
        if (mc.player == null) return false;
        PlayerEntity player = mc.player;

        return !player.isOnGround()
                && !player.isTouchingWater()
                && !player.isClimbing()
                && !player.hasVehicle()
                && !player.isGliding()
                && player.fallDistance > 0.0f
                && player.getAttackCooldownProgress(0.0f) > 0.9f;
    }

    public void tick(MinecraftClient mc) {
        if (!enabled || mc.player == null || mc.world == null || mc.interactionManager == null) {
            target = null;
            return;
        }

        target = findTarget(mc);
        if (target == null) {
            nextDelay = 0L;
            return;
        }

        rotateToTarget(mc, target);

        if (!isCritReady(mc)) return;

        long now = System.currentTimeMillis();
        if (nextDelay == 0L) {
            int cps = ThreadLocalRandom.current().nextInt(CPS_MIN, CPS_MAX + 1);
            nextDelay = 1000L / cps + ThreadLocalRandom.current().nextLong(-5L, 6L);
        }

        if (now - lastAttack < nextDelay) return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        
        lastAttack = now;
        nextDelay = 0L;
    }

    private PlayerEntity findTarget(MinecraftClient mc) {
        // Исправлено: используем List<PlayerEntity> и кастуем элементы
        List<PlayerEntity> candidates = mc.world.getPlayers().stream()
                .filter(e -> e != mc.player && e.isAlive() && !e.isRemoved())
                .filter(e -> !e.isCreative() && !e.isSpectator())
                .filter(e -> mc.player.squaredDistanceTo(e) <= RANGE_SQUARED)
                .filter(mc.player::canSee)
                .filter(e -> isInFov(mc, e))
                .map(e -> (PlayerEntity) e) // Явное приведение типа
                .sorted(Comparator.comparingDouble(mc.player::squaredDistanceTo))
                .toList();

        return candidates.isEmpty() ? null : candidates.get(0);
    }

    private boolean isInFov(MinecraftClient mc, PlayerEntity entity) {
        Vec3d eyes = mc.player.getEyePos();
        Vec3d targetEyes = entity.getEyePos();
        Vec3d direction = targetEyes.subtract(eyes);
        
        float targetYaw = (float) (Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0);
        float yawDifference = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw());
        
        return Math.abs(yawDifference) <= FOV / 2.0f;
    }

    private void rotateToTarget(MinecraftClient mc, PlayerEntity entity) {
        Vec3d eyes = mc.player.getEyePos();
        Vec3d targetEyes = entity.getEyePos();
        Vec3d direction = targetEyes.subtract(eyes);

        double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float targetYaw = (float) (Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0);
        float targetPitch = (float) (-Math.toDegrees(Math.atan2(direction.y, horizontal)));

        float yawDifference = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw());
        float pitchDifference = MathHelper.wrapDegrees(targetPitch - mc.player.getPitch());

        mc.player.setYaw(mc.player.getYaw() + MathHelper.clamp(yawDifference, -MAX_ROTATION, MAX_ROTATION));
        mc.player.setPitch(MathHelper.clamp(mc.player.getPitch() + MathHelper.clamp(pitchDifference, -MAX_ROTATION, MAX_ROTATION), -90.0f, 90.0f));
    }
}
