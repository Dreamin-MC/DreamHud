package fr.dreamin.dreamHud.api.event;

import fr.dreamin.dreamHud.api.Hud;
import fr.dreamin.dreamHud.api.player.BarHud;
import fr.dreamin.dreamHud.internal.event.HudEvent;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Called when a {@link Hud} is displayed to a player.
 * <p>
 * This event is fired when a HUD instance becomes visible for a player —
 * for example when it is first added, re-enabled, or shown after being hidden.
 * <p>
 * The event can be {@linkplain #setCancelled(boolean) cancelled} to prevent
 * the HUD from being shown. This allows for plugin-side restrictions such as
 * visibility conditions, permissions, or timing control.
 * <p>
 * Example usage:
 * <pre>{@code
 * @EventHandler
 * public void onHudShow(HudShowEvent event) {
 *     if (!event.getPlayer().hasPermission("hud.show")) {
 *         event.setCancelled(true); // Block unauthorized HUDs
 *     }
 * }
 * }</pre>
 *
 * @author Dreamin
 * @since 1.0.0
 *
 * @see Hud
 * @see BarHud
 * @see HudHideEvent
 * @see fr.dreamin.dreamHud.internal.event.HudEvent
 */
@Getter
public final class HudShowEvent extends HudEvent {

  /**
   * Constructs a new {@link HudShowEvent}.
   *
   * @param owner   the UUID of the player who owns the HUD
   * @param barHud  the {@link BarHud} container in which the HUD is displayed
   * @param hud     the {@link Hud} instance being shown
   */
  public HudShowEvent(final @NotNull UUID owner, final @NotNull BarHud barHud, final @NotNull Hud hud){
    super(owner, barHud, hud);
  }

}
