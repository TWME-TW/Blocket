# Blockify Library 遷移指南

此文件説明如何從舊的插件版本遷移到新的 Library 版本。

## 主要變化

### 1. 不再需要插件依賴
- **舊版**: 需要在 `plugin.yml` 中添加 `depend: [Blockify]`
- **新版**: 直接將 Blockify 作為 Maven/Gradle 依賴添加

### 2. 初始化方式改變
- **舊版**: Blockify 作為插件自動初始化
- **新版**: 需要手動初始化 BlockifyAPI

### 3. API 訪問方式改變
- **舊版**: `Blockify.getInstance().getStageManager()`
- **新版**: `blockifyAPI.getStageManager()`

## 遷移步驟

### 步驟 1: 更新依賴

#### 移除插件依賴
```yaml
# plugin.yml - 移除這一行
depend: [Blockify]  # ❌ 刪除
```

#### 添加 Maven 依賴
```xml
<!-- pom.xml -->
<dependency>
    <groupId>codes.kooper</groupId>
    <artifactId>blockify-api</artifactId>
    <version>1.0.0</version>
    <scope>compile</scope>
</dependency>
```

### 步驟 2: 初始化 API

#### 舊版寫法 ❌
```java
public class MyPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        // Blockify 會自動初始化
        StageManager stageManager = Blockify.getInstance().getStageManager();
    }
}
```

#### 新版寫法 ✅
```java
public class MyPlugin extends JavaPlugin {
    private BlockifyAPI blockifyAPI;
    
    @Override
    public void onEnable() {
        // 手動初始化 BlockifyAPI
        blockifyAPI = BlockifyAPI.initialize(this);
        StageManager stageManager = blockifyAPI.getStageManager();
    }
    
    @Override
    public void onDisable() {
        // 必須手動清理
        if (blockifyAPI != null) {
            blockifyAPI.shutdown();
        }
    }
}
```

### 步驟 3: 更新 API 調用

#### 舊版 API 調用 ❌
```java
// 舊版
Blockify.getInstance().getStageManager().createStage(stage);
Blockify.getInstance().getBlockChangeManager().sendBlockChanges(...);
```

#### 新版 API 調用 ✅
```java
// 新版
blockifyAPI.getStageManager().createStage(stage);
blockifyAPI.getBlockChangeManager().sendBlockChanges(...);
```

### 步驟 4: 更新導入語句

#### 新增導入

```java


```

#### 移除導入 (如果不再需要)
```java
// 如果不再直接使用，可以移除
import codes.kooper.blockify.Blockify;
```

## 完整遷移範例

### 遷移前 ❌
```java
public class MinePlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        // 舊版不需要初始化
    }
    
    public void createMine(Player player, Location loc1, Location loc2) {
        // 舊版 API 調用
        StageManager stageManager = Blockify.getInstance().getStageManager();
        
        // ... 其他邏輯
        stage.sendBlocksToAudience();
    }
}
```

### 遷移後 ✅
```java
public class MinePlugin extends JavaPlugin {
    private BlockifyAPI blockifyAPI;
    
    @Override
    public void onEnable() {
        // 新版需要手動初始化
        blockifyAPI = BlockifyAPI.initialize(this);
    }
    
    @Override
    public void onDisable() {
        // 新版需要手動清理
        if (blockifyAPI != null) {
            blockifyAPI.shutdown();
        }
    }
    
    public void createMine(Player player, Location loc1, Location loc2) {
        // 新版 API 調用
        StageManager stageManager = blockifyAPI.getStageManager();
        
        // ... 其他邏輯不變
        stage.sendBlocksToAudience();
    }
}
```

## 新功能

### 自定義配置
新版本支援自定義配置：

```java
BlockifyConfig config = BlockifyConfig.builder()
    .autoInitialize(true)
    .enableStageBoundListener(true)
    .enablePacketListeners(true)
    .defaultChunksPerTick(3)
    .build();

blockifyAPI = BlockifyAPI.initialize(this, config);
```

### 手動控制監聽器
```java
// 如果 autoInitialize = false
BlockifyConfig config = BlockifyConfig.builder()
    .autoInitialize(false)
    .build();

blockifyAPI = BlockifyAPI.initialize(this, config);

// 稍後手動初始化監聽器
blockifyAPI.initializeListeners();
```

## 故障排除

### 常見問題

#### 1. ClassNotFoundException
```
java.lang.ClassNotFoundException: codes.kooper.blockify.Blockify
```
**解決方案**: 確保已移除對舊 `Blockify.getInstance()` 的引用，改用 `blockifyAPI`

#### 2. IllegalStateException: BlockifyAPI not initialized
```
IllegalStateException: BlockifyAPI is not initialized! Call initialize() first.
```
**解決方案**: 確保在 `onEnable()` 中調用了 `BlockifyAPI.initialize(this)`

#### 3. 重複初始化錯誤
```
IllegalStateException: BlockifyAPI is already initialized
```
**解決方案**: 確保只調用一次 `BlockifyAPI.initialize()`

### 檢查清單

- [ ] 移除 `plugin.yml` 中的 `depend: [Blockify]`
- [ ] 添加 Maven/Gradle 依賴
- [ ] 在 `onEnable()` 中初始化 BlockifyAPI
- [ ] 在 `onDisable()` 中調用 `shutdown()`
- [ ] 將所有 `Blockify.getInstance()` 改為使用 `blockifyAPI`
- [ ] 添加必要的導入語句
- [ ] 測試所有功能是否正常運作

## 需要協助？

如果遇到遷移問題，請：
1. 檢查上述故障排除部分
2. 查看完整的範例代碼
3. 在 GitHub Issues 中報告問題
4. 加入 Discord 尋求協助
