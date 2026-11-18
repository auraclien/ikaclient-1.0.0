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

    // Вызывается из MainClient каждый тик
    public static void onTick() {
        for (Module module : modules) {
            module.onTick();
        }
    }

    // ЭТОТ МЕТОД МЫ ДОБАВИЛИ, ЧТОБЫ HUD МОГ НАЙТИ КИЛКУ
    public static Module getModule(String name) {
        for (Module module : modules) {
            if (module.name.equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }
}
