# Blocket Custom Block Lighting 功能

## 概述

Blocket 1.1.0 版本新增了自定義方塊亮度功能，允許開發者為特定的虛擬方塊設置自定義的光照等級。此功能與現有的光照保持功能完全兼容，提供更細粒度的光照控制。

## 功能特點

- **精確光照控制**: 可以為任何虛擬方塊位置設置 0-15 級的亮度
- **雙重光照支持**: 支持方塊光照（Block Light）和天空光照（Sky Light）的獨立設置
- **視圖級別和舞台級別**: 支持在 View 和 Stage 兩個層級設置自定義光照
- **動態更新**: 可以在運行時動態更改光照等級
- **事件系統**: 提供光照變更事件用於監聽和響應
- **向後兼容**: 與現有的 `preserveOriginalLighting` 功能完全兼容

## 快速開始

### 基本用法

```java
// 獲取 API 實例
BlocketAPI api = BlocketAPI.getInstance();
BlockLightingManager lightingManager = api.getBlockLightingManager();

// 為特定位置設置方塊光照
BlocketPosition position = new BlocketPosition(100, 64, 100);
View myView = stage.getView("myView");

// 設置最大亮度（15級）
myView.setBlockLight(position, 15);

// 設置天空光照
myView.setSkyLight(position, 12);

// 同時設置兩種光照
myView.setLighting(position, 15, 12);
```

### 創建發光路徑

```java
public void createGlowingPath(Player player, Stage stage) {
    View pathView = stage.getView("pathway");
    
    // 創建一條發光的路徑
    for (int x = 0; x < 20; x++) {
        BlocketPosition pos = new BlocketPosition(
            player.getLocation().getBlockX() + x,
            player.getLocation().getBlockY(),
            player.getLocation().getBlockZ()
        );
        
        // 添加方塊
        pathView.addBlock(pos);
        
        // 設置最大亮度使其發光
        pathView.setBlockLight(pos, 15);
    }
    
    // 刷新顯示
    stage.sendBlocksToAudience();
}
```

### 動態光照效果

```java
public void createPulsingLight(Player player, View view, BlocketPosition position) {
    AtomicInteger currentLight = new AtomicInteger(0);
    AtomicBoolean increasing = new AtomicBoolean(true);
    
    // 每 10 tick 更新一次光照等級
    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
        int light = currentLight.get();
        
        // 設置當前光照等級
        view.setBlockLight(position, light);
        
        // 更新光照等級
        if (increasing.get()) {
            if (light >= 15) {
                increasing.set(false);
            } else {
                currentLight.incrementAndGet();
            }
        } else {
            if (light <= 0) {
                increasing.set(true);
            } else {
                currentLight.decrementAndGet();
            }
        }
        
        // 刷新單個方塊
        view.getStage().refreshBlocksToAudience(Set.of(position));
    }, 0L, 10L);
}
```

## API 參考

### BlockLightingManager 類

#### 主要方法

##### setBlockLight()
```java
// View 級別設置
boolean setBlockLight(View view, BlocketPosition position, int blockLight)
boolean setBlockLight(String viewName, BlocketPosition position, int blockLight)

// Stage 級別設置  
boolean setStageBlockLight(Stage stage, BlocketPosition position, int blockLight)
boolean setStageBlockLight(String stageName, BlocketPosition position, int blockLight)
```

##### setSkyLight()
```java
boolean setSkyLight(View view, BlocketPosition position, int skyLight)
boolean setSkyLight(String viewName, BlocketPosition position, int skyLight)
```

##### setLighting()
```java
boolean setLighting(View view, BlocketPosition position, int blockLight, int skyLight)
boolean setLighting(String viewName, BlocketPosition position, int blockLight, int skyLight)
```

##### getLighting()
```java
LightingData getLighting(String viewName, BlocketPosition position)
LightingData getStageLighting(String stageName, BlocketPosition position)
```

##### removeLighting()
```java
boolean removeLighting(String viewName, BlocketPosition position)
boolean removeAllViewLighting(String viewName)
```

