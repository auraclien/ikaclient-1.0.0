package com.rocket.ikaclient.modules;

import com.rocket.ikaclient.utils.FriendManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.Optional;
import java.util.Random;

public class KillAura extends Module {

    private static final double RANGE = 3.0;
    private static final float MIN_COOLDOWN = 0.9f;
    private static final int MIN_CPS = 8;
    private static final int MAX_CPS = 12;

    private static final float SMOOTHING = 0.45f;     
    private static final float MAX_STEP = 35f;        
    private static final float NOISE_AMPLITUDE = 0.8f; 

    private final Random random = new Random();
    private final PerlinNoise1D yawNoise = new PerlinNoise1D(random.nextLong());
    private final PerlinNoise1D pitchNoise = new PerlinNoise1D(random.nextLong());

    private double noiseTime = random.nextDouble() * 1000.0;
    private long nextAttackTime = 0L;

    public KillAura() {
        super("KillAura", GLFW.GLFW_KEY_R);
    }

    public boolean isCritReady() {
        if (mc.player == null) return false;
        return mc.player.fallDistance > 0
                && !mc.player.isOnGround()
                && !mc.player.isClimbing()
                && !mc.player.isTouchingWater()
                && !mc.player.hasVehicle();
    }

    @Override
    public void onTick() {
        if (!enabled) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null || !player.isAlive()) return;

        Optional<PlayerEntity> found = mc.world
                .getEntitiesByClass(PlayerEntity.class,
                        player.getBoundingBox().expand(RANGE),
                        e -> e != player && e.isAlive() && !e.isInvulnerable() && !e.isCreative() && !e.isSpectator())
                .stream()
                .filter(e -> !FriendManager.isFriend(e.getName().getString()))
                .filter(e -> player.distanceTo(e) <= RANGE)
                .min(Comparator.comparingDouble(player::distanceTo));

        if (found.isEmpty()) return;
        PlayerEntity target = found.get();

        // 1. Упреждение (Prediction) - наводимся туда, куда цель бежит
        aimAtWithPrediction(player, target);

        // 2. Если мы на земле, но хотим ударить - прыгаем
        if (player.isOnGround() && player.getAttackCooldownProgress(0.5f) > MIN_COOLDOWN) {
            if (player.isSprinting()) {
                player.jump(); // Прыжок для крита на ходу
            }
            return;
        }

        // 3. Если мы в воздухе (падаем) - бьем
        if (!isCritReady()) return;
        if (player.getAttackCooldownProgress(0.5f) <= MIN_COOLDOWN) return;

        long now = System.currentTimeMillis();
        if (now < nextAttackTime) return;

        // 4. Сброс спринта только на 1 тик (чтобы крит засчитался, но мы не остановились)
        boolean wasSprinting = player.isSprinting();
        if (wasSprinting) {
            player.setSprinting(false);
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        }

        mc.interactionManager.attackEntity(player, target);
        player.swingHand(Hand.MAIN_HAND);

        // Возвращаем спринт обратно (если он был)
        if (wasSprinting) {
            player.setSprinting(true);
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_SPRINTING));
        }

        int cps = MIN_CPS + random.nextInt(MAX_CPS - MIN_CPS + 1);
        nextAttackTime = now + (long) (1000.0 / cps);
    }

    private void aimAtWithPrediction(ClientPlayerEntity player, PlayerEntity target) {
        Vec3d eyes = player.getEyePos();
        
        // Берем текущую позицию через getX/getY/getZ и добавляем скорость (упреждение на 2 тика)
        Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
        Vec3d targetVel = target.getVelocity();
        Vec3d predictedPos = targetPos.add(targetVel.multiply(2.0)); 
        
        // Наводимся на центр хитбокса + упреждение
        Vec3d aimPoint = new Vec3d(predictedPos.x, targetPos.y + target.getHeight() / 2.0, predictedPos.z);

        double dx = aimPoint.x - eyes.x;
        double dy = aimPoint.y - eyes.y;
        double dz = aimPoint.z - eyes.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));

        // Шум (делает наведение человеческим)
        noiseTime += 0.07;
        targetYaw += (float) yawNoise.noise(noiseTime) * NOISE_AMPLITUDE;
        targetPitch += (float) pitchNoise.noise(noiseTime + 500.0) * NOISE_AMPLITUDE;

        float yawDiff = MathHelper.wrapDegrees(targetYaw - player.getYaw());
        float pitchDiff = targetPitch - player.getPitch();

        float yawStep = MathHelper.clamp(yawDiff * SMOOTHING, -MAX_STEP, MAX_STEP);
        float pitchStep = MathHelper.clamp(pitchDiff * SMOOTHING, -MAX_STEP, MAX_STEP);

        player.setYaw(player.getYaw() + yawStep);
        player.setPitch(MathHelper.clamp(player.getPitch() + pitchStep, -90f, 90f));
    }

    @Override
    public void onDisable() {
        nextAttackTime = 0L;
    }

    private static final class PerlinNoise1D {
        private final int[] perm = new int[512];
        private final double[] grad = new double[256];

        PerlinNoise1D(long seed) {
            Random r = new Random(seed);
            int[] p = new int[256];
            for (int i = 0; i < 256; i++) {
                p[i] = i;
                grad[i] = r.nextDouble() * 2.0 - 1.0;
            }
            for (int i = 255; i > 0; i--) {
                int j = r.nextInt(i + 1);
                int t = p[i];
                p[i] = p[j];
                p[j] = t;
            }
            for (int i = 0; i < 512; i++) perm[i] = p[i & 255];
        }

        double noise(double x) {
            int xi = (int) Math.floor(x) & 255;
            double xf = x - Math.floor(x);
            double u = xf * xf * xf * (xf * (xf * 6 - 15) + 10); 

            double g0 = grad[perm[xi]] * xf;
            double g1 = grad[perm[xi + 1]] * (xf - 1.0);
            return g0 + u * (g1 - g0);
        }
    }
}
