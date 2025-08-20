# Event System
This page is 100% AI-generated and may contain inaccuracies. Please check for yourself.

Learn about Blocket's comprehensive event system for handling block interactions and system events.

## Overview

Blocket provides a rich event system that allows you to respond to various interactions with virtual blocks. These events integrate seamlessly with Bukkit's event system while providing Blocket-specific context.

## Event Types

### Block Interaction Events

#### BlocketBreakEvent

Fired when a player breaks a virtual block.

```java
package dev.twme.blocket.events;

public class BlocketBreakEvent extends Event implements Cancellable {
    private final Player player;
    private final BlocketPosition position;
    private final BlockData blockData;
    private final Stage stage;
    private final View view;
    private boolean cancelled = false;
    
    // Event methods...
}
```

**Event Details:**
- **When**: Player completes breaking a virtual block
- **Cancellable**: Yes
- **Async**: No (main thread only)

**Example Usage:**
```java
@EventHandler
public void onBlocketBreak(BlocketBreakEvent event) {
    Player player = event.getPlayer();
    Material blockType = event.getBlockData().getMaterial();
    
    // Give rewards based on block type
    switch (blockType) {
        case COAL_ORE -> giveReward(player, new ItemStack(Material.COAL, 2));
        case IRON_ORE -> giveReward(player, new ItemStack(Material.RAW_IRON, 1));
        case DIAMOND_ORE -> {
            giveReward(player, new ItemStack(Material.DIAMOND, 1));
            player.sendMessage("§b✦ You found a diamond!");
        }
    }
    
    // Regenerate block after 30 seconds
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
        event.getView().resetBlock(event.getPosition());
    }, 20 * 30);
}
```

#### BlocketInteractEvent

Fired when a player interacts with a virtual block (right-click).

```java
public class BlocketInteractEvent extends Event implements Cancellable {
    private final Player player;
    private final BlocketPosition position;
    private final BlockData blockData;
    private final Stage stage;
    private final View view;
    private final Action action; // RIGHT_CLICK_BLOCK, LEFT_CLICK_BLOCK
    private boolean cancelled = false;
}
```

**Example Usage:**
```java
@EventHandler
public void onBlocketInteract(BlocketInteractEvent event) {
    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
        Material blockType = event.getBlockData().getMaterial();
        Player player = event.getPlayer();
        
        if (blockType == Material.CHEST) {
            // Open custom GUI for virtual chest
            openVirtualChest(player, event.getPosition());
            event.setCancelled(true);
        }
    }
}
```

#### BlocketPlaceEvent

Fired when attempting to place a block in a virtual area.

```java
public class BlocketPlaceEvent extends Event implements Cancellable {
    private final Player player;
    private final BlocketPosition position;
    private final BlockData placedBlock;
    private final Stage stage;
    private boolean cancelled = false;
}
```

**Example Usage:**
```java
@EventHandler
public void onBlocketPlace(BlocketPlaceEvent event) {
    Player player = event.getPlayer();
    Stage stage = event.getStage();
    
    // Only allow placing in player's own mine
    if (stage.getId().equals("mine_" + player.getUniqueId())) {
        // Allow placement
        return;
    }
    
    // Deny placement in other stages
    event.setCancelled(true);
    player.sendMessage("§cYou cannot build in this area!");
}
```

### Stage Management Events

#### CreateStageEvent

Fired when a new stage is created.

```java
public class CreateStageEvent extends Event {
    private final Stage stage;
    private final Plugin plugin; // Plugin that created the stage
}
```

**Example Usage:**
```java
@EventHandler
public void onStageCreate(CreateStageEvent event) {
    Stage stage = event.getStage();
    Plugin creator = event.getPlugin();
    
    getLogger().info(String.format(
        "Stage '%s' created by plugin '%s'",
        stage.getId(),
        creator.getName()
    ));
    
    // Track stage creation for analytics
    analyticsTracker.recordStageCreation(stage, creator);
}
```

#### DeleteStageEvent

Fired when a stage is deleted.

```java
public class DeleteStageEvent extends Event {
    private final String stageId;
    private final Stage stage; // May be null if stage was already removed
}
```

**Example Usage:**
```java
@EventHandler
public void onStageDelete(DeleteStageEvent event) {
    String stageId = event.getStageId();
    
    // Clean up any associated data
    cleanupStageData(stageId);
    
    // Notify players if they were in this stage
    if (stageId.startsWith("mine_")) {
        UUID playerId = extractPlayerIdFromStageId(stageId);
        Player player = Bukkit.getPlayer(playerId);
        
        if (player != null && player.isOnline()) {
            player.sendMessage("§cYour private mine has been removed.");
        }
    }
}
```

