# Stages and Views
This page is 100% AI-generated and may contain inaccuracies. Please check for yourself.

Understand the core architecture of Blocket's virtual block system: Stages and Views.

## Architecture Overview

Blocket organizes virtual blocks using a hierarchical system:

```
Stage (Container)
├── Audience (Who can see it)
├── Boundaries (Where it exists)
└── Views (What they see)
    ├── View 1 (Layer 1)
    ├── View 2 (Layer 2)
    └── View N (Layer N)
```

## Stages

A **Stage** is a bounded 3D area containing an audience and one or more views. Think of it as a "world within a world" visible only to specific players.

### Stage Components

#### 1. Unique Identifier
Every stage needs a unique string ID within your plugin:

```java
String stageId = "player_mine_" + player.getUniqueId();
String stageId = "guild_farm_" + guildId;
String stageId = "event_area_spawn";
```

#### 2. World Reference
Stages exist within a specific Bukkit world:

```java
World world = player.getWorld();
World world = Bukkit.getWorld("custom_world");
```

#### 3. Boundaries
Define the 3D area using two corner positions:

```java
// Create boundaries
BlocketPosition corner1 = new BlocketPosition(100, 60, 100);  // Lower corner
BlocketPosition corner2 = new BlocketPosition(200, 120, 200); // Upper corner

// From Bukkit locations
BlocketPosition pos1 = BlocketPosition.fromLocation(location1);
BlocketPosition pos2 = BlocketPosition.fromLocation(location2);
```

#### 4. Audience
The group of players who can see and interact with the stage:

```java
// Single player
Set<Player> players = Set.of(player);
Audience audience = Audience.fromPlayers(players);

// Multiple players
Set<Player> teamPlayers = Set.of(player1, player2, player3);
Audience teamAudience = Audience.fromPlayers(teamPlayers);

// From UUIDs (for offline players)
Set<UUID> memberUUIDs = guild.getMemberUUIDs();
Audience guildAudience = Audience.fromUUIDs(memberUUIDs);
```

### Creating a Stage

```java
import dev.twme.blocket.models.Stage;
import dev.twme.blocket.models.Audience;
import dev.twme.blocket.types.BlocketPosition;

public void createStage(Player owner, Location center) {
    // Define boundaries (50x50x25 area)
    BlocketPosition pos1 = new BlocketPosition(
        center.getBlockX() - 25,
        center.getBlockY() - 10,
        center.getBlockZ() - 25
    );
    BlocketPosition pos2 = new BlocketPosition(
        center.getBlockX() + 25,
        center.getBlockY() + 15,
        center.getBlockZ() + 25
    );
    
    // Create audience
    Audience audience = Audience.fromPlayers(Set.of(owner));
    
    // Create and register stage
    String stageId = "mine_" + owner.getUniqueId();
    Stage stage = new Stage(stageId, center.getWorld(), pos1, pos2, audience);
    
    // Register with the manager
    BlocketAPI.getInstance().getStageManager().createStage(stage);
}
```

### Stage Properties

#### Chunks Per Tick
Control how fast blocks are processed:

```java
stage.setChunksPerTick(2); // Process 2 chunks per tick
```

#### Boundary Checking
Stages automatically handle player movement:

```java
// Check if a position is within stage boundaries
boolean isInside = stage.isPositionInside(position);

// Get all positions within boundaries
Set<BlocketPosition> allPositions = stage.getAllPositions();
```

## Views

A **View** is a layer within a stage that contains virtual blocks following a specific pattern. Stages can have multiple views, creating complex layered systems.

### View Components

#### 1. Name and Stage
Every view belongs to a stage and has a unique name within that stage:

```java
View oreView = new View("ore_layer", stage, pattern, true);
View rareView = new View("rare_layer", stage, rarePattern, true);
```

#### 2. Pattern
Defines what blocks appear and their probabilities:

