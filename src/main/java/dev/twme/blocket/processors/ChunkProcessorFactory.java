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
 * Chunk processing factory class
 * Responsible for coordinating various processors to create complete chunk data packets
 *
 * <p>Main functions include:
 * <ul>
 *   <li>Coordinating ChunkDataProcessor and LightDataProcessor</li>
 *   <li>Creating complete chunk Column objects</li>
 *   <li>Handling biome data</li>
 *   <li>Providing advanced chunk processing interfaces</li>
 * </ul>
 *
 * <p>Uses builder pattern to configure processing options, supporting:
 * <ul>
 *   <li>Choosing lighting processing mode (full lighting or empty lighting)</li>
 *   <li>Configuring cache strategy</li>
 *   <li>Custom biome settings</li>
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
     * Constructor
     */
    public ChunkProcessorFactory() {
        this.chunkDataProcessor = new ChunkDataProcessor();
        this.lightDataProcessor = new LightDataProcessor();
    }
    
    /**
     * Constructor, allows specifying cache size
     *
     * @param cacheSize Block state cache size
     */
    public ChunkProcessorFactory(int cacheSize) {
        this.chunkDataProcessor = new ChunkDataProcessor(cacheSize);
        this.lightDataProcessor = new LightDataProcessor();
    }
    
    /**
     * Create a complete chunk Column object
     *
     * @param player Player
     * @param chunk Chunk
     * @param customBlockData Custom block data
     * @param options Processing options
     * @return Complete Column object
     * @throws ChunkProcessingException Thrown when an error occurs during processing
     */
    public Column createChunkColumn(
            Player player,
            BlocketChunk chunk,
            Map<BlocketPosition, BlockData> customBlockData,
            ChunkProcessingOptions options) throws ChunkProcessingException {
        
        validateInputs(player, chunk, options);
        
        try {
            // Get basic information
            User packetUser = options.getPacketUser();
            int ySections = packetUser.getTotalWorldHeight() >> ChunkConstants.SECTION_SHIFT;
            Chunk bukkitChunk = player.getWorld().getChunkAt(chunk.x(), chunk.z());
            ChunkSnapshot chunkSnapshot = bukkitChunk.getChunkSnapshot();
            int maxHeight = player.getWorld().getMaxHeight();
            int minHeight = player.getWorld().getMinHeight();
            
            // Extract default block data
            BlockData[][][][] defaultBlockData = chunkDataProcessor.extractDefaultBlockData(
                chunkSnapshot, ySections, minHeight, maxHeight);
            
            // Create chunk section list
            List<BaseChunk> chunks = createChunkSections(
                defaultBlockData, customBlockData, chunk, ySections, minHeight, maxHeight, options);
            
            // Create light data
            LightData lightData = createLightData(chunkSnapshot, ySections, minHeight, maxHeight, options);
            
            // Create and return Column
            return new Column(chunk.x(), chunk.z(), true, chunks.toArray(new BaseChunk[0]), null);
            
        } catch (Exception e) {
            throw new ChunkProcessingException("Error occurred while creating chunk Column", e);
        }
    }
    
    /**
     * Create chunk section list
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
     * Create a single chunk section
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
        
        // Fill block data
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
        
        // Set biome data
        setBiomeData(baseChunk, options.getBiomeId());
        
        return baseChunk;
    }
    
    /**
     * Process block at specified position
     */
    private void processBlockAtPosition(
            Chunk_v1_18 baseChunk,
            BlockData[][][][] defaultBlockData,
            Map<BlocketPosition, BlockData> customBlockData,
            BlocketChunk chunk,
            int section,
            int x, int y, int z,
            int minHeight) {
        
        // Calculate world coordinates
        BlocketPosition position = new BlocketPosition(
            x + (chunk.x() << ChunkConstants.SECTION_SHIFT),
            (section << ChunkConstants.SECTION_SHIFT) + y + minHeight,
            z + (chunk.z() << ChunkConstants.SECTION_SHIFT)
        );
        
        // Get block data
        BlockData blockData = chunkDataProcessor.getBlockDataAtPosition(
            position, chunk, customBlockData, defaultBlockData, section, minHeight);
        
        if (blockData != null) {
            // Convert to WrappedBlockState and set to chunk
            WrappedBlockState state = chunkDataProcessor.getWrappedBlockState(blockData);
            baseChunk.set(x, y, z, state);
        }
    }
    
    /**
     * Set biome data
     */
    private void setBiomeData(Chunk_v1_18 baseChunk, int biomeId) {
        int actualBiomeId = baseChunk.getBiomeData().palette.stateToId(biomeId);
        int storageSize = baseChunk.getBiomeData().storage.getData().length;
        
        for (int index = 0; index < storageSize; index++) {
            baseChunk.getBiomeData().storage.set(index, actualBiomeId);
        }
    }
    
    /**
     * Create light data
     */
    private LightData createLightData(
            ChunkSnapshot chunkSnapshot,
            int ySections,
            int minHeight,
            int maxHeight,
            ChunkProcessingOptions options) throws ChunkProcessingException {
        
        if (options.isUseEmptyLighting() && !options.isPreserveOriginalLighting()) {
            return lightDataProcessor.createEmptyLightData(ySections);
        } else {
            return lightDataProcessor.createLightData(chunkSnapshot, ySections, minHeight, maxHeight);
        }
    }
    
    /**
     * Check if world Y coordinate is valid
     */
    private boolean isValidWorldY(long worldY, int minHeight, int maxHeight) {
        return worldY >= minHeight && worldY < maxHeight;
    }
    
    /**
     * Validate input parameters
     */
    private void validateInputs(Player player, BlocketChunk chunk, ChunkProcessingOptions options) 
            throws ChunkProcessingException {
        if (player == null) {
            throw new ChunkProcessingException("Player cannot be null");
        }
        if (chunk == null) {
            throw new ChunkProcessingException("Chunk cannot be null");
        }
        if (options == null) {
            throw new ChunkProcessingException("Processing options cannot be null");
        }
        if (options.getPacketUser() == null) {
            throw new ChunkProcessingException("PacketUser cannot be null");
        }
    }
    
    /**
     * Clear all caches
     */
    public void clearCaches() {
        chunkDataProcessor.clearCache();
    }
    
    /**
     * Get cache statistics
     *
     * @return Cache size
     */
    public int getCacheSize() {
        return chunkDataProcessor.getCacheSize();
    }
    
    /**
     * Chunk processing options class
     * Use builder pattern to configure processing options
     */
    public static class ChunkProcessingOptions {
        private User packetUser;
        private boolean useEmptyLighting = false;
        private boolean preserveOriginalLighting = false;
        private int biomeId = ChunkConstants.DEFAULT_BIOME_ID;
        
        /**
         * Constructs ChunkProcessingOptions with the specified packet user.
         * @param packetUser The user for packet processing
         */
        public ChunkProcessingOptions(User packetUser) {
            this.packetUser = packetUser;
        }

        /**
         * Sets whether to use empty lighting for chunk processing.
         * @param useEmptyLighting true to use empty lighting, false otherwise
         * @return this ChunkProcessingOptions instance
         */
        public ChunkProcessingOptions useEmptyLighting(boolean useEmptyLighting) {
            this.useEmptyLighting = useEmptyLighting;
            return this;
        }

        /**
         * Sets whether to preserve original lighting from the chunk.
         * When enabled, the system will attempt to extract and preserve the original
         * chunk lighting data instead of creating empty lighting.
         *
         * @param preserveOriginalLighting true to preserve original lighting, false otherwise
         * @return this ChunkProcessingOptions instance
         */
        public ChunkProcessingOptions preserveOriginalLighting(boolean preserveOriginalLighting) {
            this.preserveOriginalLighting = preserveOriginalLighting;
            return this;
        }

        /**
         * Sets the biome ID for chunk processing.
         * @param biomeId The biome ID to set
         * @return this ChunkProcessingOptions instance
         */
        public ChunkProcessingOptions biomeId(int biomeId) {
            this.biomeId = biomeId;
            return this;
        }

        /**
         * Gets the packet user for chunk processing.
         * @return The packet user
         */
        public User getPacketUser() {
            return packetUser;
        }

        /**
         * Checks if empty lighting is used for chunk processing.
         * @return true if empty lighting is used, false otherwise
         */
        public boolean isUseEmptyLighting() {
            return useEmptyLighting;
        }

        /**
         * Checks if original lighting should be preserved for chunk processing.
         *
         * @return true if original lighting should be preserved, false otherwise
         */
        public boolean isPreserveOriginalLighting() {
            return preserveOriginalLighting;
        }

        /**
         * Gets the biome ID for chunk processing.
         * @return The biome ID
         */
        public int getBiomeId() {
            return biomeId;
        }
    }
}