# Blocket

## About
**✅ 重構完成！** Blocket 已成功從插件轉換為獨立開發庫。

Ever wondered how servers like FadeCloud or AkumaMC do private farms and mines?
Well, let Blocket take care of it for you! Blocket is a public library that can manage and create client-sided blocks.

**🆕 Blocket is now a Library!** - You can integrate it directly into your plugins without needing to install a separate plugin dependency.

## Installation

### As a Maven Dependency
Add this to your plugin's `pom.xml`:

```xml
<dependency>
    <groupId>dev.twme</groupId>
    <artifactId>blocket-api</artifactId>
    <version>1.0.0</version>
    <scope>compile</scope>
</dependency>
```

### As a Gradle Dependency
Add this to your plugin's `build.gradle`:

```gradle
dependencies {
    implementation 'dev.twme:blocket-api:1.0.0'
}
```

### Quick Start Example

```java
public class MyPlugin extends JavaPlugin {
    private BlocketAPI BlocketAPI;
    
    @Override
    public void onEnable() {
        // Initialize Blocket API
        BlocketAPI = BlocketAPI.initialize(this);
        
        // Your plugin logic here...
    }
    
    @Override
    public void onDisable() {
        // Always shutdown Blocket API
        if (BlocketAPI != null) {
            BlocketAPI.shutdown();
        }
    }
}
```

## Features
1. **Stage Management**: Blocket has different stages for an audience. Each stage has multiple "views", which represent different patterns within a stage.
2. **Block Interaction Events**: The project handles block interaction events, such as starting to dig a block, as seen in the `BlockDigAdapter` class.
3. **Block Breaking Events**: Blocket also handles block-breaking events, including checking if a block is breakable and sending block change updates to the player.
4. **Chunk Loading**: The `ChunkLoadAdapter` class handles chunk-loading events, including sending block changes to the player.
5. **Game Mode Checks**: The project checks the player's game mode and adjusts block-breaking speed accordingly.
6. **Memory Management**: Blocket manages memory efficiently by using custom data types like `BlocketPosition` and `BlocketChunk`.
7. **Custom Events:** Blocket has a custom event `BlocketBlockBreakEvent` that is called when a block is broken. 
8. **Complex Block Patterns:** Blocket can handle complex block patterns using the `BlocketPattern` class. In addition, it can handle setting crop ages and other custom block data.

## API 使用指南

### 基本概念
- **Stage（舞台）**: 代表一個有界限的區域，包含一個觀眾群體（Audience）
- **View（視圖）**: Stage 中的一個圖層，包含虛擬方塊的模式（Pattern）
- **Pattern（模式）**: 定義了方塊類型及其出現機率的配置
- **Audience（觀眾）**: 可以看到虛擬方塊的玩家集合

### 建立虛擬方塊系統

#### 0. 初始化 Blocket API
首先在你的插件中初始化 BlocketAPI：

```java
public class MyPlugin extends JavaPlugin {
    private BlocketAPI BlocketAPI;
    
    @Override
    public void onEnable() {
        // 基本初始化
        BlocketAPI = BlocketAPI.initialize(this);
        
        // 或自定義配置初始化
        BlocketConfig config = BlocketConfig.builder()
            .autoInitialize(true)
            .enableStageBoundListener(true)
            .enablePacketListeners(true)
            .defaultChunksPerTick(2)
            .build();
        BlocketAPI = BlocketAPI.initialize(this, config);
    }
    
    @Override
    public void onDisable() {
        if (BlocketAPI != null) {
            BlocketAPI.shutdown();
        }
    }
    
    // 使用 API
    public void createMine(Player player) {
        StageManager stageManager = BlocketAPI.getStageManager();
        BlockChangeManager blockManager = BlocketAPI.getBlockChangeManager();
        // ... 你的邏輯
    }
}
```

#### 1. 建立觀眾群體

```java
import models.dev.twme.blocket.Audience;

// 從玩家集合建立觀眾
Set<Player> players = Set.of(player1, player2);
        Audience audience = Audience.fromPlayers(players);

        // 或從 UUID 集合建立
        Set<UUID> playerUUIDs = Set.of(uuid1, uuid2);
        Audience audience = Audience.fromUUIDs(playerUUIDs);
```

#### 2. 定義方塊模式

