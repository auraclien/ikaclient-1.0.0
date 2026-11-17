package com.rocket.ikaclient.modules;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public static final List<Module> modules = new ArrayList<>();

    public static void init() {
        // Регистрируем модули
        modules.add(new KillAura());
        modules.add(new FullBridge());
    }
}
