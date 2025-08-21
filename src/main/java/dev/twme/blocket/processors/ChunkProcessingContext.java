package dev.twme.blocket.processors;

import java.util.Map;

import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import com.github.retrooper.packetevents.protocol.player.User;

import dev.twme.blocket.types.BlocketChunk;
import dev.twme.blocket.types.BlocketPosition;

/**
 * 區塊處理上下文
 * 封裝區塊處理過程中需要的所有上下文信息
 * 
 * <p>這個類作為數據傳輸對象(DTO)，包含：
 * <ul>
 *   <li>玩家信息</li>
 *   <li>區塊信息</li>
 *   <li>PacketEvents用戶對象</li>
 *   <li>自定義方塊數據</li>
 *   <li>處理標誌</li>
 * </ul>
 * 
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
public class ChunkProcessingContext {
    
    private final Player player;
    private final BlocketChunk chunk;
    private final User packetUser;
    private final Map<BlocketPosition, BlockData> customBlockData;
    private final boolean unload;
    
    /**
     * 建構子
     * 
     * @param player 玩家
     * @param chunk 區塊
     * @param packetUser PacketEvents用戶對象
     * @param customBlockData 自定義方塊數據映射
     * @param unload 是否為卸載操作
     */
    public ChunkProcessingContext(
            Player player,
            BlocketChunk chunk,
            User packetUser,
            Map<BlocketPosition, BlockData> customBlockData,
            boolean unload) {
        this.player = player;
        this.chunk = chunk;
        this.packetUser = packetUser;
        this.customBlockData = customBlockData;
        this.unload = unload;
    }
    
    /**
     * 獲取玩家
     * 
     * @return 玩家對象
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * 獲取區塊
     * 
     * @return 區塊對象
     */
    public BlocketChunk getChunk() {
        return chunk;
    }
    
    /**
     * 獲取PacketEvents用戶對象
     * 
     * @return PacketEvents用戶對象
     */
    public User getPacketUser() {
        return packetUser;
    }
    
    /**
     * 獲取自定義方塊數據
     * 
     * @return 自定義方塊數據映射，可能為null
     */
    public Map<BlocketPosition, BlockData> getCustomBlockData() {
        return customBlockData;
    }
    
    /**
     * 是否為卸載操作
     * 
     * @return true如果是卸載操作，false否則
     */
    public boolean isUnload() {
        return unload;
    }
    
    /**
     * 檢查是否有自定義方塊數據
     * 
     * @return true如果有自定義方塊數據，false否則
     */
    public boolean hasCustomBlockData() {
        return customBlockData != null && !customBlockData.isEmpty();
    }
    
    /**
     * 獲取Y軸區塊段數量
     * 
     * @return Y軸區塊段數量
     */
    public int getYSections() {
        return packetUser.getTotalWorldHeight() >> 4;
    }
    
    /**
     * 獲取世界最大高度
     * 
     * @return 世界最大高度
     */
    public int getMaxHeight() {
        return player.getWorld().getMaxHeight();
    }
    
    /**
     * 獲取世界最小高度
     * 
     * @return 世界最小高度
     */
    public int getMinHeight() {
        return player.getWorld().getMinHeight();
    }
    
    @Override
    public String toString() {
        return String.format(
            "ChunkProcessingContext{player=%s, chunk=(%d,%d), unload=%s, hasCustomData=%s}",
            player.getName(), chunk.x(), chunk.z(), unload, hasCustomBlockData()
        );
    }
}