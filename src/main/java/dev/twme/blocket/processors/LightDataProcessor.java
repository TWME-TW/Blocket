package dev.twme.blocket.processors;

import java.util.BitSet;

import org.bukkit.ChunkSnapshot;

import com.github.retrooper.packetevents.protocol.world.chunk.LightData;

import dev.twme.blocket.constants.ChunkConstants;
import dev.twme.blocket.exceptions.ChunkProcessingException;

/**
 * Processor for handling chunk lighting data in Blocket.
 * Responsible for extracting, packing, and creating LightData objects for chunk sections.
 */
public class LightDataProcessor {
    
    /**
     * Create a complete LightData object
     *
     * @param chunkSnapshot Chunk snapshot
     * @param ySections Number of Y-axis chunk sections
     * @param minHeight Minimum world height
     * @param maxHeight Maximum world height
     * @return Complete LightData object
     * @throws ChunkProcessingException Thrown when an error occurs during processing
     */
    public LightData createLightData(
            ChunkSnapshot chunkSnapshot,
            int ySections,
            int minHeight,
            int maxHeight) throws ChunkProcessingException {
        
        validateInputParameters(chunkSnapshot, ySections, minHeight, maxHeight);
        
        try {
            // Create light data arrays
            byte[][] blockLightArray = createLightArray(ySections);
            byte[][] skyLightArray = createLightArray(ySections);
            
            // Populate light data
            populateLightData(chunkSnapshot, blockLightArray, skyLightArray, ySections, minHeight, maxHeight);
            
            // Create and configure LightData object
            return buildLightData(blockLightArray, skyLightArray, ySections);
            
        } catch (Exception e) {
            throw new ChunkProcessingException("Error occurred while creating light data", e);
        }
    }
    
    /**
     * Create an empty LightData object (let the client calculate lighting)
     *
     * @param ySections Number of Y-axis chunk sections
     * @return Empty LightData object
     * @throws ChunkProcessingException Thrown when an error occurs during processing
     */
    public LightData createEmptyLightData(int ySections) throws ChunkProcessingException {
        if (ySections <= 0) {
            throw new ChunkProcessingException("Number of Y-axis chunk sections must be greater than 0, actual value: " + ySections);
        }
        
        try {
            // Create empty light data arrays
            byte[][] emptyLightArray = createEmptyLightArray(ySections);
            
            LightData lightData = new LightData();
            lightData.setBlockLightArray(emptyLightArray);
            lightData.setSkyLightArray(emptyLightArray);
            lightData.setBlockLightCount(ySections);
            lightData.setSkyLightCount(ySections);
            
            // Set empty masks (let the client handle lighting)
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
            throw new ChunkProcessingException("Error occurred while creating empty light data", e);
        }
    }
    
    /**
     * Create merged LightData object
     * Retain original chunk lighting data while handling custom block lighting effects
     *
     * @param chunkSnapshot Chunk snapshot
     * @param customBlockPositions Set of custom block positions (can be null)
     * @param ySections Number of Y-axis chunk sections
     * @param minHeight Minimum world height
     * @param maxHeight Maximum world height
     * @return Merged LightData object
     * @throws ChunkProcessingException Thrown when an error occurs during processing
     */
    public LightData createMergedLightData(
            ChunkSnapshot chunkSnapshot,
            java.util.Set<dev.twme.blocket.types.BlocketPosition> customBlockPositions,
            int ySections,
            int minHeight,
            int maxHeight) throws ChunkProcessingException {
        
        validateInputParameters(chunkSnapshot, ySections, minHeight, maxHeight);
        
        try {
            // First create complete light data based on the original chunk
            LightData baseLightData = createLightData(chunkSnapshot, ySections, minHeight, maxHeight);
            
            // If there are no custom blocks, return the original lighting
            if (customBlockPositions == null || customBlockPositions.isEmpty()) {
                return baseLightData;
            }
            
            // Create modified light data arrays
            byte[][] modifiedBlockLightArray = cloneLightArray(baseLightData.getBlockLightArray());
            byte[][] modifiedSkyLightArray = cloneLightArray(baseLightData.getSkyLightArray());
            
            // Handle the impact of custom blocks on lighting
            processCustomBlockLighting(customBlockPositions, modifiedBlockLightArray, modifiedSkyLightArray,
                                     ySections, minHeight, maxHeight);
            
            // Create and return modified light data
            return buildLightData(modifiedBlockLightArray, modifiedSkyLightArray, ySections);
            
        } catch (Exception e) {
            throw new ChunkProcessingException("Error occurred while creating merged light data", e);
        }
    }
    
    /**
     * Clone light data arrays
     */
    private byte[][] cloneLightArray(byte[][] original) {
        if (original == null) return null;
        
        byte[][] cloned = new byte[original.length][];
        for (int i = 0; i < original.length; i++) {
            if (original[i] != null) {
                cloned[i] = original[i].clone();
            }
        }
        return cloned;
    }
    
