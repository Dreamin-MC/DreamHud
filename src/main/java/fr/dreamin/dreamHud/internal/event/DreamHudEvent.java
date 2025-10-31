package fr.dreamin.dreamHud.internal.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for all custom events used within the DreamHud system.
 * <p>
 * This abstract class serves as the foundation for every event fired by the HUD engine.
 * It provides standard Bukkit event handling support via {@link HandlerList}, and
 * supports both synchronous and asynchronous execution contexts.
 * <p>
 * Every specific HUD-related event (such as render, update, visibility changes, etc.)
 * should extend this class or its cancellable counterpart {@link DreamHudCancelEvent}.
 * <p>
 * Example usage:
 * <pre>{@code
 * public final class DreamHudRenderEvent extends DreamHudEvent {
 *
 *     private final Player player;
 *
 *     public DreamHudRenderEvent(Player player) {
 *         this.player = player;
 *     }
 *
 *     public Player getPlayer() {
 *         return player;
 *     }
 * }
 * }</pre>
 *
 * @author Dreamin
 * @since 1.0.0
 *
 * @see org.bukkit.event.Event
 * @see DreamHudCancelEvent
 */
public abstract class DreamHudEvent extends Event {

  /** Shared handler list for all DreamHud events. */
  private static final HandlerList HANDLERS = new HandlerList();

  /**
   * Constructs a new synchronous DreamHud event.
   * <p>
   * This constructor should be used for events fired from the main server thread,
   * which represents the majority of HUD-related logic (e.g. rendering, updates).
   */
  public DreamHudEvent() {
    super();
  }

  /**
   * Constructs a new DreamHud event with explicit async mode.
   * <p>
   * This allows you to create and call events safely from asynchronous tasks
   * when needed (for example, data preloading or external updates).
   *
   * @param isAsync {@code true} if this event runs asynchronously, otherwise {@code false}
   */
  public DreamHudEvent(boolean isAsync) {
    super(isAsync);
  }

  /**
   * Retrieves the {@link HandlerList} used by Bukkit for this event type.
   * <p>
   * This method is automatically called by the Bukkit event system
   * to register and dispatch listeners.
   *
   * @return the {@link HandlerList} for this event
   */
  @NotNull
  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }

  /**
   * Provides access to the static {@link HandlerList} for this event type.
   * <p>
   * This method is required by the Bukkit event system to correctly handle
   * listener registration and must be present in all event subclasses.
   *
   * @return the static {@link HandlerList} shared by this event
   */
  @NotNull
  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

}
