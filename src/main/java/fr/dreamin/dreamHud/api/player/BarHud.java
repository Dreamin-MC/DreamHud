package fr.dreamin.dreamHud.api.player;

import fr.dreamin.dreamHud.DreamHud;
import fr.dreamin.dreamHud.api.Hud;
import fr.dreamin.dreamHud.api.event.HudHideEvent;
import fr.dreamin.dreamHud.api.event.HudShowEvent;
import fr.dreamin.dreamHud.api.event.HudUpdateEvent;
import fr.dreamin.dreamHud.internal.config.CodexService;
import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a single in-game bossbar used as a rendering surface
 * for one or multiple {@link Hud} layers.
 *
 * <p>A {@code BarHud} is the visual container assigned to a {@link HPlayer}.
 * Each player can have multiple named BarHUDs, allowing complex
 * and layered HUD compositions (e.g., overlays, alerts, timers...)</p>
 *
 * <p><strong>Behavior:</strong></p>
 * <ul>
 *   <li>Each BarHud holds a collection of {@link Hud} objects, sorted by {@link Hud#zIndex()}.</li>
 *   <li>HUDs are automatically rendered into a single {@link Component} attached to the {@link BossBar}.</li>
 *   <li>An optional automatic update task may be started to refresh the bar every X ticks.</li>
 * </ul>
 *
 * <p><strong>Example:</strong></p>
 * <pre>{@code
 * BarHud bar = new BarHud(hPlayer, "main", BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS, 20L);
 * bar.addHud(myHud);
 * }</pre>
 *
 * @author Dreamin
 * @since 1.0.0
 */
@Getter
public final class BarHud {

  private final @NotNull HPlayer owner;

  private final @NotNull String name;
  private final @NotNull BossBar bossBar;
  private final List<Hud> huds = new ArrayList<>();

  private @Nullable BukkitTask updateTask;

  /**
   * Creates a new {@link BarHud} for the given player and name, using
   * default color and overlay from {@link CodexService}.
   *
   * @param owner the player owning this BarHud
   * @param name the unique name identifying this bar
   */
  public BarHud(final @NotNull HPlayer owner, final @NotNull String name) {
    this(owner, name, DreamHud.getService(CodexService.class).getConfig().bar_color, BossBar.Overlay.PROGRESS);
  }

  /**
   * Creates a new {@link BarHud} with an automatic updater.
   *
   * @param owner the player owning this BarHud
   * @param name the unique name identifying this bar
   * @param periodTicks the update frequency in ticks
   */
  public BarHud(final @NotNull HPlayer owner, final @NotNull String name, long periodTicks) {
    this(owner, name, DreamHud.getService(CodexService.class).getConfig().bar_color, BossBar.Overlay.PROGRESS, periodTicks);
  }

  /**
   * Creates a new {@link BarHud} with a specific color and overlay.
   *
   * @param owner the player owning this BarHud
   * @param name the bar identifier
   * @param color the {@link BossBar.Color} used
   * @param overlay the {@link BossBar.Overlay} type (PROGRESS, NOTCHED_10, etc.)
   */
  public BarHud(final @NotNull HPlayer owner, final @NotNull String name, final @NotNull BossBar.Color color, final @NotNull BossBar.Overlay overlay) {
    this.owner = owner;
    this.name = name;

    this.bossBar = BossBar.bossBar(Component.empty(), 0, color, overlay);
  }

  /**
   * Creates a new {@link BarHud} with custom color, overlay, and update period.
   *
   * @param owner the player owning this BarHud
   * @param name the bar identifier
   * @param color the {@link BossBar.Color} used
   * @param overlay the {@link BossBar.Overlay} type
   * @param periodTicks how often to refresh the HUD (in ticks)
   */
  public BarHud(final @NotNull HPlayer owner, final @NotNull String name, final @NotNull BossBar.Color color, final @NotNull BossBar.Overlay overlay, long periodTicks) {
    this.owner = owner;
    this.name = name;

    this.bossBar = BossBar.bossBar(Component.empty(), 0, color, overlay);

    startUpdater(periodTicks);
  }


  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  /**
   * Retrieves a HUD by its ID if it exists within this bar.
   *
   * @param id the HUD identifier
   * @return the matching {@link Hud} instance, or {@code null} if not found
   */
  public @Nullable Hud getHud(final @NotNull String id) {
    return this.huds.stream()
      .filter(h -> h.id().equalsIgnoreCase(id))
      .findFirst()
      .orElse(null);

  }

  /**
   * Adds a new HUD layer to this bar.
   * If another HUD with the same ID exists, it will be replaced.
   *
   * <p>HUDs are sorted by {@link Hud#zIndex()} so that
   * higher z-index values render above lower ones.</p>
   *
   * @param hud the HUD to add
   */
  public void addHud(final @NotNull Hud hud) {
    final var event = new HudShowEvent(owner.getUuid(), this, hud);
    DreamHud.callEvent(event);

    if (event.isCancelled()) return;

    this.huds.removeIf(h -> h.id().equalsIgnoreCase(hud.id())); // évite les doublons
    this.huds.add(hud);
    this.huds.sort(Comparator.comparingInt(Hud::zIndex));
    update();
  }

  /**
   * Removes a HUD layer by its ID.
   *
   * @param id the HUD identifier
   */
  public void removeHud(final @NotNull String id) {
    this.huds.removeIf(h -> {
      if (h.id().equalsIgnoreCase(id)) {
        final var event = new HudHideEvent(owner.getUuid(), this, h);
        DreamHud.callEvent(event);
        return event.isCancelled();
      }
      return false;
    });
    update();
  }

  /**
   * Removes a HUD instance.
   *
   * @param hud the {@link Hud} to remove
   */
  public void removeHud(final @NotNull Hud hud) {
    final var event = new HudHideEvent(owner.getUuid(), this, hud);
    DreamHud.callEvent(event);
    if (event.isCancelled()) return;

    this.huds.remove(hud);
    update();
  }

  /**
   * Checks whether the specified HUD is currently registered.
   *
   * @param hud the HUD instance to check
   * @return {@code true} if present, {@code false} otherwise
   */
  public boolean hasHud(final @NotNull Hud hud) {
    return this.huds.stream().anyMatch(h -> h.equals(hud));
  }

  /**
   * Checks whether a HUD with the specified ID is registered.
   *
   * @param id the HUD identifier
   * @return {@code true} if a matching HUD is found
   */
  public boolean hasHud(@NotNull String id) {
    return this.huds.stream().anyMatch(h -> h.id().equalsIgnoreCase(id));
  }

  /**
   * Clears all HUDs from this bar and resets the displayed component.
   */
  public void clear() {
    this.huds.clear();
    this.bossBar.name(Component.empty());
  }


  /**
   * Rebuilds and updates the bossbar’s visual name by combining all registered HUDs.
   * <p>The HUDs are sorted by {@link Hud#zIndex()} before being concatenated.</p>
   */
  public void update() {
    if (huds.isEmpty()) {
      bossBar.name(Component.empty());
      return;
    }
    if (this.owner.getPlayer() == null) return;

    var combined = Component.text();
    huds.stream()
      .sorted(Comparator.comparingInt(Hud::zIndex))
      .forEach(hud -> {
        final var event = new HudUpdateEvent(this.owner.getUuid(), this, hud);
        DreamHud.callEvent(event);

        if (!event.isCancelled()) combined.append(hud.toComponent());
      });

    this.bossBar.name(combined.build());
  }

  // ###############################################################
  // --------------------- AUTO UPDATE SYSTEM ----------------------
  // ###############################################################

  /**
   * Starts a repeating task that automatically refreshes the HUD every given tick interval.
   * <p>Has no effect if an updater is already running.</p>
   *
   * @param periodTicks the refresh interval in ticks
   */
  public void startUpdater(final long periodTicks) {
    if (this.updateTask != null && !this.updateTask.isCancelled())
      return;

    this.updateTask = Bukkit.getScheduler().runTaskTimer(
      DreamHud.getInstance(),
      this::update,
      0L,
      periodTicks
    );
  }

  /**
   * Stops the currently running automatic updater, if any.
   */
  public void stopUpdater() {
    if (this.updateTask != null) {
      this.updateTask.cancel();
      this.updateTask = null;
    }
  }

  /**
   * Checks whether the automatic update task is currently running.
   *
   * @return {@code true} if the updater is active, {@code false} otherwise
   */
  public boolean isUpdaterRunning() {
    return this.updateTask != null && !this.updateTask.isCancelled();
  }

}