### Player Movement Events

#### PlayerEnterStageEvent

Fired when a player enters a stage's boundaries.

```java
public class PlayerEnterStageEvent extends Event {
    private final Player player;
    private final Stage stage;
    private final BlocketPosition fromPosition;
    private final BlocketPosition toPosition;
}
```

**Example Usage:**
```java
@EventHandler
public void onPlayerEnterStage(PlayerEnterStageEvent event) {
    Player player = event.getPlayer();
    Stage stage = event.getStage();
    
    // Send welcome message
    if (stage.getId().startsWith("mine_")) {
        player.sendMessage("§aEntered private mine area");
        player.sendTitle("§6Private Mine", "§7Happy mining!", 10, 40, 10);
    }
    
    // Apply stage-specific effects
    applyStageEffects(player, stage);
}

private void applyStageEffects(Player player, Stage stage) {
    if (stage.getId().contains("speed_mine")) {
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 1, false, false
        ));
    }
}
```

#### PlayerExitStageEvent

Fired when a player exits a stage's boundaries.

```java
public class PlayerExitStageEvent extends Event {
    private final Player player;
    private final Stage stage;
    private final BlocketPosition fromPosition;
    private final BlocketPosition toPosition;
}
```

**Example Usage:**
```java
@EventHandler
public void onPlayerExitStage(PlayerExitStageEvent event) {
    Player player = event.getPlayer();
    Stage stage = event.getStage();
    
    // Remove stage-specific effects
    removeStageEffects(player, stage);
    
    // Save player progress
    savePlayerProgress(player, stage);
    
    player.sendMessage("§7Left private mine area");
}
```

### Advanced Events

#### OnBlockChangeSendEvent

Fired when block changes are sent to a player (low-level event).

```java
public class OnBlockChangeSendEvent extends Event implements Cancellable {
    private final Player player;
    private final Map<BlocketPosition, BlockData> blockChanges;
    private boolean cancelled = false;
}
```

**Example Usage:**
```java
@EventHandler
public void onBlockChangeSend(OnBlockChangeSendEvent event) {
    Player player = event.getPlayer();
    
    // Limit block updates for players with poor connection
    if (isLowBandwidth(player) && event.getBlockChanges().size() > 100) {
        // Split into smaller chunks
        splitAndSendBlocks(player, event.getBlockChanges());
        event.setCancelled(true);
    }
}
```

## Event Registration

### Basic Event Listener

```java
public class BlocketEventListener implements Listener {
    private final MyPlugin plugin;
    
    public BlocketEventListener(MyPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlocketBreak(BlocketBreakEvent event) {
        // Handle event
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlocketInteract(BlocketInteractEvent event) {
        // Handle event with high priority, ignore if cancelled
    }
}

// Register the listener
Bukkit.getPluginManager().registerEvents(new BlocketEventListener(this), this);
```

### Multiple Listeners

```java
public class MineEventHandler implements Listener {
    
    @EventHandler
    public void onMineBreak(BlocketBreakEvent event) {
        if (isMineStage(event.getStage())) {
            handleMineBreak(event);
        }
    }
    
    @EventHandler
    public void onMineEnter(PlayerEnterStageEvent event) {
        if (isMineStage(event.getStage())) {
            handleMineEntry(event);
        }
    }
    
    private boolean isMineStage(Stage stage) {
        return stage.getId().startsWith("mine_");
    }
}

public class FarmEventHandler implements Listener {
    
    @EventHandler
    public void onCropInteract(BlocketInteractEvent event) {
        if (isFarmStage(event.getStage())) {
            handleCropInteraction(event);
        }
    }
}
```

## Event Priority and Cancellation

### Understanding Event Priority

```java
// LOWEST - Runs first, can set up context
@EventHandler(priority = EventPriority.LOWEST)
public void setupContext(BlocketBreakEvent event) {
    // Set up context for other handlers
}

// LOW - Early processing
@EventHandler(priority = EventPriority.LOW)
public void earlyProcessing(BlocketBreakEvent event) {
    // Early game logic
}

// NORMAL - Default priority (most handlers use this)
@EventHandler(priority = EventPriority.NORMAL)
public void normalProcessing(BlocketBreakEvent event) {
    // Standard game logic
}

// HIGH - Override or modify behavior
@EventHandler(priority = EventPriority.HIGH)
public void modifyBehavior(BlocketBreakEvent event) {
    // Modify or override normal behavior
}

// HIGHEST - Final processing
@EventHandler(priority = EventPriority.HIGHEST)
public void finalProcessing(BlocketBreakEvent event) {
    // Final processing, cleanup
}

// MONITOR - Observe only (don't modify)
@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
public void monitorEvent(BlocketBreakEvent event) {
    // Log or track the event (don't modify!)
}
```

