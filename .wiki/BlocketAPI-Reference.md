# BlocketAPI Reference
This page is 100% AI-generated and may contain inaccuracies. Please check for yourself.

Complete reference documentation for the main BlocketAPI class and its components.

## BlocketAPI Class

The central API class for initializing and managing Blocket functionality.

### Static Methods

#### initialize(Plugin plugin)

Initialize BlocketAPI with default configuration.

```java
public static BlocketAPI initialize(Plugin plugin)
```

**Parameters:**
- `plugin` - The plugin that will own this BlocketAPI instance

**Returns:** `BlocketAPI` instance

**Throws:** `IllegalStateException` if already initialized

**Example:**
```java
@Override
public void onEnable() {
    BlocketAPI blocketAPI = BlocketAPI.initialize(this);
}
```

#### initialize(Plugin plugin, BlocketConfig config)

Initialize BlocketAPI with custom configuration.

```java
public static BlocketAPI initialize(Plugin plugin, BlocketConfig config)
```

**Parameters:**
- `plugin` - The plugin that will own this BlocketAPI instance
- `config` - Custom configuration settings

**Returns:** `BlocketAPI` instance

**Throws:** `IllegalStateException` if already initialized

**Example:**
```java
BlocketConfig config = BlocketConfig.builder()
    .defaultChunksPerTick(3)
    .enableStageBoundListener(true)
    .build();
    
BlocketAPI blocketAPI = BlocketAPI.initialize(this, config);
```

#### getInstance()

Get the current BlocketAPI instance.

```java
public static BlocketAPI getInstance()
```

**Returns:** Current `BlocketAPI` instance

**Throws:** `IllegalStateException` if not initialized

**Example:**
```java
if (BlocketAPI.isInitialized()) {
    BlocketAPI api = BlocketAPI.getInstance();
}
```

#### isInitialized()

Check if BlocketAPI has been initialized.

```java
public static boolean isInitialized()
```

**Returns:** `true` if initialized, `false` otherwise

**Example:**
```java
if (!BlocketAPI.isInitialized()) {
    getLogger().warning("Blocket not initialized!");
}
```

### Instance Methods

#### getStageManager()

Get the stage manager for creating and managing stages.

```java
public StageManager getStageManager()
```

**Returns:** `StageManager` instance

**Example:**
```java
StageManager stageManager = blocketAPI.getStageManager();
Stage stage = new Stage("my-stage", world, pos1, pos2, audience);
stageManager.createStage(stage);
```

#### getBlockChangeManager()

Get the block change manager for handling block updates.

```java
public BlockChangeManager getBlockChangeManager()
```

**Returns:** `BlockChangeManager` instance

**Example:**
```java
BlockChangeManager blockManager = blocketAPI.getBlockChangeManager();
blockManager.initializePlayer(player);
```

#### getConfig()

Get the configuration used to initialize this API instance.

```java
public BlocketConfig getConfig()
```

**Returns:** `BlocketConfig` instance

**Example:**
```java
BlocketConfig config = blocketAPI.getConfig();
int chunksPerTick = config.getDefaultChunksPerTick();
```

#### getOwnerPlugin()

Get the plugin that owns this BlocketAPI instance.

```java
public Plugin getOwnerPlugin()
```

**Returns:** Owner `Plugin` instance

**Example:**
```java
Plugin owner = blocketAPI.getOwnerPlugin();
owner.getLogger().info("API initialized successfully!");
```

#### getServerVersion()

Get the detected server version.

```java
public ServerVersion getServerVersion()
```

**Returns:** `ServerVersion` from PacketEvents

**Example:**
```java
ServerVersion version = blocketAPI.getServerVersion();
if (version.isNewerThan(ServerVersion.V_1_16)) {
    // Use newer features
}
```

#### initializeListeners()

Manually initialize listeners (if autoInitialize is false).

```java
public void initializeListeners()
```

**Example:**
```java
BlocketConfig config = BlocketConfig.builder()
    .autoInitialize(false)
    .build();
    
BlocketAPI api = BlocketAPI.initialize(this, config);

// Later, when ready...
api.initializeListeners();
```

#### shutdown()

Shutdown and cleanup all resources. Must be called in plugin's onDisable().

```java
public void shutdown()
```

**Example:**
```java
@Override
public void onDisable() {
    if (blocketAPI != null) {
        blocketAPI.shutdown();
    }
}
```

## StageManager Class

Manages the lifecycle of stages within Blocket.

### Methods

#### createStage(Stage stage)

Register a new stage with the manager.

```java
public void createStage(Stage stage)
```

**Parameters:**
- `stage` - The stage to register

**Throws:** `IllegalArgumentException` if stage ID already exists

**Example:**
```java
Stage stage = new Stage("unique-id", world, pos1, pos2, audience);
stageManager.createStage(stage);
```

