package fr.dreamin.dreamHud.api;

import fr.dreamin.dreamHud.api.Hud;
import fr.dreamin.dreamHud.api.player.BarHud;
import fr.dreamin.dreamHud.api.player.HPlayer;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Central contract allowing plugins to interact with the DreamHud API.
 *
 * <p>The {@code HudService} acts as the main entry point for creating and managing
 * HUD layers bound to boss bars. It is responsible for maintaining
 * {@link HPlayer} wrappers, orchestrating {@link BarHud} creation,
 * and ensuring HUDs are properly registered, updated, and destroyed.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Track all players currently managed by DreamHud.</li>
 *   <li>Handle the lifecycle of {@link Hud} instances per player.</li>
 *   <li>Manage {@link BarHud} registration and automatic updates.</li>
 *   <li>Provide global update and cleanup utilities.</li>
 * </ul>
 *
 * <p><strong>Invariants:</strong></p>
 * <ul>
 *   <li>Each {@link HPlayer} instance is unique and reused for a given {@link UUID}.</li>
 *   <li>HUD identifiers ({@link Hud#id()}) are unique per {@link BarHud} and replace older ones when duplicated.</li>
 * </ul>
 *
 * <p><strong>Example usage:</strong></p>
 * <pre>{@code
 * HudService service = DreamHud.getService(HudService.class);
 *
 * Hud hud = Hud.builder()
 *     .id("welcome")
 *     .layout(Layout.builder()
 *         .element(TextElement.builder()
 *             .textSupplier(() -> "Welcome " + player.getName())
 *             .build())
 *         .build())
 *     .zIndex(10)
 *     .build();
 *
 * service.addHud(player, "main", hud);
 * }</pre>
 *
 * @author Dreamin
 * @since 1.0.0
 */
public interface HudService {


  // ###############################################################
  // ---------------- PLAYER REGISTRATION METHODS ------------------
  // ###############################################################

  /**
   * Retrieves all currently tracked {@link HPlayer} instances.
   *
   * @return an immutable list of all active players registered in DreamHud
   */
  List<HPlayer> getAllPlayers();

  /**
   * Gets the number of active players currently managed by the service.
   *
   * @return the number of players registered
   */
  int getPlayerCount();

  /**
   * Registers a new {@link HPlayer} wrapper for the given Bukkit {@link Player}.
   * If the player is already registered, the same instance is returned.
   *
   * @param player the Bukkit player to register
   * @return the associated {@link HPlayer} instance
   */
  @NotNull HPlayer addPlayer(final @NotNull Player player);

  /**
   * Removes a registered {@link HPlayer} and all its HUDs.
   *
   * @param player the Bukkit player to remove
   */
  void removePlayer(final @NotNull Player player);

  /**
   * Retrieves the {@link HPlayer} associated with a given {@link UUID}.
   *
   * @param uuid the player's unique identifier
   * @return the matching {@link HPlayer}, or {@code null} if not found
   */
  @Nullable HPlayer getPlayer(final @NotNull UUID uuid);

  /**
   * Retrieves the {@link HPlayer} associated with a Bukkit {@link Player}.
   *
   * @param player the Bukkit player
   * @return the matching {@link HPlayer}, or {@code null} if not found
   */
  @Nullable HPlayer getPlayer(final @NotNull Player player);

  /**
   * Retrieves or creates a new {@link HPlayer} instance for the given {@link Player}.
   *
   * @param player the Bukkit player
   * @return the existing or newly created {@link HPlayer}
   */
  @NotNull HPlayer getOrCreate(final @NotNull Player player);

  // ###############################################################
  // ----------------------- HUD MANAGEMENT ------------------------
  // ###############################################################

  /**
   * Adds a new {@link Hud} to a named {@link BarHud} for a specific player.
   * The bar is created automatically if it does not exist.
   *
   * @param player the target player
   * @param barName the name of the bossbar
   * @param hud the HUD to attach
   */
  void addHud(final @NotNull Player player, final @NotNull String barName, final @NotNull Hud hud);

  /**
   * Adds a new {@link Hud} to a {@link BarHud} that automatically updates every {@code periodTicks}.
   *
   * @param player the target player
   * @param barName the name of the bossbar
   * @param periodTicks the update interval in ticks
   * @param hud the HUD to attach
   */
  void addHud(final @NotNull Player player, final @NotNull String barName, long periodTicks, final @NotNull Hud hud);

  /**
   * Adds a {@link Hud} to an existing {@link BarHud} instance.
   *
   * @param player the target player
   * @param barHud the bar instance
   * @param hud the HUD to attach
   */
  void addHud(final @NotNull Player player, final @NotNull BarHud barHud, final @NotNull Hud hud);

  /**
   * Adds a {@link Hud} to a player’s bar with full control over bar color and overlay.
   *
   * @param player the target player
   * @param barName the name of the bossbar
   * @param color the {@link BossBar.Color} of the bar
   * @param overlay the {@link BossBar.Overlay} type
   * @param periodTicks the update frequency in ticks
   * @param hud the HUD to attach
   */
  void addHud(final @NotNull Player player, final @NotNull String barName,
              final @NotNull BossBar.Color color, final @NotNull BossBar.Overlay overlay,
              long periodTicks, final @NotNull Hud hud);

  // ###############################################################
  // --------------------- HUD REMOVAL METHODS ---------------------
  // ###############################################################

  /**
   * Removes a specific HUD by its ID from a named {@link BarHud}.
   *
   * @param player the target player
   * @param barName the bar name
   * @param hudId the unique HUD identifier
   */
  void removeHud(final @NotNull Player player, final @NotNull String barName, final @NotNull String hudId);

  /**
   * Removes a specific {@link Hud} instance from a named {@link BarHud}.
   *
   * @param player the target player
   * @param barName the bar name
   * @param hud the HUD instance to remove
   */
  void removeHud(final @NotNull Player player, final @NotNull String barName, final @NotNull Hud hud);

  /**
   * Removes a HUD by its ID from all bars belonging to the given player.
   *
   * @param player the target player
   * @param hudId the HUD identifier
   */
  void removeHud(final @NotNull Player player, final @NotNull String hudId);

  /**
   * Removes a specific {@link Hud} instance from all bars belonging to the player.
   *
   * @param player the target player
   * @param hud the HUD to remove
   */
  void removeHud(final @NotNull Player player, final @NotNull Hud hud);

  // ###############################################################
  // --------------------- BAR HUD MANAGEMENT -----------------------
  // ###############################################################

  /**
   * Removes an entire {@link BarHud} identified by name from the given player.
   *
   * @param player the target player
   * @param barName the name of the bar to remove
   */
  void removeBarHud(final @NotNull Player player, final @NotNull String barName);

  /**
   * Removes a specific {@link BarHud} instance from the given player.
   *
   * @param player the target player
   * @param barHud the {@link BarHud} to remove
   */
  void removeBarHud(final @NotNull Player player, final @NotNull BarHud barHud);

  // ###############################################################
  // ------------------ GLOBAL CONTROL & UPDATES -------------------
  // ###############################################################

  /**
   * Temporarily hides all HUDs currently displayed for a player.
   *
   * @param player the target player
   */
  void hideAll(final @NotNull Player player);

  /**
   * Shows all HUDs currently hidden for a player.
   *
   * @param player the target player
   */
  void showAll(final @NotNull Player player);

  /**
   * Forces an immediate update of all {@link Hud} layers for the given player.
   *
   * @param player the target player
   */
  void updateAll(final @NotNull Player player);

  /**
   * Updates all registered HUDs for all tracked players.
   */
  void updateAllGlobal();

  /**
   * Stops all running update tasks for every player and bar.
   */
  void stopAllGlobalTasks();

  /**
   * Clears all data managed by this service, removing all players and HUDs.
   */
  void clearAll();
}