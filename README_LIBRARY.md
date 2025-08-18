# Blockify Library

Blockify has been refactored into a standalone library that can be used independently of the Bukkit plugin ecosystem.

## Core Components

### Library Core
- `BlockifyLibrary` - Main entry point and library coordinator
- `BlockifyLogger` - Logging abstraction interface
- `BlockifyServer` - Server operations abstraction interface  
- `BlockifyTaskScheduler` - Task scheduling abstraction interface

### Core Models (Plugin-agnostic)
- `Stage` - Virtual block areas with audiences and views
- `View` - Collections of blocks within a stage
- `Audience` - Player groups with mining speeds and visibility settings
- `Pattern` - Block generation patterns for views

### Core Managers  
- `StageManager` - Manages all stages and their lifecycle
- `BlockChangeManager` - Handles block changes and packet sending

### Core Types and Utils
- `BlockifyPosition`, `BlockifyChunk` - Position and chunk utilities
- `BlockUtils` - Block manipulation utilities  
- Custom events for block interactions

## Usage

### As a Standalone Library

```java
import codes.kooper.blockify.BlockifyLibrary;
import codes.kooper.blockify.BlockifyLogger;
import codes.kooper.blockify.BlockifyServer; 
import codes.kooper.blockify.BlockifyTaskScheduler;

// Implement the required interfaces
BlockifyLogger logger = new MyLogger();
BlockifyServer server = new MyServer();
BlockifyTaskScheduler scheduler = new MyScheduler();

// Initialize the library
BlockifyLibrary library = new BlockifyLibrary(logger, server, scheduler);
library.initialize();

// Use the library
StageManager stageManager = library.getStageManager();
BlockChangeManager blockManager = library.getBlockChangeManager();

// Create stages, manage blocks, etc.
Stage stage = new Stage("example", world, pos1, pos2, audience);
stageManager.createStage(stage);

// Shutdown when done
library.shutdown();
```

### As a Bukkit Plugin

The library can still be used as a traditional Bukkit plugin:

1. Place the JAR in your plugins folder
2. Ensure PacketEvents is installed
3. The plugin will automatically initialize the library with Bukkit implementations

## Architecture Changes

### Before (Plugin-only)
```
Blockify (JavaPlugin)
├── StageManager
├── BlockChangeManager  
├── Models (Stage, View, etc.)
└── Plugin-specific listeners/adapters
```

### After (Library + Plugin)
```
BlockifyLibrary (Core)
├── StageManager  
├── BlockChangeManager
├── Models (Stage, View, etc.) 
└── Abstraction interfaces

codes.kooper.blockify.plugin
├── Blockify (JavaPlugin wrapper)
├── Bukkit implementations of interfaces
└── Plugin-specific listeners/adapters
```

## Key Benefits

1. **Modular Design**: Core functionality separated from plugin-specific code
2. **Platform Agnostic**: Can be used outside Bukkit/Spigot environments
3. **Testable**: Easy to mock dependencies for unit testing
4. **Extensible**: New implementations can be plugged in easily
5. **Backward Compatible**: Existing plugin functionality preserved

## Migration Guide

If you were using Blockify as a plugin dependency:

**Before:**
```java
Blockify.getInstance().getStageManager()
```

**After:**  
```java
BlockifyLibrary.getInstance().getStageManager()
```

The API remains largely the same, with the main change being the entry point class.

## Dependencies

### Core Library
- Lombok (provided)
- Bukkit API (optional, for types only)

### Plugin Components  
- Paper API (provided)
- PacketEvents (provided)

The core library can function without Bukkit dependencies when used standalone.