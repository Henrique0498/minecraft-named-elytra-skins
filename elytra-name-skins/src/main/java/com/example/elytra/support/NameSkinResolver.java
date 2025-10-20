package com.example.elytra.support;

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
import java.text.Normalizer;

public class NameSkinResolver {
    private static final Gson GSON = new Gson();
    private static final Path CONFIG = FabricLoader.getInstance().getConfigDir().resolve("elytra_name_skins.json");

    private static volatile Config CACHE = null;

    public static String resolve(String displayName) {
        // Special bootstrap call to ensure default mappings are present and saved once
        if ("__init__".equals(displayName)) {
            ensureDefaultsSaved();
            return "";
        }
        Config cfg = load();
        String lower = normalize(displayName);
        for (Mapping m : cfg.mappings) {
            if (ipatternMatches(lower, normalize(m.match))) {
                return m.skin;
            }
        }
        return cfg.fallback;
    }

    private static boolean ipatternMatches(String textLower, String patternLower) {
        String[] parts = patternLower.split("\\*");
        int idx = 0;
        for (String part : parts) {
            if (part.isEmpty()) continue;
            int found = textLower.indexOf(part, idx);
            if (found < 0) return false;
            idx = found + part.length();
        }
        if (!patternLower.startsWith("*") && !textLower.startsWith(parts.length > 0 ? parts[0] : "")) {
            return false;
        }
        if (!patternLower.endsWith("*") && !textLower.endsWith(parts.length > 0 ? parts[parts.length - 1] : "")) {
            return false;
        }
        return true;
    }

