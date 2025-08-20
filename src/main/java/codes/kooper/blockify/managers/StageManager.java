package codes.kooper.blockify.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import codes.kooper.blockify.api.BlockifyAPI;
import codes.kooper.blockify.events.CreateStageEvent;
import codes.kooper.blockify.events.DeleteStageEvent;
import codes.kooper.blockify.models.Stage;
import lombok.Getter;

@Getter
public class StageManager {
    private final Map<String, Stage> stages;
    private final BlockifyAPI api;

    public StageManager(BlockifyAPI api) {
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