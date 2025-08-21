package dev.twme.blocket.processors;

import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.chunk.LightData;

/**
 * Chunk data packet
 * Encapsulates all data needed to send a chunk data packet
 *
 * <p>This class acts as a Data Transfer Object (DTO), containing:
 * <ul>
 *   <li>Chunk Column object</li>
 *   <li>Light data LightData object</li>
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
     * Constructor
     *
     * @param column Chunk Column object
     * @param lightData Light data object
     */
    public ChunkPacketData(Column column, LightData lightData) {
        this.column = column;
        this.lightData = lightData;
    }
    
    /**
     * Get chunk Column object
     *
     * @return Chunk Column object
     */
    public Column getColumn() {
        return column;
    }
    
    /**
     * Get light data object
     *
     * @return Light data object
     */
    public LightData getLightData() {
        return lightData;
    }
    
    /**
     * Check if data is valid
     *
     * @return true if both Column and LightData are not null, false otherwise
     */
    public boolean isValid() {
        return column != null && lightData != null;
    }
    
    /**
     * Get chunk coordinate X
     *
     * @return Chunk X coordinate, returns 0 if Column is null
     */
    public int getChunkX() {
        return column != null ? column.getX() : 0;
    }
    
    /**
     * Get chunk coordinate Z
     *
     * @return Chunk Z coordinate, returns 0 if Column is null
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