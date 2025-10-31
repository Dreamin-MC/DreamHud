package fr.dreamin.dreamHud.api.event;

import fr.dreamin.dreamHud.api.Hud;
import fr.dreamin.dreamHud.api.player.BarHud;
import fr.dreamin.dreamHud.internal.event.HudEvent;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Called when a {@link Hud} is hidden from a player.
 * <p>
 * This event is triggered whenever a HUD instance is removed, hidden,
 * or temporarily deactivated from a player's {@link BarHud}.
 * <p>
 * The event can be {@linkplain #setCancelled(boolean) cancelled} to
 * prevent the HUD from being hidden, allowing for plugin-side control
 * over visibility (e.g. locking certain HUDs, preventing removal under conditions).
 * <p>
 * Example usage:
 * <pre>{@code
 * @EventHandler
 * public void onHudHide(HudHideEvent event) {
 *     if (event.getHud().id().equalsIgnoreCase("main_status")) {
 *         event.setCancelled(true); // Prevent hiding the main HUD
 *     }
 * }
 * }</pre>
 *
 * @author Dreamin
 * @since 1.0.0
 *
 * @see Hud
 * @see BarHud
 * @see HudEvent
 * @see HudShowEvent
 * @see HudUpdateEvent
 */
@Getter
public final class HudHideEvent extends HudEvent {

  /**
   * Constructs a new {@link HudHideEvent}.
   *
   * @param owner   the UUID of the player who owns the HUD
   * @param barHud  the {@link BarHud} container holding the HUD
   * @param hud     the {@link Hud} instance being hidden
   */
  public HudHideEvent(final @NotNull UUID owner, final @NotNull BarHud barHud, final @NotNull Hud hud){
    super(owner, barHud, hud);
  }

}
