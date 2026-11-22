package com.rocket.ikaclient.utils;

import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class ChatListener {

    public static void register() {
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            // Если сообщение начинается с точки - обрабатываем
            if (message.startsWith(".")) {
                handleCommand(message);
                return false; // false значит "не отправлять на сервер"
            }
            return true; // обычные сообщения отправляем
        });
    }

    private static void handleCommand(String message) {
        String[] args = message.split(" ");
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // Команда .friend
        if (args[0].equalsIgnoreCase(".friend")) {
            if (args.length < 2) {
                sendMessage(mc, "§c[Ika] Usage: .friend <add/remove/list> [nick]");
                return;
            }

            String action = args[1].toLowerCase();

            if (action.equals("add") && args.length >= 3) {
                FriendManager.add(args[2]);
                sendMessage(mc, "§a[Ika] Added §e" + args[2] + " §ato friends.");
            } 
            else if (action.equals("remove") && args.length >= 3) {
                FriendManager.remove(args[2]);
                sendMessage(mc, "§a[Ika] Removed §e" + args[2] + " §afrom friends.");
            } 
            else if (action.equals("list")) {
                if (FriendManager.getFriends().isEmpty()) {
                    sendMessage(mc, "§e[Ika] Friend list is empty.");
                } else {
                    sendMessage(mc, "§a[Ika] Friends: §e" + String.join(", ", FriendManager.getFriends()));
                }
            } 
            else {
                sendMessage(mc, "§c[Ika] Usage: .friend <add/remove/list> [nick]");
            }
        }
    }

    private static void sendMessage(MinecraftClient mc, String text) {
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal(text), false);
        }
    }
}
