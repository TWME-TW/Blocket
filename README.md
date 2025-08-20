# Blocket

## About

Blocket is a development library that helps you manage and create client-side virtual blocks.

Ever wondered how servers like FadeCloud or AkumaMC implement private farms and mines? Now, Blocket can help you achieve these features! Blocket is a public library for managing and creating client-side virtual blocks.

## Installation

### As a Maven Dependency

Add the following to your plugin's `pom.xml`:

```xml
<repository>
    <id>twme-repo-releases</id>
    <name>TWME Repository</name>
    <url>https://repo.twme.dev/releases</url>
</repository>

<dependency>
    <groupId>dev.twme</groupId>
    <artifactId>blocket-api</artifactId>
    <version>1.0.0</version>
    <scope>compile</scope>
</dependency>
```

### As a Gradle Dependency

Add the following to your plugin's `build.gradle`:

```gradle
repositories {
    maven {
        url 'https://repo.twme.dev/releases'
    }
}
dependencies {
    implementation 'dev.twme:blocket-api:1.0.0'
}
```

### Quick Start Example

```java
public class MyPlugin extends JavaPlugin {
    private BlocketAPI blocketAPI;
    
    @Override
    public void onEnable() {
        // Initialize Blocket API
        blocketAPI = BlocketAPI.initialize(this);
        // Plugin logic...
    }
    
    @Override
    public void onDisable() {
        // Ensure Blocket API is shut down
        if (blocketAPI != null) {
            blocketAPI.shutdown();
        }
    }
}
```

## Features

1. **Stage Management**: Blocket provides different stages for audiences, each stage contains multiple "views" representing different modes within the stage.

2. **Block Interaction Events**: Handles block interaction events, such as starting to dig a block, as seen in the `BlockDigAdapter` class.

3. **Block Break Events**: Handles block break events, including checking if a block can be broken and sending block change updates to players.

4. **Chunk Loading**: The `ChunkLoadAdapter` class handles chunk loading events, including sending block changes to players.

5. **Game Mode Checking**: Checks the player's game mode and adjusts block breaking speed accordingly.

6. **Memory Management**: Efficient memory management using custom data types like `BlocketPosition` and `BlocketChunk`.

7. **Custom Events**: Provides custom event `BlocketBlockBreakEvent` triggered when a block is broken.

8. **Complex Block Patterns**: Handles complex block patterns via the `BlocketPattern` class, including crop age and other custom block data.

## API Usage Guide

### Basic Concepts

- **Stage**: Represents a bounded area containing an audience.
- **View**: A layer within a stage containing virtual block patterns.
- **Pattern**: Configuration defining block types and their probabilities.
- **Audience**: A set of players who can see the virtual blocks.

### Creating a Virtual Block System

#### 0. Initialize Blocket API

First, initialize BlocketAPI in your plugin:

```java
public class MyPlugin extends JavaPlugin {
    private BlocketAPI blocketAPI;
    
    @Override
    public void onEnable() {
        // Basic initialization
        blocketAPI = BlocketAPI.initialize(this);
        // Or custom config initialization
        BlocketConfig config = BlocketConfig.builder()
            .autoInitialize(true)
            .enableStageBoundListener(true)
            .enablePacketListeners(true)
            .defaultChunksPerTick(2)
            .build();
        blocketAPI = BlocketAPI.initialize(this, config);
    }
    
    @Override
    public void onDisable() {
        if (blocketAPI != null) {
            blocketAPI.shutdown();
        }
    }
    // Using the API
    public void createMine(Player player) {
        StageManager stageManager = blocketAPI.getStageManager();
        BlockChangeManager blockManager = blocketAPI.getBlockChangeManager();
        // ... your logic
    }
}
```

#### 1. Create Audience

```java
import models.dev.twme.blocket.Audience;

// Create audience from player set
Set<Player> players = Set.of(player1, player2);
Audience audience = Audience.fromPlayers(players);

// Or from UUID set
Set<UUID> playerUUIDs = Set.of(uuid1, uuid2);
Audience audience = Audience.fromUUIDs(playerUUIDs);
```

#### 2. Define Block Pattern

```java
import models.dev.twme.blocket.Pattern;
import org.bukkit.Material;

// Create block pattern (supports weighted distribution)
Map<BlockData, Double> blockPattern = new HashMap<>();
blockPattern.put(Material.STONE.createBlockData(), 70.0); // 70% chance
blockPattern.put(Material.COAL_ORE.createBlockData(), 20.0); // 20% chance
blockPattern.put(Material.IRON_ORE.createBlockData(), 10.0); // 10% chance

Pattern pattern = new Pattern(blockPattern);
```

