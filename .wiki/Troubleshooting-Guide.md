# Troubleshooting Guide

This page is 100% AI-generated and may contain inaccuracies. Please check for yourself.

Common issues and solutions when working with Blocket.

## Installation Issues

### PacketEvents Not Found

**Problem**: `ClassNotFoundException` or `NoClassDefFoundError` related to PacketEvents.

```
[ERROR] Could not load plugin 'MyPlugin' in folder 'plugins'
java.lang.NoClassDefFoundError: com/github/retrooper/packetevents/PacketEvents
```

**Solutions**:

1. **Download PacketEvents**: Ensure PacketEvents is installed on your server
   ```bash
   # Download from:
   # https://www.spigotmc.org/resources/packetevents.80279/
   ```

2. **Correct Version**: Make sure PacketEvents version matches your Minecraft version
   ```yaml
   # For MC 1.20.x
   PacketEvents: 2.0.0+
   
   # For MC 1.16-1.19
   PacketEvents: 1.8.0+
   ```

3. **Load Order**: Ensure PacketEvents loads before your plugin
   ```yaml
   # In your plugin.yml
   depend: [packetevents]
   # or
   softdepend: [packetevents]
   ```

### Maven/Gradle Dependencies

**Problem**: Compilation errors or missing classes during build.

**Maven Solution**:
```xml
<!-- Add TWME repository if not present -->
<repositories>
    <repository>
        <id>twme-repo</id>
        <url>https://repo.twme.dev/releases</url>
    </repository>
</repositories>

<!-- Check dependency version -->
<dependencies>
    <dependency>
        <groupId>dev.twme</groupId>
        <artifactId>blocket-api</artifactId>
        <version>1.0.0-SNAPSHOT</version> <!-- Use correct version -->
        <scope>provided</scope>
    </dependency>
</dependencies>
```

**Gradle Solution**:
```gradle
repositories {
    maven {
        name = 'twme-repo'
        url = 'https://repo.twme.dev/releases'
    }
}

dependencies {
    compileOnly 'dev.twme:blocket-api:1.0.0-SNAPSHOT'
}
```

### Java Version Compatibility

**Problem**: `UnsupportedClassVersionError` when loading the plugin.

**Solution**:
```yaml
# Check Java version requirements
# Blocket requires Java 17+
# Your plugin must target the same Java version

# In pom.xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>

# In build.gradle
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
```

## Runtime Issues

### BlocketAPI Not Initialized

**Problem**: `NullPointerException` when trying to use Blocket API.

```java
// This throws NPE
BlocketAPI api = BlocketAPI.getInstance(); // null
```

**Solutions**:

1. **Initialize BlocketAPI**:
   ```java
   @Override
   public void onEnable() {
       // Initialize Blocket
       BlocketConfig config = BlocketConfig.builder()
           .enableBreakListener(true)
           .enableInteractListener(true)
           .build();
       
       BlocketAPI.initialize(config);
       
       // Now safe to use
       BlocketAPI api = BlocketAPI.getInstance();
   }
   ```

2. **Check Initialization Timing**:
   ```java
   @Override
   public void onEnable() {
       // Wait for server to be fully loaded
       Bukkit.getScheduler().runTaskLater(this, () -> {
           BlocketAPI.initialize(BlocketConfig.builder().build());
           setupBlocketFeatures();
       }, 1L);
   }
   ```

3. **Defensive Programming**:
   ```java
   public void safeBlocketOperation() {
       BlocketAPI api = BlocketAPI.getInstance();
       if (api == null) {
           getLogger().severe("BlocketAPI not initialized!");
           return;
       }
       
       // Safe to proceed
       StageManager stageManager = api.getStageManager();
   }
   ```

### Blocks Not Visible to Players

**Problem**: Virtual blocks are created but players can't see them.

**Diagnostic Steps**:

```java
public void diagnoseBlockVisibility(Player player, Stage stage) {
    getLogger().info("=== Block Visibility Diagnosis ===");
    
    // Check 1: Is player in stage bounds?
    boolean inBounds = stage.getBounds().contains(player.getLocation());
    getLogger().info("Player in stage bounds: " + inBounds);
    
    // Check 2: Is player in audience?
    boolean inAudience = stage.getAudience().contains(player);
    getLogger().info("Player in audience: " + inAudience);
    
    // Check 3: Are there views in the stage?
    int viewCount = stage.getViews().size();
    getLogger().info("Views in stage: " + viewCount);
    
    // Check 4: Check view bounds
    stage.getViews().forEach(view -> {
        boolean playerInView = view.getBounds().contains(player.getLocation());
        getLogger().info("Player in view " + view.getId() + ": " + playerInView);
    });
    
    // Check 5: Force refresh
    if (inAudience && inBounds) {
        getLogger().info("Forcing block refresh...");
        stage.getViews().forEach(view -> view.sendBlocksToPlayer(player));
    }
}
```

