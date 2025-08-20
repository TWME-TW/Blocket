# Configuration
This page is 100% AI-generated and may contain inaccuracies. Please check for yourself.

Learn how to configure Blocket API for optimal performance and functionality in your plugin.

## BlocketConfig Overview

`BlocketConfig` provides fine-grained control over Blocket's behavior. You can customize initialization, performance settings, and feature toggles.

## Configuration Builder

### Basic Usage

```java
import dev.twme.blocket.api.BlocketConfig;

// Default configuration
BlocketConfig config = BlocketConfig.defaultConfig();

// Custom configuration using builder pattern
BlocketConfig config = BlocketConfig.builder()
    .autoInitialize(true)
    .enableStageBoundListener(true)
    .enablePacketListeners(true)
    .defaultChunksPerTick(2)
    .build();
```

### Configuration Options

#### Auto Initialize
```java
.autoInitialize(boolean enable)
```

**Default**: `true`

Controls whether listeners are automatically registered during API initialization.

- `true`: Listeners are registered immediately when `BlocketAPI.initialize()` is called
- `false`: You must manually call `blocketAPI.initializeListeners()` later

**Use case**: Set to `false` if you need to delay listener registration or want more control over the initialization process.

```java
// Manual initialization example
BlocketConfig config = BlocketConfig.builder()
    .autoInitialize(false)
    .build();

BlocketAPI api = BlocketAPI.initialize(this, config);

// Later, when ready...
api.initializeListeners();
```

#### Stage Bound Listener
```java
.enableStageBoundListener(boolean enable)
```

**Default**: `true`

Enables/disables the listener that handles player movement between stages.

- `true`: Players automatically enter/exit stages based on their location
- `false`: Manual stage management required

**Performance impact**: Monitors player movement events. Disable if you don't use location-based stage boundaries.

```java
// For manual stage management
BlocketConfig config = BlocketConfig.builder()
    .enableStageBoundListener(false)
    .build();

// You'll need to manually manage stage entry/exit
stageManager.addPlayerToStage(player, stage);
stageManager.removePlayerFromStage(player, stage);
```

#### Packet Listeners
```java
.enablePacketListeners(boolean enable)
```

**Default**: `true`

Enables/disables low-level packet listeners for block interactions.

- `true`: Block dig, place, and chunk load events are handled
- `false`: Virtual blocks won't respond to player interactions

**Note**: Disabling this breaks most Blocket functionality. Only disable if you're implementing custom packet handling.

```java
// Generally not recommended unless you know what you're doing
BlocketConfig config = BlocketConfig.builder()
    .enablePacketListeners(false)
    .build();
```

#### Default Chunks Per Tick
```java
.defaultChunksPerTick(int chunksPerTick)
```

**Default**: `1`

**Range**: `1` to `10` (recommended)

Sets the default number of chunks processed per server tick for new stages.

- Lower values: Better server performance, slower block loading
- Higher values: Faster block loading, higher server load

```java
// For high-performance servers
BlocketConfig config = BlocketConfig.builder()
    .defaultChunksPerTick(4)
    .build();

// For servers with performance concerns
BlocketConfig config = BlocketConfig.builder()
    .defaultChunksPerTick(1)
    .build();
```

## Configuration Examples

### High Performance Setup
```java
BlocketConfig config = BlocketConfig.builder()
    .autoInitialize(true)
    .enableStageBoundListener(true)
    .enablePacketListeners(true)
    .defaultChunksPerTick(4)
    .build();

BlocketAPI api = BlocketAPI.initialize(this, config);
```

**Best for**: Dedicated servers with good hardware

### Conservative Setup
```java
BlocketConfig config = BlocketConfig.builder()
    .autoInitialize(true)
    .enableStageBoundListener(false)  // Manual management
    .enablePacketListeners(true)
    .defaultChunksPerTick(1)          // Slower but safer
    .build();

BlocketAPI api = BlocketAPI.initialize(this, config);
```

**Best for**: Shared hosting, lower-end hardware

### Development Setup
```java
BlocketConfig config = BlocketConfig.builder()
    .autoInitialize(false)            // Manual control
    .enableStageBoundListener(true)
    .enablePacketListeners(true)
    .defaultChunksPerTick(2)
    .build();

BlocketAPI api = BlocketAPI.initialize(this, config);

// Initialize when ready
if (getConfig().getBoolean("enable-blocket", true)) {
    api.initializeListeners();
}
```

**Best for**: Plugin development and testing

## Runtime Configuration Changes

Some settings can be modified after initialization:

### Per-Stage Chunk Processing

```java
// Override default for specific stages
Stage stage = new Stage("my-stage", world, pos1, pos2, audience);
stage.setChunksPerTick(3); // Override default setting
```

### Dynamic Listener Management

