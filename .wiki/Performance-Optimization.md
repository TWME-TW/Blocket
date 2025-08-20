# Performance Optimization
This page is 100% AI-generated and may contain inaccuracies. Please check for yourself.

Learn how to optimize Blocket for maximum performance and minimal server impact.

## Understanding Performance Impact

Blocket operates by sending virtual block changes to players through packets. The main performance considerations are:

1. **Packet Volume**: Number of block change packets sent
2. **Memory Usage**: Storage of block positions and data
3. **CPU Usage**: Pattern generation and block processing
4. **Network Bandwidth**: Data sent to clients

## Configuration Optimization

### Chunks Per Tick Settings

The most important performance setting is `chunksPerTick`:

```java
// Conservative (low-end servers)
BlocketConfig config = BlocketConfig.builder()
    .defaultChunksPerTick(1)
    .build();

// Balanced (most servers)
BlocketConfig config = BlocketConfig.builder()
    .defaultChunksPerTick(2)
    .build();

// Aggressive (high-end servers)
BlocketConfig config = BlocketConfig.builder()
    .defaultChunksPerTick(4)
    .build();
```

### Per-Stage Optimization

```java
// For large stages
largeStage.setChunksPerTick(1); // Process slowly

// For small stages
smallStage.setChunksPerTick(3); // Process quickly

// For inactive stages
inactiveStage.setChunksPerTick(0); // Pause processing
```

### Listener Configuration

```java
// Disable unused features
BlocketConfig config = BlocketConfig.builder()
    .enableStageBoundListener(false) // If not using location-based stages
    .enablePacketListeners(true)     // Keep this enabled for functionality
    .build();
```

## Memory Optimization

### Efficient Stage Management

#### Stage Lifecycle Management

```java
public class OptimizedStageManager {
    private final Map<String, Long> stageLastAccess = new ConcurrentHashMap<>();
    
    public void trackStageAccess(String stageId) {
        stageLastAccess.put(stageId, System.currentTimeMillis());
    }
    
    public void cleanupInactiveStages(long maxInactiveTime) {
        long currentTime = System.currentTimeMillis();
        
        stageLastAccess.entrySet().removeIf(entry -> {
            String stageId = entry.getKey();
            long lastAccess = entry.getValue();
            
            if (currentTime - lastAccess > maxInactiveTime) {
                stageManager.deleteStage(stageId);
                getLogger().info("Cleaned up inactive stage: " + stageId);
                return true;
            }
            return false;
        });
    }
}
```

#### Memory-Efficient Position Storage

```java
// Good: Use sets for position storage
Set<BlocketPosition> positions = new HashSet<>();

// Better: Use memory-efficient collections for large datasets
Set<BlocketPosition> positions = new THashSet<>(); // Trove collections

// Best: Stream processing for very large areas
public void processLargeArea(BlocketPosition pos1, BlocketPosition pos2, Consumer<BlocketPosition> processor) {
    for (int x = Math.min(pos1.getX(), pos2.getX()); x <= Math.max(pos1.getX(), pos2.getX()); x++) {
        for (int y = Math.min(pos1.getY(), pos2.getY()); y <= Math.max(pos1.getY(), pos2.getY()); y++) {
            for (int z = Math.min(pos1.getZ(), pos2.getZ()); z <= Math.max(pos1.getZ(), pos2.getZ()); z++) {
                processor.accept(new BlocketPosition(x, y, z));
            }
        }
    }
}
```

### Pattern Optimization

#### Pattern Caching

```java
public class PatternCache {
    private static final Map<String, Pattern> CACHE = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 100;
    
    public static Pattern getCachedPattern(String key, Supplier<Pattern> creator) {
        return CACHE.computeIfAbsent(key, k -> {
            if (CACHE.size() >= MAX_CACHE_SIZE) {
                // Simple LRU: remove oldest entries
                String oldestKey = CACHE.keySet().iterator().next();
                CACHE.remove(oldestKey);
            }
            return creator.get();
        });
    }
    
    public static void clearCache() {
        CACHE.clear();
    }
}

// Usage
Pattern levelPattern = PatternCache.getCachedPattern(
    "level_" + playerLevel + "_biome_" + biome.name(),
    () -> createLevelBiomePattern(playerLevel, biome)
);
```

#### Lightweight Patterns

```java
// Heavy: Creating new BlockData objects every time
public Pattern createHeavyPattern() {
    Map<BlockData, Double> blocks = new HashMap<>();
    blocks.put(Material.STONE.createBlockData(), 70.0); // New object every call
    blocks.put(Material.COAL_ORE.createBlockData(), 30.0);
    return new Pattern(blocks);
}

// Lightweight: Reuse BlockData objects
public class OptimizedPatterns {
    private static final BlockData STONE = Material.STONE.createBlockData();
    private static final BlockData COAL_ORE = Material.COAL_ORE.createBlockData();
    
    public static final Pattern BASIC_MINE = createBasicMinePattern();
    
    private static Pattern createBasicMinePattern() {
        Map<BlockData, Double> blocks = new HashMap<>();
        blocks.put(STONE, 70.0);
        blocks.put(COAL_ORE, 30.0);
        return new Pattern(blocks);
    }
}
```

