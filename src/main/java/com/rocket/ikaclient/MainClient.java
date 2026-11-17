
package com.rocket.ikaclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import org.lwjgl.glfw.GLFW;

public final class MainClient implements ClientModInitializer {

    public static final String MOD_ID = "ikaclient";

    public static KillAura aura;

    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {

        aura = new KillAura();

        toggleKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.ikaclient.toggle_aura",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                KeyBinding.Category.MISC
            )
        );

        HudRenderer.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            while (toggleKey.wasPressed()) {
                aura.toggle();
            }

            aura.tick(client);
        });
    }
}
