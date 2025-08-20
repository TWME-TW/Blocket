package dev.twme.blocket.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import dev.twme.blocket.models.Stage;
import lombok.Getter;

/**
 * Event fired when a stage is being deleted from the system.
 * This event is triggered before a stage is removed from the
 * StageManager and its resources are cleaned up.
 *
 * <p>This event is not cancellable as it represents notification
 * of a deletion operation. Use this event to perform cleanup
 * operations or logging when stages are deleted.</p>
 *
 * <p>Event Naming Convention: Blocket[Action][Subject]Event for player interaction events
 * or [Action][Subject]Event for system events. All events should follow a consistent
 * asynchronous processing mechanism where appropriate.</p>
 *
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
public class DeleteStageEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Stage stage;

    /**
     * Event that is called when a stage is deleted.
     *
     * @param stage The stage that was deleted.
     */
    public DeleteStageEvent(Stage stage) {
        this.stage = stage;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
