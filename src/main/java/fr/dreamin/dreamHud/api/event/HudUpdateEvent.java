package fr.dreamin.dreamHud.api.event;

import fr.dreamin.dreamHud.api.Hud;
import fr.dreamin.dreamHud.api.player.BarHud;
import fr.dreamin.dreamHud.internal.event.HudEvent;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Called when a {@link Hud} is updated for a player.
 * <p>
 * This event is triggered whenever a HUD's content, layout, or visual state
 * is refreshed — for instance, during a scheduled update cycle or when
 * a tracked value changes (such as player health, score, or status).
 * <p>
 * The event can be {@linkplain #setCancelled(boolean) cancelled} to
 * prevent the HUD from being redrawn or refreshed in that cycle.
 * <p>
 * Example usage:
 * <pre>{@code
 * @EventHandler
 * public void onHudUpdate(HudUpdateEvent event) {
 *     if (event.getHud().id().equals("debug")) {
 *         event.setCancelled(true); // Disable updates for the debug HUD
 *     }
 * }
 * }</pre>
 *
 * @author Dreamin
 * @since 1.0.0
 *
 * @see Hud
 * @see BarHud
 * @see HudShowEvent
 * @see HudHideEvent
 * @see fr.dreamin.dreamHud.internal.event.HudEvent
 */
@Getter
public final class HudUpdateEvent extends HudEvent {

  /**
   * Constructs a new {@link HudUpdateEvent}.
   *
   * @param owner   the UUID of the player who owns the HUD
   * @param barHud  the {@link BarHud} container where the HUD is updated
   * @param hud     the {@link Hud} instance being updated
   */
  public HudUpdateEvent(final @NotNull UUID owner, final @NotNull BarHud barHud, final @NotNull Hud hud){
    super(owner, barHud, hud);
  }

}
