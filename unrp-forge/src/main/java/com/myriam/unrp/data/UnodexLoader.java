package com.myriam.unrp.data;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.myriam.unrp.data.Models.Ability;
import com.myriam.unrp.data.Models.Character;
import com.myriam.unrp.data.Models.School;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

public class UnodexLoader extends SimplePreparableReloadListener<UnodexData> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String NAMESPACE = "unrp";
    private static final String ROOT = "unodex";
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    @Override
    protected UnodexData prepare(ResourceManager manager, ProfilerFiller profiler) {
        RarityTable rarity = null;
        Map<String, School> schools = new LinkedHashMap<>();
        Map<String, Character> characters = new LinkedHashMap<>();
        Map<String, Ability> abilities = new LinkedHashMap<>();
        Map<String, AbilityPower> powers = new LinkedHashMap<>();
        Map<String, String> aliases = new LinkedHashMap<>();
        Map<String, Integer> pool = new LinkedHashMap<>();
        Map<String, double[][]> ranges = new LinkedHashMap<>();

        Map<ResourceLocation, Resource> files = manager.listResources(ROOT,
                rl -> rl.getNamespace().equals(NAMESPACE) && rl.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> e : files.entrySet()) {
            String path = e.getKey().getPath().substring(ROOT.length() + 1); // ex. "characters/lore.json"
            try (Reader reader = e.getValue().openAsReader()) {
                JsonObject root = GSON.fromJson(reader, JsonObject.class);
                if (path.equals("rarity.json")) {
                    rarity = readRarity(root);
                } else if (path.equals("schools.json")) {
                    for (JsonElement el : root.getAsJsonArray("schools")) {
                        School s = GSON.fromJson(el, School.class);
                        schools.put(s.id, s);
                    }
                } else if (path.equals("abilities.json")) {
                    for (JsonElement el : root.getAsJsonArray("abilities")) {
                        Ability a = GSON.fromJson(el, Ability.class);
                        abilities.put(a.id, a);
                    }
                } else if (path.equals("ability_powers.json")) {
                    for (Map.Entry<String, JsonElement> pw : root.getAsJsonObject("powers").entrySet()) {
                        powers.put(pw.getKey(), GSON.fromJson(pw.getValue(), AbilityPower.class));
                    }
                    if (root.has("aliases")) {
                        for (Map.Entry<String, JsonElement> al : root.getAsJsonObject("aliases").entrySet()) {
                            aliases.put(al.getKey(), al.getValue().getAsString());
                        }
                    }
                } else if (path.equals("ability_pool.json")) {
                    for (Map.Entry<String, JsonElement> pe : root.getAsJsonObject("pool").entrySet()) pool.put(pe.getKey(), pe.getValue().getAsInt());
                } else if (path.equals("stat_ranges.json")) {
                    for (Map.Entry<String, JsonElement> re : root.getAsJsonObject("ranges").entrySet()) {
                        double[][] r = new double[5][2];
                        for (int i = 0; i < 5; i++) {
                            r[i][0] = re.getValue().getAsJsonArray().get(i).getAsJsonArray().get(0).getAsDouble();
                            r[i][1] = re.getValue().getAsJsonArray().get(i).getAsJsonArray().get(1).getAsDouble();
                        }
                        ranges.put(re.getKey(), r);
                    }
                } else if (path.startsWith("characters/")) {
                    for (JsonElement el : root.getAsJsonArray("characters")) {
                        Character c = GSON.fromJson(el, Character.class);
                        characters.put(c.id, c);
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("[unrp] Skipped file ({}): {}", e.getKey(), ex.toString());
            }
        }
        UnodexData data = new UnodexData(rarity, schools, characters, abilities);
        data.powers.putAll(powers);
        data.aliases.putAll(aliases);
        data.pool.putAll(pool);
        data.ranges.putAll(ranges);
        return data;
    }

    private static RarityTable readRarity(JsonObject root) {
        java.util.List<RarityTier> list = new java.util.ArrayList<>();
        root.getAsJsonArray("tiers").forEach(el -> list.add(GSON.fromJson(el, RarityTier.class)));
        return new RarityTable(list);
    }

    @Override
    protected void apply(UnodexData data, ResourceManager manager, ProfilerFiller profiler) {
        UnodexData.set(data);
        LOGGER.info("[unrp] Loaded Unodex: {} characters, {} abilities ({} playable powers), {} schools/groups, rarity {}",
                data.characters.size(), data.abilities.size(), data.powers.size(), data.schools.size(),
                data.rarity == null ? "ABSENTE" : "ok");
    }
}
