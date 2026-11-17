package com.rocket.ikaclient.modules;

import net.minecraft.client.MinecraftClient;

public abstract class Module {
    public String name;
    public boolean enabled;
    public int key;
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    public Module(String name, int key) {
        this.name = name;
        this.key = key;
        this.enabled = false;
    }

    public void toggle() {
        this.enabled = !this.enabled;
        if (this.enabled) onEnable(); else onDisable();
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
}
