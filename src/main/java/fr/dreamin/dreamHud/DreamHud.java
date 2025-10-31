package fr.dreamin.dreamHud;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import fr.dreamin.api.service.DreaminServiceManager;
import fr.dreamin.dreamHud.internal.cmd.StandardSuggestions;
import fr.dreamin.dreamHud.internal.cmd.admin.AdminCmd;
import fr.dreamin.dreamHud.internal.cmd.player.PlayerCmd;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.logging.Level;

/**
 * Main entry point of the DreamHud plugin.
 *
 * <p>This class bootstraps the DreamHud ecosystem, initializes its modular
 * service system via {@link DreaminServiceManager}, and registers command
 * structures using Cloud Command Framework.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Initialize the {@link DreaminServiceManager} and load all annotated services.</li>
 *   <li>Register annotated commands via Cloud Framework.</li>
 *   <li>Expose static utility methods for event dispatching and service access.</li>
 *   <li>Provide access to global components like {@link MiniMessage} and {@link #instance}.</li>
 * </ul>
 *
 * <p><strong>Service Layer:</strong></p>
 * DreamHud relies on the <em>Dreamin API</em> service manager to discover and load
 * internal modules such as:
 * <ul>
 *   <li>{@code CodexService} – core configuration and resource settings</li>
 *   <li>{@code FontLoaderService} – font and glyph management</li>
 *   <li>{@code HudService} – runtime bossbar-based HUD rendering</li>
 *   <li>{@code PackLoaderService} – resource pack generation and management</li>
 * </ul>
 *
 * <p>All internal services are automatically scanned and registered through
 * the {@link fr.dreamin.api.service.DreaminAutoService} annotation.</p>
 *
 * @see DreaminServiceManager
 * @see fr.dreamin.api.service.DreaminService
 * @see fr.dreamin.api.service.DreaminAutoService
 * @see fr.dreamin.dreamHud.api.HudService
 * @see cloud.commandframework.paper.PaperCommandManager
 *
 * @author Dreamin
 * @since 1.0.0
 */
@Getter
public final class DreamHud extends JavaPlugin {

  /** Singleton instance of the plugin. */
  @Getter private static DreamHud instance;

  /** Global MiniMessage instance used for rich Adventure text serialization. */
  @Getter private static final MiniMessage MM = MiniMessage.miniMessage();

  /** Central service manager responsible for loading and managing DreamHud modules. */
  @Getter private static DreaminServiceManager serviceManager;

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  /**
   * Called by Bukkit when the plugin is enabled.
   * <p>Initializes the service manager and command registration system.</p>
   */
  @Override
  public void onEnable() {
    instance = this;
    loadServices();
    loadCloudCommands();
  }

  /**
   * Called by Bukkit when the plugin is disabled.
   * <p>Currently performs default cleanup (handled internally by {@link DreaminServiceManager}).</p>
   */
  @Override
  public void onDisable() {
    super.onDisable();
  }

  // ###############################################################
  // ----------------------- STATIC METHODS ------------------------
  // ###############################################################

  /**
   * Retrieves a service implementation of the specified type from the Bukkit {@link org.bukkit.plugin.ServicesManager}.
   *
   * <p>Throws an {@link IllegalStateException} if the requested service is
   * not registered or has failed to load properly.</p>
   *
   * @param <T>           the type of the service to retrieve
   * @param serviceClass  the class of the service to load (non-null)
   * @return the active instance of the requested service
   * @throws IllegalStateException if the service is not loaded
   */
  public static <T> T getService(Class<T> serviceClass) {
    final var sm = Bukkit.getServicesManager();
    T service = sm.load(serviceClass);
    if (service == null)
      throw new IllegalStateException("Service " + serviceClass.getName() + " is not loaded");
    return service;
  }

  /**
   * Dispatches a {@link org.bukkit.event.Event} safely through the Bukkit event system.
   *
   * @param event the event instance to fire (non-null)
   */
  public static void callEvent(final @NotNull Event event) {
    Bukkit.getPluginManager().callEvent(event);
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  /**
   * Initializes and loads all DreamHud-related services using {@link DreaminServiceManager}.
   *
   * <p>This method must be called before any API interaction with DreamHud
   * components, as it ensures all annotated services are properly registered
   * in Bukkit’s {@link org.bukkit.plugin.ServicesManager}.</p>
   */
  private void loadServices() {
    serviceManager = new DreaminServiceManager(this);
    serviceManager.loadAllServices();
  }

  /**
   * Registers all Cloud Framework commands for DreamHud.
   *
   * <p>Uses {@link cloud.commandframework.annotations.AnnotationParser} to
   * automatically detect and register commands from annotated classes such as:</p>
   * <ul>
   *   <li>{@link StandardSuggestions}</li>
   *   <li>{@link AdminCmd}</li>
   *   <li>{@link PlayerCmd}</li>
   * </ul>
   *
   * <p>Supports asynchronous tab completions if the underlying platform allows it.</p>
   */
  private void loadCloudCommands() {
    PaperCommandManager<CommandSender> manager;
    AnnotationParser<CommandSender> annotationParser;
    try {
      manager = new PaperCommandManager<>(this, CommandExecutionCoordinator.simpleCoordinator(), Function.identity(), Function.identity());

      if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION))
        manager.registerAsynchronousCompletions();

      annotationParser = new AnnotationParser<>(manager, CommandSender.class, p -> SimpleCommandMeta.empty());
    } catch (Exception e) {
      this.getLogger().log(Level.SEVERE, "Unable to register commands", e);
      this.getServer().getPluginManager().disablePlugin(this);
      return;
    }

    annotationParser.parse(new StandardSuggestions());
    annotationParser.parse(new AdminCmd(this));
    annotationParser.parse(new PlayerCmd(this));
  }

}
