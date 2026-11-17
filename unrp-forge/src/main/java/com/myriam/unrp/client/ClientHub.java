package com.myriam.unrp.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class ClientHub {
    private static JsonObject state = new JsonObject();

    private ClientHub() {}

    public static JsonObject get() {
        return state;
    }

    static void set(String json) {
        try {
            state = JsonParser.parseString(json).getAsJsonObject();
        } catch (Exception ignored) {

        }
    }
}
