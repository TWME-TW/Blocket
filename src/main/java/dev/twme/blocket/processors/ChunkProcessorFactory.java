package dev.twme.blocket.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.chunk.LightData;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v_1_18.Chunk_v1_18;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;

import dev.twme.blocket.constants.ChunkConstants;
import dev.twme.blocket.exceptions.ChunkProcessingException;
import dev.twme.blocket.types.BlocketChunk;
import dev.twme.blocket.types.BlocketPosition;

/**
 * 區塊處理工廠類
 * 負責協調各個處理器，創建完整的區塊數據包
 * 
 * <p>主要功能包括：
 * <ul>
 *   <li>協調ChunkDataProcessor和LightDataProcessor</li>
 *   <li>創建完整的區塊Column對象</li>
 *   <li>處理生物群系數據</li>
 *   <li>提供高級的區塊處理接口</li>
 * </ul>
 * 
 * <p>使用建造者模式來配置處理選項，支援：
 * <ul>
 *   <li>選擇光照處理模式（完整光照或空光照）</li>
 *   <li>配置快取策略</li>
 *   <li>自定義生物群系設置</li>
 * </ul>
 * 
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
public class ChunkProcessorFactory {
    
    private final ChunkDataProcessor chunkDataProcessor;
    private final LightDataProcessor lightDataProcessor;
    
    /**
     * 建構子
     */
    public ChunkProcessorFactory() {
        this.chunkDataProcessor = new ChunkDataProcessor();
        this.lightDataProcessor = new LightDataProcessor();
    }
    
    /**
     * 建構子，允許指定快取大小
     * 
     * @param cacheSize 方塊狀態快取大小
     */
    public ChunkProcessorFactory(int cacheSize) {
        this.chunkDataProcessor = new ChunkDataProcessor(cacheSize);
        this.lightDataProcessor = new LightDataProcessor();
    }
    
    /**
     * 創建完整的區塊Column對象
     * 
     * @param player 玩家
     * @param chunk 區塊
     * @param customBlockData 自定義方塊數據
     * @param options 處理選項
     * @return 完整的Column對象
     * @throws ChunkProcessingException 當處理過程中發生錯誤時拋出
     */
    public Column createChunkColumn(
            Player player,
            BlocketChunk chunk,
            Map<BlocketPosition, BlockData> customBlockData,
            ChunkProcessingOptions options) throws ChunkProcessingException {
        
        validateInputs(player, chunk, options);
        
        try {
            // 獲取基本信息
            User packetUser = options.getPacketUser();
            int ySections = packetUser.getTotalWorldHeight() >> ChunkConstants.SECTION_SHIFT;
            Chunk bukkitChunk = player.getWorld().getChunkAt(chunk.x(), chunk.z());
            ChunkSnapshot chunkSnapshot = bukkitChunk.getChunkSnapshot();
            int maxHeight = player.getWorld().getMaxHeight();
            int minHeight = player.getWorld().getMinHeight();
            
            // 提取預設方塊數據
            BlockData[][][][] defaultBlockData = chunkDataProcessor.extractDefaultBlockData(
                chunkSnapshot, ySections, minHeight, maxHeight);
            
            // 創建區塊段列表
            List<BaseChunk> chunks = createChunkSections(
                defaultBlockData, customBlockData, chunk, ySections, minHeight, maxHeight, options);
            
            // 創建光照數據
            LightData lightData = createLightData(chunkSnapshot, ySections, minHeight, maxHeight, options);
            
            // 創建並返回Column
            return new Column(chunk.x(), chunk.z(), true, chunks.toArray(new BaseChunk[0]), null);
            
        } catch (Exception e) {
            throw new ChunkProcessingException("創建區塊Column時發生錯誤", e);
        }
    }
    
    /**
     * 創建區塊段列表
     */
    private List<BaseChunk> createChunkSections(
            BlockData[][][][] defaultBlockData,
            Map<BlocketPosition, BlockData> customBlockData,
            BlocketChunk chunk,
            int ySections,
            int minHeight,
            int maxHeight,
            ChunkProcessingOptions options) throws ChunkProcessingException {
        
        List<BaseChunk> chunks = new ArrayList<>(ySections);
        
        for (int section = 0; section < ySections; section++) {
            Chunk_v1_18 baseChunk = createChunkSection(
                defaultBlockData, customBlockData, chunk, section, minHeight, maxHeight, options);
            chunks.add(baseChunk);
        }
        
        return chunks;
    }
    
    /**
     * 創建單個區塊段
     */
    private Chunk_v1_18 createChunkSection(
            BlockData[][][][] defaultBlockData,
            Map<BlocketPosition, BlockData> customBlockData,
            BlocketChunk chunk,
            int section,
            int minHeight,
            int maxHeight,
            ChunkProcessingOptions options) throws ChunkProcessingException {
        
        Chunk_v1_18 baseChunk = new Chunk_v1_18();
        long baseY = (section << ChunkConstants.SECTION_SHIFT) + minHeight;
        
        // 填充方塊數據
        for (int x = 0; x < ChunkConstants.CHUNK_WIDTH; x++) {
            for (int y = 0; y < ChunkConstants.CHUNK_SECTION_HEIGHT; y++) {
                long worldY = baseY + y;
                if (isValidWorldY(worldY, minHeight, maxHeight)) {
                    for (int z = 0; z < ChunkConstants.CHUNK_DEPTH; z++) {
                        processBlockAtPosition(baseChunk, defaultBlockData, customBlockData, 
                                             chunk, section, x, y, z, minHeight);
                    }
                }
            }
        }
        
        // 設置生物群系數據
        setBiomeData(baseChunk, options.getBiomeId());
        
        return baseChunk;
    }
    
    /**
     * 處理指定位置的方塊
     */
    private void processBlockAtPosition(
            Chunk_v1_18 baseChunk,
            BlockData[][][][] defaultBlockData,
            Map<BlocketPosition, BlockData> customBlockData,
            BlocketChunk chunk,
            int section,
            int x, int y, int z,
            int minHeight) {
        
        // 計算世界座標
        BlocketPosition position = new BlocketPosition(
            x + (chunk.x() << ChunkConstants.SECTION_SHIFT),
            (section << ChunkConstants.SECTION_SHIFT) + y + minHeight,
            z + (chunk.z() << ChunkConstants.SECTION_SHIFT)
        );
        
        // 獲取方塊數據
        BlockData blockData = chunkDataProcessor.getBlockDataAtPosition(
            position, chunk, customBlockData, defaultBlockData, section, minHeight);
        
        if (blockData != null) {
            // 轉換為WrappedBlockState並設置到區塊中
            WrappedBlockState state = chunkDataProcessor.getWrappedBlockState(blockData);
            baseChunk.set(x, y, z, state);
        }
    }
    
    /**
     * 設置生物群系數據
     */
    private void setBiomeData(Chunk_v1_18 baseChunk, int biomeId) {
        int actualBiomeId = baseChunk.getBiomeData().palette.stateToId(biomeId);
        int storageSize = baseChunk.getBiomeData().storage.getData().length;
        
        for (int index = 0; index < storageSize; index++) {
            baseChunk.getBiomeData().storage.set(index, actualBiomeId);
        }
    }
    
    /**
     * 創建光照數據
     */
    private LightData createLightData(
            ChunkSnapshot chunkSnapshot,
            int ySections,
            int minHeight,
            int maxHeight,
            ChunkProcessingOptions options) throws ChunkProcessingException {
        
        if (options.isUseEmptyLighting()) {
            return lightDataProcessor.createEmptyLightData(ySections);
        } else {
            return lightDataProcessor.createLightData(chunkSnapshot, ySections, minHeight, maxHeight);
        }
    }
    
    /**
     * 檢查世界Y座標是否有效
     */
    private boolean isValidWorldY(long worldY, int minHeight, int maxHeight) {
        return worldY >= minHeight && worldY < maxHeight;
    }
    
    /**
     * 驗證輸入參數
     */
    private void validateInputs(Player player, BlocketChunk chunk, ChunkProcessingOptions options) 
            throws ChunkProcessingException {
        if (player == null) {
            throw new ChunkProcessingException("玩家不能為null");
        }
        if (chunk == null) {
            throw new ChunkProcessingException("區塊不能為null");
        }
        if (options == null) {
            throw new ChunkProcessingException("處理選項不能為null");
        }
        if (options.getPacketUser() == null) {
            throw new ChunkProcessingException("PacketUser不能為null");
        }
    }
    
    /**
     * 清除所有快取
     */
    public void clearCaches() {
        chunkDataProcessor.clearCache();
    }
    
    /**
     * 獲取快取統計信息
     * 
     * @return 快取大小
     */
    public int getCacheSize() {
        return chunkDataProcessor.getCacheSize();
    }
    
    /**
     * 區塊處理選項類
     * 使用建造者模式來配置處理選項
     */
    public static class ChunkProcessingOptions {
        private User packetUser;
        private boolean useEmptyLighting = false;
        private int biomeId = ChunkConstants.DEFAULT_BIOME_ID;
        
        public ChunkProcessingOptions(User packetUser) {
            this.packetUser = packetUser;
        }
        
        public ChunkProcessingOptions useEmptyLighting(boolean useEmptyLighting) {
            this.useEmptyLighting = useEmptyLighting;
            return this;
        }
        
        public ChunkProcessingOptions biomeId(int biomeId) {
            this.biomeId = biomeId;
            return this;
        }
        
        public User getPacketUser() {
            return packetUser;
        }
        
        public boolean isUseEmptyLighting() {
            return useEmptyLighting;
        }
        
        public int getBiomeId() {
            return biomeId;
        }
    }
}