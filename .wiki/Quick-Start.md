# Quick Start Guide
This page is 100% AI-generated and may contain inaccuracies. Please check for yourself.

Get up and running with Blocket in minutes! This guide will walk you through creating your first virtual block system.

## Prerequisites

- Blocket installed in your project ([Installation Guide](Installation))
- Basic understanding of Bukkit/Spigot plugin development
- A test server for experimentation

## Your First Virtual Block System

Let's create a simple private mine system where each player gets their own virtual ore deposits.

### Step 1: Initialize Blocket API

First, set up Blocket in your main plugin class:

```java
package com.example.myplugin;

import dev.twme.blocket.api.BlocketAPI;
import dev.twme.blocket.api.BlocketConfig;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {
    private BlocketAPI blocketAPI;
    
    @Override
    public void onEnable() {
        // Initialize with default configuration
        blocketAPI = BlocketAPI.initialize(this);
        
        // Or with custom configuration
        /*
        BlocketConfig config = BlocketConfig.builder()
            .autoInitialize(true)
            .enableStageBoundListener(true)
            .enablePacketListeners(true)
            .defaultChunksPerTick(2)
            .build();
        blocketAPI = BlocketAPI.initialize(this, config);
        */
        
        getLogger().info("MyPlugin with Blocket enabled!");
    }
    
    @Override
    public void onDisable() {
        if (blocketAPI != null) {
            blocketAPI.shutdown();
        }
    }
    
    public BlocketAPI getBlocketAPI() {
        return blocketAPI;
    }
}
```

### Step 2: Create a Simple Mine Command

Add a command that creates a private mine for players:

```java
import dev.twme.blocket.managers.StageManager;
import dev.twme.blocket.models.*;
import dev.twme.blocket.types.BlocketPosition;
import dev.twme.blocket.utils.BlockUtils;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Override
public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player player)) {
        sender.sendMessage("Only players can use this command!");
        return true;
    }
    
    if (command.getName().equalsIgnoreCase("createmine")) {
        createPrivateMine(player);
        return true;
    }
    
    return false;
}

private void createPrivateMine(Player player) {
    // Get player's current location as mine center
    Location center = player.getLocation();
    World world = center.getWorld();
    
    // Define mine boundaries (20x20x10 area)
    BlocketPosition pos1 = new BlocketPosition(
        center.getBlockX() - 10,
        center.getBlockY() - 5,
        center.getBlockZ() - 10
    );
    BlocketPosition pos2 = new BlocketPosition(
        center.getBlockX() + 10,
        center.getBlockY() + 5,
        center.getBlockZ() + 10
    );
    
    // 1. Create audience (only this player can see the mine)
    Set<Player> players = Set.of(player);
    Audience audience = Audience.fromPlayers(players);
    
    // 2. Define ore pattern with probabilities
    Map<BlockData, Double> orePattern = new HashMap<>();
    orePattern.put(Material.STONE.createBlockData(), 60.0);      // 60% stone
    orePattern.put(Material.COAL_ORE.createBlockData(), 25.0);   // 25% coal
    orePattern.put(Material.IRON_ORE.createBlockData(), 10.0);   // 10% iron
    orePattern.put(Material.DIAMOND_ORE.createBlockData(), 5.0); // 5% diamond
    
    Pattern pattern = new Pattern(orePattern);
    
    // 3. Create stage
    String stageId = "mine_" + player.getUniqueId();
    Stage stage = new Stage(stageId, world, pos1, pos2, audience);
    
    // Register stage with the manager
    StageManager stageManager = blocketAPI.getStageManager();
    stageManager.createStage(stage);
    
    // 4. Create view with the ore pattern
    View mineView = new View("ore_layer", stage, pattern, true); // true = breakable
    stage.addView(mineView);
    
    // 5. Fill the mine area with virtual blocks
    Set<BlocketPosition> mineBlocks = BlockUtils.getBlocksBetween(pos1, pos2);
    mineView.addBlocks(mineBlocks);
    
    // 6. Send blocks to the player
    stage.sendBlocksToAudience();
    
    player.sendMessage("§a✓ Your private mine has been created!");
    player.sendMessage("§7Mine ID: " + stageId);
    player.sendMessage("§7Area: " + mineBlocks.size() + " blocks");
}
```

### Step 3: Register the Command

Add the command to your `plugin.yml`:

```yaml
name: MyPlugin
version: 1.0.0
main: com.example.myplugin.MyPlugin
api-version: 1.16

commands:
  createmine:
    description: Create a private mine
    usage: /<command>
    permission: myplugin.createmine
    permission-message: You don't have permission to use this command.
```