```java
Map<BlockData, Double> orePattern = new HashMap<>();
orePattern.put(Material.STONE.createBlockData(), 60.0);
orePattern.put(Material.COAL_ORE.createBlockData(), 30.0);
orePattern.put(Material.IRON_ORE.createBlockData(), 10.0);

Pattern pattern = new Pattern(orePattern);
```

#### 3. Breakability
Whether players can break blocks in this view:

```java
View breakableView = new View("breakable", stage, pattern, true);
View decorativeView = new View("decoration", stage, pattern, false);
```

#### 4. Z-Index (Layer Priority)
Controls which view appears when blocks overlap:

```java
oreView.setZIndex(1);      // Bottom layer
rareView.setZIndex(2);     // Top layer (visible when overlapping)
decorativeView.setZIndex(0); // Behind everything
```

### Creating Views

#### Basic View Creation

```java
public View createOreView(Stage stage) {
    // Define ore distribution
    Map<BlockData, Double> ores = new HashMap<>();
    ores.put(Material.STONE.createBlockData(), 70.0);
    ores.put(Material.COAL_ORE.createBlockData(), 20.0);
    ores.put(Material.IRON_ORE.createBlockData(), 8.0);
    ores.put(Material.DIAMOND_ORE.createBlockData(), 2.0);
    
    Pattern orePattern = new Pattern(ores);
    
    // Create breakable view
    View view = new View("standard_ores", stage, orePattern, true);
    view.setZIndex(1);
    
    return view;
}
```

#### Multi-Layer System

```java
public void createLayeredMine(Stage stage) {
    // Base stone layer
    Map<BlockData, Double> basePattern = new HashMap<>();
    basePattern.put(Material.STONE.createBlockData(), 100.0);
    View baseView = new View("base_stone", stage, new Pattern(basePattern), false);
    baseView.setZIndex(0);
    
    // Common ore layer
    Map<BlockData, Double> commonPattern = new HashMap<>();
    commonPattern.put(Material.COAL_ORE.createBlockData(), 70.0);
    commonPattern.put(Material.IRON_ORE.createBlockData(), 30.0);
    View commonView = new View("common_ores", stage, new Pattern(commonPattern), true);
    commonView.setZIndex(1);
    
    // Rare ore layer (higher Y levels only)
    Map<BlockData, Double> rarePattern = new HashMap<>();
    rarePattern.put(Material.DIAMOND_ORE.createBlockData(), 60.0);
    rarePattern.put(Material.EMERALD_ORE.createBlockData(), 40.0);
    View rareView = new View("rare_ores", stage, new Pattern(rarePattern), true);
    rareView.setZIndex(2);
    
    // Add all views to stage
    stage.addView(baseView);
    stage.addView(commonView);
    stage.addView(rareView);
}
```

### Block Management in Views

#### Adding Blocks to Views

```java
// Add all blocks in stage boundaries
Set<BlocketPosition> allPositions = BlockUtils.getBlocksBetween(pos1, pos2);
view.addBlocks(allPositions);

// Add specific blocks
Set<BlocketPosition> oreSpots = new HashSet<>();
oreSpots.add(new BlocketPosition(100, 65, 100));
oreSpots.add(new BlocketPosition(101, 65, 100));
view.addBlocks(oreSpots);

// Add blocks with conditions
allPositions.stream()
    .filter(pos -> pos.getY() > 70) // Only upper levels
    .forEach(view::addBlock);
```

#### Dynamic Block Updates

```java
// Change specific block
BlocketPosition pos = new BlocketPosition(100, 65, 100);
view.setBlock(pos, Material.GOLD_ORE.createBlockData());

// Reset block to pattern
view.resetBlock(pos);

// Remove block entirely
view.removeBlock(pos);

// Batch operations
Set<BlocketPosition> positions = Set.of(pos1, pos2, pos3);
view.setBlocks(positions, Material.EMERALD_ORE.createBlockData());
view.resetBlocks(positions);
view.removeBlocks(positions);
```

