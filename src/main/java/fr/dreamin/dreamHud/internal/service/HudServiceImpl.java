package fr.dreamin.dreamHud.internal.service;

import fr.dreamin.api.service.DreaminAutoService;
import fr.dreamin.api.service.DreaminService;
import fr.dreamin.dreamHud.DreamHud;
import fr.dreamin.dreamHud.api.Hud;
import fr.dreamin.dreamHud.api.HudService;
import fr.dreamin.dreamHud.api.event.HudShowEvent;
import fr.dreamin.dreamHud.api.player.BarHud;
import fr.dreamin.dreamHud.api.player.HPlayer;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of the {@link HudService}.
 *
 * <p>This service is the runtime core of DreamHud. It manages all HUD-related
 * operations per player, including the creation and synchronization of
 * {@link HPlayer}, {@link BarHud}, and {@link Hud} instances.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Maintain an in-memory registry of all connected {@link HPlayer} instances.</li>
 *   <li>Attach and remove {@link Hud}s to players through {@link BarHud} overlays.</li>
 *   <li>Provide global operations (update, hide, clear, stop) across all players.</li>
 *   <li>Ensure safe concurrency using a {@link ConcurrentHashMap} for thread safety.</li>
 * </ul>
 *
 * <p>Each player maintains its own set of {@link BarHud}s, which contain
 * multiple layered {@link Hud}s. This architecture allows dynamic composition
 * of multiple HUD layers per boss bar (e.g. health, objective, notifications).</p>
 *
 * @see HudService
 * @see HPlayer
 * @see BarHud
 * @see Hud
 *
 * @author Dreamin
 * @since 1.0.0
 */
@DreaminAutoService(value = HudService.class)
@RequiredArgsConstructor
public final class HudServiceImpl implements DreaminService, HudService {

  private final DreamHud plugin;

  /** Map of active players and their associated HPlayer instances. */
  private final Map<UUID, HPlayer> players = new ConcurrentHashMap<>();

  // ###############################################################
  // ---------------------- SERVICE METHODS ------------------------
  // ###############################################################

  /**
   * Called when the service is closed.
   * <p>Ensures all HUDs and boss bars are properly cleared and detached
   * from players before plugin shutdown.</p>
   */
  @Override
  public void onClose() {
    clearAll();
  }

  // ###############################################################
  // ----------------------- PLAYER METHODS ------------------------
  // ###############################################################

  @Override
  public List<HPlayer> getAllPlayers() {
    return new ArrayList<>(this.players.values());
  }

  @Override
  public int getPlayerCount() {
    return this.players.size();
  }

  @Override
  public @NotNull HPlayer addPlayer(@NotNull Player player) {
    return this.players.computeIfAbsent(player.getUniqueId(), HPlayer::new);
  }

  @Override
  public void removePlayer(@NotNull Player player) {
    final var hPlayer = this.players.remove(player.getUniqueId());
    if (hPlayer != null) hPlayer.clearAllBarHuds();
  }

  @Override
  public @Nullable HPlayer getPlayer(@NotNull UUID uuid) {
    return this.players.get(uuid);
  }

  @Override
  public @Nullable HPlayer getPlayer(@NotNull Player player) {
    return this.players.get(player.getUniqueId());
  }

  @Override
  public @NotNull HPlayer getOrCreate(@NotNull Player player) {
    return this.players.computeIfAbsent(player.getUniqueId(), HPlayer::new);
  }

  // ###############################################################
  // ------------------------- HUD METHODS -------------------------
  // ###############################################################

  @Override
  public void addHud(@NotNull Player player, @NotNull String barName, @NotNull Hud hud) {
    final var hPlayer = getOrCreate(player);
    var barHud = hPlayer.getBarHud(barName);
    if (barHud == null) {
      barHud = new BarHud(hPlayer, barName);
      hPlayer.addBarHud(barHud);
    }
    addHud(player, barHud, hud);
  }

  @Override
  public void addHud(@NotNull Player player, @NotNull String barName, long periodTicks, @NotNull Hud hud) {
    final var hPlayer = getOrCreate(player);
    var barHud = hPlayer.getBarHud(barName);
    if (barHud == null) {
      barHud = new BarHud(hPlayer, barName, periodTicks);
      hPlayer.addBarHud(barHud);
    }
    addHud(player, barHud, hud);
  }

  @Override
  public void addHud(@NotNull Player player, @NotNull BarHud barHud, @NotNull Hud hud) {
    barHud.addHud(hud);
  }

  @Override
  public void addHud(@NotNull Player player, @NotNull String barName, @NotNull BossBar.Color color, @NotNull BossBar.Overlay overlay, long periodTicks, @NotNull Hud hud) {
    final var hPlayer = getOrCreate(player);
    var barHud = hPlayer.getBarHud(barName);
    if (barHud == null) {
      barHud = new BarHud(hPlayer, barName, color, overlay, periodTicks); // Utilisation des nouveaux paramètres
      hPlayer.addBarHud(barHud);
    }
    addHud(player, barHud, hud);
  }

  @Override
  public void removeHud(@NotNull Player player, @NotNull String barName, @NotNull String hudId) {
    final var hPlayer = getPlayer(player);
    if (hPlayer == null) return;

    var barHud = hPlayer.getBarHud(barName);
    if (barHud != null) barHud.removeHud(hudId);
  }

  @Override
  public void removeHud(@NotNull Player player, @NotNull String barName, @NotNull Hud hud) {
    final var hPlayer = getPlayer(player);
    if (hPlayer == null) return;

    var barHud = hPlayer.getBarHud(barName);
    if (barHud != null) barHud.removeHud(hud);
  }

  @Override
  public void removeHud(@NotNull Player player, @NotNull String hudId) {
    final var hPlayer = getPlayer(player);
    if (hPlayer == null) return;

    hPlayer.removeHud(hudId);
  }

  @Override
  public void removeHud(@NotNull Player player, @NotNull Hud hud) {
    final var hPlayer = getPlayer(player);
    if (hPlayer == null) return;

    hPlayer.removeHud(hud);
  }

  @Override
  public void removeBarHud(@NotNull Player player, @NotNull String barName) {
    final var hPlayer = getPlayer(player);
    if (hPlayer != null) hPlayer.removeBarHud(barName);
  }

  @Override
  public void removeBarHud(@NotNull Player player, @NotNull BarHud barHud) {
    final var hPlayer = getPlayer(player);
    if (hPlayer != null) hPlayer.removeBarHud(barHud);
  }

  @Override
  public void hideAll(@NotNull Player player) {
    final var hPlayer = getPlayer(player);
    if (hPlayer != null) hPlayer.hideAllBars();
  }

  @Override
  public void showAll(@NotNull Player player) {
    final var hPlayer = getPlayer(player);
    if (hPlayer != null) hPlayer.showAllBars();
  }

  @Override
  public void updateAll(@NotNull Player player) {
    final var hPlayer = getPlayer(player);
    if (hPlayer != null) hPlayer.updateAll();
  }

  // ###############################################################
  // ----------------------- GLOBAL METHODS ------------------------
  // ###############################################################

  @Override
  public void updateAllGlobal() {
    this.players.values().forEach(HPlayer::updateAll);
  }

  @Override
  public void stopAllGlobalTasks() {
    this.players.values().forEach(HPlayer::stopAllTasks);
  }

  @Override
  public void clearAll() {
    this.players.values().forEach(HPlayer::clearAllBarHuds);
    this.players.clear();
  }

}
