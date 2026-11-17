package com.rocket.ikaclient.modules;

public class AutoSprint extends Module {

    public AutoSprint() {
        super("AutoSprint", -1); // -1 значит, что клавиши нет, включается только через меню
    }

    @Override
    public void onTick() {
        if (!enabled || mc.player == null || mc.world == null) return;

        boolean movingForward = mc.options.forwardKey.isPressed();
        boolean canSprint = movingForward && !mc.player.isSneaking() && !mc.player.isUsingItem();

        mc.player.setSprinting(canSprint);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setSprinting(false);
        }
    }
}