#### getStage(String stageId)

Get a stage by its unique ID.

```java
public Stage getStage(String stageId)
```

**Parameters:**
- `stageId` - The unique stage identifier

**Returns:** `Stage` instance or `null` if not found

**Example:**
```java
Stage stage = stageManager.getStage("player-mine-123");
if (stage != null) {
    // Stage exists
}
```

#### hasStage(String stageId)

Check if a stage exists.

```java
public boolean hasStage(String stageId)
```

**Parameters:**
- `stageId` - The unique stage identifier

**Returns:** `true` if stage exists, `false` otherwise

**Example:**
```java
if (stageManager.hasStage("temp-stage")) {
    stageManager.deleteStage("temp-stage");
}
```

#### getAllStages()

Get all registered stages.

```java
public Set<Stage> getAllStages()
```

**Returns:** Set of all `Stage` instances

**Example:**
```java
Set<Stage> allStages = stageManager.getAllStages();
getLogger().info("Total stages: " + allStages.size());
```

#### deleteStage(String stageId)

Delete a stage and clean up its resources.

```java
public void deleteStage(String stageId)
```

**Parameters:**
- `stageId` - The unique stage identifier

**Example:**
```java
stageManager.deleteStage("temporary-stage");
```

## BlockChangeManager Class

Manages block changes and synchronization with players.

### Methods

#### initializePlayer(Player player)

Initialize block change tracking for a player.

```java
public void initializePlayer(Player player)
```

**Parameters:**
- `player` - The player to initialize

**Example:**
```java
@EventHandler
public void onPlayerJoin(PlayerJoinEvent event) {
    blockChangeManager.initializePlayer(event.getPlayer());
}
```

#### removePlayer(Player player)

Remove all tracking data for a player.

```java
public void removePlayer(Player player)
```

**Parameters:**
- `player` - The player to remove

**Example:**
```java
@EventHandler
public void onPlayerQuit(PlayerQuitEvent event) {
    blockChangeManager.removePlayer(event.getPlayer());
}
```

#### hideView(Player player, View view)

Hide a specific view from a player.

```java
public void hideView(Player player, View view)
```

**Parameters:**
- `player` - The player to hide the view from
- `view` - The view to hide

**Example:**
```java
// Hide a view temporarily
blockChangeManager.hideView(player, secretView);
```

#### sendBlockChange(Player player, BlocketPosition position, BlockData blockData)

Send a single block change to a player.

```java
public void sendBlockChange(Player player, BlocketPosition position, BlockData blockData)
```

**Parameters:**
- `player` - The target player
- `position` - The block position
- `blockData` - The block data to send

**Example:**
```java
BlocketPosition pos = new BlocketPosition(100, 65, 100);
BlockData diamond = Material.DIAMOND_ORE.createBlockData();
blockChangeManager.sendBlockChange(player, pos, diamond);
```

#### sendBlockChanges(Player player, Map<BlocketPosition, BlockData> changes)

Send multiple block changes to a player.

```java
public void sendBlockChanges(Player player, Map<BlocketPosition, BlockData> changes)
```

**Parameters:**
- `player` - The target player
- `changes` - Map of positions to block data

**Example:**
```java
Map<BlocketPosition, BlockData> changes = new HashMap<>();
changes.put(pos1, Material.GOLD_ORE.createBlockData());
changes.put(pos2, Material.IRON_ORE.createBlockData());
blockChangeManager.sendBlockChanges(player, changes);
```

## Stage Class

Represents a bounded area containing virtual blocks.

### Constructor

```java
public Stage(String id, World world, BlocketPosition pos1, BlocketPosition pos2, Audience audience)
```

**Parameters:**
- `id` - Unique stage identifier
- `world` - The world the stage exists in
- `pos1` - First corner position
- `pos2` - Opposite corner position  
- `audience` - Players who can see this stage

### Methods

#### getId()

Get the unique stage identifier.

```java
public String getId()
```

**Returns:** Stage ID string

#### getWorld()

Get the world this stage exists in.

```java
public World getWorld()
```

**Returns:** Bukkit `World` instance

#### getPos1() / getPos2()

Get the boundary positions.

```java
public BlocketPosition getPos1()
public BlocketPosition getPos2()
```

**Returns:** `BlocketPosition` boundary corners

#### getAudience()

Get the audience for this stage.

```java
public Audience getAudience()
```

**Returns:** `Audience` instance

#### addView(View view)

Add a view to this stage.

```java
public void addView(View view)
```

**Parameters:**
- `view` - The view to add

**Example:**
```java
View oreView = new View("ores", stage, pattern, true);
stage.addView(oreView);
```

#### removeView(String viewName)

Remove a view by name.

```java
public void removeView(String viewName)
```

