package fr.dreamin.dreamHud.internal.service;

import com.fasterxml.jackson.core.type.TypeReference;
import fr.dreamin.api.config.Configurations;
import fr.dreamin.api.service.DreaminAutoService;
import fr.dreamin.api.service.DreaminService;
import fr.dreamin.dreamHud.DreamHud;
import fr.dreamin.dreamHud.api.TranslationMetaService;
import fr.dreamin.dreamHud.internal.config.CodexService;
import fr.dreamin.dreamHud.internal.config.ICodexService;
import fr.dreamin.dreamHud.internal.pack.font.FontLoaderService;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Supplier;

/**
 * Default implementation of the {@link TranslationMetaService}.
 *
 * <p>This service manages the {@code translation_meta.json} file, which maps
 * translation keys to their precomputed pixel widths. These widths are used
 * by DreamHud’s rendering system to ensure consistent text alignment across
 * translated HUD elements.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Load and cache translation metadata at startup.</li>
 *   <li>Provide a fast lookup mechanism for key widths.</li>
 *   <li>Fallback gracefully to font-based measurement using
 *       {@link FontLoaderService} when metadata is missing.</li>
 *   <li>Optionally log debug information for validation and tracing.</li>
 * </ul>
 *
 * <p>Translation metadata improves performance by avoiding repeated text
 * measurement at runtime. Developers can update {@code translation_meta.json}
 * manually or through build scripts that precompute key widths.</p>
 *
 * <p><strong>Example:</strong></p>
 * <pre>{@code
 * TranslationMetaService meta = DreamHud.getService(TranslationMetaService.class);
 * OptionalInt width = meta.getWidthForKey("hud.objective.complete");
 *
 * width.ifPresent(w -> logger.info("Key width = " + w + "px"));
 * }</pre>
 *
 * @see TranslationMetaService
 * @see FontLoaderService
 * @see CodexService
 * @see fr.dreamin.dreamHud.internal.config.ICodexService
 *
 * @author Dreamin
 * @since 1.0.0
 */
@DreaminAutoService(value = TranslationMetaService.class, dependencies = {ICodexService.class})
public final class TranslationMetaServiceImpl implements DreaminService, TranslationMetaService {

  /** Name of the JSON file containing translation width metadata. */
  private static final String META_FILE_NAME = "translation_meta.json";

  private final @NotNull DreamHud plugin;
  private final @NotNull CodexService codexService;
  private final @NotNull Supplier<FontLoaderService> fontLoaderSupplier;

  /** Immutable map of translation keys to their measured pixel widths. */
  private Map<String, Integer> metaMap = Collections.emptyMap();

  /**
   * Constructs a new instance of {@code TranslationMetaServiceImpl} using the
   * default service references.
   *
   * @param plugin the main {@link DreamHud} plugin instance
   */
  public TranslationMetaServiceImpl(final @NotNull DreamHud plugin) {
    this(plugin, DreamHud.getService(CodexService.class), () -> DreamHud.getService(FontLoaderService.class));
  }

  /**
   * Internal constructor used for testing and dependency injection.
   *
   * @param plugin             the DreamHud plugin instance
   * @param codexService       the configuration service
   * @param fontLoaderSupplier supplier for the {@link FontLoaderService}
   */
  TranslationMetaServiceImpl(
    final @NotNull DreamHud plugin,
    final @NotNull CodexService codexService,
    final @NotNull Supplier<FontLoaderService> fontLoaderSupplier
  ) {
    this.plugin = plugin;
    this.codexService = codexService;
    this.fontLoaderSupplier = fontLoaderSupplier;
  }
  
  // ###############################################################
  // ---------------------- SERVICE METHODS ------------------------
  // ###############################################################

  /**
   * Loads the translation metadata from the JSON file.
   *
   * <p>If the file does not exist, an empty file is created automatically.
   * The loaded map is then wrapped in an unmodifiable view to prevent
   * accidental mutation at runtime.</p>
   *
   * <p>Logs the number of loaded entries when debug mode is active.</p>
   */
  @Override
  public void onLoad(@NotNull Plugin plugin) {
    try {
      final var file = new File(this.plugin.getDataFolder(), META_FILE_NAME);

      if (!file.exists()) {
        if (this.codexService.getConfig().debug)
          this.plugin.getLogger().info("Création du fichier translations_meta.json (vide)");
        final var parent = file.getParentFile();
        if (parent != null) Files.createDirectories(parent.toPath());
        Configurations.MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, new LinkedHashMap<>());
      }

      final Map<String, Integer> loadedMeta = Configurations.MAPPER.readValue(file, new TypeReference<>() {});
      this.metaMap = Collections.unmodifiableMap(new LinkedHashMap<>(loadedMeta));

      if (this.codexService.getConfig().debug)
        this.plugin.getLogger().info(String.format("Translation chargées : %s", this.metaMap.size()));
    } catch (IOException e) {
      this.plugin.getLogger().severe("Erreur de lecture translations_meta.json : " + e.getMessage());
    }
  }

  @Override
  public @NotNull OptionalInt getWidthForKey(final @NotNull String key) {
    Objects.requireNonNull(key, "key");

    final var width = this.metaMap.get(key);
    if (width != null) {
      return OptionalInt.of(width);
    }

    this.plugin.getLogger().warning(String.format("Aucune largeur renseignée pour la clé de traduction '%s'", key));

    final var defaultFont = this.codexService.getConfig().default_font_name;
    if (defaultFont == null || defaultFont.isBlank()) {
      return OptionalInt.empty();
    }

    try {
      final var fontLoader = this.fontLoaderSupplier.get();
      final var fallbackWidth = fontLoader.getStringWidth(key, defaultFont);
      return OptionalInt.of(fallbackWidth);
    } catch (IllegalStateException ex) {
      this.plugin.getLogger().severe("FontLoaderService indisponible pour le fallback de traduction : " + ex.getMessage());
      return OptionalInt.empty();
    }
  }
}
