package com.rocket.ikaclient.modules;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.lwjgl.glfw.GLFW;

public class FullBridge extends Module {

    public FullBridge() {
        super("FullBright", GLFW.GLFW_KEY_B);
    }

    @Override
    public void onTick() {
        if (!enabled || mc.player == null) return;
        
        // Выдаем бесконечное ночное зрение
        if (!mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            // Убираем эффект при выключении
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }
}