#### View-Specific Operations

```java
// Clear all blocks from view
view.clearBlocks();

// Get all blocks in view
Set<BlocketPosition> viewBlocks = view.getBlocks();

// Check if view contains a block
boolean hasBlock = view.hasBlock(position);

// Get block data at position
BlockData blockData = view.getBlockData(position);
```

## Stage and View Management

### Stage Manager Operations

```java
StageManager stageManager = BlocketAPI.getInstance().getStageManager();

// Create stage
stageManager.createStage(stage);

// Get stage by ID
Stage stage = stageManager.getStage("stage_id");

// Get all stages
Set<Stage> allStages = stageManager.getAllStages();

// Delete stage
stageManager.deleteStage("stage_id");

// Check if stage exists
boolean exists = stageManager.hasStage("stage_id");
```

### Per-Player View Management

```java
// Add view for specific player
stage.addViewForPlayer(player, view);
stage.addViewForPlayer(player, "view_name");

// Remove view for specific player
stage.removeViewForPlayer(player, view);
stage.removeViewForPlayer(player, "view_name");

// Get player's active views
Set<View> playerViews = stage.getViewsForPlayer(player);

// Hide view from player
BlocketAPI.getInstance().getBlockChangeManager().hideView(player, view);
```

## Advanced Patterns

### Conditional Views

```java
public void createConditionalViews(Stage stage, Player player) {
    // Different views based on player level
    int playerLevel = player.getLevel();
    
    if (playerLevel >= 50) {
        // High-level player gets rare ores
        View rareView = createRareOreView(stage);
        stage.addViewForPlayer(player, rareView);
    } else {
        // Low-level player gets common ores
        View commonView = createCommonOreView(stage);
        stage.addViewForPlayer(player, commonView);
    }
}
```

### Dynamic Stage Expansion

```java
public void expandStage(Stage stage, BlocketPosition newCorner) {
    // Calculate new boundary
    BlocketPosition currentMax = stage.getPos2();
    BlocketPosition expandedCorner = new BlocketPosition(
        Math.max(currentMax.getX(), newCorner.getX()),
        Math.max(currentMax.getY(), newCorner.getY()),
        Math.max(currentMax.getZ(), newCorner.getZ())
    );
    
    // Create new expanded stage
    Stage expandedStage = new Stage(
        stage.getId() + "_expanded",
        stage.getWorld(),
        stage.getPos1(),
        expandedCorner,
        stage.getAudience()
    );
    
    // Transfer views
    for (View view : stage.getViews()) {
        expandedStage.addView(view);
    }
    
    // Replace old stage
    StageManager manager = BlocketAPI.getInstance().getStageManager();
    manager.deleteStage(stage.getId());
    manager.createStage(expandedStage);
}
```

### Temporary Stages

```java
public void createTemporaryStage(Player player, int durationSeconds) {
    Stage tempStage = createStage(player);
    
    // Auto-cleanup after duration
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
        BlocketAPI.getInstance().getStageManager().deleteStage(tempStage.getId());
        player.sendMessage("§eTemporary area has expired!");
    }, durationSeconds * 20L);
}
```

## Performance Considerations

### Optimize Stage Size

```java
// Good: Reasonable size
BlocketPosition pos1 = new BlocketPosition(0, 60, 0);
BlocketPosition pos2 = new BlocketPosition(100, 80, 100); // 100x20x100 = 200,000 blocks

// Avoid: Too large
BlocketPosition pos2 = new BlocketPosition(1000, 256, 1000); // 1000x196x1000 = 196M blocks
```

### Limit Active Stages

```java
// Monitor stage count
public void checkStageCount() {
    Set<Stage> stages = stageManager.getAllStages();
    if (stages.size() > 100) { // Your limit
        getLogger().warning("Too many active stages: " + stages.size());
        // Clean up old stages
    }
}
```