**Common Solutions**:

1. **Add Player to Audience**:
   ```java
   // Player must be in audience to see blocks
   stage.getAudience().addPlayer(player);
   
   // Refresh blocks for the player
   stage.getViews().forEach(view -> view.sendBlocksToPlayer(player));
   ```

2. **Check Stage Bounds**:
   ```java
   // Verify stage encompasses the area
   BlocketPosition pos1 = new BlocketPosition(x1, y1, z1);
   BlocketPosition pos2 = new BlocketPosition(x2, y2, z2);
   
   // Make sure pos1 and pos2 create the correct bounds
   if (pos1.getX() > pos2.getX()) {
       // Swap positions if needed
       BlocketPosition temp = pos1;
       pos1 = pos2;
       pos2 = temp;
   }
   ```

3. **Verify View Configuration**:
   ```java
   // Ensure view has blocks
   View view = stage.getView("myview");
   if (view.isEmpty()) {
       getLogger().warning("View is empty - no blocks to display");
       
       // Add some blocks
       view.setBlock(new BlocketPosition(x, y, z), Material.STONE);
   }
   ```

### Performance Issues

**Problem**: Server lag or high memory usage with many virtual blocks.

**Memory Diagnosis**:

```java
public class BlocketMemoryProfiler {
    
    public void profileMemoryUsage() {
        BlocketAPI api = BlocketAPI.getInstance();
        StageManager stageManager = api.getStageManager();
        
        int totalStages = stageManager.getStages().size();
        int totalViews = 0;
        int totalBlocks = 0;
        int totalAudienceMembers = 0;
        
        for (Stage stage : stageManager.getStages()) {
            totalViews += stage.getViews().size();
            totalAudienceMembers += stage.getAudience().size();
            
            for (View view : stage.getViews()) {
                totalBlocks += view.getBlockCount();
            }
        }
        
        getLogger().info("=== Blocket Memory Profile ===");
        getLogger().info("Stages: " + totalStages);
        getLogger().info("Views: " + totalViews);
        getLogger().info("Virtual blocks: " + totalBlocks);
        getLogger().info("Audience members: " + totalAudienceMembers);
        getLogger().info("Estimated memory: " + estimateMemoryUsage(totalBlocks) + " MB");
        
        // Warning thresholds
        if (totalBlocks > 100000) {
            getLogger().warning("High block count detected - consider optimization");
        }
        
        if (totalAudienceMembers > 1000) {
            getLogger().warning("Large audience size - consider audience cleanup");
        }
    }
    
    private double estimateMemoryUsage(int blockCount) {
        // Rough estimate: each block ~= 50 bytes
        return (blockCount * 50) / (1024.0 * 1024.0);
    }
}
```

**Performance Solutions**:

1. **Batch Operations**:
   ```java
   // Instead of setting blocks one by one
   // BAD:
   for (int i = 0; i < 1000; i++) {
       view.setBlock(new BlocketPosition(x + i, y, z), Material.STONE);
       view.sendBlocksToAudience(); // Expensive!
   }
   
   // GOOD:
   Map<BlocketPosition, Material> blocks = new HashMap<>();
   for (int i = 0; i < 1000; i++) {
       blocks.put(new BlocketPosition(x + i, y, z), Material.STONE);
   }
   view.setBlocks(blocks);
   view.sendBlocksToAudience(); // Only once
   ```

2. **Limit Audience Size**:
   ```java
   public void cleanupOfflineAudiences() {
       BlocketAPI api = BlocketAPI.getInstance();
       
       api.getStageManager().getStages().forEach(stage -> {
           Audience audience = stage.getAudience();
           Set<Player> onlinePlayers = audience.getOnlinePlayers();
           
           // Create new audience with only online players
           Audience cleanedAudience = Audience.fromPlayers(onlinePlayers);
           
           // Replace old audience (if API supports this)
           // stage.setAudience(cleanedAudience);
       });
   }
   ```

3. **Async Processing**:
   ```java
   public void buildLargeStructureAsync(View view, Structure structure) {
       Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
           Map<BlocketPosition, Material> blocks = structure.generateBlocks();
           
           // Switch back to main thread for block setting
           Bukkit.getScheduler().runTask(plugin, () -> {
               view.setBlocks(blocks);
               view.sendBlocksToAudience();
           });
       });
   }
   ```