**Parameters:**
- `viewName` - The name of the view to remove

#### getView(String viewName)

Get a view by name.

```java
public View getView(String viewName)
```

**Parameters:**
- `viewName` - The name of the view

**Returns:** `View` instance or `null` if not found

#### getViews()

Get all views in this stage.

```java
public Set<View> getViews()
```

**Returns:** Set of all `View` instances

#### isPositionInside(BlocketPosition position)

Check if a position is within stage boundaries.

```java
public boolean isPositionInside(BlocketPosition position)
```

**Parameters:**
- `position` - The position to check

**Returns:** `true` if inside boundaries, `false` otherwise

#### sendBlocksToAudience()

Send all stage blocks to the audience.

```java
public void sendBlocksToAudience()
```

**Example:**
```java
// After adding blocks to views
stage.sendBlocksToAudience();
```

#### setChunksPerTick(int chunksPerTick)

Set chunks processed per tick for this stage.

```java
public void setChunksPerTick(int chunksPerTick)
```

**Parameters:**
- `chunksPerTick` - Number of chunks to process per tick

## View Class

Represents a layer of blocks within a stage.

### Constructor

```java
public View(String name, Stage stage, Pattern pattern, boolean breakable)
```

**Parameters:**
- `name` - Unique view name within the stage
- `stage` - The parent stage
- `pattern` - Block generation pattern
- `breakable` - Whether blocks can be broken

### Methods

#### getName()

Get the view name.

```java
public String getName()
```

**Returns:** View name string

#### getStage()

Get the parent stage.

```java
public Stage getStage()
```

**Returns:** Parent `Stage` instance

#### getPattern()

Get the block pattern.

```java
public Pattern getPattern()
```

**Returns:** `Pattern` instance

#### isBreakable()

Check if blocks in this view can be broken.

```java
public boolean isBreakable()
```

**Returns:** `true` if breakable, `false` otherwise

#### setZIndex(int zIndex)

Set the view's layer priority (higher = more visible).

```java
public void setZIndex(int zIndex)
```

**Parameters:**
- `zIndex` - Layer priority (0 = bottom)

#### getZIndex()

Get the view's layer priority.

```java
public int getZIndex()
```

**Returns:** Current z-index value

#### addBlock(BlocketPosition position)

Add a single block using the view's pattern.

```java
public void addBlock(BlocketPosition position)
```

**Parameters:**
- `position` - The position to add a block at

#### addBlocks(Set<BlocketPosition> positions)

Add multiple blocks using the view's pattern.

```java
public void addBlocks(Set<BlocketPosition> positions)
```

**Parameters:**
- `positions` - Set of positions to add blocks at

#### setBlock(BlocketPosition position, BlockData blockData)

Set a specific block at a position.

```java
public void setBlock(BlocketPosition position, BlockData blockData)
```

**Parameters:**
- `position` - The position to set
- `blockData` - The block data to set

#### removeBlock(BlocketPosition position)

Remove a block from the view.

```java
public void removeBlock(BlocketPosition position)
```

**Parameters:**
- `position` - The position to remove

#### removeBlocks(Set<BlocketPosition> positions)

Remove multiple blocks from the view.

```java
public void removeBlocks(Set<BlocketPosition> positions)
```

**Parameters:**
- `positions` - Set of positions to remove

#### resetBlock(BlocketPosition position)

Reset a block to use the view's pattern.

```java
public void resetBlock(BlocketPosition position)
```

**Parameters:**
- `position` - The position to reset

#### hasBlock(BlocketPosition position)

Check if the view contains a block at a position.

```java
public boolean hasBlock(BlocketPosition position)
```

**Parameters:**
- `position` - The position to check

**Returns:** `true` if block exists, `false` otherwise

#### getBlocks()

Get all block positions in this view.

```java
public Set<BlocketPosition> getBlocks()
```

**Returns:** Set of all block positions

#### getBlockData(BlocketPosition position)

Get the block data at a position.

```java
public BlockData getBlockData(BlocketPosition position)
```

**Parameters:**
- `position` - The position to check

**Returns:** `BlockData` at the position or `null` if not found

#### clearBlocks()

Remove all blocks from the view.

```java
public void clearBlocks()
```

## Pattern Class

Defines block types and their generation probabilities.

### Constructor

```java
public Pattern(Map<BlockData, Double> blockWeights)
```

**Parameters:**
- `blockWeights` - Map of block data to probability weights

### Methods

#### generateBlock()

Generate a random block based on the pattern.

```java
public BlockData generateBlock()
```

**Returns:** Random `BlockData` according to pattern weights

#### getBlocks()

Get all block types in this pattern.

```java
public Set<BlockData> getBlocks()
```

**Returns:** Set of all `BlockData` in the pattern

#### hasBlock(BlockData blockData)

