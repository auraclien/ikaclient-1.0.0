package com.rocket.ikaclient.modules;

import com.rocket.ikaclient.utils.FriendManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class KillAura extends Module {
    private static final double RANGE = 3.0;
    private static final double RANGE_SQUARED = RANGE * RANGE;
    
    private PlayerEntity target = null;
    private long lastAttack = 0L;
    private long nextDelay = 0L;
    private final Random random = new Random();

    // Переменные из Aethereal (SpookyTime режим)
    private long stAttackWindowStart = 0L;
    private float stNoiseYaw = 0f;
    private float stNoisePitch = 0f;
    private int stJoltTicks = 0;
    private int stJoltCooldown = 0;
    private float stJoltDir = 0f;
    
    // Переменные из Neptune (Точки тела)
    private int currentAnchorIndex = 0;
    private long lastAnchorSwitch = 0L;
    private static final float[] HEIGHT_FACTORS = {0.87f, 0.75f, 0.66f, 0.58f, 0.46f, 0.36f};

    public KillAura() { super("KillAura", GLFW.GLFW_KEY_R); }
    public PlayerEntity getTarget() { return target; }

    public boolean isCritReady() {
        if (mc.player == null) return false;
        return !mc.player.isOnGround() && !mc.player.isTouchingWater() && !mc.player.isClimbing()
                && !mc.player.hasVehicle() && !mc.player.isGliding() && mc.player.fallDistance > 0.0f
                && mc.player.getAttackCooldownProgress(0.0f) > 0.9f;
    }

    @Override
    public void onTick() {
        if (!enabled || mc.player == null || mc.world == null || mc.interactionManager == null) {
            target = null; return;
        }
        
        target = findTarget();
        if (target == null) { 
            nextDelay = 0L; 
            return; 
        }
        
        // Наводимся в стиле SpookyTime
        aimAtTargetSpookyTime(target);
        
        if (!isCritReady()) return;

        long now = System.currentTimeMillis();
        if (nextDelay == 0L) {
            int cps = ThreadLocalRandom.current().nextInt(8, 12);
            nextDelay = 1000L / cps;
        }
        if (now - lastAttack < nextDelay) return;

        // Логика сброса спринта перед ударом (как в Aethereal)
        if (mc.player.isSprinting()) {
            mc.player.setSprinting(false);
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
        }

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastAttack = now; 
        nextDelay = 0L;
        
        // После удара начинаем окно "дрожания" и меняем точку
        stAttackWindowStart = now;
        currentAnchorIndex = ThreadLocalRandom.current().nextInt(HEIGHT_FACTORS.length);
    }

    private PlayerEntity findTarget() {
        List<PlayerEntity> candidates = mc.world.getPlayers().stream()
                .filter(e -> e != mc.player && e.isAlive() && !e.isRemoved() && !e.isCreative() && !e.isSpectator())
                .filter(e -> !FriendManager.isFriend(e.getName().getString())) 
                .filter(e -> mc.player.squaredDistanceTo(e) <= RANGE_SQUARED)
                .filter(mc.player::canSee) 
                .map(e -> (PlayerEntity) e)
                .sorted(Comparator.comparingDouble(mc.player::squaredDistanceTo))
                .toList();
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    private void aimAtTargetSpookyTime(PlayerEntity entity) {
        long now = System.currentTimeMillis();
        
        // 1. Выбираем точку на теле (как в Neptune)
        if (now - lastAnchorSwitch > ThreadLocalRandom.current().nextLong(110, 430)) {
            currentAnchorIndex = ThreadLocalRandom.current().nextInt(HEIGHT_FACTORS.length);
            lastAnchorSwitch = now;
        }
        float heightFactor = HEIGHT_FACTORS[currentAnchorIndex];
        double targetX = entity.getX();
        double targetY = entity.getY() + (entity.getHeight() * heightFactor);
        double targetZ = entity.getZ();

        // 2. Базовый расчет углов
        Vec3d eyes = mc.player.getEyePos();
        Vec3d targetEyes = new Vec3d(targetX, targetY, targetZ);
        Vec3d direction = targetEyes.subtract(eyes);

        double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float targetYaw = (float) (Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0);
        float targetPitch = (float) (-Math.toDegrees(Math.atan2(direction.y, horizontal)));

        // 3. Применяем "SpookyTime" шум (из Aethereal)
        long sinceAttack = now - stAttackWindowStart;
        
        // Если только что ударили - делаем микро-рывок
        if (stAttackWindowStart > 0L && sinceAttack < 180L) {
            float microYaw = mc.player.getYaw() + (random.nextFloat() - 0.5f) * 0.04f;
            float microPitch = mc.player.getPitch() + (random.nextFloat() - 0.5f) * 0.02f;
            mc.player.setYaw(microYaw);
            mc.player.setPitch(microPitch);
            return;
        }

        // Шум Перлина (дрожание рук)
        float theta = 0.35f;
        float sigmaYaw = 0.9f;
        float sigmaPitch = 0.35f;
        stNoiseYaw += -theta * stNoiseYaw + sigmaYaw * (float) random.nextGaussian();
        stNoisePitch += -theta * stNoisePitch + sigmaPitch * (float) random.nextGaussian();
        stNoiseYaw = MathHelper.clamp(stNoiseYaw, -5f, 5f);
        stNoisePitch = MathHelper.clamp(stNoisePitch, -2f, 2f);

        // Рывки (Jolts) каждые 30-80 тиков
        stJoltCooldown--;
        if (stJoltTicks <= 0 && stJoltCooldown <= 0) {
            stJoltTicks = 2 + random.nextInt(3);
            stJoltCooldown = 30 + random.nextInt(50);
            stJoltDir = (random.nextBoolean() ? 1f : -1f) * (7f + random.nextFloat() * 12f);
        }
        float jolt = 0f;
        if (stJoltTicks > 0) {
            jolt = stJoltDir * (stJoltTicks / 4f);
            stJoltTicks--;
        }

        // Плавность (с разной скоростью в зависимости от дистанции)
        float yawDelta = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw());
        float pitchDelta = targetPitch - mc.player.getPitch();
        float dist = (float) Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);
        float burst = random.nextFloat();
        
        float moveFrac;
        if (dist > 15f) {
            moveFrac = 0.5f + burst * 0.3f;
        } else if (dist > 5f) {
            moveFrac = 0.3f + burst * 0.3f;
        } else {
            moveFrac = 0.12f + burst * 0.23f;
        }

        // Итоговый поворот
        float rawYaw = mc.player.getYaw() + yawDelta * moveFrac + stNoiseYaw + jolt;
        float rawPitch = mc.player.getPitch() + pitchDelta * moveFrac + stNoisePitch;

        // Микро-корректировка (чтобы не было идеально ровно)
        rawYaw += (random.nextFloat() - 0.5f) * 0.06f;
        rawPitch += (random.nextFloat() - 0.5f) * 0.04f;

        mc.player.setYaw(rawYaw);
        mc.player.setPitch(MathHelper.clamp(rawPitch, -90f, 90f));
    }
    }