### Event Handling Issues

**Problem**: Blocket events not firing or being handled incorrectly.

**Check Event Registration**:

```java
@Override
public void onEnable() {
    // Ensure BlocketAPI is initialized first
    BlocketAPI.initialize(BlocketConfig.builder().build());
    
    // Register event listeners
    Bukkit.getPluginManager().registerEvents(new MyBlocketListener(), this);
}

public class MyBlocketListener implements Listener {
    
    @EventHandler
    public void onBlocketBreak(BlocketBreakEvent event) {
        // Make sure this method is called
        Bukkit.getLogger().info("BlocketBreakEvent triggered!");
        
        Player player = event.getPlayer();
        Stage stage = event.getStage();
        BlocketPosition position = event.getPosition();
        
        // Your logic here
    }
}
```

**Event Priority Issues**:

```java
// If other plugins are cancelling events
@EventHandler(priority = EventPriority.HIGH)
public void onBlocketBreak(BlocketBreakEvent event) {
    if (event.isCancelled()) {
        getLogger().info("Event was cancelled by another plugin");
        return;
    }
    
    // Handle event
}

// Or use MONITOR to always run regardless of cancellation
@EventHandler(priority = EventPriority.MONITOR)
public void monitorBlocketBreak(BlocketBreakEvent event) {
    // This always runs, even if event is cancelled
}
```

### Chunk Loading Issues

**Problem**: Blocks disappear when chunks unload or players move away.

**Solution**: Ensure proper chunk management:

```java
public class ChunkManager {
    
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        BlocketChunk blocketChunk = new BlocketChunk(chunk);
        
        // Find stages that overlap this chunk
        List<Stage> overlappingStages = findStagesInChunk(blocketChunk);
        
        // Refresh virtual blocks for this chunk
        overlappingStages.forEach(stage -> {
            stage.getViews().forEach(view -> {
                view.refreshChunk(blocketChunk);
            });
        });
    }
    
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        // Clean up any chunk-specific data
        Chunk chunk = event.getChunk();
        BlocketChunk blocketChunk = new BlocketChunk(chunk);
        
        // Save any necessary state before chunk unloads
        saveChunkState(blocketChunk);
    }
    
    private List<Stage> findStagesInChunk(BlocketChunk chunk) {
        return BlocketAPI.getInstance().getStageManager().getStages().stream()
            .filter(stage -> stage.getBounds().intersectsChunk(chunk))
            .collect(Collectors.toList());
    }
}
```

## Configuration Problems

### Invalid Configuration

**Problem**: BlocketConfig builder failing or producing unexpected results.

**Debug Configuration**:

```java
public void debugConfiguration() {
    BlocketConfig config = BlocketConfig.builder()
        .enableBreakListener(true)
        .enableInteractListener(true)
        .enablePlaceListener(false)
        .maxAudienceSize(100)
        .chunkLoadRadius(5)
        .build();
    
    getLogger().info("=== Blocket Configuration ===");
    getLogger().info("Break listener: " + config.isBreakListenerEnabled());
    getLogger().info("Interact listener: " + config.isInteractListenerEnabled());
    getLogger().info("Place listener: " + config.isPlaceListenerEnabled());
    getLogger().info("Max audience size: " + config.getMaxAudienceSize());
    getLogger().info("Chunk load radius: " + config.getChunkLoadRadius());
    
    // Test initialization
    try {
        BlocketAPI.initialize(config);
        getLogger().info("Configuration valid - API initialized successfully");
    } catch (Exception e) {
        getLogger().severe("Configuration invalid: " + e.getMessage());
    }
}
```

### Resource Limits

**Problem**: Running into memory or performance limits.

**Configuration Tuning**:

```java
// For high-performance servers
BlocketConfig highPerformanceConfig = BlocketConfig.builder()
    .maxAudienceSize(50)           // Limit audience size
    .chunkLoadRadius(3)            // Smaller chunk radius
    .enableAsyncProcessing(true)   // Use async where possible
    .cacheBlockUpdates(true)       // Cache frequent updates
    .build();

// For low-resource servers
BlocketConfig lightweightConfig = BlocketConfig.builder()
    .maxAudienceSize(20)           // Very small audiences
    .chunkLoadRadius(2)            // Minimal chunk loading
    .enableBreakListener(false)    // Disable unused features
    .enableInteractListener(false)
    .updateInterval(100)           // Less frequent updates
    .build();
```