    /**
     * Handle the impact of custom blocks on lighting
     * This method can be further extended to handle lighting effects of specific block types
     */
    private void processCustomBlockLighting(
            java.util.Set<dev.twme.blocket.types.BlocketPosition> customBlockPositions,
            byte[][] blockLightArray,
            byte[][] skyLightArray,
            int ySections,
            int minHeight,
            int maxHeight) {
        
        // Current implementation keeps the original lighting unchanged
        // Future logic for handling specific block types can be added here
        // For example: increase light value for glowing blocks, reduce light propagation for opaque blocks, etc.
        
        for (dev.twme.blocket.types.BlocketPosition pos : customBlockPositions) {
            int worldY = pos.getY();
            
            // Check if Y coordinate is within valid range
            if (worldY < minHeight || worldY >= maxHeight) {
                continue;
            }
            
            // Calculate chunk section index
            int section = (worldY - minHeight) >> ChunkConstants.SECTION_SHIFT;
            if (section < 0 || section >= ySections) {
                continue;
            }
            
            // Calculate local coordinates within the chunk
            int localX = pos.getX() & 15;
            int localY = worldY & 15;
            int localZ = pos.getZ() & 15;
            
            // Calculate light index
            int lightIndex = calculateLightIndex(localX, localY, localZ);
            int byteIndex = lightIndex >> ChunkConstants.BYTE_INDEX_SHIFT;
            int nibbleIndex = lightIndex & ChunkConstants.NIBBLE_INDEX_MASK;
            
            // Adjust lighting based on custom block type
            // Currently keeps the original light value unchanged
            // Future additions can include:
            // - Glowing blocks: increase block light value
            // - Opaque blocks: reduce sky light value
            // - Transparent blocks: maintain light propagation
        }
    }
    
    /**
     * Create light data arrays
     */
    private byte[][] createLightArray(int ySections) {
        byte[][] lightArray = new byte[ySections][];
        for (int i = 0; i < ySections; i++) {
            lightArray[i] = new byte[ChunkConstants.LIGHT_DATA_SIZE];
        }
        return lightArray;
    }
    
    /**
     * Create empty light data arrays
     */
    private byte[][] createEmptyLightArray(int ySections) {
        byte[][] emptyLightArray = new byte[ySections][];
        for (int i = 0; i < ySections; i++) {
            emptyLightArray[i] = new byte[ChunkConstants.LIGHT_DATA_SIZE]; // All zeros
        }
        return emptyLightArray;
    }
    
    /**
     * Populate light data
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
     * Populate light data for a single chunk section
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
     * Process light data at a specific position
     */
    private void processLightAtPosition(
            ChunkSnapshot chunkSnapshot,
            byte[] blockLightData,
            byte[] skyLightData,
            int x, int y, int z, int worldY) {
        
        // Get light values
        int blockLight = chunkSnapshot.getBlockEmittedLight(x, worldY, z);
        int skyLight = chunkSnapshot.getBlockSkyLight(x, worldY, z);
        
        // Ensure light values are within valid range
        blockLight = Math.max(0, Math.min(ChunkConstants.MAX_LIGHT_LEVEL, blockLight));
        skyLight = Math.max(0, Math.min(ChunkConstants.MAX_LIGHT_LEVEL, skyLight));
        
        // Calculate light data index
        int index = calculateLightIndex(x, y, z);
        int byteIndex = index >> ChunkConstants.BYTE_INDEX_SHIFT;
        int nibbleIndex = index & ChunkConstants.NIBBLE_INDEX_MASK;
        
        // Pack light data
        packLightValue(blockLightData, byteIndex, nibbleIndex, blockLight);
        packLightValue(skyLightData, byteIndex, nibbleIndex, skyLight);
    }
    
    /**
     * Calculate light data index
     */
    private int calculateLightIndex(int x, int y, int z) {
        return y << ChunkConstants.LIGHT_INDEX_Y_SHIFT | 
               z << ChunkConstants.LIGHT_INDEX_Z_SHIFT | 
               x;
    }
    
    /**
     * Pack light value into byte array
     */
    private void packLightValue(byte[] lightData, int byteIndex, int nibbleIndex, int lightValue) {
        if (byteIndex >= 0 && byteIndex < lightData.length) {
            if (nibbleIndex == 0) {
                // Lower 4 bits
                lightData[byteIndex] = (byte) ((lightData[byteIndex] & ChunkConstants.LIGHT_MASK_HIGH) | 
                                             (lightValue & ChunkConstants.LIGHT_MASK_LOW));
            } else {
                // Upper 4 bits
                lightData[byteIndex] = (byte) ((lightData[byteIndex] & ChunkConstants.LIGHT_MASK_LOW) | 
                                             ((lightValue & ChunkConstants.LIGHT_MASK_LOW) << ChunkConstants.LIGHT_SHIFT));
            }
        }
    }
    
    /**
     * Build complete LightData object
     */
    private LightData buildLightData(byte[][] blockLightArray, byte[][] skyLightArray, int ySections) {
        LightData lightData = new LightData();
        lightData.setBlockLightArray(blockLightArray);
        lightData.setSkyLightArray(skyLightArray);
        lightData.setBlockLightCount(ySections);
        lightData.setSkyLightCount(ySections);
        
        // Set light mask (all sections have light data)
        BitSet lightMask = createFullBitSet(ySections);
        lightData.setBlockLightMask(lightMask);
        lightData.setSkyLightMask(lightMask);
        
        // Set empty section mask (no empty sections)
        BitSet emptyMask = new BitSet(ySections);
        lightData.setEmptyBlockLightMask(emptyMask);
        lightData.setEmptySkyLightMask(emptyMask);
        
        return lightData;
    }
    
    /**
     * Create a full BitSet
     */
    private BitSet createFullBitSet(int size) {
        BitSet bitSet = new BitSet(size);
        for (int i = 0; i < size; i++) {
            bitSet.set(i);
        }
        return bitSet;
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
            throw new ChunkProcessingException("Number of Y-axis chunk sections must be greater than 0, actual value: " + ySections);
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
}