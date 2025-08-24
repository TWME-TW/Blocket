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
import dev.twme.blocket.managers.BlockLightingManager;
import dev.twme.blocket.models.Audience;
import dev.twme.blocket.models.Stage;
import dev.twme.blocket.models.View;
import dev.twme.blocket.types.BlocketPosition;

/**
 * Example class demonstrating the new custom block lighting functionality.
 * 
 * This example shows how to:
 * 1. Set custom light levels for specific blocks
 * 2. Create glowing effects for virtual blocks
 * 3. Control both block light and sky light independently
 * 4. Combine custom lighting with existing lighting preservation features
 *
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.1.0
 */
public class CustomLightingExample {
    
    private final JavaPlugin plugin;
    
    public CustomLightingExample(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Example 1: Basic custom lighting setup
     * Creates blocks with different light levels to demonstrate the functionality
     */
    public void createBasicCustomLightingDemo(Player player) {
        // Initialize API with lighting preservation enabled
        BlocketConfig config = BlocketConfig.builder()
            .autoInitialize(true)
            .enablePacketListeners(true)
            .preserveOriginalLighting(true) // Enable lighting preservation
            .build();
            
        BlocketAPI api = BlocketAPI.initialize(plugin, config);
        
        // Create stage
        World world = player.getWorld();
        Location playerLoc = player.getLocation();
        
        BlocketPosition pos1 = BlocketPosition.fromLocation(playerLoc);
        BlocketPosition pos2 = new BlocketPosition(
            pos1.getX() + 15,
            pos1.getY() + 5,
            pos1.getZ() + 15
        );
        
        Audience audience = Audience.fromPlayers(Set.of(player));
        Stage stage = new Stage("custom-lighting-demo", world, pos1, pos2, audience);
        api.getStageManager().createStage(stage);
        
        // Create view with various blocks
        Map<BlockData, Double> lightingBlocks = new HashMap<>();
        lightingBlocks.put(Material.STONE.createBlockData(), 30.0);           // Base blocks
        lightingBlocks.put(Material.COBBLESTONE.createBlockData(), 20.0);     // Variation
        lightingBlocks.put(Material.OBSIDIAN.createBlockData(), 10.0);        // Dark blocks
        lightingBlocks.put(Material.QUARTZ_BLOCK.createBlockData(), 40.0);    // Bright blocks
        
        dev.twme.blocket.models.Pattern pattern = new dev.twme.blocket.models.Pattern(lightingBlocks);
        View lightingView = new View("lighting-demo", stage, pattern, false);
        stage.addView(lightingView);
        
        // Add blocks to the view
        Set<BlocketPosition> demoArea = dev.twme.blocket.utils.BlockUtils.getBlocksBetween(pos1, pos2);
        lightingView.addBlocks(demoArea);
        
        // Now set custom lighting for specific positions
        setupCustomLightingPatterns(lightingView, pos1, pos2);
        
        // Send blocks to player
        stage.sendBlocksToAudience();
        
        player.sendMessage("§aCustom lighting demo created! Different blocks now have custom light levels.");
        player.sendMessage("§eLook around to see blocks with different brightness levels.");
    }
    
    /**
     * Example 2: Creating a glowing pathway
     * Demonstrates how to create a trail of blocks with high light emission
     */
    public void createGlowingPathway(Player player) {
        BlocketAPI api = BlocketAPI.getInstance();
        if (api == null) {
            player.sendMessage("§cBlocketAPI not initialized!");
            return;
        }
        
        World world = player.getWorld();
        Location start = player.getLocation();
        
        // Create a straight pathway
        BlocketPosition startPos = BlocketPosition.fromLocation(start);
        BlocketPosition endPos = new BlocketPosition(
            startPos.getX() + 20,
            startPos.getY(),
            startPos.getZ()
        );
        
        Audience audience = Audience.fromPlayers(Set.of(player));
        Stage pathwayStage = new Stage("glowing-pathway", world, startPos, endPos, audience);
        api.getStageManager().createStage(pathwayStage);
        
        // Create view with glowing blocks
        Map<BlockData, Double> pathBlocks = new HashMap<>();
        pathBlocks.put(Material.GLOWSTONE.createBlockData(), 100.0);
        
        dev.twme.blocket.models.Pattern pathPattern = new dev.twme.blocket.models.Pattern(pathBlocks);
        View pathView = new View("pathway", pathwayStage, pathPattern, false);
        pathwayStage.addView(pathView);
        
        // Create the pathway blocks
        for (int x = startPos.getX(); x <= endPos.getX(); x++) {
            BlocketPosition pathPos = new BlocketPosition(x, startPos.getY(), startPos.getZ());
            pathView.addBlock(pathPos);
            
            // Set maximum light level for each pathway block
            pathView.setBlockLight(pathPos, 15); // Maximum block light
        }
        
        pathwayStage.sendBlocksToAudience();
        
        player.sendMessage("§eGlowing pathway created! Each block emits maximum light.");
    }
    
    /**
     * Example 3: Dynamic lighting changes
     * Shows how to change lighting levels dynamically
     */
    public void createDynamicLightingDemo(Player player) {
        BlocketAPI api = BlocketAPI.getInstance();
        if (api == null) {
            player.sendMessage("§cBlocketAPI not initialized!");
            return;
        }
        
        World world = player.getWorld();
        Location playerLoc = player.getLocation();
        
        BlocketPosition centerPos = BlocketPosition.fromLocation(playerLoc.add(10, 2, 0));
        BlocketPosition cornerPos = new BlocketPosition(
            centerPos.getX() + 5,
            centerPos.getY() + 3,
            centerPos.getZ() + 5
        );
        
        Audience audience = Audience.fromPlayers(Set.of(player));
        Stage dynamicStage = new Stage("dynamic-lighting", world, centerPos, cornerPos, audience);
        api.getStageManager().createStage(dynamicStage);
        
        // Create view
        Map<BlockData, Double> dynamicBlocks = new HashMap<>();
        dynamicBlocks.put(Material.COAL_BLOCK.createBlockData(), 100.0);
        
        dev.twme.blocket.models.Pattern dynamicPattern = new dev.twme.blocket.models.Pattern(dynamicBlocks);
        View dynamicView = new View("dynamic", dynamicStage, dynamicPattern, false);
        dynamicStage.addView(dynamicView);
        
        // Add some blocks
        Set<BlocketPosition> dynamicArea = dev.twme.blocket.utils.BlockUtils.getBlocksBetween(centerPos, cornerPos);
        dynamicView.addBlocks(dynamicArea);
        
        // Set up a lighting pattern that changes over time
        scheduleGradualLightingChanges(dynamicView, dynamicArea, player);
        
        dynamicStage.sendBlocksToAudience();
        
        player.sendMessage("§dDynamic lighting demo started! Watch the blocks change brightness over time.");
    }
    
    /**
     * Sets up custom lighting patterns for the demo
     */
    private void setupCustomLightingPatterns(View view, BlocketPosition pos1, BlocketPosition pos2) {
        int minX = Math.min(pos1.getX(), pos2.getX());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());
        
        // Create different lighting zones
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlocketPosition pos = new BlocketPosition(x, y, z);
                    
                    // Create a gradient effect based on position
                    int distanceFromCenter = Math.abs(x - (minX + maxX) / 2) + 
                                           Math.abs(z - (minZ + maxZ) / 2);
                    
                    // Set light level based on distance (closer to center = brighter)
                    int lightLevel = Math.max(0, 15 - distanceFromCenter);
                    
                    if (lightLevel > 0) {
                        view.setBlockLight(pos, lightLevel);
                    }
                }
            }
        }
    }
    
    /**
     * Schedules gradual lighting changes to demonstrate dynamic lighting
     */
    private void scheduleGradualLightingChanges(View view, Set<BlocketPosition> positions, Player player) {
        // Use Bukkit's scheduler to create a pulsing effect
        final int[] currentLevel = {0};
        final boolean[] increasing = {true};
        
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            // Update light levels for all positions
            for (BlocketPosition pos : positions) {
                view.setBlockLight(pos, currentLevel[0]);
            }
            
            // Update the light level for next iteration
            if (increasing[0]) {
                currentLevel[0]++;
                if (currentLevel[0] >= 15) {
                    increasing[0] = false;
                }
            } else {
                currentLevel[0]--;
                if (currentLevel[0] <= 0) {
                    increasing[0] = true;
                }
            }
            
            // Refresh the blocks for the player
            try {
                Stage stage = view.getStage();
                stage.refreshBlocksToAudience(positions);
            } catch (Exception e) {
                String errorMessage = "Error refreshing dynamic lighting: " + e.getMessage();
                plugin.getLogger().warning(errorMessage);
            }
            
        }, 20L, 10L); // Start after 1 second, repeat every 0.5 seconds
        
        // Stop the animation after 30 seconds
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage("§7Dynamic lighting demo completed.");
        }, 600L);
    }
    
    /**
     * Example 4: Mixed lighting types
     * Demonstrates controlling both block light and sky light independently
     */
    public void createMixedLightingDemo(Player player) {
        BlocketAPI api = BlocketAPI.getInstance();
        if (api == null) {
            player.sendMessage("§cBlocketAPI not initialized!");
            return;
        }
        
        World world = player.getWorld();
        Location playerLoc = player.getLocation();
        
        BlocketPosition pos1 = BlocketPosition.fromLocation(playerLoc.add(-10, 0, -10));
        BlocketPosition pos2 = new BlocketPosition(pos1.getX() + 5, pos1.getY() + 5, pos1.getZ() + 5);
        
        Audience audience = Audience.fromPlayers(Set.of(player));
        Stage mixedStage = new Stage("mixed-lighting", world, pos1, pos2, audience);
        api.getStageManager().createStage(mixedStage);
        
        // Create view
        Map<BlockData, Double> mixedBlocks = new HashMap<>();
        mixedBlocks.put(Material.WHITE_CONCRETE.createBlockData(), 100.0);
        
        dev.twme.blocket.models.Pattern mixedPattern = new dev.twme.blocket.models.Pattern(mixedBlocks);
        View mixedView = new View("mixed", mixedStage, mixedPattern, false);
        mixedStage.addView(mixedView);
        
        // Add blocks and set different lighting combinations
        Set<BlocketPosition> area = dev.twme.blocket.utils.BlockUtils.getBlocksBetween(pos1, pos2);
        mixedView.addBlocks(area);
        
        // Set up different lighting combinations
        int i = 0;
        for (BlocketPosition pos : area) {
            switch (i % 4) {
                case 0 -> // High block light, low sky light
                    mixedView.setLighting(pos, 15, 3);
                case 1 -> // Low block light, high sky light
                    mixedView.setLighting(pos, 3, 15);
                case 2 -> // Medium both
                    mixedView.setLighting(pos, 8, 8);
                case 3 -> // Maximum both
                    mixedView.setLighting(pos, 15, 15);
            }
            i++;
        }
        
        mixedStage.sendBlocksToAudience();
        
        player.sendMessage("§bMixed lighting demo created!");
        player.sendMessage("§7Blocks have different combinations of block light and sky light.");
    }
    
    /**
     * Utility method to get the BlockLightingManager
     */
    public BlockLightingManager getLightingManager() {
        return BlocketAPI.getInstance().getBlockLightingManager();
    }
    
    /**
     * Method to demonstrate lighting data retrieval
     */
    public void showLightingInfo(Player player, String viewName, BlocketPosition position) {
        BlockLightingManager manager = getLightingManager();
        BlockLightingManager.LightingData lighting = manager.getLighting(viewName, position);
        
        if (lighting != null) {
            player.sendMessage(String.format("§eLighting info for position %s in view %s:", position, viewName));
            if (lighting.hasCustomBlockLight()) {
                player.sendMessage("§7Block Light: " + lighting.getBlockLight());
            }
            if (lighting.hasCustomSkyLight()) {
                player.sendMessage("§7Sky Light: " + lighting.getSkyLight());
            }
        } else {
            player.sendMessage("§cNo custom lighting set for position " + position + " in view " + viewName);
        }
    }
}
