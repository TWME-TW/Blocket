package dev.twme.blocket.examples;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import dev.twme.blocket.api.BlocketAPI;
import dev.twme.blocket.api.BlocketConfig;
import dev.twme.blocket.models.Audience;
import dev.twme.blocket.models.Stage;
import dev.twme.blocket.models.View;
import dev.twme.blocket.types.BlocketPosition;

/**
 * 展示光照修復功能的示例類
 * 
 * 這個示例展示了如何使用新的光照保留功能來解決區塊亮度問題：
 * 1. 向後兼容性：預設情況下保持原有行為
 * 2. 新功能：可選擇保留原始光照以維持一致的亮度
 * 3. 配置靈活性：開發者可以根據需要選擇光照處理策略
 */
public class LightingFixExample {
    
    private final JavaPlugin plugin;
    private BlocketAPI blocketAPI;
    
    public LightingFixExample(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 示例1：使用預設配置（向後兼容）
     * 這將使用空光照，客戶端會重新計算光照
     */
    public void createStageWithDefaultLighting(Player player) {
        // 預設配置 - preserveOriginalLighting = false
        BlocketConfig defaultConfig = BlocketConfig.builder()
            .autoInitialize(true)
            .enablePacketListeners(true)
            .build();
            
        BlocketAPI api = BlocketAPI.initialize(plugin, defaultConfig);
        
        // 創建舞台和視圖
        createExampleStage(api, player, "default-lighting-stage");
        
        player.sendMessage("§e已創建使用預設光照處理的舞台（向後兼容模式）");
    }
    
    /**
     * 示例2：使用新的光照保留功能
     * 這將保留原始區塊的光照，避免亮度不一致問題
     */
    public void createStageWithPreservedLighting(Player player) {
        // 啟用光照保留功能
        BlocketConfig preserveLightingConfig = BlocketConfig.builder()
            .autoInitialize(true)
            .enablePacketListeners(true)
            .preserveOriginalLighting(true) // 關鍵設置：保留原始光照
            .build();
            
        BlocketAPI api = BlocketAPI.initialize(plugin, preserveLightingConfig);
        
        // 創建舞台和視圖
        createExampleStage(api, player, "preserved-lighting-stage");
        
        player.sendMessage("§a已創建使用光照保留功能的舞台（修復亮度問題）");
    }
    
    /**
     * 創建示例舞台的輔助方法
     */
    private void createExampleStage(BlocketAPI api, Player player, String stageName) {
        World world = player.getWorld();
        Location playerLoc = player.getLocation();
        
        // 定義一個小區域用於測試
        BlocketPosition pos1 = BlocketPosition.fromLocation(playerLoc);
        BlocketPosition pos2 = new BlocketPosition(
            pos1.getX() + 10, 
            pos1.getY() + 5, 
            pos1.getZ() + 10
        );
        
        // 創建觀眾
        Audience audience = Audience.fromPlayers(Set.of(player));
        
        // 創建舞台
        Stage stage = new Stage(stageName, world, pos1, pos2, audience);
        api.getStageManager().createStage(stage);
        
        // 創建包含不同方塊的視圖來測試光照效果
        Map<BlockData, Double> testBlocks = new HashMap<>();
        testBlocks.put(Material.GLOWSTONE.createBlockData(), 30.0);      // 發光方塊
        testBlocks.put(Material.STONE.createBlockData(), 50.0);          // 普通方塊
        testBlocks.put(Material.GLASS.createBlockData(), 20.0);          // 透明方塊
        
        dev.twme.blocket.models.Pattern pattern = new dev.twme.blocket.models.Pattern(testBlocks);
        View testView = new View("lighting-test", stage, pattern, false);
        stage.addView(testView);
        
        // 添加方塊到視圖
        Set<BlocketPosition> testArea = dev.twme.blocket.utils.BlockUtils.getBlocksBetween(pos1, pos2);
        testView.addBlocks(testArea);
        
        // 發送方塊給玩家
        stage.sendBlocksToAudience();
    }
    
    /**
     * 比較測試：同時創建兩個舞台來比較光照效果
     */
    public void createComparisonTest(Player player) {
        player.sendMessage("§6正在創建光照比較測試...");
        
        // 在玩家左側創建預設光照舞台
        Location leftLoc = player.getLocation().clone().add(-20, 0, 0);
        createStageAtLocation(player, leftLoc, false, "comparison-default");
        
        // 在玩家右側創建保留光照舞台
        Location rightLoc = player.getLocation().clone().add(20, 0, 0);
        createStageAtLocation(player, rightLoc, true, "comparison-preserved");
        
        player.sendMessage("§a比較測試已創建！");
        player.sendMessage("§e左側：預設光照處理（可能有亮度差異）");
        player.sendMessage("§a右側：保留原始光照（亮度一致）");
    }
    
    /**
     * 在指定位置創建測試舞台
     */
    private void createStageAtLocation(Player player, Location location, boolean preserveLighting, String stageName) {
        BlocketConfig config = BlocketConfig.builder()
            .autoInitialize(true)
            .enablePacketListeners(true)
            .preserveOriginalLighting(preserveLighting)
            .build();
            
        BlocketAPI api = BlocketAPI.initialize(plugin, config);
        
        BlocketPosition pos1 = BlocketPosition.fromLocation(location);
        BlocketPosition pos2 = new BlocketPosition(pos1.getX() + 5, pos1.getY() + 3, pos1.getZ() + 5);
        
        Audience audience = Audience.fromPlayers(Set.of(player));
        Stage stage = new Stage(stageName, player.getWorld(), pos1, pos2, audience);
        api.getStageManager().createStage(stage);
        
        // 創建混合光照環境的測試方塊
        Map<BlockData, Double> mixedBlocks = new HashMap<>();
        mixedBlocks.put(Material.TORCH.createBlockData(), 10.0);         // 光源
        mixedBlocks.put(Material.COBBLESTONE.createBlockData(), 60.0);   // 普通方塊
        mixedBlocks.put(Material.OBSIDIAN.createBlockData(), 30.0);      // 深色方塊
        
        dev.twme.blocket.models.Pattern pattern = new dev.twme.blocket.models.Pattern(mixedBlocks);
        View testView = new View("mixed-lighting-test", stage, pattern, false);
        stage.addView(testView);
        
        Set<BlocketPosition> area = dev.twme.blocket.utils.BlockUtils.getBlocksBetween(pos1, pos2);
        testView.addBlocks(area);
        stage.sendBlocksToAudience();
    }
    
    /**
     * 獲取配置建議的方法
     */
    public static String getLightingConfigurationAdvice() {
        return """
            §6=== Blocket 光照配置建議 ===
            
            §e1. 向後兼容性（預設）：
            §7   .preserveOriginalLighting(false)
            §7   - 使用空光照，客戶端重新計算
            §7   - 可能導致虛擬方塊亮度與原始方塊不同
            
            §a2. 光照保留（推薦）：
            §7   .preserveOriginalLighting(true)
            §7   - 保留原始區塊光照數據
            §7   - 維持一致的方塊亮度
            §7   - 解決光照不一致問題
            
            §c3. 性能考量：
            §7   - 保留光照會略微增加處理時間
            §7   - 但能提供更好的視覺體驗
            §7   - 建議在生產環境中啟用
            """;
    }
}