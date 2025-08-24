package dev.twme.blocket.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import dev.twme.blocket.managers.BlockLightingManager.LightType;
import dev.twme.blocket.managers.BlockLightingManager.LightingData;
import dev.twme.blocket.types.BlocketPosition;
import lombok.Getter;

/**
 * Event fired when custom block lighting is changed for a specific position.
 * This event allows plugins to react to lighting changes and potentially
 * modify or cancel them.
 *
 * <p>This event is fired whenever the BlockLightingManager changes the lighting
 * values for a specific block position. It provides information about the old
 * and new lighting values, allowing for comprehensive lighting management.</p>
 *
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.1.0
 */
@Getter
public class BlockLightingChangeEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;
    
    private final String viewName;
    private final BlocketPosition position;
    private final LightingData newLightingData;
    private final LightType lightType;
    
    /**
     * Creates a new BlockLightingChangeEvent.
     *
     * @param viewName The name of the view where lighting changed
     * @param position The position where lighting changed
     * @param newLightingData The new lighting data
     * @param lightType The type of lighting that changed
     */
    public BlockLightingChangeEvent(String viewName, BlocketPosition position, 
                                   LightingData newLightingData, LightType lightType) {
        super(true); // Asynchronous event processing
        this.viewName = viewName;
        this.position = position;
        this.newLightingData = newLightingData;
        this.lightType = lightType;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
    
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
