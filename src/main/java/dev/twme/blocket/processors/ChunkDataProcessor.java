package dev.twme.blocket.processors;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChunkSnapshot;
import org.bukkit.block.data.BlockData;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;

import dev.twme.blocket.constants.ChunkConstants;
import dev.twme.blocket.exceptions.ChunkProcessingException;
import dev.twme.blocket.types.BlocketChunk;
import dev.twme.blocket.types.BlocketPosition;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;

/**
 * 區塊數據處理器
 * 負責處理區塊的方塊數據提取、轉換和快取
 * 
 * <p>主要功能包括：
 * <ul>
 *   <li>從ChunkSnapshot提取預設方塊數據</li>
 *   <li>處理自定義方塊數據覆蓋</li>
 *   <li>BlockData到WrappedBlockState的轉換和快取</li>
 *   <li>邊界檢查和錯誤處理</li>
 * </ul>
 * 
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
public class ChunkDataProcessor {
    
    // 快取BlockData到WrappedBlockState的轉換，避免重複轉換
    private final Map<BlockData, WrappedBlockState> blockStateCache;
    
    /**
     * 建構子
     */
    public ChunkDataProcessor() {
        this.blockStateCache = new HashMap<>();
    }
    
    /**
     * 建構子，允許指定初始快取大小
     * 
     * @param initialCacheSize 初始快取大小
     */
    public ChunkDataProcessor(int initialCacheSize) {
        this.blockStateCache = new HashMap<>(initialCacheSize);
    }
    
    /**
     * 從區塊快照提取預設方塊數據
     * 
     * @param chunkSnapshot 區塊快照
     * @param ySections Y軸區塊段數量
     * @param minHeight 世界最小高度
     * @param maxHeight 世界最大高度
     * @return 四維陣列 [section][x][y][z] 的方塊數據
     * @throws ChunkProcessingException 當處理過程中發生錯誤時拋出
     */
    public BlockData[][][][] extractDefaultBlockData(
            ChunkSnapshot chunkSnapshot, 
            int ySections, 
            int minHeight, 
            int maxHeight) throws ChunkProcessingException {
        
        validateInputParameters(chunkSnapshot, ySections, minHeight, maxHeight);
        
        BlockData[][][][] defaultBlockData = new BlockData[ySections][ChunkConstants.CHUNK_WIDTH][ChunkConstants.CHUNK_SECTION_HEIGHT][ChunkConstants.CHUNK_DEPTH];
        
        try {
            for (int section = 0; section < ySections; section++) {
                extractSectionBlockData(chunkSnapshot, defaultBlockData, section, minHeight, maxHeight);
            }
        } catch (Exception e) {
            throw new ChunkProcessingException("提取預設方塊數據時發生錯誤", e);
        }
        
        return defaultBlockData;
    }
    
    /**
     * 提取單個區塊段的方塊數據
     */
    private void extractSectionBlockData(
            ChunkSnapshot chunkSnapshot,
            BlockData[][][][] defaultBlockData,
            int section,
            int minHeight,
            int maxHeight) {
        
        int baseY = (section << ChunkConstants.SECTION_SHIFT) + minHeight;
        
        for (int x = 0; x < ChunkConstants.CHUNK_WIDTH; x++) {
            for (int y = 0; y < ChunkConstants.CHUNK_SECTION_HEIGHT; y++) {
                int worldY = baseY + y;
                if (isValidWorldY(worldY, minHeight, maxHeight)) {
                    for (int z = 0; z < ChunkConstants.CHUNK_DEPTH; z++) {
                        defaultBlockData[section][x][y][z] = chunkSnapshot.getBlockData(x, worldY, z);
                    }
                }
            }
        }
    }
    
    /**
     * 獲取指定位置的方塊數據
     * 優先使用自定義方塊數據，如果沒有則使用預設數據
     * 
     * @param position 方塊位置
     * @param chunk 區塊
     * @param customBlockData 自定義方塊數據映射
     * @param defaultBlockData 預設方塊數據陣列
     * @param section 區塊段索引
     * @param minHeight 世界最小高度
     * @return 方塊數據
     */
    public BlockData getBlockDataAtPosition(
            BlocketPosition position,
            BlocketChunk chunk,
            Map<BlocketPosition, BlockData> customBlockData,
            BlockData[][][][] defaultBlockData,
            int section,
            int minHeight) {
        
        // 優先使用自定義方塊數據
        if (customBlockData != null) {
            BlockData customData = customBlockData.get(position);
            if (customData != null) {
                return customData;
            }
        }
        
        // 計算在區塊內的相對座標
        int localX = position.getX() - (chunk.x() << ChunkConstants.SECTION_SHIFT);
        int localY = position.getY() - ((section << ChunkConstants.SECTION_SHIFT) + minHeight);
        int localZ = position.getZ() - (chunk.z() << ChunkConstants.SECTION_SHIFT);
        
        // 邊界檢查
        if (isValidLocalCoordinate(localX, localY, localZ)) {
            return defaultBlockData[section][localX][localY][localZ];
        }
        
        return null;
    }
    
    /**
     * 將BlockData轉換為WrappedBlockState，使用快取避免重複轉換
     * 
     * @param blockData 要轉換的方塊數據
     * @return 轉換後的WrappedBlockState
     */
    public WrappedBlockState getWrappedBlockState(BlockData blockData) {
        return blockStateCache.computeIfAbsent(blockData, SpigotConversionUtil::fromBukkitBlockData);
    }
    
    /**
     * 清除快取
     */
    public void clearCache() {
        blockStateCache.clear();
    }
    
    /**
     * 獲取快取大小
     * 
     * @return 當前快取中的項目數量
     */
    public int getCacheSize() {
        return blockStateCache.size();
    }
    
    /**
     * 驗證輸入參數
     */
    private void validateInputParameters(ChunkSnapshot chunkSnapshot, int ySections, int minHeight, int maxHeight) 
            throws ChunkProcessingException {
        if (chunkSnapshot == null) {
            throw new ChunkProcessingException("區塊快照不能為null");
        }
        if (ySections <= 0) {
            throw new ChunkProcessingException("Y軸區塊段數量必須大於0，實際值：" + ySections);
        }
        if (minHeight >= maxHeight) {
            throw new ChunkProcessingException("最小高度必須小於最大高度，最小高度：" + minHeight + "，最大高度：" + maxHeight);
        }
    }
    
    /**
     * 檢查世界Y座標是否有效
     */
    private boolean isValidWorldY(int worldY, int minHeight, int maxHeight) {
        return worldY >= minHeight && worldY < maxHeight;
    }
    
    /**
     * 檢查區塊內相對座標是否有效
     */
    private boolean isValidLocalCoordinate(int localX, int localY, int localZ) {
        return localX >= 0 && localX < ChunkConstants.CHUNK_WIDTH &&
               localY >= 0 && localY < ChunkConstants.CHUNK_SECTION_HEIGHT &&
               localZ >= 0 && localZ < ChunkConstants.CHUNK_DEPTH;
    }
}