### Event Cancellation Best Practices

```java
@EventHandler
public void validateBreak(BlocketBreakEvent event) {
    Player player = event.getPlayer();
    
    // Check permissions
    if (!player.hasPermission("mine.break")) {
        event.setCancelled(true);
        player.sendMessage("§cYou don't have permission to break blocks here!");
        return;
    }
    
    // Check cooldown
    if (isOnCooldown(player)) {
        event.setCancelled(true);
        player.sendMessage("§cPlease wait before breaking another block!");
        return;
    }
    
    // Check tool requirements
    ItemStack tool = player.getInventory().getItemInMainHand();
    if (!isValidTool(tool, event.getBlockData().getMaterial())) {
        event.setCancelled(true);
        player.sendMessage("§cYou need the correct tool to break this block!");
        return;
    }
}
```

## Advanced Event Patterns

### Event Chaining

```java
public class EventChainHandler implements Listener {
    
    @EventHandler(priority = EventPriority.LOW)
    public void startChain(BlocketBreakEvent event) {
        // Start a chain of events
        ChainContext context = new ChainContext(event);
        event.setMetadata("chain", context);
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void processChain(BlocketBreakEvent event) {
        ChainContext context = (ChainContext) event.getMetadata("chain");
        if (context != null) {
            context.addStep("process", this);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void finalizeChain(BlocketBreakEvent event) {
        ChainContext context = (ChainContext) event.getMetadata("chain");
        if (context != null) {
            context.finalize();
        }
    }
}
```

### Conditional Event Handling

```java
public class ConditionalHandler implements Listener {
    
    @EventHandler
    public void onBreak(BlocketBreakEvent event) {
        Player player = event.getPlayer();
        Stage stage = event.getStage();
        Material blockType = event.getBlockData().getMaterial();
        
        // Handle based on multiple conditions
        if (isVIPPlayer(player) && isRareOre(blockType)) {
            handleVIPRareBreak(event);
        } else if (isNewPlayer(player)) {
            handleNewPlayerBreak(event);
        } else if (isEventActive() && isEventStage(stage)) {
            handleEventBreak(event);
        } else {
            handleNormalBreak(event);
        }
    }
}
```

### Async Event Processing

```java
public class AsyncEventHandler implements Listener {
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    
    @EventHandler
    public void onBreak(BlocketBreakEvent event) {
        // Quick validation on main thread
        if (!basicValidation(event)) {
            event.setCancelled(true);
            return;
        }
        
        // Heavy processing on async thread
        CompletableFuture.runAsync(() -> {
            processRewards(event.getPlayer(), event.getBlockData());
            updateStatistics(event.getPlayer(), event.getStage());
            
        }, executor).exceptionally(throwable -> {
            plugin.getLogger().log(Level.WARNING, "Async processing failed", throwable);
            return null;
        });
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}
```

## Custom Events

### Creating Custom Events

```java
public class CustomMineEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    
    private final Player player;
    private final String mineId;
    private final MineType mineType;
    private final Map<String, Object> properties;
    
    public CustomMineEvent(Player player, String mineId, MineType mineType) {
        this.player = player;
        this.mineId = mineId;
        this.mineType = mineType;
        this.properties = new HashMap<>();
    }
    
    // Getters and setters
    public Player getPlayer() { return player; }
    public String getMineId() { return mineId; }
    public MineType getMineType() { return mineType; }
    
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
    
    public <T> T getProperty(String key, Class<T> type) {
        Object value = properties.get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }
    
    // Required methods
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
    
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
```

### Firing Custom Events

```java
public class MineManager {
    
    public void createMine(Player player, MineType type) {
        // Create the mine...
        String mineId = generateMineId(player);
        
        // Fire custom event
        CustomMineEvent event = new CustomMineEvent(player, mineId, type);
        event.setProperty("creation_time", System.currentTimeMillis());
        event.setProperty("location", player.getLocation());
        
        Bukkit.getPluginManager().callEvent(event);
        
        // Continue with mine creation...
    }
}
```

### Handling Custom Events

```java
@EventHandler
public void onCustomMine(CustomMineEvent event) {
    Player player = event.getPlayer();
    MineType type = event.getMineType();
    
    // Send congratulations
    player.sendMessage("§aCongratulations on your new " + type.getDisplayName() + " mine!");
    
    // Track for achievements
    achievementTracker.recordMineCreation(player, type);
    
    // Award creation bonus
    if (type == MineType.DIAMOND_MINE) {
        giveReward(player, new ItemStack(Material.DIAMOND, 5));
    }
}
```