## Batch Operations

### Efficient Block Addition

```java
// Inefficient: Individual operations
for (BlocketPosition pos : positions) {
    view.addBlock(pos); // Triggers individual updates
}

// Efficient: Batch operations
view.addBlocks(positions); // Single batch update
```

### Chunked Processing

```java
public class ChunkedProcessor {
    private static final int CHUNK_SIZE = 1000;
    
    public void processLargeBlockSet(Set<BlocketPosition> allPositions, View view) {
        List<BlocketPosition> positionList = new ArrayList<>(allPositions);
        
        // Process in chunks
        for (int i = 0; i < positionList.size(); i += CHUNK_SIZE) {
            int end = Math.min(i + CHUNK_SIZE, positionList.size());
            Set<BlocketPosition> chunk = new HashSet<>(positionList.subList(i, end));
            
            view.addBlocks(chunk);
            
            // Small delay to prevent server overload
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
```

### Asynchronous Processing

```java
public class AsyncBlockProcessor {
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    
    public CompletableFuture<Void> processAsync(Set<BlocketPosition> positions, View view) {
        return CompletableFuture.runAsync(() -> {
            // Process blocks on async thread
            Set<BlocketPosition> processedPositions = positions.stream()
                .filter(this::isValidPosition)
                .collect(Collectors.toSet());
            
            // Switch back to main thread for Blocket operations
            Bukkit.getScheduler().runTask(plugin, () -> {
                view.addBlocks(processedPositions);
            });
        }, executor);
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}
```

## Network Optimization

### Minimize Packet Volume

```java
// Inefficient: Frequent individual updates
public void inefficientUpdates(Player player) {
    for (int i = 0; i < 100; i++) {
        BlocketPosition pos = new BlocketPosition(100 + i, 65, 100);
        BlockData block = Material.STONE.createBlockData();
        blockChangeManager.sendBlockChange(player, pos, block); // 100 packets
    }
}

// Efficient: Batch updates
public void efficientUpdates(Player player) {
    Map<BlocketPosition, BlockData> changes = new HashMap<>();
    for (int i = 0; i < 100; i++) {
        BlocketPosition pos = new BlocketPosition(100 + i, 65, 100);
        BlockData block = Material.STONE.createBlockData();
        changes.put(pos, block);
    }
    blockChangeManager.sendBlockChanges(player, changes); // 1 packet
}
```

### Smart Update Frequency

```java
public class ThrottledUpdates {
    private final Map<Player, Long> lastUpdate = new ConcurrentHashMap<>();
    private static final long MIN_UPDATE_INTERVAL = 50; // 50ms
    
    public void throttledUpdate(Player player, Runnable updateTask) {
        long currentTime = System.currentTimeMillis();
        long lastUpdateTime = lastUpdate.getOrDefault(player, 0L);
        
        if (currentTime - lastUpdateTime >= MIN_UPDATE_INTERVAL) {
            updateTask.run();
            lastUpdate.put(player, currentTime);
        }
    }
}
```

## CPU Optimization

### Efficient Pattern Generation

```java
// Inefficient: Complex calculations every time
public Pattern createDynamicPattern(Player player, BlocketPosition position) {
    Map<BlockData, Double> blocks = new HashMap<>();
    
    // Expensive calculations
    double playerScore = calculateComplexPlayerScore(player);
    double positionModifier = calculatePositionModifier(position);
    double timeModifier = calculateTimeModifier();
    
    // Generate pattern...
    return new Pattern(blocks);
}

// Efficient: Cache and reuse calculations
public class EfficientPatternGenerator {
    private final Map<UUID, Double> playerScoreCache = new ConcurrentHashMap<>();
    private final Map<BlocketPosition, Double> positionCache = new ConcurrentHashMap<>();
    private double lastTimeModifier = 0;
    private long lastTimeCalculation = 0;
    
    public Pattern createOptimizedPattern(Player player, BlocketPosition position) {
        // Use cached values
        double playerScore = playerScoreCache.computeIfAbsent(
            player.getUniqueId(), 
            k -> calculateComplexPlayerScore(player)
        );
        
        double positionModifier = positionCache.computeIfAbsent(
            position,
            this::calculatePositionModifier
        );
        
        // Only recalculate time modifier every minute
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTimeCalculation > 60000) {
            lastTimeModifier = calculateTimeModifier();
            lastTimeCalculation = currentTime;
        }
        
        return generatePatternFromValues(playerScore, positionModifier, lastTimeModifier);
    }
}
```

### Lazy Loading

```java
public class LazyStage {
    private final String stageId;
    private Stage stage;
    private boolean initialized = false;
    
    public Stage getStage() {
        if (!initialized) {
            initializeStage();
            initialized = true;
        }
        return stage;
    }
    
    private void initializeStage() {
        // Only create stage when actually needed
        this.stage = createActualStage();
    }
}
```

