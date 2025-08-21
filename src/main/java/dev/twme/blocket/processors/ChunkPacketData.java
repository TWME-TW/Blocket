package dev.twme.blocket.processors;

import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.chunk.LightData;

/**
 * 區塊數據包數據
 * 封裝發送區塊數據包所需的所有數據
 * 
 * <p>這個類作為數據傳輸對象(DTO)，包含：
 * <ul>
 *   <li>區塊Column對象</li>
 *   <li>光照數據LightData對象</li>
 * </ul>
 * 
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
public class ChunkPacketData {
    
    private final Column column;
    private final LightData lightData;
    
    /**
     * 建構子
     * 
     * @param column 區塊Column對象
     * @param lightData 光照數據對象
     */
    public ChunkPacketData(Column column, LightData lightData) {
        this.column = column;
        this.lightData = lightData;
    }
    
    /**
     * 獲取區塊Column對象
     * 
     * @return 區塊Column對象
     */
    public Column getColumn() {
        return column;
    }
    
    /**
     * 獲取光照數據對象
     * 
     * @return 光照數據對象
     */
    public LightData getLightData() {
        return lightData;
    }
    
    /**
     * 檢查數據是否有效
     * 
     * @return true如果Column和LightData都不為null，false否則
     */
    public boolean isValid() {
        return column != null && lightData != null;
    }
    
    /**
     * 獲取區塊座標X
     * 
     * @return 區塊X座標，如果Column為null則返回0
     */
    public int getChunkX() {
        return column != null ? column.getX() : 0;
    }
    
    /**
     * 獲取區塊座標Z
     * 
     * @return 區塊Z座標，如果Column為null則返回0
     */
    public int getChunkZ() {
        return column != null ? column.getZ() : 0;
    }
    
    @Override
    public String toString() {
        return String.format(
            "ChunkPacketData{chunk=(%d,%d), valid=%s}",
            getChunkX(), getChunkZ(), isValid()
        );
    }
}