```java
import models.dev.twme.blocket.Pattern;
import org.bukkit.Material;

// 建立方塊模式（支援權重分配）
Map<BlockData, Double> blockPattern = new HashMap<>();
blockPattern.

        put(Material.STONE.createBlockData(), 70.0); // 70% 機率
        blockPattern.

        put(Material.COAL_ORE.createBlockData(), 20.0); // 20% 機率
        blockPattern.

        put(Material.IRON_ORE.createBlockData(), 10.0); // 10% 機率

        Pattern pattern = new Pattern(blockPattern);
```

#### 3. 建立舞台

```java
import models.dev.twme.blocket.Stage;
import types.dev.twme.blocket.BlocketPosition;

// 定義舞台範圍
BlocketPosition pos1 = new BlocketPosition(100, 60, 100);
        BlocketPosition pos2 = new BlocketPosition(150, 100, 150);

        // 建立舞台
        Stage stage = new Stage("my-mine", world, pos1, pos2, audience);

// 註冊舞台到管理器
Blocket.

        getInstance().

        getStageManager().

        createStage(stage);
```

#### 4. 建立視圖並添加方塊

```java
import models.dev.twme.blocket.View;

// 建立視圖
View view = new View("ore-layer", stage, pattern, true); // true = 可破壞
view.

        setZIndex(1); // 設定圖層優先級

// 添加視圖到舞台
stage.

        addView(view);

        // 添加方塊到視圖
        Set<BlocketPosition> positions = BlockUtils.getBlocksBetween(pos1, pos2);
view.

        addBlocks(positions);

// 發送方塊變化給觀眾
stage.

        sendBlocksToAudience();
```

### 高級功能

#### 動態方塊管理
```java
// 單獨添加方塊
BlocketPosition position = new BlocketPosition(125, 75, 125);
view.addBlock(position);

// 設定特定方塊
view.setBlock(position, Material.DIAMOND_ORE.createBlockData());

// 重置方塊（使用原始模式）
view.resetBlock(position);

// 移除方塊
view.removeBlock(position);

// 批次操作
Set<BlocketPosition> blockPositions = Set.of(pos1, pos2, pos3);
view.addBlocks(blockPositions);
view.setBlocks(blockPositions, Material.EMERALD_ORE.createBlockData());
view.removeBlocks(blockPositions);
```

#### 玩家視圖管理
```java
// 為特定玩家添加視圖
stage.addViewForPlayer(player, view);
stage.addViewForPlayer(player, "ore-layer");

// 為特定玩家移除視圖
stage.removeViewForPlayer(player, view);
stage.removeViewForPlayer(player, "ore-layer");

// 隱藏視圖
Blocket.getInstance().getBlockChangeManager().hideView(player, view);
```

#### 觀眾管理
```java
// 添加玩家到觀眾
audience.addPlayer(player);

// 移除玩家
audience.removePlayer(player);

// 設定挖掘速度
audience.setMiningSpeed(player, 2.0f); // 2倍速度

// 重置挖掘速度
audience.resetMiningSpeed(player);
```

#### 事件處理

```java
import dev.twme.blocket.events.BlocketBreakEvent;
import dev.twme.blocket.events.BlocketInteractEvent;
import dev.twme.blocket.events.PlayerEnterStageEvent;

@EventHandler
public void onBlocketBreak(BlocketBreakEvent event) {
    Player player = event.getPlayer();
    BlocketPosition position = event.getPosition();
    View view = event.getView();

    // 自定義破壞邏輯
    if (shouldCancelBreak(player)) {
        event.setCancelled(true);
    }
}

@EventHandler
public void onBlocketInteract(BlocketInteractEvent event) {
    // 處理方塊互動
}

@EventHandler
public void onPlayerEnterStage(PlayerEnterStageEvent event) {
    // 玩家進入舞台時的處理
}
```

### 實用工具

#### BlockUtils 工具類

```java
import utils.dev.twme.blocket.BlockUtils;

// 獲取兩點間的所有方塊位置
Set<BlocketPosition> blocks = BlockUtils.getBlocksBetween(pos1, pos2);

        // 設定作物年齡
        BlockData wheatData = Material.WHEAT.createBlockData();
        BlockData agedWheat = BlockUtils.setAge(wheatData, 7); // 完全成熟
```

