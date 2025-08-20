package dev.twme.blocket.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import dev.twme.blocket.models.Stage;
import lombok.Getter;

/**
 * Event fired when a new stage is created in the system.
 * This event is triggered after a stage is successfully created
 * and registered in the StageManager.
 *
 * <p>This event is not cancellable as it represents notification
 * of a completed creation operation. Use this event to perform
 * additional setup or logging when stages are created.</p>
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
public class CreateStageEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Stage stage;

    /**
     * Event that is called when a stage is created.
     *
     * @param stage The stage that was created
     */
    public CreateStageEvent(Stage stage) {
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
