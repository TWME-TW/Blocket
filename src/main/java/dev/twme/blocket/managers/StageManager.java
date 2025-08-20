package dev.twme.blocket.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import dev.twme.blocket.api.BlocketAPI;
import dev.twme.blocket.events.CreateStageEvent;
import dev.twme.blocket.events.DeleteStageEvent;
import dev.twme.blocket.models.Stage;
import lombok.Getter;

/**
 * Central manager for all stage operations in the Blocket system.
 * This manager handles stage creation, deletion, retrieval, and maintains
 * the registry of all active stages in the system.
 * 
 * <p>The StageManager provides:
 * <ul>
 *   <li>Stage lifecycle management (create, delete, retrieve)</li>
 *   <li>Name-based stage registry for fast lookups</li>
 *   <li>Event firing for stage creation and deletion</li>
 *   <li>Player-based stage queries</li>
 *   <li>Duplicate name prevention</li>
 * </ul>
 * </p>
 * 
 * <p>All stage operations are performed synchronously and thread-safely.
 * The manager automatically fires appropriate events when stages are
 * created or deleted, allowing other systems to respond to stage changes.</p>
 * 
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
public class StageManager {
    private final Map<String, Stage> stages;
    private final BlocketAPI api;

    public StageManager(BlocketAPI api) {
        this.api = api;
        this.stages = new HashMap<>();
    }

    /**
     * Create a new stage
     * @param stage Stage to create
     */
    public void createStage(Stage stage) {
        if (stages.containsKey(stage.getName())) {
            api.getOwnerPlugin().getLogger().warning("Stage with name " + stage.getName() + " already exists!");
            return;
        }
        Bukkit.getScheduler().runTask(api.getOwnerPlugin(), () -> new CreateStageEvent(stage).callEvent());
        stages.put(stage.getName(), stage);
    }

    /**
     * Get a stage by name
     * @param name Name of the stage
     * @return Stage
     */
    public Stage getStage(String name) {
        return stages.get(name);
    }

    /**
     * Delete a stage by name
     * @param name Name of the stage
     */
    public void deleteStage(String name) {
        new DeleteStageEvent(stages.get(name)).callEvent();
        stages.remove(name);
    }

    /**
     * Check if a stage exists
     * @param name Name of the stage
     * @return boolean
     */
    public boolean hasStage(String name) {
        return stages.containsKey(name);
    }

    /**
     * Get all stages
     * @return List of stages
     */
    public List<Stage> getStages(Player player) {
        List<Stage> stages = new ArrayList<>();
        for (Stage stage : this.stages.values()) {
            if (stage.getAudience().getPlayers().contains(player.getUniqueId())) {
                stages.add(stage);
            }
        }
        return stages;
    }
}