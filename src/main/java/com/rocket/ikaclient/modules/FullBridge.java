package com.rocket.ikaclient.modules;

import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public class FullBridge extends Module {
    public FullBridge() {
        super("FullBridge", GLFW.GLFW_KEY_B); // Вкл/выкл на B
    }

    @Override
    public void onTick() {
        if (!enabled || mc.player == null || mc.world == null) return;

        // Проверяем, что в руке блок
        if (!(mc.player.getMainHandStack().getItem() instanceof BlockItem)) return;

        // Логика авто-моста (упрощенная): если смотрим вниз и идем вперед, ставим блок
        if (mc.player.isOnGround() && mc.options.forwardKey.isPressed()) {
            mc.player.setPitch(80.0f); // Смотрим вниз
            BlockPos below = mc.player.getBlockPos().down();
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, 
                new net.minecraft.util.hit.BlockHitResult(
                    net.minecraft.util.math.Vec3d.ofCenter(below), 
                    net.minecraft.util.math.Direction.UP, 
                    below, false
                )
            );
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
