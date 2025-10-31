package fr.dreamin.dreamHud.internal.event;

import lombok.Getter;
import org.bukkit.event.Cancellable;

/**
 * Represents a cancellable {@link DreamHudEvent} within the DreamHud system.
 * <p>
 * This abstract base class provides built-in support for cancellation, allowing
 * event listeners to prevent the execution of subsequent logic tied to the event.
 * <p>
 * It is typically used for events where actions may be interrupted or vetoed,
 * such as HUD rendering, interaction, or dynamic updates.
 * <p>
 * Example usage:
 * <pre>{@code
 * @EventHandler
 * public void onHudRender(DreamHudRenderEvent event) {
 *     if (event.getPlayer().hasPermission("dreamhud.freeze")) {
 *         event.setCancelled(true);
 *     }
 * }
 * }</pre>
 *
 * @author Dreamin
 * @since 1.0.0
 *
 * @see DreamHudEvent
 * @see org.bukkit.event.Cancellable
 *
 */
@Getter
public abstract class DreamHudCancelEvent extends DreamHudEvent implements Cancellable {

  /** Whether this event has been cancelled. */
  private boolean cancelled = false;

  /**
   * Constructs a new synchronous cancellable HUD event.
   * <p>
   * This constructor should be used for most standard events that occur
   * on the main server thread.
   */
  public DreamHudCancelEvent() {
    super();
  }

  /**
   * Constructs a new cancellable HUD event with control over async execution.
   *
   * @param isAsync whether this event is asynchronous
   */
  public DreamHudCancelEvent(boolean isAsync) {
    super(isAsync);
  }

  /**
   * Checks if this event has been cancelled.
   *
   * @return {@code true} if the event has been cancelled, otherwise {@code false}
   */
  @Override
  public boolean isCancelled() {
    return this.cancelled;
  }

  /**
   * Sets the cancellation state of this event.
   *
   * @param cancel {@code true} to cancel the event, {@code false} to allow it to proceed
   */
  @Override
  public void setCancelled(boolean cancel) {
    this.cancelled = cancel;
  }

}
