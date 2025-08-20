package dev.twme.blocket.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import dev.twme.blocket.models.Stage;
import lombok.Getter;

/**
 * Event fired when a player programmatically joins a stage's audience.
 * This event is different from PlayerEnterStageEvent as it represents
 * logical membership addition rather than physical boundary crossing.
 * 
 * <p>This event is typically fired when a player is added to a stage's
 * audience through API calls or admin commands. It is not cancellable
 * as it represents an administrative action.</p>
 * 
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
public class PlayerJoinStageEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Stage stage;
    private final Player player;

    /**
     * Event that is called when a player joins a stage.
     *
     * @param stage  The stage the player joined.
     * @param player The player that joined the stage.
     */
    public PlayerJoinStageEvent(Stage stage, Player player) {
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
}
