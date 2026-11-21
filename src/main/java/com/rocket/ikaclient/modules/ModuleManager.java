package com.rocket.ikaclient.modules;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static final List<Module> modules = new ArrayList<>();

    public static void init() {
        modules.clear();
        modules.add(new KillAura());
        modules.add(new FullBridge());
        modules.add(new AutoSprint());
        modules.add(new ChestESP());
    }

    public static void onTick() {
        for (Module module : modules) {
            module.onTick();
        }
    }

    public static Module getModule(String name) {
        for (Module module : modules) {
            if (module.name.equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }
}
