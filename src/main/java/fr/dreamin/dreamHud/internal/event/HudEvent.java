package fr.dreamin.dreamHud.internal.event;

import fr.dreamin.dreamHud.api.Hud;
import fr.dreamin.dreamHud.api.player.BarHud;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a cancellable event related to a specific {@link Hud} instance
 * within the DreamHud system.
 * <p>
 * This abstract base class provides contextual information about the HUD being
 * manipulated or updated, as well as the owning player and the {@link BarHud}
 * container it belongs to.
 * <p>
 * It is extended by all HUD-specific events such as rendering, showing,
 * hiding, or updating operations.
 * <p>
 * Example usage:
 * <pre>{@code
 * public final class HudRenderEvent extends HudEvent {
 *
 *     public HudRenderEvent(UUID owner, BarHud barHud, Hud hud) {
 *         super(owner, barHud, hud);
 *     }
 * }
 * }</pre>
 *
 * Typical subclasses:
 * <ul>
 *   <li>{@code HudShowEvent}</li>
 *   <li>{@code HudHideEvent}</li>
 *   <li>{@code HudUpdateEvent}</li>
 *   <li>{@code HudRemoveEvent}</li>
 * </ul>
 *
 * @author Dreamin
 * @since 1.0.0
 *
 * @see DreamHudEvent
 * @see DreamHudCancelEvent
 * @see Hud
 * @see BarHud
 */
@Getter
@RequiredArgsConstructor
public abstract class HudEvent extends DreamHudCancelEvent {

  /**
   * The unique identifier of the player who owns this HUD.
   */
  private final @NotNull UUID owner;

  /**
   * The {@link BarHud} instance that contains the {@link Hud}.
   */
  private final @NotNull BarHud barHud;

  /**
   * The specific {@link Hud} instance involved in this event.
   */
  private final @NotNull Hud hud;

}
