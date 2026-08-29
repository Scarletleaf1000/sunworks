package me.scarletleaf1000.sunworks.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.scarletleaf1000.sunworks.Sunworks;
import me.scarletleaf1000.sunworks.compat.ponder.PonderSunworksPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

/**
 * Generates Ponder-specific language entries and merges them into the existing
 * {@code en_us.json}. Manual translations are preserved, while scene titles,
 * tag names, and shared Ponder text are pulled from the registered plugin.
 */
public class PonderLangProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final PackOutput output;
    private final String modid;

    public PonderLangProvider(PackOutput output, String modid) {
        this.output = output;
        this.modid = modid;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        Path outputPath = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(modid + "/lang/en_us.json");

        Map<String, String> translations = new TreeMap<>();
        loadExisting(translations);

        PonderIndex.addPlugin(new PonderSunworksPlugin());
        PonderIndex.getLangAccess().provideLang(modid, translations::put);

        JsonObject result = new JsonObject();
        translations.forEach(result::addProperty);

        return DataProvider.saveStable(cachedOutput, result, outputPath);
    }

    private void loadExisting(Map<String, String> target) {
        Path generatedResources = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK).getParent();
        Path mainResources = generatedResources.getParent().resolve("../main/resources").normalize();
        Path langDir = mainResources.resolve("assets/" + modid + "/lang");

        Path defaultFile = langDir.resolve("default/en_us.json");
        Path rootFile = langDir.resolve("en_us.json");

        Path existing = Files.exists(defaultFile) ? defaultFile : Files.exists(rootFile) ? rootFile : null;
        if (existing == null) {
            return;
        }

        try (InputStreamReader reader = new InputStreamReader(
                Files.newInputStream(existing), StandardCharsets.UTF_8)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                if (entry.getValue().isJsonPrimitive()) {
                    target.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read existing language file: " + existing, e);
        }
    }

    @Override
    public String getName() {
        return "Ponder Language Provider";
    }
}
