package dev.twme.blocket.events;

import java.util.Map;

import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import dev.twme.blocket.models.Stage;
import dev.twme.blocket.types.BlocketChunk;
import dev.twme.blocket.types.BlocketPosition;
import lombok.Getter;

/**
 * Event fired when block changes are being sent to players in a stage.
 * This event provides information about which blocks are being updated
 * and allows listeners to monitor block change activity.
 * 
 * <p>This event is fired before block change packets are sent to players,
 * allowing for logging, monitoring, or additional processing of block updates.
 * It is not cancellable as it represents information about outgoing packets.</p>
 * 
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
public class OnBlockChangeSendEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Stage stage;
    private final Map<BlocketChunk, Map<BlocketPosition, BlockData>> blocks;

    /**
     * Event that is called when block(s) are being changed.
     * @param stage The stage that the block change is happening in.
     * @param blocks The blocks that are being changed.
     */
    public OnBlockChangeSendEvent(Stage stage, Map<BlocketChunk, Map<BlocketPosition, BlockData>> blocks) {
        this.stage = stage;
        this.blocks = blocks;
    }


    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