### Step 4: Test Your Mine

1. Start your test server
2. Join the server as a player
3. Run the command: `/createmine`
4. You should see virtual ore blocks appear around you
5. Try breaking them - they should behave like real blocks!

## Understanding What Happened

Let's break down what our code accomplished:

### 1. **Audience Creation**
```java
Audience audience = Audience.fromPlayers(players);
```
- Created a group containing only the command executor
- Only this player will see the virtual blocks

### 2. **Pattern Definition**
```java
Map<BlockData, Double> orePattern = new HashMap<>();
orePattern.put(Material.STONE.createBlockData(), 60.0);
```
- Defined which blocks appear and their probabilities
- Total percentages should add up to 100.0

### 3. **Stage and View Creation**
```java
Stage stage = new Stage(stageId, world, pos1, pos2, audience);
View mineView = new View("ore_layer", stage, pattern, true);
```
- **Stage**: The container with boundaries and audience
- **View**: The layer containing the actual virtual blocks

### 4. **Block Generation**
```java
mineView.addBlocks(mineBlocks);
stage.sendBlocksToAudience();
```
- Added blocks to the view using the defined pattern
- Sent the visual changes to all players in the audience

## Next Steps

Now that you have a basic understanding, explore these concepts:

### Add Multiple Layers

```java
// Create a rare ore layer on top
Map<BlockData, Double> rareOrePattern = new HashMap<>();
rareOrePattern.put(Material.STONE.createBlockData(), 50.0);
rareOrePattern.put(Material.EMERALD_ORE.createBlockData(), 30.0);
rareOrePattern.put(Material.DIAMOND_ORE.createBlockData(), 20.0);

Pattern rarePattern = new Pattern(rareOrePattern);
View rareView = new View("rare_layer", stage, rarePattern, true);
rareView.setZIndex(2); // Higher priority than ore_layer

stage.addView(rareView);
// Add blocks to specific Y levels
Set<BlocketPosition> rareBlocks = mineBlocks.stream()
    .filter(pos -> pos.getY() > center.getBlockY())
    .collect(Collectors.toSet());
rareView.addBlocks(rareBlocks);
```

### Dynamic Block Management

```java
// Reset a specific block after it's broken
BlocketPosition brokenBlock = new BlocketPosition(x, y, z);
mineView.resetBlock(brokenBlock);

// Add a new block at runtime
mineView.addBlock(new BlocketPosition(x, y, z));

// Change a block to specific material
mineView.setBlock(brokenBlock, Material.GOLD_ORE.createBlockData());
```

### Event Handling

```java
import dev.twme.blocket.events.BlocketBreakEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MineListener implements Listener {
    
    @EventHandler
    public void onBlocketBreak(BlocketBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlockData().getMaterial();
        
        // Give rewards based on block type
        switch (blockType) {
            case COAL_ORE -> giveCoal(player);
            case IRON_ORE -> giveIron(player);
            case DIAMOND_ORE -> giveDiamond(player);
        }
        
        // Regenerate the block after 30 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            event.getView().resetBlock(event.getPosition());
        }, 20 * 30);
    }
}
```

## Common Patterns

### Multi-Player Mines

```java
// Create a mine for multiple players
Set<Player> teamPlayers = Set.of(player1, player2, player3);
Audience teamAudience = Audience.fromPlayers(teamPlayers);
```

### Temporary Stages

```java
// Auto-cleanup after 1 hour
Bukkit.getScheduler().runTaskLater(this, () -> {
    stageManager.deleteStage(stageId);
}, 20 * 60 * 60);
```

### Performance-Friendly Loading

```java
// Process large areas gradually
stage.setChunksPerTick(1); // Process 1 chunk per tick
```

## Troubleshooting

### Blocks Not Visible

- Check if PacketEvents is properly installed
- Verify the player is in the audience
- Ensure `sendBlocksToAudience()` was called

### Performance Issues

- Reduce `chunksPerTick` for large areas
- Use smaller stage boundaries
- Clean up unused stages regularly

### Blocks Not Breaking

- Ensure the view is marked as `breakable` (true parameter)
- Check if BlockDigAdapter is properly initialized
- Verify player permissions

## What's Next?

- 📖 Learn about [Stages and Views](Stages-and-Views) architecture
- 🎯 Explore [Block Patterns](Block-Patterns) in detail
- 👥 Master [Audience Management](Audience-Management)
- ⚡ Optimize with [Performance Tips](Performance-Optimization)
- 🎮 Check out more [Examples](Example-Private-Mine)

---

**Great job!** You've created your first virtual block system. The possibilities are endless from here!
