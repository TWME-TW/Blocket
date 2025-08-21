package dev.twme.blocket.constants;

/**
 * Constants related to chunk processing
 * Extract magic numbers as meaningful constants to improve code readability and maintainability
 *
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ChunkConstants {
    
    // Constants related to chunk dimensions
    /** Chunk width (number of blocks in the X-axis direction) */
    public static final int CHUNK_WIDTH = 16;
    
    /** Chunk depth (number of blocks in the Z-axis direction) */
    public static final int CHUNK_DEPTH = 16;
    
    /** Chunk section height (number of blocks in each chunk section) */
    public static final int CHUNK_SECTION_HEIGHT = 16;
    
    /** Bit shift operation constant: used to calculate chunk section index {@code (section << 4)} */
    public static final int SECTION_SHIFT = 4;
    
    // Constants related to light data
    /** Light data array size: each chunk section requires 2048 bytes to store light data */
    public static final int LIGHT_DATA_SIZE = 2048;
    
    /** Maximum light level (4 bits, 0-15) */
    public static final int MAX_LIGHT_LEVEL = 15;
    
    /** Bit mask for light data: used to extract the lower 4 bits */
    public static final int LIGHT_MASK_LOW = 0x0F;
    
    /** Bit mask for light data: used to extract the upper 4 bits */
    public static final int LIGHT_MASK_HIGH = 0xF0;
    
    /** Bit shift for light data: used to process the upper 4 bits */
    public static final int LIGHT_SHIFT = 4;
    
    // Constants related to bitwise operations
    /** Bit shift constant used to calculate light data index */
    public static final int LIGHT_INDEX_Y_SHIFT = 8;
    
    /** Bit shift constant used to calculate light data index */
    public static final int LIGHT_INDEX_Z_SHIFT = 4;
    
    /** Bit shift constant used to calculate byte index */
    public static final int BYTE_INDEX_SHIFT = 1;
    
    /** Bit mask used to calculate nibble index */
    public static final int NIBBLE_INDEX_MASK = 1;
    
    // Constants related to biomes
    /** Default biome ID */
    public static final int DEFAULT_BIOME_ID = 1;
    
    // Private constructor to prevent instantiation
    private ChunkConstants() {
        throw new UnsupportedOperationException("Constant class should not be instantiated");
    }
}