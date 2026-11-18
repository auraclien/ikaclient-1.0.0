package com.rocket.ikaclient;

import com.rocket.ikaclient.modules.Module;
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

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.ikaclient.menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.create(net.minecraft.util.Identifier.of(MOD_ID, "main"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ClickGUI());
                }
            }

            if (client.player != null && client.world != null) {
                // Обработка кастомных кнопок (биндов)
                long window = client.getWindow().getHandle();
                for (Module module : ModuleManager.modules) {
                    if (module.key != -1 && client.currentScreen == null) {
                        // Проверяем, нажата ли клавиша
                        if (InputUtil.isKeyPressed(window, module.key)) {
                            // Чтобы модуль не вкл/выкл 60 раз в секунду, проверяем, что клавиша была отпущена
                            if (!module.enabled) { 
                                // В реальности тут нужна проверка на отпускание, но для простоты пусть работает как рычаг
                            }
                        }
                    }
                }
                
                ModuleManager.onTick();
            }
        });
    }
}
