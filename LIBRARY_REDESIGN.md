# Blockify Library 重構計劃

## 目標
將 Blockify 從一個獨立插件改寫為一個可被其他插件使用的 Library。

## 主要改動

### 1. 移除插件相關文件
- ❌ 刪除 `plugin.yml`
- ❌ 移除 `JavaPlugin` 繼承
- ✅ 改為靜態初始化系統

### 2. 新的初始化系統

#### 建議的新架構：

```java
// 新的主要 API 類
public class BlockifyAPI {
    private static BlockifyAPI instance;
    private final Plugin ownerPlugin;
    private final StageManager stageManager;
    private final BlockChangeManager blockChangeManager;
    private final boolean autoInitialize;
    
    // 私有構造函數
    private BlockifyAPI(Plugin plugin, BlockifyConfig config) {
        this.ownerPlugin = plugin;
        this.autoInitialize = config.isAutoInitialize();
        // 初始化管理器
    }
    
    // 靜態初始化方法
    public static BlockifyAPI initialize(Plugin plugin) {
        return initialize(plugin, BlockifyConfig.defaultConfig());
    }
    
    public static BlockifyAPI initialize(Plugin plugin, BlockifyConfig config) {
        if (instance != null) {
            throw new IllegalStateException("BlockifyAPI already initialized!");
        }
        instance = new BlockifyAPI(plugin, config);
        return instance;
    }
    
    public static BlockifyAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("BlockifyAPI not initialized!");
        }
        return instance;
    }
    
    // API 方法
    public StageManager getStageManager() { return stageManager; }
    public BlockChangeManager getBlockChangeManager() { return blockChangeManager; }
    
    // 清理方法
    public void shutdown() {
        blockChangeManager.shutdown();
        instance = null;
    }
}
```

#### 配置類：

```java
public class BlockifyConfig {
    private boolean autoInitialize = true;
    private boolean enableStageBoundListener = true;
    private boolean enablePacketListeners = true;
    private int defaultChunksPerTick = 1;
    
    public static BlockifyConfig defaultConfig() {
        return new BlockifyConfig();
    }
    
    public static BlockifyConfig builder() {
        return new BlockifyConfig();
    }
    
    // Fluent API
    public BlockifyConfig autoInitialize(boolean enable) {
        this.autoInitialize = enable;
        return this;
    }
    
    public BlockifyConfig enableStageBoundListener(boolean enable) {
        this.enableStageBoundListener = enable;
        return this;
    }
    
    // ... 其他配置方法
}
```

### 3. 使用方式範例

#### 在其他插件中使用：

```java
public class MyPlugin extends JavaPlugin {
    private BlockifyAPI blockifyAPI;
    
    @Override
    public void onEnable() {
        // 基本初始化
        blockifyAPI = BlockifyAPI.initialize(this);
        
        // 或者自定義配置初始化
        BlockifyConfig config = BlockifyConfig.builder()
            .autoInitialize(true)
            .enableStageBoundListener(true)
            .enablePacketListeners(true)
            .build();
        blockifyAPI = BlockifyAPI.initialize(this, config);
        
        // 使用 API
        StageManager stageManager = blockifyAPI.getStageManager();
        // ... 業務邏輯
    }
    
    @Override
    public void onDisable() {
        if (blockifyAPI != null) {
            blockifyAPI.shutdown();
        }
    }
}
```

### 4. Maven 依賴配置

#### 新的 pom.xml：
```xml
<groupId>codes.kooper</groupId>
<artifactId>blockify-api</artifactId>
<version>1.0.0</version>
<packaging>jar</packaging>

<!-- 移除 maven-shade-plugin，改為普通 jar -->
<!-- 依賴範圍改為 compile 而不是 provided -->
```

#### 其他插件使用方式：
```xml
<dependency>
    <groupId>codes.kooper</groupId>
    <artifactId>blockify-api</artifactId>
    <version>1.0.0</version>
    <scope>compile</scope>
</dependency>
```

### 5. 需要修改的文件

#### 核心文件修改：
1. **BlockifyAPI.java** (新建) - 主要 API 入口
2. **BlockifyConfig.java** (新建) - 配置類
3. **所有 Manager 類** - 移除對 Blockify.getInstance() 的依賴
4. **事件監聽器** - 改為可選註冊
5. **Protocol Adapters** - 支援動態註冊/取消註冊

#### 輔助類修改：
1. **Logger 處理** - 使用傳入的插件的 Logger
2. **任務調度** - 使用傳入的插件進行任務調度
3. **事件調用** - 使用傳入的插件調用事件

### 6. 向後兼容

#### 提供兼容包裝器：
```java
// 可選：為現有用戶提供兼容性
@Deprecated
public class Blockify extends JavaPlugin {
    private BlockifyAPI api;
    
    @Override
    public void onEnable() {
        api = BlockifyAPI.initialize(this);
    }
    
    @Override
    public void onDisable() {
        if (api != null) {
            api.shutdown();
        }
    }
    
    @Deprecated
    public static Blockify getInstance() {
        // 提供向後兼容
        return // ...
    }
    
    public StageManager getStageManager() {
        return api.getStageManager();
    }
    
    public BlockChangeManager getBlockChangeManager() {
        return api.getBlockChangeManager();
    }
}
```

## 優勢總結

### ✅ 對開發者的優勢：
1. **更靈活的整合** - 可以選擇性啟用功能
2. **更好的控制** - 可以自定義初始化邏輯
3. **減少衝突** - 不需要額外的插件依賴
4. **更好的測試** - 可以在單元測試中使用

### ✅ 對使用者的優勢：
1. **減少插件數量** - 不需要額外安裝 Blockify 插件
2. **更好的性能** - 按需初始化功能
3. **更少的問題** - 減少插件間的相互影響

### ✅ 對項目的優勢：
1. **更廣泛的採用** - Library 比插件更容易被採用
2. **更標準的分發** - 可以發布到 Maven 倉庫
3. **更好的維護** - 更清晰的 API 邊界

## 實施步驟

1. **階段一：核心重構**
   - 建立 BlockifyAPI 和 BlockifyConfig
   - 重構管理器類以移除靜態依賴

2. **階段二：可選功能**
   - 將監聽器和協議適配器改為可選
   - 實現動態註冊/取消註冊

3. **階段三：文檔和測試**
   - 更新 README 和 API 文檔
   - 建立使用範例和測試

4. **階段四：發布**
   - 發布到 Maven 倉庫
   - 提供遷移指南

這個改寫將大大提高 Blockify 的實用性和採用率！