    /**
     * Normaliza texto para comparação case-insensitive e mais robusta:
     * - Converte para minúsculas
     * - Remove acentos/diacríticos (NFD -> remove \p{M})
     * - Substitui qualquer caractere não alfanumérico (exceto '*') por espaço
     * - Compacta espaços múltiplos
     */
    private static String normalize(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase();
        // preservar '*' (coringa), remover demais pontuações
        n = n.replaceAll("[^a-z0-9*]+", " ");
        n = n.trim().replaceAll("\\s+", " ");
        return n;
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
                // Merge in any missing defaults without clobbering user entries
                boolean changed = mergeDefaults(cfg);
                CACHE = cfg;
                if (changed) {
                    persist(cfg);
                }
                return cfg;
            } catch (IOException e) {
            }
        }
        Config def = defaultConfig();
        persist(def);
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
        // Complete default mappings for all 64 entries (names and synonyms)
        // 1-12 (Creators)
        c.mappings.add(new Mapping("*TheTerrain*", "theterrain"));
        c.mappings.add(new Mapping("*Handsome*", "theterrain"));
        c.mappings.add(new Mapping("*Ivan*", "ivan"));
        c.mappings.add(new Mapping("*Dream*", "dream"));
        c.mappings.add(new Mapping("*Technoblade*", "technoblade"));
        c.mappings.add(new Mapping("*Techno*", "technoblade"));
        c.mappings.add(new Mapping("*Fundy*", "fundy"));
        c.mappings.add(new Mapping("*Furry*", "fundy"));
        c.mappings.add(new Mapping("*fruitberries*", "fruitberries"));
        c.mappings.add(new Mapping("*Gauntlet*", "fruitberries"));
        c.mappings.add(new Mapping("*Geosquare*", "geosquare"));
        c.mappings.add(new Mapping("*Tall Cactus*", "geosquare"));
        c.mappings.add(new Mapping("*CommandGeek*", "geek"));
        c.mappings.add(new Mapping("*BlueCommander*", "bluecommander"));
        c.mappings.add(new Mapping("*PaulGG*", "paulgg"));
        c.mappings.add(new Mapping("*Slicedlime*", "slicedlime"));
        c.mappings.add(new Mapping("*billwurtz*", "billwurtz"));

        // 13-22 (Blocks/items)
        c.mappings.add(new Mapping("*Bed*", "bed"));
        c.mappings.add(new Mapping("*Cookie*", "cookie"));
        c.mappings.add(new Mapping("*Stronghold*", "endportal"));
        c.mappings.add(new Mapping("*End Portal*", "endportal"));
        c.mappings.add(new Mapping("*Nether Portal*", "netherportal"));
        c.mappings.add(new Mapping("*Magma*", "magma"));
        c.mappings.add(new Mapping("*Blackstone*", "gildedblackstone"));
        c.mappings.add(new Mapping("*Calvin*", "gildedblackstone"));
        c.mappings.add(new Mapping("*Mushroom*", "red_mushroom"));
        c.mappings.add(new Mapping("*Red Mushroom*", "red_mushroom"));
        c.mappings.add(new Mapping("*TNT*", "tnt"));
        c.mappings.add(new Mapping("*Lava*", "lava"));
        c.mappings.add(new Mapping("*Water*", "water"));
        c.mappings.add(new Mapping("*Bucket*", "water"));

        // 23-30 (Minerals)
        c.mappings.add(new Mapping("*Coal*", "coal"));
        c.mappings.add(new Mapping("*Iron*", "iron"));
        c.mappings.add(new Mapping("*Gold*", "gold"));
        c.mappings.add(new Mapping("*Redstone*", "redstone"));
        c.mappings.add(new Mapping("*Lapis*", "lapis"));
        c.mappings.add(new Mapping("*Diamond*", "diamond"));
        c.mappings.add(new Mapping("*Emerald*", "emerald"));
        c.mappings.add(new Mapping("*Netherite*", "netherite"));

        // 31-36 (Icons/paintings)
        c.mappings.add(new Mapping("*Hardcore*", "hardcore"));
        c.mappings.add(new Mapping("*Philza*", "hardcore"));
        c.mappings.add(new Mapping("*Food*", "hunger"));
        c.mappings.add(new Mapping("*Hunger*", "hunger"));
        c.mappings.add(new Mapping("*Golden Apple*", "gapple"));
        c.mappings.add(new Mapping("*Gapple*", "gapple"));
        c.mappings.add(new Mapping("*Citrus*", "citrus"));
        c.mappings.add(new Mapping("*Orange*", "citrus"));
        c.mappings.add(new Mapping("*Graham*", "graham_painting"));
        c.mappings.add(new Mapping("*Wanderer*", "man_painting"));

        // 37-43 (Mobs)
        c.mappings.add(new Mapping("*Pink Axolotl*", "axolotl_pink"));
        c.mappings.add(new Mapping("*Axolotl (Pink)*", "axolotl_pink"));
        c.mappings.add(new Mapping("*Yellow Axolotl*", "axolotl_yellow"));
        c.mappings.add(new Mapping("*Axolotl (Yellow)*", "axolotl_yellow"));
        c.mappings.add(new Mapping("*Blue Axolotl*", "axolotl_blue"));
        c.mappings.add(new Mapping("*Glow Squid*", "glowsquid"));
        c.mappings.add(new Mapping("*Golem*", "irongolem"));
        c.mappings.add(new Mapping("*Iron Golem*", "irongolem"));
        c.mappings.add(new Mapping("*Strider*", "strider_normal"));
        c.mappings.add(new Mapping("*Cold Strider*", "strider_cold"));

        // 44-53 (Capes)
        c.mappings.add(new Mapping("*Minecon 2011*", "minecon_2011"));
        c.mappings.add(new Mapping("*Minecon 2012*", "minecon_2012"));
        c.mappings.add(new Mapping("*Minecon 2013*", "minecon_2013"));
        c.mappings.add(new Mapping("*Minecon 2015*", "minecon_2015"));
        c.mappings.add(new Mapping("*Minecon 2016*", "minecon_2016"));
        c.mappings.add(new Mapping("*Turtle*", "turtle_cape"));
        c.mappings.add(new Mapping("*Bacon*", "bacon"));
        c.mappings.add(new Mapping("*Veteran*", "veteran_cape"));
        c.mappings.add(new Mapping("*Migration*", "veteran_cape"));
        c.mappings.add(new Mapping("*Millionth Customer*", "millionthcustomer_cape"));
        c.mappings.add(new Mapping("*Julian Clark*", "julianclark"));

        // 54-61 (Logos)
        c.mappings.add(new Mapping("*Illustrator*", "ai"));
        c.mappings.add(new Mapping("*AI*", "ai"));
        c.mappings.add(new Mapping("*PS*", "ps"));
        c.mappings.add(new Mapping("*Premiere*", "premiere"));
        c.mappings.add(new Mapping("*Pr*", "premiere"));
        c.mappings.add(new Mapping("*Synthesia*", "synthesia"));
        c.mappings.add(new Mapping("*Piano*", "synthesia"));
        c.mappings.add(new Mapping("*YouTube*", "youtube"));
        c.mappings.add(new Mapping("*Twitter*", "twitter"));
        c.mappings.add(new Mapping("*Tweet*", "twitter"));
        c.mappings.add(new Mapping("*Red Optfine*", "optifine_red"));
        c.mappings.add(new Mapping("*Blue Optifine*", "optifine_blue"));

        // 62-64 (Styles)
        c.mappings.add(new Mapping("*Rainbow*", "rainbow"));
        c.mappings.add(new Mapping("*Pride*", "rainbow"));
        c.mappings.add(new Mapping("*Solo Cup*", "solojazz"));
        c.mappings.add(new Mapping("*Solo*", "solojazz"));
        c.mappings.add(new Mapping("*Vaporwave*", "vaporwave"));
        c.mappings.add(new Mapping("*Synthwave*", "vaporwave"));

        c.fallback = ""; // vazio = vanilla
        return c;
    }

    private static void ensureDefaultsSaved() {
        Config cfg = load();
        boolean changed = mergeDefaults(cfg);
        if (changed) {
            persist(cfg);
        }
    }

    private static boolean mergeDefaults(Config cfg) {
        Config def = defaultConfig();
        boolean changed = false;
        // Avoid duplicates by (match, skin) pair
        for (Mapping dm : def.mappings) {
            boolean exists = false;
            for (Mapping m : cfg.mappings) {
                if (m.match.equals(dm.match) && m.skin.equals(dm.skin)) { exists = true; break; }
            }
            if (!exists) {
                cfg.mappings.add(dm);
                changed = true;
            }
        }
        return changed;
    }

    private static void persist(Config cfg) {
        try {
            Files.createDirectories(CONFIG.getParent());
            Files.writeString(CONFIG, GSON.toJson(toJson(cfg)), StandardCharsets.UTF_8);
        } catch (IOException ignored) {}
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