## Event Debugging and Monitoring

### Event Debug Logger

```java
public class EventDebugger implements Listener {
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void debugBlocketBreak(BlocketBreakEvent event) {
        if (plugin.getConfig().getBoolean("debug.events", false)) {
            getLogger().info(String.format(
                "BlocketBreak: player=%s, block=%s, stage=%s, cancelled=%s",
                event.getPlayer().getName(),
                event.getBlockData().getMaterial(),
                event.getStage().getId(),
                event.isCancelled()
            ));
        }
    }
}
```

### Event Performance Monitor

```java
public class EventPerformanceMonitor implements Listener {
    private final Map<String, LongAdder> eventCounts = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> eventTimes = new ConcurrentHashMap<>();
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void monitorBreakEvent(BlocketBreakEvent event) {
        long startTime = System.nanoTime();
        
        // Count the event
        eventCounts.computeIfAbsent("BlocketBreak", k -> new LongAdder()).increment();
        
        // This would typically be done in a wrapper, but shown for example
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            long duration = System.nanoTime() - startTime;
            eventTimes.computeIfAbsent("BlocketBreak", k -> new LongAdder()).add(duration);
        }, 1);
    }
    
    public void printStatistics() {
        eventCounts.forEach((event, count) -> {
            long totalTime = eventTimes.getOrDefault(event, new LongAdder()).sum();
            double avgTimeMs = (totalTime / count.sum()) / 1_000_000.0;
            
            getLogger().info(String.format(
                "%s: %d events, %.2fms avg processing time",
                event, count.sum(), avgTimeMs
            ));
        });
    }
}
```

## Integration Examples

### Economy Integration

```java
@EventHandler
public void onBreakWithEconomy(BlocketBreakEvent event) {
    Player player = event.getPlayer();
    Material blockType = event.getBlockData().getMaterial();
    
    double reward = getBlockValue(blockType);
    if (reward > 0) {
        economy.depositPlayer(player, reward);
        player.sendMessage(String.format("§a+$%.2f", reward));
    }
}
```

### Achievement System

```java
@EventHandler
public void onBreakAchievements(BlocketBreakEvent event) {
    Player player = event.getPlayer();
    Material blockType = event.getBlockData().getMaterial();
    
    // Increment block break counter
    int totalBroken = incrementBlockCounter(player, blockType);
    
    // Check for achievements
    if (blockType == Material.DIAMOND_ORE && totalBroken == 100) {
        unlockAchievement(player, "diamond_master");
        player.sendMessage("§6Achievement Unlocked: Diamond Master!");
    }
}
```

### Statistics Tracking

```java
@EventHandler
public void onBreakStatistics(BlocketBreakEvent event) {
    Player player = event.getPlayer();
    
    CompletableFuture.runAsync(() -> {
        // Record statistics asynchronously
        statsDatabase.recordBlockBreak(
            player.getUniqueId(),
            event.getBlockData().getMaterial(),
            event.getStage().getId(),
            System.currentTimeMillis()
        );
    });
}
```

## Best Practices

### Event Handler Organization

```java
// Good: Specific, focused handlers
public class MineEventHandler implements Listener {
    // Only handle mine-related events
}

public class EconomyIntegrationHandler implements Listener {
    // Only handle economy-related aspects
}

// Avoid: Giant event handlers that do everything
public class MegaEventHandler implements Listener {
    // Handles mines, farms, economy, achievements, etc. - too much!
}
```

### Performance Considerations

```java
@EventHandler
public void efficientHandler(BlocketBreakEvent event) {
    // Fast checks first
    if (!event.getStage().getId().startsWith("mine_")) {
        return; // Exit early for non-mine stages
    }
    
    Player player = event.getPlayer();
    
    // Check permissions (fast)
    if (!player.hasPermission("mine.rewards")) {
        return;
    }
    
    // Expensive operations last
    processRewards(player, event.getBlockData());
}
```

### Error Handling in Events

```java
@EventHandler
public void safeHandler(BlocketBreakEvent event) {
    try {
        // Your event logic here
        processEvent(event);
        
    } catch (Exception e) {
        // Log error but don't let it break other handlers
        plugin.getLogger().log(Level.WARNING, "Error handling BlocketBreakEvent", e);
        
        // Optionally cancel event if critical failure
        if (isCriticalError(e)) {
            event.setCancelled(true);
        }
    }
}
```

---

**The event system is powerful and flexible. Use it to create rich, interactive experiences with your virtual blocks!**
