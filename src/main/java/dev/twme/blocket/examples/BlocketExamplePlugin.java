package dev.twme.blocket.examples;

import dev.twme.blocket.api.BlocketAPI;
import dev.twme.blocket.api.BlocketConfig;
import dev.twme.blocket.models.Audience;
import dev.twme.blocket.models.Pattern;
import dev.twme.blocket.models.Stage;
import dev.twme.blocket.models.View;
import dev.twme.blocket.types.BlocketPosition;
import dev.twme.blocket.utils.BlockUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Example showing how to use Blocket as a library in your plugin
 */
public class BlocketExamplePlugin extends JavaPlugin {
    
    private BlocketAPI blocketAPI;
    
    @Override
    public void onEnable() {
        // Initialize Blocket API with custom configuration
        BlocketConfig config = BlocketConfig.builder()
            .autoInitialize(true)
            .enableStageBoundListener(true)
            .enablePacketListeners(true)
            .defaultChunksPerTick(2)
            .build();
            
        blocketAPI = BlocketAPI.initialize(this, config);
        
        getLogger().info("Blocket API initialized successfully!");
        
        // Example: Create a command to set up a private mine
        getCommand("createmine").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player player) {
                createPrivateMine(player);
                return true;
            }
            return false;
        });
    }
    
    @Override
    public void onDisable() {
        // Always shutdown Blocket API in onDisable
        if (blocketAPI != null) {
            blocketAPI.shutdown();
        }
    }
    
    /**
     * Example method showing how to create a private mine for a player
     */
    private void createPrivateMine(Player player) {
        World world = player.getWorld();
        Location playerLoc = player.getLocation();
        
        // Define mine area (50x50x20 area starting from player location)
        BlocketPosition pos1 = BlocketPosition.fromLocation(playerLoc);
        BlocketPosition pos2 = new BlocketPosition(
            pos1.getX() + 50, 
            pos1.getY() + 20, 
            pos1.getZ() + 50
        );
        
        // Create audience (only this player can see the virtual blocks)
        Set<Player> players = Set.of(player);
        Audience audience = Audience.fromPlayers(players);
        
        // Create ore pattern with weighted distribution
        Map<BlockData, Double> orePattern = new HashMap<>();
        orePattern.put(Material.STONE.createBlockData(), 60.0);         // 60% stone
        orePattern.put(Material.COAL_ORE.createBlockData(), 25.0);      // 25% coal
        orePattern.put(Material.IRON_ORE.createBlockData(), 10.0);      // 10% iron
        orePattern.put(Material.GOLD_ORE.createBlockData(), 4.0);       // 4% gold
        orePattern.put(Material.DIAMOND_ORE.createBlockData(), 1.0);    // 1% diamond
        
        Pattern pattern = new Pattern(orePattern);
        
        // Create stage
        Stage mineStage = new Stage("mine-" + player.getName(), world, pos1, pos2, audience);
        mineStage.setChunksPerTick(3); // Process 3 chunks per tick for faster loading
        
        // Register stage
        blocketAPI.getStageManager().createStage(mineStage);
        
        // Create view for ore deposits
        View oreView = new View("ore-deposits", mineStage, pattern, true); // true = breakable
        oreView.setZIndex(1); // Higher priority than other views
        mineStage.addView(oreView);
        
        // Fill the area with ore blocks (async for performance)
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            Set<BlocketPosition> mineBlocks = BlockUtils.getBlocksBetween(pos1, pos2);
            
            // Add blocks to view (this will automatically distribute according to pattern)
            oreView.addBlocks(mineBlocks);
            
            // Send blocks to audience (sync task)
            getServer().getScheduler().runTask(this, () -> {
                mineStage.sendBlocksToAudience();
                player.sendMessage("§a私人礦場已建立！區域: " + 
                    pos1.getX() + "," + pos1.getY() + "," + pos1.getZ() + " 到 " +
                    pos2.getX() + "," + pos2.getY() + "," + pos2.getZ());
            });
        });
    }
    
    /**
     * Example: Create a farm with different crop ages
     */
    public void createPrivateFarm(Player player, Location corner1, Location corner2) {
        // Create wheat farm with different growth stages
        Map<BlockData, Double> farmPattern = new HashMap<>();
        
        // Different wheat growth stages
        BlockData youngWheat = Material.WHEAT.createBlockData();
        BlockData matureWheat = BlockUtils.setAge(Material.WHEAT.createBlockData(), 7);
        
        farmPattern.put(youngWheat, 70.0);    // 70% young wheat
        farmPattern.put(matureWheat, 30.0);   // 30% mature wheat
        
        Pattern pattern = new Pattern(farmPattern);
        
        // Create stage and view
        Audience audience = Audience.fromPlayers(Set.of(player));
        BlocketPosition pos1 = BlocketPosition.fromLocation(corner1);
        BlocketPosition pos2 = BlocketPosition.fromLocation(corner2);
        
        Stage farmStage = new Stage("farm-" + player.getName(), corner1.getWorld(), pos1, pos2, audience);
        blocketAPI.getStageManager().createStage(farmStage);
        
        View farmView = new View("wheat-field", farmStage, pattern, true);
        farmStage.addView(farmView);
        
        // Add farm blocks
        Set<BlocketPosition> farmBlocks = BlockUtils.getBlocksBetween(pos1, pos2);
        farmView.addBlocks(farmBlocks);
        farmStage.sendBlocksToAudience();
        
        player.sendMessage("§a私人農場已建立！");
    }
    
    /**
     * Example: Dynamic view management
     */
    public void addSecretLayer(Player player, String stageName) {
        Stage stage = blocketAPI.getStageManager().getStage(stageName);
        if (stage == null) {
            player.sendMessage("§c舞台不存在: " + stageName);
            return;
        }
        
        // Create a secret ore layer with rare ores
        Map<BlockData, Double> secretOres = new HashMap<>();
        secretOres.put(Material.EMERALD_ORE.createBlockData(), 50.0);
        secretOres.put(Material.DIAMOND_ORE.createBlockData(), 30.0);
        secretOres.put(Material.ANCIENT_DEBRIS.createBlockData(), 20.0);
        
        Pattern secretPattern = new Pattern(secretOres);
        View secretView = new View("secret-ores", stage, secretPattern, true);
        secretView.setZIndex(2); // Higher priority
        
        stage.addView(secretView);
        
        // Add secret ores only in a specific area
        Set<BlocketPosition> secretArea = BlockUtils.getBlocksBetween(
            new BlocketPosition(stage.getMinPosition().getX(), stage.getMinPosition().getY(), stage.getMinPosition().getZ()),
            new BlocketPosition(stage.getMinPosition().getX() + 10, stage.getMinPosition().getY() + 5, stage.getMinPosition().getZ() + 10)
        );
        
        secretView.addBlocks(secretArea);
        
        // Only show to this specific player
        stage.addViewForPlayer(player, secretView);
        
        player.sendMessage("§6秘密礦層已為您開啟！");
    }
}