Check if the pattern contains a block type.

```java
public boolean hasBlock(BlockData blockData)
```

**Parameters:**
- `blockData` - The block data to check for

**Returns:** `true` if pattern contains the block type

## Audience Class

Manages the group of players who can see virtual blocks.

### Static Factory Methods

#### fromPlayers(Set<Player> players)

Create an audience from online players.

```java
public static Audience fromPlayers(Set<Player> players)
```

**Parameters:**
- `players` - Set of online players

**Returns:** `Audience` instance

#### fromUUIDs(Set<UUID> playerUUIDs)

Create an audience from player UUIDs (supports offline players).

```java
public static Audience fromUUIDs(Set<UUID> playerUUIDs)
```

**Parameters:**
- `playerUUIDs` - Set of player UUIDs

**Returns:** `Audience` instance

### Methods

#### addPlayer(Player player)

Add a player to the audience.

```java
public void addPlayer(Player player)
```

**Parameters:**
- `player` - The player to add

#### removePlayer(Player player)

Remove a player from the audience.

```java
public void removePlayer(Player player)
```

**Parameters:**
- `player` - The player to remove

#### contains(Player player)

Check if a player is in the audience.

```java
public boolean contains(Player player)
```

**Parameters:**
- `player` - The player to check

**Returns:** `true` if player is in audience

#### getOnlinePlayers()

Get all online players in the audience.

```java
public Set<Player> getOnlinePlayers()
```

**Returns:** Set of online players

#### isEmpty()

Check if the audience is empty.

```java
public boolean isEmpty()
```

**Returns:** `true` if no players in audience

#### size()

Get the total number of players in the audience.

```java
public int size()
```

**Returns:** Number of players (online and offline)

## BlocketPosition Class

Represents a position in 3D space optimized for Blocket operations.

### Constructors

```java
public BlocketPosition(int x, int y, int z)
public BlocketPosition(double x, double y, double z)
```

### Static Methods

#### fromLocation(Location location)

Create a BlocketPosition from a Bukkit Location.

```java
public static BlocketPosition fromLocation(Location location)
```

**Parameters:**
- `location` - Bukkit location

**Returns:** `BlocketPosition` instance

### Methods

#### getX() / getY() / getZ()

Get coordinate values.

```java
public int getX()
public int getY() 
public int getZ()
```

**Returns:** Integer coordinate value

#### toLocation(World world)

Convert to a Bukkit Location.

```java
public Location toLocation(World world)
```

**Parameters:**
- `world` - The world for the location

**Returns:** Bukkit `Location` instance

#### distance(BlocketPosition other)

Calculate distance to another position.

```java
public double distance(BlocketPosition other)
```

**Parameters:**
- `other` - The other position

**Returns:** Distance as double

#### add(int x, int y, int z)

Create a new position with added offsets.

```java
public BlocketPosition add(int x, int y, int z)
```

**Parameters:**
- `x`, `y`, `z` - Offsets to add

**Returns:** New `BlocketPosition` instance

## Error Handling

### Common Exceptions

#### IllegalStateException

Thrown when BlocketAPI is used incorrectly:

```java
// API not initialized
BlocketAPI.getInstance(); // Throws IllegalStateException

// Already initialized
BlocketAPI.initialize(plugin); // Throws if already done
```

#### IllegalArgumentException

Thrown for invalid parameters:

```java
// Invalid chunks per tick
config.defaultChunksPerTick(0); // Must be positive

// Duplicate stage ID
stageManager.createStage(stage); // Throws if ID exists
```

### Best Practices for Error Handling

```java
public void safeAPIUsage() {
    try {
        // Check before using
        if (!BlocketAPI.isInitialized()) {
            getLogger().warning("BlocketAPI not initialized!");
            return;
        }
        
        BlocketAPI api = BlocketAPI.getInstance();
        // Use API safely...
        
    } catch (Exception e) {
        getLogger().log(Level.SEVERE, "BlocketAPI error", e);
    }
}
```

## Performance Considerations

### Memory Usage

- **Stages**: Each stage consumes memory for boundary data and views
- **Views**: Block positions are stored in memory
- **Patterns**: Reuse pattern instances to save memory

### Thread Safety

- **BlocketAPI**: Thread-safe singleton
- **StageManager**: Thread-safe operations
- **BlockChangeManager**: Thread-safe for concurrent access
- **Views**: Not thread-safe, modify on main thread only

### Optimization Tips

```java
// Reuse patterns
private static final Pattern COMMON_ORE = createCommonOrePattern();

// Batch operations
view.addBlocks(allPositions); // Better than individual addBlock() calls

// Clean up unused stages
stageManager.deleteStage(unusedStageId);
```

---

**This reference covers all major classes and methods in the Blocket API. Use it as your complete guide for development!**
