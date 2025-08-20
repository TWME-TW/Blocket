package dev.twme.blocket.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import dev.twme.blocket.models.Stage;
import lombok.Getter;

/**
 * Event fired when a player physically exits a stage's boundaries.
 * This event is triggered by the StageBoundListener when it detects
 * that a player has moved out of a stage area.
 * 
 * <p>This event is cancellable. If cancelled, the player's movement
 * that would cause them to exit the stage will be blocked.</p>
 * 
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
public class PlayerExitStageEvent extends Event implements Cancellable {
    private boolean cancelled = false;
    private static final HandlerList HANDLERS = new HandlerList();
    private final Stage stage;
    private final Player player;

    /**
     * Event that is called when a player exits a stage.
     *
     * @param stage  The stage the player exited.
     * @param player The player that exited the stage.
     */
    public PlayerExitStageEvent(Stage stage, Player player) {
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

