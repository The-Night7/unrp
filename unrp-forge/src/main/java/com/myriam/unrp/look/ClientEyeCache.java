package com.myriam.unrp.look;

import java.util.UUID;

public final class ClientEyeCache {
    private ClientEyeCache() {}

    public static String get(UUID id) {

        for (String path : com.myriam.unrp.pass.PassBridge.paths(id)) {
            if (path.endsWith("_am") || path.equals("aura_manipulation")) continue;
            java.util.Set<String> k = com.myriam.unrp.pass.PassBridge.keysOf(id, path);
            if (k != null && k.contains("toggle")) return path;
        }
        return "";
    }

    public static void clearAll() {}
}
