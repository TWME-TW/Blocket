package codes.kooper.blockify.managers;

import codes.kooper.blockify.BlockifyLibrary;
import codes.kooper.blockify.events.CreateStageEvent;
import codes.kooper.blockify.events.DeleteStageEvent;
import codes.kooper.blockify.models.Stage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class StageManager {
    private final Map<String, Stage> stages;

    public StageManager() {
        this.stages = new HashMap<>();
    }

    /**
     * Create a new stage
     * @param stage Stage to create
     */
    public void createStage(Stage stage) {
        if (stages.containsKey(stage.getName())) {
            BlockifyLibrary.getInstance().getLogger().warning("Stage with name " + stage.getName() + " already exists!");
            return;
        }
        
        // Only call Bukkit event if running in a Bukkit environment
        if (isBukkitAvailable()) {
            BlockifyLibrary.getInstance().getTaskScheduler().runTask(() -> new CreateStageEvent(stage).callEvent());
        }
        
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
        Stage stage = stages.get(name);
        if (stage != null && isBukkitAvailable()) {
            new DeleteStageEvent(stage).callEvent();
        }
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
    
    /**
     * Check if Bukkit is available in the current environment
     * @return true if Bukkit is available
     */
    private boolean isBukkitAvailable() {
        try {
            Class.forName("org.bukkit.Bukkit");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}