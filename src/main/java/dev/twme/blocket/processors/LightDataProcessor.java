package dev.twme.blocket.processors;

import java.util.BitSet;

import org.bukkit.ChunkSnapshot;

import com.github.retrooper.packetevents.protocol.world.chunk.LightData;

import dev.twme.blocket.constants.ChunkConstants;
import dev.twme.blocket.exceptions.ChunkProcessingException;

/**
 * 光照數據處理器
 * 負責處理區塊的光照數據提取、打包和創建LightData對象
 * 
 * <p>主要功能包括：
 * <ul>
 *   <li>從ChunkSnapshot提取方塊光照和天空光照數據</li>
 *   <li>將光照值打包為半字節格式</li>
 *   <li>創建完整的LightData對象</li>
 *   <li>處理光照遮罩和空區段標記</li>
 * </ul>
 * 
 * <p>光照數據格式說明：
 * 每個光照值佔4位（0-15），兩個光照值打包為一個字節。
 * 索引計算：index = y << 8 | z << 4 | x
 * 字節索引：byteIndex = index >> 1
 * 半字節索引：nibbleIndex = index & 1
 * 
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
public class LightDataProcessor {
    
    /**
     * 創建完整的光照數據對象
     * 
     * @param chunkSnapshot 區塊快照
     * @param ySections Y軸區塊段數量
     * @param minHeight 世界最小高度
     * @param maxHeight 世界最大高度
     * @return 完整的LightData對象
     * @throws ChunkProcessingException 當處理過程中發生錯誤時拋出
     */
    public LightData createLightData(
            ChunkSnapshot chunkSnapshot,
            int ySections,
            int minHeight,
            int maxHeight) throws ChunkProcessingException {
        
        validateInputParameters(chunkSnapshot, ySections, minHeight, maxHeight);
        
        try {
            // 創建光照數據陣列
            byte[][] blockLightArray = createLightArray(ySections);
            byte[][] skyLightArray = createLightArray(ySections);
            
            // 填充光照數據
            populateLightData(chunkSnapshot, blockLightArray, skyLightArray, ySections, minHeight, maxHeight);
            
            // 創建並配置LightData對象
            return buildLightData(blockLightArray, skyLightArray, ySections);
            
        } catch (Exception e) {
            throw new ChunkProcessingException("創建光照數據時發生錯誤", e);
        }
    }
    
    /**
     * 創建空的光照數據對象（讓客戶端自行計算光照）
     * 
     * @param ySections Y軸區塊段數量
     * @return 空的LightData對象
     * @throws ChunkProcessingException 當處理過程中發生錯誤時拋出
     */
    public LightData createEmptyLightData(int ySections) throws ChunkProcessingException {
        if (ySections <= 0) {
            throw new ChunkProcessingException("Y軸區塊段數量必須大於0，實際值：" + ySections);
        }
        
        try {
            // 創建空的光照數據陣列
            byte[][] emptyLightArray = createEmptyLightArray(ySections);
            
            LightData lightData = new LightData();
            lightData.setBlockLightArray(emptyLightArray);
            lightData.setSkyLightArray(emptyLightArray);
            lightData.setBlockLightCount(ySections);
            lightData.setSkyLightCount(ySections);
            
            // 設置空遮罩（讓客戶端處理光照）
            BitSet emptyBitSet = new BitSet(ySections);
            for (int i = 0; i < ySections; i++) {
                emptyBitSet.set(i);
            }
            
            lightData.setBlockLightMask(new BitSet(ySections));
            lightData.setSkyLightMask(new BitSet(ySections));
            lightData.setEmptyBlockLightMask(emptyBitSet);
            lightData.setEmptySkyLightMask(emptyBitSet);
            
            return lightData;
            
        } catch (Exception e) {
            throw new ChunkProcessingException("創建空光照數據時發生錯誤", e);
        }
    }
    
    /**
     * 創建光照數據陣列
     */
    private byte[][] createLightArray(int ySections) {
        byte[][] lightArray = new byte[ySections][];
        for (int i = 0; i < ySections; i++) {
            lightArray[i] = new byte[ChunkConstants.LIGHT_DATA_SIZE];
        }
        return lightArray;
    }
    
    /**
     * 創建空的光照數據陣列
     */
    private byte[][] createEmptyLightArray(int ySections) {
        byte[][] emptyLightArray = new byte[ySections][];
        for (int i = 0; i < ySections; i++) {
            emptyLightArray[i] = new byte[ChunkConstants.LIGHT_DATA_SIZE]; // 全部為0
        }
        return emptyLightArray;
    }
    
    /**
     * 填充光照數據
     */
    private void populateLightData(
            ChunkSnapshot chunkSnapshot,
            byte[][] blockLightArray,
            byte[][] skyLightArray,
            int ySections,
            int minHeight,
            int maxHeight) {
        
        for (int section = 0; section < ySections; section++) {
            populateSectionLightData(chunkSnapshot, blockLightArray[section], skyLightArray[section], 
                                   section, minHeight, maxHeight);
        }
    }
    
    /**
     * 填充單個區塊段的光照數據
     */
    private void populateSectionLightData(
            ChunkSnapshot chunkSnapshot,
            byte[] blockLightData,
            byte[] skyLightData,
            int section,
            int minHeight,
            int maxHeight) {
        
        int baseY = (section << ChunkConstants.SECTION_SHIFT) + minHeight;
        
        for (int x = 0; x < ChunkConstants.CHUNK_WIDTH; x++) {
            for (int y = 0; y < ChunkConstants.CHUNK_SECTION_HEIGHT; y++) {
                int worldY = baseY + y;
                if (isValidWorldY(worldY, minHeight, maxHeight)) {
                    for (int z = 0; z < ChunkConstants.CHUNK_DEPTH; z++) {
                        processLightAtPosition(chunkSnapshot, blockLightData, skyLightData, 
                                             x, y, z, worldY);
                    }
                }
            }
        }
    }
    
    /**
     * 處理指定位置的光照數據
     */
    private void processLightAtPosition(
            ChunkSnapshot chunkSnapshot,
            byte[] blockLightData,
            byte[] skyLightData,
            int x, int y, int z, int worldY) {
        
        // 獲取光照值
        int blockLight = chunkSnapshot.getBlockEmittedLight(x, worldY, z);
        int skyLight = chunkSnapshot.getBlockSkyLight(x, worldY, z);
        
        // 確保光照值在有效範圍內
        blockLight = Math.max(0, Math.min(ChunkConstants.MAX_LIGHT_LEVEL, blockLight));
        skyLight = Math.max(0, Math.min(ChunkConstants.MAX_LIGHT_LEVEL, skyLight));
        
        // 計算光照數據索引
        int index = calculateLightIndex(x, y, z);
        int byteIndex = index >> ChunkConstants.BYTE_INDEX_SHIFT;
        int nibbleIndex = index & ChunkConstants.NIBBLE_INDEX_MASK;
        
        // 打包光照數據
        packLightValue(blockLightData, byteIndex, nibbleIndex, blockLight);
        packLightValue(skyLightData, byteIndex, nibbleIndex, skyLight);
    }
    
    /**
     * 計算光照數據索引
     */
    private int calculateLightIndex(int x, int y, int z) {
        return y << ChunkConstants.LIGHT_INDEX_Y_SHIFT | 
               z << ChunkConstants.LIGHT_INDEX_Z_SHIFT | 
               x;
    }
    
    /**
     * 將光照值打包到字節陣列中
     */
    private void packLightValue(byte[] lightData, int byteIndex, int nibbleIndex, int lightValue) {
        if (byteIndex >= 0 && byteIndex < lightData.length) {
            if (nibbleIndex == 0) {
                // 低4位
                lightData[byteIndex] = (byte) ((lightData[byteIndex] & ChunkConstants.LIGHT_MASK_HIGH) | 
                                             (lightValue & ChunkConstants.LIGHT_MASK_LOW));
            } else {
                // 高4位
                lightData[byteIndex] = (byte) ((lightData[byteIndex] & ChunkConstants.LIGHT_MASK_LOW) | 
                                             ((lightValue & ChunkConstants.LIGHT_MASK_LOW) << ChunkConstants.LIGHT_SHIFT));
            }
        }
    }
    
    /**
     * 構建完整的LightData對象
     */
    private LightData buildLightData(byte[][] blockLightArray, byte[][] skyLightArray, int ySections) {
        LightData lightData = new LightData();
        lightData.setBlockLightArray(blockLightArray);
        lightData.setSkyLightArray(skyLightArray);
        lightData.setBlockLightCount(ySections);
        lightData.setSkyLightCount(ySections);
        
        // 設置光照遮罩（所有區段都有光照數據）
        BitSet lightMask = createFullBitSet(ySections);
        lightData.setBlockLightMask(lightMask);
        lightData.setSkyLightMask(lightMask);
        
        // 設置空區段遮罩（沒有空區段）
        BitSet emptyMask = new BitSet(ySections);
        lightData.setEmptyBlockLightMask(emptyMask);
        lightData.setEmptySkyLightMask(emptyMask);
        
        return lightData;
    }
    
    /**
     * 創建全滿的BitSet
     */
    private BitSet createFullBitSet(int size) {
        BitSet bitSet = new BitSet(size);
        for (int i = 0; i < size; i++) {
            bitSet.set(i);
        }
        return bitSet;
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
}