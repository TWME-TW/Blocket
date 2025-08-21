package dev.twme.blocket.processors;

import java.util.Map;

import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import com.github.retrooper.packetevents.protocol.player.User;

import dev.twme.blocket.types.BlocketChunk;
import dev.twme.blocket.types.BlocketPosition;

/**
 * Chunk processing context
 * Encapsulates all context information needed during chunk processing
 *
 * <p>This class acts as a Data Transfer Object (DTO), containing:
 * <ul>
 *   <li>Player information</li>
 *   <li>Chunk information</li>
 *   <li>PacketEvents user object</li>
 *   <li>Custom block data</li>
 *   <li>Processing flags</li>
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
     * Constructor
     *
     * @param player Player
     * @param chunk Chunk
     * @param packetUser PacketEvents user object
     * @param customBlockData Custom block data map
     * @param unload Whether it is an unload operation
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
     * Get player
     *
     * @return Player object
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Get chunk
     *
     * @return Chunk object
     */
    public BlocketChunk getChunk() {
        return chunk;
    }
    
    /**
     * Get PacketEvents user object
     *
     * @return PacketEvents user object
     */
    public User getPacketUser() {
        return packetUser;
    }
    
    /**
     * Get custom block data
     *
     * @return Custom block data map, may be null
     */
    public Map<BlocketPosition, BlockData> getCustomBlockData() {
        return customBlockData;
    }
    
    /**
     * Whether it is an unload operation
     *
     * @return true if it is an unload operation, false otherwise
     */
    public boolean isUnload() {
        return unload;
    }
    
    /**
     * Check if there is custom block data
     *
     * @return true if there is custom block data, false otherwise
     */
    public boolean hasCustomBlockData() {
        return customBlockData != null && !customBlockData.isEmpty();
    }
    
    /**
     * Get the number of Y-axis chunk sections
     *
     * @return Number of Y-axis chunk sections
     */
    public int getYSections() {
        return packetUser.getTotalWorldHeight() >> 4;
    }
    
    /**
     * Get world maximum height
     *
     * @return World maximum height
     */
    public int getMaxHeight() {
        return player.getWorld().getMaxHeight();
    }
    
    /**
     * Get world minimum height
     *
     * @return World minimum height
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