#### 位置轉換

```java
import types.dev.twme.blocket.BlocketPosition;
import types.dev.twme.blocket.BlocketChunk;

// 從 Location 建立 BlocketPosition
BlocketPosition pos = BlocketPosition.fromLocation(location);

        // 轉換為其他格式
        Location loc = pos.toLocation(world);
        Vector vector = pos.toVector();
        BlocketChunk chunk = pos.toBlocketChunk();

        // 計算距離
        double distance = pos1.distance(pos2);
        double distanceSquared = pos1.distanceSquared(pos2);
```

### 完整範例：建立礦場
```java
public class MinePlugin extends JavaPlugin {
    private BlocketAPI BlocketAPI;
    
    @Override
    public void onEnable() {
        BlocketAPI = BlocketAPI.initialize(this);
    }
    
    @Override
    public void onDisable() {
        if (BlocketAPI != null) {
            BlocketAPI.shutdown();
        }
    }
    
    public void createMine(Player player, Location corner1, Location corner2) {
        // 1. 建立觀眾
        Set<Player> players = Set.of(player);
        Audience audience = Audience.fromPlayers(players);
        
        // 2. 定義礦石模式
        Map<BlockData, Double> orePattern = new HashMap<>();
        orePattern.put(Material.STONE.createBlockData(), 60.0);
        orePattern.put(Material.COAL_ORE.createBlockData(), 25.0);
        orePattern.put(Material.IRON_ORE.createBlockData(), 10.0);
        orePattern.put(Material.DIAMOND_ORE.createBlockData(), 5.0);
        
        Pattern pattern = new Pattern(orePattern);
        
        // 3. 建立舞台
        BlocketPosition pos1 = BlocketPosition.fromLocation(corner1);
        BlocketPosition pos2 = BlocketPosition.fromLocation(corner2);
        
        Stage stage = new Stage("player-mine", corner1.getWorld(), pos1, pos2, audience);
        BlocketAPI.getStageManager().createStage(stage);
        
        // 4. 建立視圖
        View mineView = new View("ore-deposits", stage, pattern, true);
        stage.addView(mineView);
        
        // 5. 填充區域
        Set<BlocketPosition> mineBlocks = BlockUtils.getBlocksBetween(pos1, pos2);
        mineView.addBlocks(mineBlocks);
        
        // 6. 發送給玩家
        stage.sendBlocksToAudience();
        
        player.sendMessage("§a私人礦場已建立！");
    }
}

### 性能優化建議
1. **分批處理**: 對於大型區域，使用 `stage.setChunksPerTick()` 控制每 tick 處理的區塊數量
2. **非同步操作**: 大量方塊操作建議在非同步執行緒中進行
3. **記憶體管理**: 適時清理不需要的視圖和舞台
4. **事件處理**: 在事件處理器中避免重型運算
   
## Dependencies
- [PacketEvents](https://github.com/retrooper/packetevents)

## 🎉 重構完成摘要

Blocket 已成功從 Bukkit 插件重構為獨立開發庫！主要改變包括：

### ✅ 已完成的改變
- **新 API 架構**: 創建了 `BlocketAPI` 類作為主要入口點
- **配置系統**: 實現了 `BlocketConfig` 用於靈活配置
- **依賴注入**: 所有管理器類現在使用 API 參考而非靜態單例
- **生命週期管理**: 提供了適當的初始化和關閉方法
- **Maven 配置**: 更新為庫分發配置，生成帶源碼的 JAR
- **文檔更新**: 完整的 API 文檔、安裝指南和遷移指南
- **示例代碼**: 提供完整的使用範例和最佳實踐

### 🔄 架構變更
- 移除了舊的插件主類 (`Blocket.java`)
- 移除了 `plugin.yml` （不再需要）
- 所有 `Blocket.getInstance()` 調用更新為 `BlocketAPI.getInstance()`
- 管理器類現在接受 `BlocketAPI` 參數而非使用靜態訪問

### 📦 輸出文件
- `blocket-api-1.0.0.jar` - 主要庫文件
- `blocket-api-1.0.0-sources.jar` - 源碼文件

開發者現在可以直接將 Blocket 作為依賴項整合到他們的插件中，無需單獨安裝插件！
