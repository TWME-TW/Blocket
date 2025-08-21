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
 * Chunk data processor
 * Responsible for extracting, converting, and caching chunk block data
 *
 * <p>Main functions include:
 * <ul>
 *   <li>Extracting default block data from ChunkSnapshot</li>
 *   <li>Handling custom block data overrides</li>
 *   <li>Converting and caching BlockData to WrappedBlockState</li>
 *   <li>Boundary checking and error handling</li>
 * </ul>
 *
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
public class ChunkDataProcessor {
    
    // Cache BlockData to WrappedBlockState conversion to avoid repeated conversions
    private final Map<BlockData, WrappedBlockState> blockStateCache;
    
    /**
     * Constructor
     */
    public ChunkDataProcessor() {
        this.blockStateCache = new HashMap<>();
    }
    
    /**
     * Constructor, allowing specification of initial cache size
     *
     * @param initialCacheSize Initial cache size
     */
    public ChunkDataProcessor(int initialCacheSize) {
        this.blockStateCache = new HashMap<>(initialCacheSize);
    }
    
    /**
     * Extract default block data from chunk snapshot
     *
     * @param chunkSnapshot Chunk snapshot
     * @param ySections Number of chunk sections on Y-axis
     * @param minHeight Minimum world height
     * @param maxHeight Maximum world height
     * @return Four-dimensional array [section][x][y][z] of block data
     * @throws ChunkProcessingException Thrown when an error occurs during processing
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
            throw new ChunkProcessingException("Error occurred while extracting default block data", e);
        }
        
        return defaultBlockData;
    }
    
    /**
     * Extract block data for a single chunk section
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
     * Get block data at the specified position
     * Prioritize using custom block data, if not available then use default data
     *
     * @param position Block position
     * @param chunk Chunk
     * @param customBlockData Custom block data mapping
     * @param defaultBlockData Default block data array
     * @param section Chunk section index
     * @param minHeight Minimum world height
     * @return Block data
     */
    public BlockData getBlockDataAtPosition(
            BlocketPosition position,
            BlocketChunk chunk,
            Map<BlocketPosition, BlockData> customBlockData,
            BlockData[][][][] defaultBlockData,
            int section,
            int minHeight) {
        
        // Prioritize using custom block data
        if (customBlockData != null) {
            BlockData customData = customBlockData.get(position);
            if (customData != null) {
                return customData;
            }
        }
        
        // Calculate the relative coordinates within the chunk
        int localX = position.getX() - (chunk.x() << ChunkConstants.SECTION_SHIFT);
        int localY = position.getY() - ((section << ChunkConstants.SECTION_SHIFT) + minHeight);
        int localZ = position.getZ() - (chunk.z() << ChunkConstants.SECTION_SHIFT);
        
        // Boundary check
        if (isValidLocalCoordinate(localX, localY, localZ)) {
            return defaultBlockData[section][localX][localY][localZ];
        }
        
        return null;
    }
    
    /**
     * Convert BlockData to WrappedBlockState, using cache to avoid repeated conversions
     *
     * @param blockData Block data to convert
     * @return Converted WrappedBlockState
     */
    public WrappedBlockState getWrappedBlockState(BlockData blockData) {
        return blockStateCache.computeIfAbsent(blockData, SpigotConversionUtil::fromBukkitBlockData);
    }
    
    /**
     * Clear cache
     */
    public void clearCache() {
        blockStateCache.clear();
    }
    
    /**
     * Get cache size
     *
     * @return Number of items in the current cache
     */
    public int getCacheSize() {
        return blockStateCache.size();
    }
    
    /**
     * Validate input parameters
     */
    private void validateInputParameters(ChunkSnapshot chunkSnapshot, int ySections, int minHeight, int maxHeight) 
            throws ChunkProcessingException {
        if (chunkSnapshot == null) {
            throw new ChunkProcessingException("Chunk snapshot cannot be null");
        }
        if (ySections <= 0) {
            throw new ChunkProcessingException("Number of chunk sections on Y-axis must be greater than 0, actual value: " + ySections);
        }
        if (minHeight >= maxHeight) {
            throw new ChunkProcessingException("Minimum height must be less than maximum height, minimum height: " + minHeight + ", maximum height: " + maxHeight);
        }
    }
    /**
     * Check if world Y coordinate is valid
     */
    private boolean isValidWorldY(int worldY, int minHeight, int maxHeight) {
        return worldY >= minHeight && worldY < maxHeight;
    }
    
    /**
     * Check if local coordinates within the chunk are valid
     */
    private boolean isValidLocalCoordinate(int localX, int localY, int localZ) {
        return localX >= 0 && localX < ChunkConstants.CHUNK_WIDTH &&
               localY >= 0 && localY < ChunkConstants.CHUNK_SECTION_HEIGHT &&
               localZ >= 0 && localZ < ChunkConstants.CHUNK_DEPTH;
    }
}