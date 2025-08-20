package dev.twme.blocket.events;

import dev.twme.blocket.models.Stage;
import dev.twme.blocket.models.View;
import dev.twme.blocket.types.BlocketPosition;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class BlocketPlaceEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final BlocketPosition position;
    private final View view;
    private final Stage stage;

    /**
     * Event that is called when a player places a block in the Blocket plugin.
     *
     * @param player The player that placed the block.
     * @param position The position of the block that was placed.
     * @param view The view that the player is currently in.
     * @param stage The stage that the player is currently in.
     */
    public BlocketPlaceEvent(Player player, BlocketPosition position, View view, Stage stage) {
        this.player = player;
        this.position = position;
        this.view = view;
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