### View 類擴展方法

```java
// 直接在 View 上設置光照
boolean setBlockLight(BlocketPosition position, int blockLight)
boolean setSkyLight(BlocketPosition position, int skyLight)
boolean setLighting(BlocketPosition position, int blockLight, int skyLight)

// 獲取和移除光照
LightingData getLighting(BlocketPosition position)
boolean removeLighting(BlocketPosition position)
boolean removeAllLighting()
```

### Stage 類擴展方法

```java
// 舞台級別光照設置
boolean setStageBlockLight(BlocketPosition position, int blockLight)
LightingData getStageLighting(BlocketPosition position)
```

### 事件系統

監聽光照變更事件：

```java
@EventHandler
public void onBlockLightingChange(BlockLightingChangeEvent event) {
    String viewName = event.getViewName();
    BlocketPosition position = event.getPosition();
    LightingData newLighting = event.getNewLightingData();
    LightType lightType = event.getLightType();
    
    // 處理光照變更
    System.out.println("View " + viewName + " 在位置 " + position + 
                      " 的光照已變更: " + newLighting);
    
    // 可以取消變更
    if (shouldCancelLighting(position)) {
        event.setCancelled(true);
    }
}
```

## 配置說明

### 與現有光照保持功能的兼容性

```java
BlocketConfig config = BlocketConfig.builder()
    .autoInitialize(true)
    .enablePacketListeners(true)
    .preserveOriginalLighting(true) // 保持原始光照
    .build();

BlocketAPI api = BlocketAPI.initialize(plugin, config);
```

當 `preserveOriginalLighting` 啟用時：
- 自定義光照設置會覆蓋原始光照
- 未設置自定義光照的方塊會保持原始光照
- 提供最佳的視覺體驗

## 最佳實踐

### 1. 光照等級建議

- **0-3**: 非常暗，適合營造神秘氛圍
- **4-7**: 中等亮度，適合室內照明
- **8-11**: 明亮，適合戶外日間效果
- **12-15**: 最大亮度，適合發光效果和特殊標記

### 2. 性能考慮

- 避免頻繁更改大量方塊的光照等級
- 使用 `refreshBlocksToAudience()` 批量更新方塊
- 對於動畫效果，考慮適當的更新頻率

### 3. 光照組合建議

```java
// 室內燈光效果
view.setLighting(position, 12, 0);

// 戶外日光效果
view.setLighting(position, 0, 15);

// 特殊發光效果
view.setLighting(position, 15, 15);

// 微弱光源
view.setLighting(position, 4, 2);
```

## 故障排除

### 常見問題

**Q: 設置光照後看不到效果？**
A: 確保調用了 `stage.sendBlocksToAudience()` 或 `stage.refreshBlocksToAudience(positions)`

**Q: 光照設置被忽略？**
A: 檢查是否啟用了 `preserveOriginalLighting`，並確保光照等級在 0-15 範圍內

**Q: 動態光照更新太慢？**
A: 使用 `refreshBlocksToAudience()` 而不是 `sendBlocksToAudience()`，前者更適合小範圍更新

### 調試提示

```java
// 檢查光照設置是否生效
LightingData data = view.getLighting(position);
if (data != null) {
    System.out.println("方塊光照: " + data.getBlockLight());
    System.out.println("天空光照: " + data.getSkyLight());
} else {
    System.out.println("該位置沒有自定義光照設置");
}
```

## 完整示例

請查看 `CustomLightingExample.java` 文件，其中包含：
- 基本光照設置演示
- 發光路徑創建
- 動態光照效果
- 混合光照類型示例

## 更新日誌

### 1.1.0
- 新增 `BlockLightingManager` 類
- 在 `View` 和 `Stage` 類中添加光照控制方法
- 新增 `BlockLightingChangeEvent` 事件
- 集成到現有的 `LightDataProcessor` 中
- 提供完整的示例和文檔

---

更多信息請參考 [Blocket API 文檔](../README.md)