### Efficient Block Management

```java
// Good: Batch operations
Set<BlocketPosition> positions = getPositionsBatch();
view.addBlocks(positions);

// Avoid: Individual operations in loops
for (BlocketPosition pos : positions) {
    view.addBlock(pos); // Less efficient
}
```

## Common Patterns and Use Cases

### Private Mining Areas

```java
public Stage createPrivateMine(Player owner) {
    Location center = owner.getLocation();
    BlocketPosition pos1 = new BlocketPosition(center.getBlockX() - 50, 40, center.getBlockZ() - 50);
    BlocketPosition pos2 = new BlocketPosition(center.getBlockX() + 50, 70, center.getBlockZ() + 50);
    
    Audience audience = Audience.fromPlayers(Set.of(owner));
    Stage stage = new Stage("mine_" + owner.getUniqueId(), center.getWorld(), pos1, pos2, audience);
    
    // Multiple ore layers
    View commonOres = createOreView(stage, "common", getCommonOrePattern(), 1);
    View rareOres = createOreView(stage, "rare", getRareOrePattern(), 2);
    
    stage.addView(commonOres);
    stage.addView(rareOres);
    
    return stage;
}
```

### Guild Farms

```java
public Stage createGuildFarm(Guild guild) {
    Set<UUID> memberUUIDs = guild.getMemberUUIDs();
    Audience guildAudience = Audience.fromUUIDs(memberUUIDs);
    
    Location farmCenter = guild.getFarmLocation();
    Stage farmStage = new Stage(
        "guild_farm_" + guild.getId(),
        farmCenter.getWorld(),
        // Define farm boundaries...
        pos1, pos2,
        guildAudience
    );
    
    // Crop views
    View wheatView = createCropView(farmStage, "wheat", Material.WHEAT);
    View carrotView = createCropView(farmStage, "carrots", Material.CARROTS);
    
    farmStage.addView(wheatView);
    farmStage.addView(carrotView);
    
    return farmStage;
}
```

### Event Areas

```java
public Stage createEventArea(Location eventLocation, List<Player> participants) {
    Audience eventAudience = Audience.fromPlayers(new HashSet<>(participants));
    
    Stage eventStage = new Stage(
        "event_" + System.currentTimeMillis(),
        eventLocation.getWorld(),
        // Event boundaries...
        pos1, pos2,
        eventAudience
    );
    
    // Decorative, non-breakable view
    View decorationView = new View("decorations", eventStage, decorationPattern, false);
    eventStage.addView(decorationView);
    
    return eventStage;
}
```

## Best Practices

### 1. Unique Stage IDs

```java
// Good: Guaranteed unique
String stageId = "mine_" + player.getUniqueId() + "_" + System.currentTimeMillis();

// Avoid: Potential conflicts
String stageId = "mine_" + player.getName(); // Names can change
```

### 2. Proper Cleanup

```java
@EventHandler
public void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    
    // Clean up player's private stages
    stageManager.getAllStages().stream()
        .filter(stage -> stage.getId().contains(player.getUniqueId().toString()))
        .forEach(stage -> stageManager.deleteStage(stage.getId()));
}
```

### 3. Error Handling

```java
public boolean createPlayerStage(Player player) {
    try {
        Stage stage = new Stage(/* ... */);
        stageManager.createStage(stage);
        return true;
    } catch (Exception e) {
        getLogger().severe("Failed to create stage for " + player.getName() + ": " + e.getMessage());
        return false;
    }
}
```

## Next Steps

- 📖 Learn about [Block Patterns](Block-Patterns) in detail
- 👥 Master [Audience Management](Audience-Management)
- ⚡ Explore [Dynamic Block Management](Dynamic-Block-Management)
- 🎮 See complete [Examples](Example-Private-Mine)

---

**Understanding Stages and Views is crucial for effective use of Blocket. They form the foundation of all virtual block systems!**