```java
// Check if listeners are active
if (BlocketAPI.isInitialized()) {
    BlocketAPI api = BlocketAPI.getInstance();
    
    // Listeners are automatically managed
    // But you can check configuration
    BlocketConfig config = api.getConfig();
    if (config.isEnablePacketListeners()) {
        // Packet listeners are active
    }
}
```

## Performance Tuning Guidelines

### Chunks Per Tick Recommendations

| Server Type | Recommended Value | Notes |
|-------------|-------------------|--------|
| High-end Dedicated | 4-6 | Fast loading, monitor TPS |
| VPS/Cloud | 2-3 | Balanced performance |
| Shared Hosting | 1-2 | Conservative approach |
| Development | 1 | Stable and predictable |

### Memory Considerations

```java
// Monitor stage count
StageManager stageManager = api.getStageManager();
Set<Stage> allStages = stageManager.getAllStages();
getLogger().info("Active stages: " + allStages.size());

// Clean up unused stages
stageManager.deleteStage("unused-stage-id");
```

### Async Configuration Loading

For large configurations, consider loading asynchronously:

```java
@Override
public void onEnable() {
    // Load config asynchronously
    Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
        BlocketConfig config = loadConfigFromFile();
        
        // Initialize on main thread
        Bukkit.getScheduler().runTask(this, () -> {
            BlocketAPI.initialize(this, config);
        });
    });
}

private BlocketConfig loadConfigFromFile() {
    // Load settings from config.yml or database
    boolean autoInit = getConfig().getBoolean("blocket.auto-initialize", true);
    int chunksPerTick = getConfig().getInt("blocket.chunks-per-tick", 2);
    
    return BlocketConfig.builder()
        .autoInitialize(autoInit)
        .defaultChunksPerTick(chunksPerTick)
        .build();
}
```

## Configuration Validation

Implement configuration validation to prevent issues:

```java
private BlocketConfig createValidatedConfig() {
    int chunksPerTick = getConfig().getInt("chunks-per-tick", 2);
    
    // Validate chunks per tick
    if (chunksPerTick < 1) {
        getLogger().warning("chunks-per-tick too low, using default (1)");
        chunksPerTick = 1;
    } else if (chunksPerTick > 10) {
        getLogger().warning("chunks-per-tick too high, using maximum (10)");
        chunksPerTick = 10;
    }
    
    return BlocketConfig.builder()
        .defaultChunksPerTick(chunksPerTick)
        .build();
}
```

## Best Practices

### 1. Environment-Specific Configs

```java
// Different configs for different environments
String environment = System.getProperty("server.environment", "production");

BlocketConfig config = switch (environment.toLowerCase()) {
    case "development" -> BlocketConfig.builder()
        .autoInitialize(false)
        .defaultChunksPerTick(1)
        .build();
    case "staging" -> BlocketConfig.builder()
        .autoInitialize(true)
        .defaultChunksPerTick(2)
        .build();
    default -> BlocketConfig.builder()
        .autoInitialize(true)
        .defaultChunksPerTick(3)
        .build();
};
```

### 2. Configuration Monitoring

```java
@Override
public void onEnable() {
    BlocketAPI api = BlocketAPI.initialize(this, config);
    
    // Log current configuration
    BlocketConfig config = api.getConfig();
    getLogger().info("Blocket Configuration:");
    getLogger().info("  Auto Initialize: " + config.isAutoInitialize());
    getLogger().info("  Stage Bound Listener: " + config.isEnableStageBoundListener());
    getLogger().info("  Packet Listeners: " + config.isEnablePacketListeners());
    getLogger().info("  Default Chunks/Tick: " + config.getDefaultChunksPerTick());
}
```

### 3. Graceful Fallbacks

```java
BlocketConfig config;
try {
    config = loadCustomConfig();
} catch (Exception e) {
    getLogger().warning("Failed to load custom config, using defaults: " + e.getMessage());
    config = BlocketConfig.defaultConfig();
}

BlocketAPI.initialize(this, config);
```

## Troubleshooting Configuration Issues

### Common Problems

#### "BlocketAPI already initialized"

**Cause**: Calling `initialize()` multiple times

**Solution**: Check if already initialized
```java
if (!BlocketAPI.isInitialized()) {
    BlocketAPI.initialize(this, config);
}
```

#### Poor Performance

**Cause**: `chunksPerTick` set too high

**Solution**: Reduce the value and monitor server TPS
```java
.defaultChunksPerTick(1) // Start conservative
```

#### Virtual Blocks Not Working

**Cause**: Packet listeners disabled

**Solution**: Ensure packet listeners are enabled
```java
.enablePacketListeners(true) // Must be true for functionality
```

## Next Steps

- 📖 Learn about [Stages and Views](Stages-and-Views) architecture
- ⚡ Explore [Performance Optimization](Performance-Optimization) techniques
- 🎯 Check out the [BlocketAPI Reference](BlocketAPI-Reference)

---

**Pro Tip**: Start with default configuration and adjust based on your server's performance metrics!
