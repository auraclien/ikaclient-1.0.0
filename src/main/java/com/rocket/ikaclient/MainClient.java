package com.rocket.ikaclient;

import com.rocket.ikaclient.modules.Module;
import com.rocket.ikaclient.modules.ModuleManager;
import com.rocket.ikaclient.ui.ClickGUI;
import com.rocket.ikaclient.utils.ChatListener; // <--- ДОБАВИЛИ ИМПОРТ
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class MainClient implements ClientModInitializer {
    public static final String MOD_ID = "ikaclient";
    private static KeyBinding menuKey;
    private static final Set<Integer> pressedKeys = new HashSet<>();

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        HudRenderer.register();
        ChatListener.register(); // <--- ДОБАВИЛИ ЭТУ СТРОЧКУ

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.ikaclient.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.create(net.minecraft.util.Identifier.of(MOD_ID, "main"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new ClickGUI());
            }

            if (client.player != null && client.world != null) {
                long window = client.getWindow().getHandle();
                for (Module module : ModuleManager.modules) {
                    if (module.key != -1 && client.currentScreen == null) {
                        boolean isPressed = GLFW.glfwGetKey(window, module.key) == GLFW.GLFW_PRESS;
                        if (isPressed && !pressedKeys.contains(module.key)) {
                            module.toggle();
                            pressedKeys.add(module.key);
                        } else if (!isPressed) {
                            pressedKeys.remove(module.key);
                        }
                    }
                }
                ModuleManager.onTick();
            }
        });
    }
}
