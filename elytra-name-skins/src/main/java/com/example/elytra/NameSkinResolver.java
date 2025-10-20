package com.example.elytra;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NameSkinResolver { // Deprecated duplicate; not used
    private static final Gson GSON = new Gson();
    private static final Path CONFIG = FabricLoader.getInstance().getConfigDir().resolve("elytra_name_skins.json");

    private static volatile Config CACHE = null;

    public static String resolve(String displayName) {
        Config cfg = load();
        String lower = displayName.toLowerCase();
        for (Mapping m : cfg.mappings) {
            if (ipatternMatches(lower, m.match.toLowerCase())) {
                return m.skin;
            }
        }
        return cfg.fallback;
    }

    private static boolean ipatternMatches(String textLower, String patternLower) {
        // pattern com '*' como coringa (qualquer sequência). Ex.: *veteran*
        String[] parts = patternLower.split("\\*");
        int idx = 0;
        for (String part : parts) {
            if (part.isEmpty()) continue;
            int found = textLower.indexOf(part, idx);
            if (found < 0) return false;
            idx = found + part.length();
        }
        // se o pattern não começa com '*', deve casar no início
        if (!patternLower.startsWith("*") && !textLower.startsWith(parts.length > 0 ? parts[0] : "")) {
            return false;
        }
        // se o pattern não termina com '*', deve casar até o fim
        if (!patternLower.endsWith("*") && !textLower.endsWith(parts.length > 0 ? parts[parts.length - 1] : "")) {
            return false;
        }
        return true;
    }

    private static synchronized Config load() {
        if (CACHE != null) return CACHE;
        if (Files.exists(CONFIG)) {
            try (InputStreamReader r = new InputStreamReader(Files.newInputStream(CONFIG), StandardCharsets.UTF_8)) {
                JsonObject obj = GSON.fromJson(r, JsonObject.class);
                Config cfg = new Config();
                cfg.mappings = new ArrayList<>();
                JsonArray arr = obj.has("mappings") && obj.get("mappings").isJsonArray() ? obj.getAsJsonArray("mappings") : new JsonArray();
                for (JsonElement el : arr) {
                    if (!el.isJsonObject()) continue;
                    JsonObject m = el.getAsJsonObject();
                    String match = m.has("match") ? m.get("match").getAsString() : "";
                    String skin = m.has("skin") ? m.get("skin").getAsString() : "";
                    if (!match.isEmpty() && !skin.isEmpty()) {
                        cfg.mappings.add(new Mapping(match, skin));
                    }
                }
                cfg.fallback = obj.has("fallback") ? obj.get("fallback").getAsString() : "";
                CACHE = cfg;
                return cfg;
            } catch (IOException e) {
                // ignora e cai para default
            }
        }
        Config def = defaultConfig();
        try {
            Files.createDirectories(CONFIG.getParent());
            Files.writeString(CONFIG, GSON.toJson(toJson(def)), StandardCharsets.UTF_8);
        } catch (IOException ignored) {}
        CACHE = def;
        return def;
    }

    private static JsonObject toJson(Config cfg) {
        JsonObject obj = new JsonObject();
        JsonArray arr = new JsonArray();
        for (Mapping m : cfg.mappings) {
            JsonObject mo = new JsonObject();
            mo.addProperty("match", m.match);
            mo.addProperty("skin", m.skin);
            arr.add(mo);
        }
        obj.add("mappings", arr);
        obj.addProperty("fallback", cfg.fallback);
        return obj;
    }

    private static Config defaultConfig() {
        Config c = new Config();
        c.mappings = new ArrayList<>();
        c.mappings.add(new Mapping("*Veteran*", "veteran_cape"));
        c.mappings.add(new Mapping("*Rainbow*", "rainbow"));
        c.fallback = ""; // vazio = usar vanilla quando não houver match
        return c;
    }

    static class Config {
        List<Mapping> mappings;
        String fallback;
    }

    static class Mapping {
        final String match;
        final String skin;
        Mapping(String match, String skin) { this.match = match; this.skin = skin; }
    }
}
