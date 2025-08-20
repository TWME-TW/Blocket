package dev.twme.blocket.events;

import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import dev.twme.blocket.models.Stage;
import dev.twme.blocket.models.View;
import dev.twme.blocket.types.BlocketPosition;
import lombok.Getter;

@Getter
/**
 * Event fired when a virtual block is broken by a player in a Blocket stage.
 * This event allows for custom handling of virtual block interactions and can be cancelled.
 *
 * <p>Event Naming Convention: Blocket[Action][Subject]Event for player interaction events
 * or [Action][Subject]Event for system events. All events should follow a consistent
 * asynchronous processing mechanism where appropriate.</p>
 *
 * <p>Asynchronous Processing: This event uses asynchronous processing (super(true)) to
 * prevent blocking the main server thread when handling potentially time-consuming
 * operations.</p>
 */
public class BlocketBreakEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;
    private final Player player;
    private final BlocketPosition position;
    private final BlockData blockData;
    private final View view;
    private final Stage stage;

    /**
     * Event that is called when a player breaks a block in a Blocket stage.
     *
     * @param player The player that broke the block.
     * @param position The position of the block that was broken.
     * @param blockData The block data of the block that was broken.
     * @param view The view that the player is in.
     * @param stage The stage that the player is in.
     */
    public BlocketBreakEvent(Player player, BlocketPosition position, BlockData blockData, View view, Stage stage) {
        super(true);
        this.player = player;
        this.position = position;
        this.blockData = blockData;
        this.view = view;
        this.stage = stage;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Gets the static HandlerList for this event type.
     * @return The HandlerList for BlocketBreakEvent
     */
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
