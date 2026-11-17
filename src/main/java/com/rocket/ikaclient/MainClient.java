package com.rocket.ikaclient;

import com.rocket.ikaclient.modules.ModuleManager;
import com.rocket.ikaclient.ui.ClickGUI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class MainClient implements ClientModInitializer {
    public static final String MOD_ID = "ikaclient";
    private static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        HudRenderer.register();

        // Привязка меню на Right Shift
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.ikaclient.menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.create(net.minecraft.util.Identifier.of(MOD_ID, "main"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Открытие меню
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ClickGUI());
                }
            }

            if (client.player != null && client.world != null) {
                ModuleManager.onTick();
            }
        });
    }
}
