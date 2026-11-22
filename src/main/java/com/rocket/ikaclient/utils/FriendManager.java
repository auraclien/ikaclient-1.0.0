package com.rocket.ikaclient.utils;

import java.util.HashSet;
import java.util.Set;

public class FriendManager {
    // Храним ники в нижнем регистре, чтобы не зависеть от регистра
    private static final Set<String> friends = new HashSet<>();

    public static void add(String name) {
        friends.add(name.toLowerCase());
    }

    public static void remove(String name) {
        friends.remove(name.toLowerCase());
    }

    public static boolean isFriend(String name) {
        if (name == null) return false;
        return friends.contains(name.toLowerCase());
    }

    public static Set<String> getFriends() {
        return friends;
    }
}
