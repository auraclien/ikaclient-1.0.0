package com.rocket.ikaclient.modules;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static final List<Module> modules = new ArrayList<>();

    public static void init() {
        modules.clear();
        
        // Combat
        modules.add(new KillAura());
        
        // Movement
        modules.add(new FullBridge());
        modules.add(new AutoSprint());
        
        // Render
        modules.add(new ESP());
    }

    // Мы вызываем это из MainClient. Внутри модулей уже есть проверка на enabled.
    public static void onTick() {
        for (Module module : modules) {
            module.onTick();
        }
    }
}
