package com.rocket.ikaclient.modules;

import com.rocket.ikaclient.utils.FriendManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class KillAura extends Module {
    // 2.95 вместо 3.0, чтобы Grim не палил Reach
    private static final double RANGE = 2.95; 
    private static final double RANGE_SQUARED = RANGE * RANGE;
    
    // CPS 8-12, чтобы бить быстро и больно
    private static final int CPS_MIN = 8;
    private static final int CPS_MAX = 12;

    private PlayerEntity target = null;
    private long lastAttack = 0L;
    private long nextDelay = 0L;
    private long targetAcquiredAt = 0L;

    public KillAura() { super("KillAura", GLFW.GLFW_KEY_R); }
    public PlayerEntity getTarget() { return target; }

    public boolean isCritReady() {
        if (mc.player == null) return false;
        return !mc.player.isOnGround() && !mc.player.isTouchingWater() && !mc.player.isClimbing()
                && !mc.player.hasVehicle() && !mc.player.isGliding() && mc.player.fallDistance > 0.0f
                && mc.player.getAttackCooldownProgress(0.0f) > 0.8f;
    }

    @Override
    public void onTick() {
        if (!enabled || mc.player == null || mc.world == null || mc.interactionManager == null) {
            target = null; return;
        }
        
        target = findTarget();
        if (target == null) { 
            nextDelay = 0L; 
            targetAcquiredAt = 0L;
            return; 
        }

        // Задержка реакции на новую цель (100-180 мс)
        if (targetAcquiredAt == 0L) {
            targetAcquiredAt = System.currentTimeMillis();
            return; 
        }
        if (System.currentTimeMillis() - targetAcquiredAt < ThreadLocalRandom.current().nextLong(100, 180)) return;
        
        aimAtTarget(target);
        
        if (!isCritReady()) return;

        long now = System.currentTimeMillis();
        if (nextDelay == 0L) {
            int cps = ThreadLocalRandom.current().nextInt(CPS_MIN, CPS_MAX + 1);
            nextDelay = 1000L / cps + ThreadLocalRandom.current().nextLong(-10L, 11L);
        }
        if (now - lastAttack < nextDelay) return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastAttack = now; 
        nextDelay = 0L;
    }

    private PlayerEntity findTarget() {
        List<PlayerEntity> candidates = mc.world.getPlayers().stream()
                .filter(e -> e != mc.player && e.isAlive() && !e.isRemoved() && !e.isCreative() && !e.isSpectator())
                // Игнорируем друзей
                .filter(e -> !FriendManager.isFriend(e.getName().getString())) 
                .filter(e -> mc.player.squaredDistanceTo(e) <= RANGE_SQUARED)
                .filter(mc.player::canSee) 
                .map(e -> (PlayerEntity) e)
                .sorted(Comparator.comparingDouble(mc.player::squaredDistanceTo))
                .toList();
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    private void aimAtTarget(PlayerEntity entity) {
        Vec3d eyes = mc.player.getEyePos();
        Vec3d targetEyes = entity.getEyePos();
        Vec3d direction = targetEyes.subtract(eyes);

        double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float targetYaw = (float) (Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0);
        float targetPitch = (float) (-Math.toDegrees(Math.atan2(direction.y, horizontal)));

        // Легкий джиттер (чтобы не быть математическим роботом)
        targetYaw += (float) ThreadLocalRandom.current().nextDouble(-0.4, 0.4);
        targetPitch += (float) ThreadLocalRandom.current().nextDouble(-0.4, 0.4);

        float yawDiff = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw());
        float pitchDiff = MathHelper.wrapDegrees(targetPitch - mc.player.getPitch());

        // Плавный доворот (0.45), чтобы Grim не палил снапы (резкие повороты)
        float smoothFactor = 0.45f; 
        
        mc.player.setYaw(mc.player.getYaw() + yawDiff * smoothFactor);
        mc.player.setPitch(MathHelper.clamp(mc.player.getPitch() + pitchDiff * smoothFactor, -90.0f, 90.0f));
    }
}