## Integration Issues

### Plugin Conflicts

**Problem**: Other plugins interfering with Blocket functionality.

**Detect Conflicts**:

```java
public void detectPluginConflicts() {
    String[] potentialConflicts = {
        "WorldEdit", "WorldGuard", "Multiverse", "CoreProtect", 
        "LogBlock", "HawkEye", "Prism"
    };
    
    for (String pluginName : potentialConflicts) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (plugin != null && plugin.isEnabled()) {
            getLogger().warning("Potential conflict detected with: " + pluginName);
            getLogger().info("Version: " + plugin.getDescription().getVersion());
            
            // Provide specific advice for known conflicts
            provideConflictAdvice(pluginName);
        }
    }
}

private void provideConflictAdvice(String pluginName) {
    switch (pluginName) {
        case "WorldEdit":
            getLogger().info("Advice: WorldEdit may interfere with block updates");
            getLogger().info("Consider using Blocket's async processing");
            break;
            
        case "CoreProtect":
            getLogger().info("Advice: CoreProtect may log virtual block changes");
            getLogger().info("Configure CoreProtect to ignore virtual blocks");
            break;
            
        case "WorldGuard":
            getLogger().info("Advice: WorldGuard may block virtual block interactions");
            getLogger().info("Ensure appropriate region permissions");
            break;
    }
}
```

### Database Integration Issues

**Problem**: Issues when saving/loading Blocket data to/from databases.

**Connection Testing**:

```java
public class DatabaseTroubleshooter {
    
    public void testDatabaseConnection() {
        try {
            // Test basic connection
            Connection conn = dataSource.getConnection();
            if (conn != null && !conn.isClosed()) {
                getLogger().info("Database connection: OK");
                
                // Test table existence
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet tables = meta.getTables(null, null, "blocket_stages", null);
                if (tables.next()) {
                    getLogger().info("Blocket tables: OK");
                } else {
                    getLogger().warning("Blocket tables missing - creating...");
                    createTables(conn);
                }
                
                conn.close();
            } else {
                getLogger().severe("Database connection failed");
            }
        } catch (SQLException e) {
            getLogger().severe("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void testDataSerialization() {
        // Test saving a simple stage
        Stage testStage = new Stage(
            "test_stage",
            Bukkit.getWorlds().get(0),
            new BlocketPosition(0, 0, 0),
            new BlocketPosition(10, 10, 10),
            Audience.fromPlayers(new HashSet<>())
        );
        
        try {
            saveStage(testStage);
            Stage loadedStage = loadStage("test_stage");
            
            if (loadedStage != null && loadedStage.getId().equals("test_stage")) {
                getLogger().info("Data serialization: OK");
            } else {
                getLogger().severe("Data serialization failed");
            }
            
            // Cleanup
            deleteStage("test_stage");
            
        } catch (Exception e) {
            getLogger().severe("Serialization error: " + e.getMessage());
        }
    }
}
```

## Debugging Tools

### Debug Mode

```java
public class BlocketDebugger {
    private static boolean debugMode = false;
    
    public static void enableDebugMode() {
        debugMode = true;
        Bukkit.getLogger().info("Blocket debug mode enabled");
    }
    
    public static void debugLog(String message) {
        if (debugMode) {
            Bukkit.getLogger().info("[BLOCKET-DEBUG] " + message);
        }
    }
    
    public static void debugStage(Stage stage) {
        if (!debugMode) return;
        
        debugLog("=== Stage Debug: " + stage.getId() + " ===");
        debugLog("World: " + stage.getWorld().getName());
        debugLog("Bounds: " + stage.getBounds());
        debugLog("Views: " + stage.getViews().size());
        debugLog("Audience size: " + stage.getAudience().size());
        
        stage.getViews().forEach(view -> {
            debugLog("  View: " + view.getId());
            debugLog("  Blocks: " + view.getBlockCount());
        });
    }
}
```

### Performance Monitor

```java
public class BlocketPerformanceMonitor {
    private final Map<String, Long> timings = new HashMap<>();
    
    public void startTiming(String operation) {
        timings.put(operation, System.nanoTime());
    }
    
    public void endTiming(String operation) {
        Long start = timings.remove(operation);
        if (start != null) {
            long duration = System.nanoTime() - start;
            double milliseconds = duration / 1_000_000.0;
            
            if (milliseconds > 50) { // Warn if operation takes > 50ms
                Bukkit.getLogger().warning(String.format(
                    "Slow Blocket operation: %s took %.2fms", 
                    operation, milliseconds
                ));
            } else {
                Bukkit.getLogger().info(String.format(
                    "Blocket timing: %s = %.2fms", 
                    operation, milliseconds
                ));
            }
        }
    }
    
    public void profileBlockOperation(Runnable operation, String description) {
        startTiming(description);
        try {
            operation.run();
        } finally {
            endTiming(description);
        }
    }
}
```

