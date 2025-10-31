package fr.dreamin.dreamHud.internal.pack.neg;

import fr.dreamin.api.config.Configurations;
import fr.dreamin.api.service.DreaminAutoService;
import fr.dreamin.api.service.DreaminService;
import fr.dreamin.dreamHud.DreamHud;
import fr.dreamin.dreamHud.internal.config.CodexService;
import fr.dreamin.dreamHud.internal.config.ICodexService;
import fr.dreamin.dreamHud.internal.pack.IPackLoaderService;
import fr.dreamin.dreamHud.internal.pack.PackLoaderService;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.ShadowColor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Default implementation of the {@link NegSpaceFontService}.
 *
 * <p>This service handles the loading, registration, and runtime access of
 * negative-space glyphs from the {@code space_split} font. These special glyphs
 * represent variable pixel offsets (both positive and negative) and are used
 * throughout DreamHud for precise visual alignment in HUD layouts.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Load the {@code space_split.png} texture and {@code space_split.json} definition from plugin resources.</li>
 *   <li>Send the negative-space font to the resource pack build.</li>
 *   <li>Maintain a runtime mapping of pixel height → Unicode glyph.</li>
 *   <li>Provide utility methods to generate offset {@link Component}s dynamically.</li>
 * </ul>
 *
 * <p>The glyph data is embedded within the DreamHud JAR and automatically
 * transferred to the generated resource pack via {@link PackLoaderService}.</p>
 *
 * @see NegSpaceFontService
 * @see PackLoaderService
 * @see fr.dreamin.dreamHud.internal.pack.background.BackgroundLoaderService
 * @see fr.dreamin.dreamHud.internal.pack.font.FontLoaderService
 *
 * @since 1.0.0
 */
@RequiredArgsConstructor
@DreaminAutoService(value = NegSpaceFontService.class, dependencies = {ICodexService.class, IPackLoaderService.class})
public final class INegSpaceFontservice implements DreaminService, NegSpaceFontService {

  private static final String TEXTURE_NAME = "space_split.png";
  private static final String JSON_NAME = "space_split.json";

  private final @NotNull DreamHud plugin;

  private final @NotNull CodexService codexService = DreamHud.getService(CodexService.class);
  private final @NotNull PackLoaderService packLoader = DreamHud.getService(PackLoaderService.class);

  private static final int ASCENT = -5000;

  /** Maps a pixel offset (positive or negative) to its corresponding glyph character. */
  private final Map<Integer, Character> heightMap = new LinkedHashMap<>();

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  /**
   * Initializes and loads the negative-space font.
   *
   * <p>This method extracts both the {@code space_split.png} texture and
   * {@code space_split.json} file from the plugin resources, registers them
   * in the resource pack, and parses the glyphs into memory.</p>
   *
   * <p>If debug mode is enabled, a summary of the registered offsets is logged,
   * showing the total number of positive and negative offsets available.</p>
   */
  @Override
  public void onLoad(@NotNull Plugin plugin) {
    try {
      try (var texStream = this.plugin.getResource(TEXTURE_NAME)) {
        if (texStream == null) {
          this.plugin.getLogger().warning(String.format("Texture introuvable dans les ressources : %s", TEXTURE_NAME));
          return;
        }

        final var textureTmp = new File(this.plugin.getDataFolder(), TEXTURE_NAME);
        textureTmp.getParentFile().mkdirs();
        Files.copy(texStream, textureTmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        this.packLoader.sendToFontTextures(textureTmp);
      }

      File jsonTmp;
      try (var jsonStream = this.plugin.getResource(JSON_NAME)) {
        if (jsonStream == null) {
          this.plugin.getLogger().warning(String.format("Fichier JSON introuvable dans les ressources : %s", JSON_NAME));
          return;
        }

        jsonTmp = new File(this.plugin.getDataFolder(), JSON_NAME);
        Files.copy(jsonStream, jsonTmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        this.packLoader.sendToFont(jsonTmp);
      }

      loadFromJson(jsonTmp);

      if (this.codexService.getConfig().debug) {
        long positives = this.heightMap.keySet().stream().filter(i -> i > 0).count();
        long negatives = this.heightMap.keySet().stream().filter(i -> i < 0).count();
        this.plugin.getLogger().info("Font neg (space_split) ajoutée au pack !");
        this.plugin.getLogger().info(String.format("Total splits : %d (pos=%d | neg=%d)", this.heightMap.size(), positives, negatives));
      }

    } catch (IOException e) {
      this.plugin.getLogger().severe(String.format("Erreur lors du chargement de la police negative space : %s", e.getMessage()));
      e.printStackTrace();
    }
  }

  @Override
  public @Nullable Character getCharForHeight(int height) {
    return this.heightMap.get(height);
  }

  @Override
  public @NotNull Component fromOffset(int offset) {
    if (offset == 0 || this.heightMap.isEmpty()) return Component.empty();

    final var splits = this.heightMap.entrySet().stream()
      .sorted(Comparator.comparingInt(e -> -Math.abs(e.getKey())))
      .toList();

    final var result = new ArrayList<Character>();
    var remaining = offset;

    for (final var entry : splits) {
      var h = entry.getKey();
      char c = entry.getValue();

      while(
        (remaining > 0 && h > 0 && remaining >= h) ||
        (remaining < 0 && h < 0 && remaining <= h)
      ) {
        result.add(c);
        remaining -= h;
      }
    }

    if (remaining != 0) {
      var finalRemaining = remaining;
      var closet = splits.stream()
        .min(Comparator.comparingInt(e -> Math.abs(e.getKey() - finalRemaining)))
        .map(Map.Entry::getValue)
        .orElse(null);
      if (closet != null) result.add(closet);
    }

    Component component = Component.empty();
    final var fontKey = Key.key(this.codexService.getConfig().namespace, "space_split");
    for (char c : result) {
      component = component.append(Component.text(c).font(fontKey));
    }

    return component.shadowColor(ShadowColor.none());
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  /**
   * Reads and parses the {@code space_split.json} font definition, populating
   * the internal {@link #heightMap} with all offset → glyph pairs.
   *
   * @param jsonFile the JSON definition file to load
   * @throws IOException if the file cannot be read or parsed
   */
  private void loadFromJson(final @NotNull File jsonFile) throws  IOException {
    var root = Configurations.MAPPER.readTree(jsonFile);
    var providers = root.get("providers");
    if (providers == null || !providers.isArray()) return;

    for (var provider : providers) {
      if (!provider.has("height") || !provider.has("chars")) continue;

      final var height = provider.get("height").asInt();
      var chars = provider.get("chars");
      if (chars.isArray() && !chars.isEmpty()) {
        char c = chars.get(0).asText().charAt(0);
        this.heightMap.put(height, c);
      }
    }

  }

}
