package fr.dreamin.dreamHud.internal.config;

import fr.dreamin.api.config.Configurations;
import fr.dreamin.api.service.DreaminAutoService;
import fr.dreamin.api.service.DreaminService;
import fr.dreamin.dreamHud.DreamHud;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Default runtime implementation of the {@link CodexService}.
 *
 * <p>This service is automatically registered through the
 * {@link DreaminAutoService} mechanism and provides centralized access to
 * all plugin configuration values defined in {@code config.json}.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Ensure the existence and validity of the configuration file.</li>
 *   <li>Deserialize {@code config.json} into a {@link CodexService.PluginConfig} instance.</li>
 *   <li>Expose loaded settings through {@link #getConfig()} for use by all other services.</li>
 * </ul>
 *
 * <p>If the configuration cannot be loaded, DreamHud will log an error and
 * automatically disable itself to prevent undefined behavior.</p>
 *
 * <p><strong>Example:</strong></p>
 * <pre>{@code
 * CodexService codex = DreamHud.getService(CodexService.class);
 * CodexService.PluginConfig cfg = codex.getConfig();
 *
 * if (cfg.debug) {
 *   plugin.getLogger().info("Debug mode is enabled!");
 * }
 * }</pre>
 *
 * @see CodexService
 * @see CodexService.PluginConfig
 * @see DreaminAutoService
 * @see DreaminService
 *
 * @author Dreamin
 * @since 1.0.0
 */
@RequiredArgsConstructor
@DreaminAutoService(value = CodexService.class)
public final class ICodexService implements DreaminService, CodexService {

  private final @NotNull DreamHud plugin;

  private @NotNull PluginConfig config;

  /**
   * Creates a new {@link ICodexService} instance and immediately loads
   * the plugin configuration.
   *
   * @param plugin the parent plugin instance
   */
  public ICodexService(final @NotNull DreamHud plugin) {
    this.plugin = plugin;
    load();
  }

  // ##############################################################
  // ---------------------- SERVICE METHODS -----------------------
  // ##############################################################

  @Override
  public void load() {
    plugin.getDataFolder().mkdirs();
    loadConfigFile();
  }

  @Override
  public @NotNull PluginConfig getConfig() {
    return this.config;
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  /**
   * Loads the configuration file for DreamHud from {@code config.json}.
   *
   * <p>If the file does not exist in the plugin’s data directory,
   * it will be automatically copied from the plugin JAR resources.</p>
   *
   * <p>Once available, the configuration file is deserialized into
   * a {@link PluginConfig} object using {@link Configurations#copyAndLoadJson}.</p>
   *
   * <p><strong>Behavior:</strong></p>
   * <ul>
   *   <li>Logs a confirmation message upon successful load.</li>
   *   <li>If an {@link IOException} occurs, logs the error and disables the plugin.</li>
   *   <li>This ensures that DreamHud cannot operate without a valid configuration.</li>
   * </ul>
   *
   * <p><strong>Throws:</strong></p>
   * <ul>
   *   <li>{@link IOException} — if the file cannot be read or written properly.</li>
   * </ul>
   *
   * <p><strong>Example:</strong></p>
   * <pre>{@code
   * this.config = Configurations.copyAndLoadJson(
   *     plugin.getResource("config.json"),
   *     new File(plugin.getDataFolder(), "config.json"),
   *     PluginConfig.class
   * );
   * }</pre>
   */
  private void loadConfigFile() {
    try {
      this.config = Configurations.copyAndLoadJson(
        this.plugin.getResource("config.json"),
        Paths.get(this.plugin.getDataFolder().getPath(), "config.json").toFile(),
        PluginConfig.class
      );
      this.plugin.getLogger().info("Configuration file loaded.");
    } catch (IOException e) {
      this.plugin.getLogger().severe(String.format("Unable to load configuration file. %s", e));
      this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
    }
  }

}
