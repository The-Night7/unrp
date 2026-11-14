package com.myriam.unrp.resources;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ResourceNamespaceTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets");
    private static final Path UNRP = ASSETS.resolve("unrp");

    private static JsonObject read(Path path) throws IOException {
        try (var reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    @Test
    void customAssetsUseOneNamespace() throws IOException {
        try (var paths = Files.list(ASSETS)) {
            assertEquals(Set.of("unrp"), paths.filter(Files::isDirectory)
                    .map(p -> p.getFileName().toString()).collect(java.util.stream.Collectors.toSet()));
        }
    }

    @Test
    void localModelsExistAndParentsDoNotCycle() throws IOException {
        Path root = UNRP.resolve("models");
        Map<String, JsonObject> models = new HashMap<>();
        try (var paths = Files.walk(root)) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
                String id = root.relativize(path).toString().replace('\\', '/').replaceFirst("\\.json$", "");
                models.put("unrp:" + id, read(path));
            }
        }
        for (var entry : models.entrySet()) {
            Set<String> parents = new HashSet<>();
            String id = entry.getKey();
            while (id.startsWith("unrp:")) {
                assertTrue(parents.add(id), "Model parent cycle: " + parents + " -> " + id);
                JsonObject model = models.get(id);
                assertNotNull(model, "Missing model: " + id);
                if (!model.has("parent")) break;
                id = model.get("parent").getAsString();
            }
            if (entry.getValue().has("overrides")) {
                for (var override : entry.getValue().getAsJsonArray("overrides")) {
                    String target = override.getAsJsonObject().get("model").getAsString();
                    if (target.startsWith("unrp:")) {
                        assertTrue(models.containsKey(target), "Missing override model: " + target);
                    }
                }
            }
        }
    }

    @Test
    void particleSpritesExistAndMoteVariantsStayDistinct() throws IOException {
        Path root = UNRP.resolve("particles");
        try (var paths = Files.list(root)) {
            for (Path file : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
                for (var texture : read(file).getAsJsonArray("textures")) {
                    String id = texture.getAsString();
                    assertTrue(id.startsWith("unrp:"), "Unexpected particle sprite namespace: " + id);
                    assertTrue(Files.isRegularFile(UNRP.resolve("textures/particle/" + id.substring(5) + ".png")),
                            "Missing particle sprite: " + id);
                }
            }
        }
        assertNotEquals(read(root.resolve("mote.json")), read(root.resolve("aura_mote.json")));
    }
}
