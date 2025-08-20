package dev.twme.blocket.listeners;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import dev.twme.blocket.api.BlocketAPI;
import dev.twme.blocket.events.PlayerEnterStageEvent;
import dev.twme.blocket.events.PlayerExitStageEvent;
import dev.twme.blocket.events.PlayerJoinStageEvent;
import dev.twme.blocket.events.PlayerLeaveStageEvent;
import dev.twme.blocket.models.Stage;

/**
 * Listener that handles player movement events and stage boundary detection.
 * This listener tracks when players enter or exit stage boundaries and fires
 * appropriate events for other systems to handle.
 * 
 * <p>The listener uses a caching mechanism to efficiently track which stages
 * each player belongs to, reducing database/manager queries. It processes
 * player movement events and determines stage entry/exit based on location
 * changes.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li>Cached stage membership for performance</li>
 *   <li>Movement-based stage boundary detection</li>
 *   <li>Enter/exit event triggering</li>
 *   <li>Distance-based optimization to reduce processing</li>
 * </ul>
 * 
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
public class StageBoundListener implements Listener {

    private final LoadingCache<UUID, List<Stage>> stageCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                @Override
                public @NotNull List<Stage> load(@NotNull UUID playerUUID) {
                    Player player = Bukkit.getPlayer(playerUUID);
                    if (player == null) {
                        return Collections.emptyList();
                    }
                    return BlocketAPI.getInstance().getStageManager().getStages(player);
                }
            });

    /**
     * Handles player movement events to detect stage boundary crossings.
     * Only processes movement if the player has moved a significant distance (0.5 blocks)
     * to avoid excessive processing on minor position updates.
     * 
     * <p>This method:
     * <ul>
     *   <li>Checks if movement distance is significant enough to process</li>
     *   <li>Retrieves cached stage membership for the player</li>
     *   <li>Fires PlayerEnterStageEvent when entering a stage</li>
     *   <li>Fires PlayerExitStageEvent when leaving a stage</li>
     *   <li>Cancels movement if any event is cancelled</li>
     * </ul>
     * 
     * @param event The PlayerMoveEvent containing movement information
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Only process if the player has moved a significant distance
        if (event.getFrom().distanceSquared(event.getTo()) < 0.25) return;

        // Get the cached stages; if not present, this will call the loader.
        List<Stage> stages;
        try {
            stages = stageCache.get(player.getUniqueId());
        } catch (ExecutionException e) {
            return;
        }

        // Iterate through stages and fire enter/exit events accordingly.
        for (Stage stage : stages) {
            if (stage.isLocationWithin(event.getTo())) {
                PlayerEnterStageEvent enterEvent = new PlayerEnterStageEvent(stage, player);
                enterEvent.callEvent();
                if (enterEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            } else if (stage.isLocationWithin(event.getFrom())) {
                PlayerExitStageEvent exitEvent = new PlayerExitStageEvent(stage, player);
                exitEvent.callEvent();
                if (exitEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * This method is an event handler for PlayerJoinStageEvent.
     * It is triggered when a player joins a stage.
     * <p>
     * The method checks if the player's location is within the stage and calls a PlayerEnterStageEvent if it is.
     *
     * @param event The PlayerJoinStageEvent object containing information about the join stage event.
     */
    @EventHandler
    public void onPlayerStageJoin(PlayerJoinStageEvent event) {
        if (event.getStage().isLocationWithin(event.getPlayer().getLocation())) {
            new PlayerEnterStageEvent(event.getStage(), event.getPlayer()).callEvent();
        }
    }

    /**
     * This method is an event handler for PlayerLeaveStageEvent.
     * It is triggered when a player leaves a stage.
     * <p>
     * The method checks if the player's location is within the stage and calls a PlayerExitStageEvent if it is.
     *
     * @param event The PlayerLeaveStageEvent object containing information about the leave stage event.
     */
    @EventHandler
    public void onPlayerStageLeave(PlayerLeaveStageEvent event) {
        if (event.getStage().isLocationWithin(event.getPlayer().getLocation())) {
            new PlayerExitStageEvent(event.getStage(), event.getPlayer()).callEvent();
        }
    }
}
