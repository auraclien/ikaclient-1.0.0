package com.rocket.ikaclient.modules;

import com.rocket.ikaclient.utils.FriendManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.Random;

public class KillAura extends Module {
    private static final double RANGE = 3.0;
    
    // Рандомизация CPS (для скрытности)
    private final Random random = new Random();
    private long nextAttackTime = 0L;

    public KillAura() {
        super("KillAura", GLFW.GLFW_KEY_R);
    }

    @Override
    public void onTick() {
        if (!enabled) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        ClientPlayNetworkHandler net = mc.getNetworkHandler();
        if (player == null || mc.world == null || mc.interactionManager == null || net == null) return;

        // Ждем полного кулдауна
        if (player.getAttackCooldownProgress(0.5f) < 1.0f) return;

        Box area = player.getBoundingBox().expand(RANGE);

        mc.world.getEntitiesByClass(
                        PlayerEntity.class, // ИЩЕМ ИГРОКОВ (а не мобов)
                        area,
                        e -> e != player && e.isAlive() && !e.isInvulnerable() && !e.isCreative() && !e.isSpectator())
                .stream()
                .filter(e -> !FriendManager.isFriend(e.getName().getString())) // Игнорируем друзей
                .filter(e -> player.squaredDistanceTo(e) <= RANGE * RANGE)
                .min(Comparator.comparingDouble(player::squaredDistanceTo))
                .ifPresent(target -> {
                    long now = System.currentTimeMillis();
                    if (now < nextAttackTime) return;

                    // 1. Вычисляем идеальный угол
                    float[] rot = getRotations(player, target.getBoundingBox().getCenter());
                    
                    // 2. Отправляем серверу фейковый поворот (Silent Aim)
                    net.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                            rot[0], rot[1], player.isOnGround(), player.horizontalCollision));

                    // 3. Бьем
                    mc.interactionManager.attackEntity(player, target);
                    player.swingHand(Hand.MAIN_HAND);

                    // 4. Возвращаем серверу реальный поворот камеры
                    net.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                            player.getYaw(), player.getPitch(), player.isOnGround(), player.horizontalCollision));

                    // Рандомный CPS (8-12)
                    int cps = 8 + random.nextInt(5);
                    nextAttackTime = now + (long) (1000.0 / cps);
                });
    }

    private static float[] getRotations(ClientPlayerEntity player, Vec3d to) {
        Vec3d eye = player.getEyePos();
        double dx = to.x - eye.x;
        double dy = to.y - eye.y;
        double dz = to.z - eye.z;
        double flat = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, flat));
        return new float[]{MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -90.0f, 90.0f)};
    }
}