### Health Check

```java
public class BlocketHealthCheck {
    
    public void performHealthCheck() {
        getLogger().info("=== Blocket Health Check ===");
        
        // Check 1: API Status
        BlocketAPI api = BlocketAPI.getInstance();
        boolean apiHealthy = api != null;
        getLogger().info("API Status: " + (apiHealthy ? "HEALTHY" : "UNHEALTHY"));
        
        if (!apiHealthy) {
            getLogger().severe("BlocketAPI is not initialized!");
            return;
        }
        
        // Check 2: Stage Manager
        StageManager stageManager = api.getStageManager();
        boolean stageManagerHealthy = stageManager != null;
        getLogger().info("Stage Manager: " + (stageManagerHealthy ? "HEALTHY" : "UNHEALTHY"));
        
        // Check 3: Memory Usage
        long freeMemory = Runtime.getRuntime().freeMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsagePercent = (usedMemory * 100.0) / totalMemory;
        
        getLogger().info(String.format("Memory Usage: %.1f%% (%d MB / %d MB)",
            memoryUsagePercent, usedMemory / 1024 / 1024, totalMemory / 1024 / 1024));
        
        if (memoryUsagePercent > 80) {
            getLogger().warning("High memory usage detected!");
        }
        
        // Check 4: Stage Health
        if (stageManagerHealthy) {
            Collection<Stage> stages = stageManager.getStages();
            getLogger().info("Total Stages: " + stages.size());
            
            int unhealthyStages = 0;
            for (Stage stage : stages) {
                if (!isStageHealthy(stage)) {
                    unhealthyStages++;
                    getLogger().warning("Unhealthy stage detected: " + stage.getId());
                }
            }
            
            if (unhealthyStages > 0) {
                getLogger().warning("Unhealthy stages: " + unhealthyStages);
            } else {
                getLogger().info("All stages healthy");
            }
        }
        
        getLogger().info("=== Health Check Complete ===");
    }
    
    private boolean isStageHealthy(Stage stage) {
        try {
            // Basic health checks
            return stage.getId() != null &&
                   stage.getWorld() != null &&
                   stage.getBounds() != null &&
                   stage.getAudience() != null &&
                   stage.getViews() != null;
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Error checking stage health: " + stage.getId(), e);
            return false;
        }
    }
}
```

## Getting Help

### Enable Debug Logging

```java
// Add this to your plugin
BlocketDebugger.enableDebugMode();

// Or check your server's logging configuration
# In bukkit.yml or paper.yml
settings:
  debug: true
  verbose: true
```

### Gather System Information

```java
public void generateSupportReport() {
    getLogger().info("=== Blocket Support Report ===");
    getLogger().info("Plugin Version: " + getDescription().getVersion());
    getLogger().info("Server Version: " + Bukkit.getVersion());
    getLogger().info("Bukkit Version: " + Bukkit.getBuktitVersion());
    getLogger().info("Java Version: " + System.getProperty("java.version"));
    getLogger().info("OS: " + System.getProperty("os.name"));
    
    BlocketAPI api = BlocketAPI.getInstance();
    if (api != null) {
        getLogger().info("BlocketAPI: Initialized");
        getLogger().info("Stages: " + api.getStageManager().getStages().size());
    } else {
        getLogger().info("BlocketAPI: Not initialized");
    }
    
    // List other plugins that might conflict
    Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
    getLogger().info("Installed Plugins: " + plugins.length);
    for (Plugin plugin : plugins) {
        if (plugin.isEnabled()) {
            getLogger().info("  - " + plugin.getName() + " v" + plugin.getDescription().getVersion());
        }
    }
    
    getLogger().info("=== End Support Report ===");
}
```

### Report Issues

When reporting issues, include:

1. **Complete error logs** (with stack traces)
2. **Plugin versions** (your plugin, Blocket, PacketEvents)
3. **Server information** (version, platform)
4. **Minimal reproduction case**
5. **Configuration used**

Create a [GitHub issue](https://github.com/your-org/blocket/issues) with this information for fastest support.

---

**Most Blocket issues can be resolved by ensuring proper initialization order, correct audience management, and adequate server resources.**