## Monitoring and Profiling

### Performance Monitoring

```java
public class PerformanceMonitor {
    private final Map<String, Long> operationTimes = new ConcurrentHashMap<>();
    private final Map<String, Integer> operationCounts = new ConcurrentHashMap<>();
    
    public void trackOperation(String operation, Runnable task) {
        long startTime = System.nanoTime();
        
        try {
            task.run();
        } finally {
            long duration = System.nanoTime() - startTime;
            
            operationTimes.merge(operation, duration, Long::sum);
            operationCounts.merge(operation, 1, Integer::sum);
        }
    }
    
    public void printStatistics() {
        operationTimes.forEach((operation, totalTime) -> {
            int count = operationCounts.get(operation);
            double avgTimeMs = (totalTime / count) / 1_000_000.0;
            
            plugin.getLogger().info(String.format(
                "%s: %d calls, %.2fms avg",
                operation, count, avgTimeMs
            ));
        });
    }
}

// Usage
performanceMonitor.trackOperation("stage_creation", () -> {
    stageManager.createStage(stage);
});
```

### Memory Usage Tracking

```java
public class MemoryTracker {
    public void logMemoryUsage(String operation) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        plugin.getLogger().info(String.format(
            "%s - Memory: %dMB used, %dMB free, %dMB total",
            operation,
            usedMemory / 1024 / 1024,
            freeMemory / 1024 / 1024,
            totalMemory / 1024 / 1024
        ));
    }
}
```

## Best Practices Summary

### Configuration Guidelines

| Server Type | Chunks/Tick | Max Stages | Update Rate |
|-------------|-------------|------------|-------------|
| Shared Hosting | 1 | 10-20 | 1000ms |
| VPS | 2 | 50-100 | 500ms |
| Dedicated | 3-4 | 200+ | 100ms |

### Code Patterns

#### ✅ Do This

```java
// Use batch operations
view.addBlocks(allPositions);

// Cache patterns
Pattern pattern = PatternCache.get("common_ore");

// Clean up resources
@Override
public void onDisable() {
    if (blocketAPI != null) {
        blocketAPI.shutdown();
    }
}

// Monitor performance
if (stages.size() > MAX_STAGES) {
    cleanupOldStages();
}
```

#### ❌ Avoid This

```java
// Individual operations in loops
for (BlocketPosition pos : positions) {
    view.addBlock(pos); // Inefficient
}

// Creating patterns repeatedly
Pattern pattern = new Pattern(sameBlocks); // Wasteful

// Memory leaks
// Forgetting to call blocketAPI.shutdown()

// Unlimited growth
// Not cleaning up old stages
```

### Resource Management

1. **Stage Cleanup**: Remove unused stages regularly
2. **Pattern Reuse**: Cache and reuse patterns
3. **Batch Operations**: Use bulk methods whenever possible
4. **Async Processing**: Move heavy calculations off the main thread
5. **Memory Monitoring**: Track memory usage and clean up proactively

### Troubleshooting Performance Issues

#### High Memory Usage

```java
// Check for stage leaks
Set<Stage> stages = stageManager.getAllStages();
if (stages.size() > 100) {
    getLogger().warning("High stage count: " + stages.size());
}

// Monitor pattern cache
PatternCache.printCacheStats();
```

#### High CPU Usage

```java
// Reduce chunks per tick
stage.setChunksPerTick(1);

// Check for expensive pattern calculations
PerformanceMonitor.trackOperation("pattern_generation", () -> {
    Pattern pattern = createComplexPattern();
});
```

#### Network Issues

```java
// Reduce update frequency
public boolean shouldUpdate(Player player) {
    long lastUpdate = getLastUpdate(player);
    return System.currentTimeMillis() - lastUpdate > MIN_UPDATE_INTERVAL;
}
```

## Advanced Optimization Techniques

### Custom BlockData Pool

```java
public class BlockDataPool {
    private final Map<Material, BlockData> pool = new ConcurrentHashMap<>();
    
    public BlockData get(Material material) {
        return pool.computeIfAbsent(material, Material::createBlockData);
    }
    
    public BlockData get(Material material, Consumer<BlockData> modifier) {
        BlockData base = get(material);
        BlockData copy = base.clone();
        modifier.accept(copy);
        return copy;
    }
}
```

### Spatial Partitioning

```java
public class SpatialIndex {
    private final Map<ChunkCoord, Set<Stage>> chunkToStages = new ConcurrentHashMap<>();
    
    public Set<Stage> getStagesInChunk(int chunkX, int chunkZ) {
        return chunkToStages.getOrDefault(new ChunkCoord(chunkX, chunkZ), Collections.emptySet());
    }
    
    public void addStage(Stage stage) {
        Set<ChunkCoord> chunks = getChunksForStage(stage);
        chunks.forEach(chunk -> {
            chunkToStages.computeIfAbsent(chunk, k -> new ConcurrentHashMap<>()).add(stage);
        });
    }
}
```

---

**Following these optimization guidelines will ensure Blocket runs efficiently even with large numbers of stages and players!**
