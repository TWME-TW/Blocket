package dev.twme.blocket.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import dev.twme.blocket.models.Stage;
import lombok.Getter;

/**
 * Event fired when a player physically enters a stage's boundaries.
 * This event is triggered by the StageBoundListener when it detects
 * that a player has moved into a stage area.
 *
 * <p>This event is cancellable. If cancelled, the player's movement
 * that would cause them to enter the stage will be blocked.</p>
 *
 * <p>Event Naming Convention: Blocket[Action][Subject]Event for player interaction events
 * or [Action][Subject]Event for system events. All events should follow a consistent
 * asynchronous processing mechanism where appropriate.</p>
 *
 * <p>Asynchronous Processing: This event uses asynchronous processing (super(true)) to
 * prevent blocking the main server thread when handling potentially time-consuming
 * operations.</p>
 *
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
public class PlayerEnterStageEvent extends Event implements Cancellable {
    private boolean cancelled = false;
    private static final HandlerList HANDLERS = new HandlerList();
    private final Stage stage;
    private final Player player;

    /**
     * Event that is called when a player enters a stage.
     *
     * @param stage  The stage the player entered.
     * @param player The player that entered the stage.
     */
    /**
     * Event that is called when a player enters a stage.
     *
     * @param stage  The stage the player entered.
     * @param player The player that entered the stage.
     */
    public PlayerEnterStageEvent(Stage stage, Player player) {
       super(true); // Unified asynchronous processing mechanism
       this.stage = stage;
       this.player = player;
   }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