#### 3. Create Stage

```java
import models.dev.twme.blocket.Stage;
import types.dev.twme.blocket.BlocketPosition;

// Define stage boundaries
BlocketPosition pos1 = new BlocketPosition(100, 60, 100);
BlocketPosition pos2 = new BlocketPosition(150, 100, 150);

// Create stage
Stage stage = new Stage("my-mine", world, pos1, pos2, audience);

// Register stage to manager
Blocket.getInstance().getStageManager().createStage(stage);
```

#### 4. Create View and Add Blocks

```java
import models.dev.twme.blocket.View;

// Create view
View view = new View("ore-layer", stage, pattern, true); // true = breakable
view.setZIndex(1); // Set layer priority

// Add view to stage
stage.addView(view);

// Add blocks to view
Set<BlocketPosition> positions = BlockUtils.getBlocksBetween(pos1, pos2);
view.addBlocks(positions);

// Send block changes to audience
stage.sendBlocksToAudience();
```

#### Dynamic Block Management

```java
// Add a single block
BlocketPosition position = new BlocketPosition(125, 75, 125);
view.addBlock(position);

// Set specific block
view.setBlock(position, Material.DIAMOND_ORE.createBlockData());

// Reset block (use original pattern)
view.resetBlock(position);

// Remove block
view.removeBlock(position);

// Batch operations
Set<BlocketPosition> blockPositions = Set.of(pos1, pos2, pos3);
view.addBlocks(blockPositions);
view.setBlocks(blockPositions, Material.EMERALD_ORE.createBlockData());
view.removeBlocks(blockPositions);
```

#### Player View Management

```java
// Add view for specific player
stage.addViewForPlayer(player, view);
stage.addViewForPlayer(player, "ore-layer");

// Remove view for specific player
stage.removeViewForPlayer(player, view);
stage.removeViewForPlayer(player, "ore-layer");

// Hide view
Blocket.getInstance().getBlockChangeManager().hideView(player, view);
```

#### Audience Management

```java
// Add player to audience
audience.addPlayer(player);

// Remove player
audience.removePlayer(player);

// Set mining speed
audience.setMiningSpeed(player, 2.0f); // 2x speed

// Reset mining speed
audience.resetMiningSpeed(player);
```

### Complete Example: Creating a Mine

```java
public class MinePlugin extends JavaPlugin {
    private BlocketAPI blocketAPI;
    
    @Override
    public void onEnable() {
        blocketAPI = BlocketAPI.initialize(this);
    }
    
    @Override
    public void onDisable() {
        if (blocketAPI != null) {
            blocketAPI.shutdown();
        }
    }
    
    public void createMine(Player player, Location corner1, Location corner2) {
        // 1. Create audience
        Set<Player> players = Set.of(player);
        Audience audience = Audience.fromPlayers(players);
        
        // 2. Define ore pattern
        Map<BlockData, Double> orePattern = new HashMap<>();
        orePattern.put(Material.STONE.createBlockData(), 60.0);
        orePattern.put(Material.COAL_ORE.createBlockData(), 25.0);
        orePattern.put(Material.IRON_ORE.createBlockData(), 10.0);
        orePattern.put(Material.DIAMOND_ORE.createBlockData(), 5.0);
        
        Pattern pattern = new Pattern(orePattern);
        
        // 3. Create stage
        BlocketPosition pos1 = BlocketPosition.fromLocation(corner1);
        BlocketPosition pos2 = BlocketPosition.fromLocation(corner2);
        
        Stage stage = new Stage("player-mine", corner1.getWorld(), pos1, pos2, audience);
        blocketAPI.getStageManager().createStage(stage);
        
        // 4. Create view
        View mineView = new View("ore-deposits", stage, pattern, true);
        stage.addView(mineView);
        
        // 5. Fill area
        Set<BlocketPosition> mineBlocks = BlockUtils.getBlocksBetween(pos1, pos2);
        mineView.addBlocks(mineBlocks);
        
        // 6. Send to player
        stage.sendBlocksToAudience();
        
        player.sendMessage("§aPrivate mine created!");
    }
}
```

### Performance Optimization Tips

1. **Batch Processing**: For large areas, use `stage.setChunksPerTick()` to control the number of chunks processed per tick.
2. **Asynchronous Operations**: Perform large block operations in asynchronous threads.
3. **Memory Management**: Clean up unused views and stages in time.
4. **Event Handling**: Avoid heavy computation in event handlers.

## Dependencies

- [PacketEvents](https://github.com/retrooper/packetevents)
