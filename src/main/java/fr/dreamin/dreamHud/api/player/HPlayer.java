package fr.dreamin.dreamHud.api.player;

import fr.dreamin.dreamHud.api.Hud;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * Runtime representation of a player managed by HudLib.
 *
 * <p><strong>Invariants</strong>
 * <ul>
 *   <li>Every {@link java.util.UUID} is associated with a single {@code HPlayer} instance.</li>
 *   <li>Boss bars are automatically hidden when removed from this player.</li>
 * </ul>
 *
 * <p><strong>Usage example</strong>
 * <pre>{@code
 * HPlayer hudPlayer = hudService.getOrCreate(player);
 * hudPlayer.addBarHud(new BarHud(hudPlayer, "main", 20L));
 * }</pre>
 *
 * @autor Dreamin
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public final class HPlayer {

  private final @NotNull UUID uuid;
  private final Map<String, BarHud> barHuds = new HashMap<>();

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  /**
   * Consumes the Player object if it is online.
   *
   * @param consumer the consumer to accept the Player object
   */
  public void consumePlayer(@NotNull Consumer<Player> consumer) {
    var player = getPlayer();
    if (player != null)
      consumer.accept(player);
  }

  /**
   * Gets the Player object from Bukkit if the player is online.
   *
   * @return the Player object or null if the player is offline
   */
  public @Nullable Player getPlayer() {
    return Bukkit.getPlayer(this.uuid);
  }

  /**
   * Gets a BarHud by name.
   */
  public @Nullable BarHud getBarHud(@NotNull String name) {
    return this.barHuds.get(name);
  }

  /**
   * Adds a BarHud to this player, replacing any existing one with the same name.
   */
  public void addBarHud(final @NotNull BarHud barHud) {
    var existing = this.barHuds.get(barHud.getName());
    if (existing != null) existing.stopUpdater();

    this.barHuds.put(barHud.getName(), barHud);

    consumePlayer(player -> player.showBossBar(barHud.getBossBar()));
  }

  /**
   * Removes a BarHud by name and hides it from the player.
   */
  public void removeBarHud(@NotNull String name) {
    var bar = this.barHuds.remove(name);
    removeBarHud(bar);
  }

  /**
   * Removes a BarHud and ides it from the player.
   */
  public void removeBarHud(final @NotNull BarHud bar) {
    bar.stopUpdater();
    consumePlayer(player -> player.hideBossBar(bar.getBossBar()));
  }

  public void removeHud(final @NotNull String hudId) {
    for (final var barHud : this.barHuds.values()) {
      if (!barHud.hasHud(hudId)) continue;
      barHud.removeHud(hudId);
    }
  }

  public void removeHud(final @NotNull Hud hud) {
    for (final var barHud : this.barHuds.values()) {
      if (!barHud.hasHud(hud)) continue;
      barHud.removeHud(hud);
    }
  }

  /**
   * Removes all BarHuds and hides them from the player.
   */
  public void clearAllBarHuds() {
    this.barHuds.values().forEach(bar -> {
      bar.stopUpdater();
      consumePlayer(player -> player.hideBossBar(bar.getBossBar()));
    });
    this.barHuds.clear();
  }


  /**
   * Updates all active BarHuds (useful if they don’t have auto-updater).
   */
  public void updateAll() {
    this.barHuds.values().forEach(BarHud::update);
  }

  /**
   * Stops all updater tasks (keeps the bars visible, but frozen).
   */
  public void stopAllTasks() {
    this.barHuds.values().forEach(BarHud::stopUpdater);
  }

  /**
   * Starts auto-update for all BarHuds with a given period.
   */
  public void startAllTasks(long periodTicks) {
    barHuds.values().forEach(bar -> {
      if (!bar.isUpdaterRunning()) bar.startUpdater(periodTicks);
    });
  }

  /**
   * Checks if a BarHud exists.
   */
  public boolean hasBarHud(@NotNull String name) {
    return this.barHuds.containsKey(name);
  }

  /**
   * Returns all BarHud names for debug or iteration.
   */
  public Set<String> getBarHudNames() {
    return this.barHuds.keySet();
  }

  /**
   * Hides all bossbars from the player (without clearing data).
   */
  public void hideAllBars() {
    consumePlayer(player -> this.barHuds.values().forEach(bar -> player.hideBossBar(bar.getBossBar())));
  }

  /**
   * Shows all bossbars to the player (useful when rejoining).
   */
  public void showAllBars() {
    consumePlayer(player -> this.barHuds.values().forEach(bar -> player.showBossBar(bar.getBossBar())));
  }

}
