
package com.rocket.ikaclient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class KillAura {

    private static final double RANGE = 2.85;
    private static final double RANGE_SQUARED = RANGE * RANGE;

    private static final float MAX_ROTATION = 8.0f;

    private boolean enabled = false;

    private PlayerEntity target = null;

    public void toggle() {
        enabled = !enabled;
        target = null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public PlayerEntity getTarget() {
        return target;
    }

    public boolean isCritReady(MinecraftClient mc) {

        if (mc.player == null) {
            return false;
        }

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

        if (!enabled
                || mc.player == null
                || mc.world == null
                || mc.interactionManager == null) {

            target = null;
            return;
        }

        target = findTarget(mc);

        if (target == null) {
            return;
        }

        rotateToTarget(mc, target);

        if (!isCritReady(mc)) {
            return;
        }

        mc.interactionManager.attackEntity(
                mc.player,
                target
        );

        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private PlayerEntity findTarget(MinecraftClient mc) {

        PlayerEntity bestTarget = null;

        double bestDistance = RANGE_SQUARED;

        for (PlayerEntity entity : mc.world.getPlayers()) {

            if (entity == mc.player) {
                continue;
            }

            if (!entity.isAlive() || entity.isRemoved()) {
                continue;
            }

            if (entity.isCreative() || entity.isSpectator()) {
                continue;
            }

            double distance =
                    mc.player.squaredDistanceTo(entity);

            if (distance > bestDistance) {
                continue;
            }

            if (!mc.player.canSee(entity)) {
                continue;
            }

            bestDistance = distance;
            bestTarget = entity;
        }

        return bestTarget;
    }

    private void rotateToTarget(
            MinecraftClient mc,
            PlayerEntity entity
    ) {

        Vec3d eyes = mc.player.getEyePos();

        Vec3d targetEyes = entity.getEyePos();

        Vec3d direction = targetEyes.subtract(eyes);

        double horizontal = Math.sqrt(
                direction.x * direction.x
                + direction.z * direction.z
        );

        float targetYaw = (float) (
                Math.toDegrees(
                        Math.atan2(direction.z, direction.x)
                ) - 90.0
        );

        float targetPitch = (float) (
                -Math.toDegrees(
                        Math.atan2(direction.y, horizontal)
                )
        );

        float yawDifference = MathHelper.wrapDegrees(
                targetYaw - mc.player.getYaw()
        );

        float pitchDifference = MathHelper.wrapDegrees(
                targetPitch - mc.player.getPitch()
        );

        float yawStep = MathHelper.clamp(
                yawDifference,
                -MAX_ROTATION,
                MAX_ROTATION
        );

        float pitchStep = MathHelper.clamp(
                pitchDifference,
                -MAX_ROTATION,
                MAX_ROTATION
        );

        mc.player.setYaw(
                mc.player.getYaw() + yawStep
        );

        mc.player.setPitch(
                MathHelper.clamp(
                        mc.player.getPitch() + pitchStep,
                        -90.0f,
                        90.0f
                )
        );
    }
}
