package dev.twme.blocket.examples;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import dev.twme.blocket.api.BlocketAPI;
import dev.twme.blocket.api.BlocketConfig;
import dev.twme.blocket.models.Audience;
import dev.twme.blocket.models.Stage;
import dev.twme.blocket.models.View;
import dev.twme.blocket.types.BlocketPosition;

/**
 * Example class demonstrating lighting fix functionality
 *
 * This example shows how to use the new lighting preservation feature to solve chunk brightness issues:
 * 1. Backward compatibility: Maintains original behavior by default
 * 2. New feature: Optionally preserve original lighting to maintain consistent brightness
 * 3. Configuration flexibility: Developers can choose lighting handling strategy as needed
 */
public class LightingFixExample {
    
    private final JavaPlugin plugin;
    private BlocketAPI blocketAPI;
    
    public LightingFixExample(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Example 1: Using default configuration (backward compatibility)
     * This will use empty lighting, and the client will recalculate lighting
     */
    public void createStageWithDefaultLighting(Player player) {
        // Default configuration - preserveOriginalLighting = false
        BlocketConfig defaultConfig = BlocketConfig.builder()
            .autoInitialize(true)
            .enablePacketListeners(true)
            .build();
            
        BlocketAPI api = BlocketAPI.initialize(plugin, defaultConfig);
        
        // Create stage and view
        createExampleStage(api, player, "default-lighting-stage");
        
        player.sendMessage("§eStage created with default lighting processing (backward compatibility mode)");
    }
    
    /**
     * Example 2: Using new lighting preservation feature
     * This will preserve the original chunk's lighting to avoid brightness inconsistency issues
     */
    public void createStageWithPreservedLighting(Player player) {
        // Enable lighting preservation feature
        BlocketConfig preserveLightingConfig = BlocketConfig.builder()
            .autoInitialize(true)
            .enablePacketListeners(true)
            .preserveOriginalLighting(true) // Key setting: preserve original lighting
            .build();
            
        BlocketAPI api = BlocketAPI.initialize(plugin, preserveLightingConfig);
        
        // Create stage and view
        createExampleStage(api, player, "preserved-lighting-stage");
        
        player.sendMessage("§aStage created with lighting preservation feature (fix brightness issues)");
    }
    
    /**
     * Helper method to create example stage
     */
    private void createExampleStage(BlocketAPI api, Player player, String stageName) {
        World world = player.getWorld();
        Location playerLoc = player.getLocation();
        
        // Define a small area for testing
        BlocketPosition pos1 = BlocketPosition.fromLocation(playerLoc);
        BlocketPosition pos2 = new BlocketPosition(
            pos1.getX() + 10,
            pos1.getY() + 5,
            pos1.getZ() + 10
        );
        
        // Create audience
        Audience audience = Audience.fromPlayers(Set.of(player));
        
        // Create stage
        Stage stage = new Stage(stageName, world, pos1, pos2, audience);
        api.getStageManager().createStage(stage);
        
        // Create view with different blocks to test lighting effects
        Map<BlockData, Double> testBlocks = new HashMap<>();
        testBlocks.put(Material.GLOWSTONE.createBlockData(), 30.0);      // Light-emitting block
        testBlocks.put(Material.STONE.createBlockData(), 50.0);          // Normal block
        testBlocks.put(Material.GLASS.createBlockData(), 20.0);          // Transparent block
        
        dev.twme.blocket.models.Pattern pattern = new dev.twme.blocket.models.Pattern(testBlocks);
        View testView = new View("lighting-test", stage, pattern, false);
        stage.addView(testView);
        
        // Add blocks to view
        Set<BlocketPosition> testArea = dev.twme.blocket.utils.BlockUtils.getBlocksBetween(pos1, pos2);
        testView.addBlocks(testArea);
        
        // Send blocks to player
        stage.sendBlocksToAudience();
    }
    
    /**
     * Comparison test: Create two stages simultaneously to compare lighting effects
     */
    public void createComparisonTest(Player player) {
        player.sendMessage("§6Creating lighting comparison test...");
        
        // Create default lighting stage on player's left side
        Location leftLoc = player.getLocation().clone().add(-20, 0, 0);
        createStageAtLocation(player, leftLoc, false, "comparison-default");
        
        // Create preserved lighting stage on player's right side
        Location rightLoc = player.getLocation().clone().add(20, 0, 0);
        createStageAtLocation(player, rightLoc, true, "comparison-preserved");
        
        player.sendMessage("§aComparison test created!");
        player.sendMessage("§eLeft side: Default lighting processing (may have brightness differences)");
        player.sendMessage("§aRight side: Preserve original lighting (consistent brightness)");
    }
    
    /**
     * Create test stage at specified location
     */
    private void createStageAtLocation(Player player, Location location, boolean preserveLighting, String stageName) {
        BlocketConfig config = BlocketConfig.builder()
            .autoInitialize(true)
            .enablePacketListeners(true)
            .preserveOriginalLighting(preserveLighting)
            .build();
            
        BlocketAPI api = BlocketAPI.initialize(plugin, config);
        
        BlocketPosition pos1 = BlocketPosition.fromLocation(location);
        BlocketPosition pos2 = new BlocketPosition(pos1.getX() + 5, pos1.getY() + 3, pos1.getZ() + 5);
        
        Audience audience = Audience.fromPlayers(Set.of(player));
        Stage stage = new Stage(stageName, player.getWorld(), pos1, pos2, audience);
        api.getStageManager().createStage(stage);
        
        // Create test blocks for mixed lighting environment
        Map<BlockData, Double> mixedBlocks = new HashMap<>();
        mixedBlocks.put(Material.TORCH.createBlockData(), 10.0);         // Light source
        mixedBlocks.put(Material.COBBLESTONE.createBlockData(), 60.0);   // Normal block
        mixedBlocks.put(Material.OBSIDIAN.createBlockData(), 30.0);      // Dark block
        
        dev.twme.blocket.models.Pattern pattern = new dev.twme.blocket.models.Pattern(mixedBlocks);
        View testView = new View("mixed-lighting-test", stage, pattern, false);
        stage.addView(testView);
        
        Set<BlocketPosition> area = dev.twme.blocket.utils.BlockUtils.getBlocksBetween(pos1, pos2);
        testView.addBlocks(area);
        stage.sendBlocksToAudience();
    }
    
    /**
     * Method to get configuration recommendations
     */
    public static String getLightingConfigurationAdvice() {
        return """
            §6=== Blocket Lighting Configuration Advice ===
            
            §e1. Backward Compatibility (Default):
            §7   .preserveOriginalLighting(false)
            §7   - Use empty lighting, client recalculates
            §7   - May cause virtual block brightness to differ from original blocks
            
            §a2. Lighting Preservation (Recommended):
            §7   .preserveOriginalLighting(true)
            §7   - Preserve original chunk lighting data
            §7   - Maintain consistent block brightness
            §7   - Solve lighting inconsistency issues
            
            §c3. Performance Considerations:
            §7   - Preserving lighting will slightly increase processing time
            §7   - But provides a better visual experience
            §7   - Recommended to enable in production environments
            """;
    }
}