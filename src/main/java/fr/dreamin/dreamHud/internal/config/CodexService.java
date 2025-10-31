package fr.dreamin.dreamHud.internal.config;

import net.kyori.adventure.bossbar.BossBar;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Central configuration service for DreamHud.
 *
 * <p>The {@code CodexService} is responsible for loading and providing access to all
 * plugin-related configuration values, including visual rendering parameters,
 * resource pack settings, and internal build options.</p>
 *
 * <p>This service acts as the single source of truth for runtime configuration,
 * ensuring that all subsystems (fonts, backgrounds, HUDs, etc.) use consistent
 * values throughout the plugin lifecycle.</p>
 *
 * <p><strong>Usage example:</strong></p>
 * <pre>{@code
 * CodexService codex = DreamHud.getService(CodexService.class);
 * CodexService.PluginConfig config = codex.getConfig();
 *
 * if (config.debug) {
 *   plugin.getLogger().info("DreamHud running in debug mode!");
 * }
 * }</pre>
 *
 * @author Dreamin
 * @since 1.0.0
 */
public interface CodexService {

  /**
   * Reloads or initializes the configuration from the plugin’s configuration files.
   * <p>This method should be called during plugin startup or when performing a manual reload.</p>
   */
  void load();

  /**
   * Retrieves the currently active {@link PluginConfig} instance.
   *
   * @return the loaded configuration, never {@code null}
   */
  @NotNull
  PluginConfig getConfig();


  // ###############################################################
  // ----------------------- CONFIGURATION -------------------------
  // ###############################################################


  /**
   * Represents the full configuration schema for DreamHud.
   *
   * <p>This class encapsulates all parameters that control the behavior,
   * appearance, and build logic of DreamHud. It is typically deserialized
   * from a YAML configuration file during plugin initialization.</p>
   *
   * <p><strong>Main categories:</strong></p>
   * <ul>
   *   <li><strong>Rendering:</strong> colors, fonts, namespace</li>
   *   <li><strong>Debug:</strong> development and logging options</li>
   *   <li><strong>Resource pack:</strong> paths, build folders, obfuscation</li>
   *   <li><strong>Network:</strong> self-hosting IPs and ports for asset delivery</li>
   * </ul>
   *
   * <p><strong>Example:</strong></p>
   * <pre>{@code
   * CodexService.PluginConfig config = codexService.getConfig();
   * BossBar.Color color = config.bar_color;
   * boolean debug = config.debug;
   * String font = config.default_font_name;
   * }</pre>
   *
   * @author Dreamin
   * @since 1.0.0
   */
  class PluginConfig {
    /** Default {@link BossBar.Color} used for all BarHUDs when not explicitly defined. */
    public BossBar.Color bar_color;

    /** Enables verbose debug logging and in-game diagnostic HUD outputs. */
    public boolean debug;

    /** Namespace used for resource-pack registration and Adventure font keys. */
    public String namespace;

    /** General description of the plugin or current configuration profile. */
    public String desc;

    /** Default font name used when no font is explicitly specified by an element. */
    public String default_font_name;

    /** Absolute or relative path to the resource-pack build folder. */
    public String build_folder_location;

    /** Type of the generated pack (e.g., "hud", "popup", or "mixed"). */
    public String pack_type;

    /** Whether DreamHud should force resource-pack updates on player join. */
    public boolean force_update;

    /** The IP address used for self-hosted resource-pack delivery. */
    public String self_host_ip;

    /** The port number used for self-hosted resource-pack delivery. */
    public int self_host_port;

    /** List of additional directories to merge during resource-pack build. */
    public List<String> merge_other_folders;

    /** Default HUD identifiers automatically loaded when the plugin starts. */
    public List<String> default_hud;

    /** Default popup identifiers automatically loaded when the plugin starts. */
    public List<String> default_popup;

    /** Enables resource-pack file obfuscation for protection purposes. */
    public boolean resourcepack_obfuscation;
  }

}
