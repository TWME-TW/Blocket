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

/**
 * Event fired when a player interacts with a virtual block in a Blocket stage.
 * This event is triggered before block breaking logic is applied, allowing
 * for custom interaction handling and cancellation.
 * 
 * <p>This event is cancellable. If cancelled, the interaction will be stopped
 * and no further block breaking logic will be processed.</p>
 * 
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
public class BlocketInteractEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;
    private final Player player;
    private final BlocketPosition position;
    private final BlockData blockData;
    private final View view;
    private final Stage stage;

    /**
     * Event that is called when a player interacts with a block in a stage.
     *
     * @param player The player that interacted with the block.
     * @param position The position of the block that was interacted with.
     * @param blockData The block data of the block that was interacted with.
     * @param view The view that the player is currently in.
     * @param stage The stage that the player is currently in.
     */
    public BlocketInteractEvent(Player player, BlocketPosition position, BlockData blockData, View view, Stage stage) {
        this.player = player;
        this.position = position;
        this.blockData = blockData;
        this.view = view;
        this.stage = stage;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
