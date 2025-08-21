package dev.twme.blocket.constants;

/**
 * 區塊處理相關的常數定義
 * 將魔術數字提取為有意義的常數，提高代碼可讀性和維護性
 * 
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ChunkConstants {
    
    // 區塊尺寸相關常數
    /** 區塊寬度（X軸方向的方塊數量） */
    public static final int CHUNK_WIDTH = 16;
    
    /** 區塊深度（Z軸方向的方塊數量） */
    public static final int CHUNK_DEPTH = 16;
    
    /** 區塊段高度（每個區塊段的方塊數量） */
    public static final int CHUNK_SECTION_HEIGHT = 16;
    
    /** 位移操作常數：用於計算區塊段索引 {@code (section << 4)} */
    public static final int SECTION_SHIFT = 4;
    
    // 光照數據相關常數
    /** 光照數據陣列大小：每個區塊段需要2048字節存儲光照數據 */
    public static final int LIGHT_DATA_SIZE = 2048;
    
    /** 光照值的最大值（4位，0-15） */
    public static final int MAX_LIGHT_LEVEL = 15;
    
    /** 光照數據的位掩碼：用於提取低4位 */
    public static final int LIGHT_MASK_LOW = 0x0F;
    
    /** 光照數據的位掩碼：用於提取高4位 */
    public static final int LIGHT_MASK_HIGH = 0xF0;
    
    /** 光照數據的位移量：用於處理高4位 */
    public static final int LIGHT_SHIFT = 4;
    
    // 位運算相關常數
    /** 用於計算光照數據索引的位移常數 */
    public static final int LIGHT_INDEX_Y_SHIFT = 8;
    
    /** 用於計算光照數據索引的位移常數 */
    public static final int LIGHT_INDEX_Z_SHIFT = 4;
    
    /** 用於計算字節索引的位移常數 */
    public static final int BYTE_INDEX_SHIFT = 1;
    
    /** 用於計算半字節索引的位掩碼 */
    public static final int NIBBLE_INDEX_MASK = 1;
    
    // 生物群系相關常數
    /** 預設生物群系ID */
    public static final int DEFAULT_BIOME_ID = 1;
    
    // 私有建構子，防止實例化
    private ChunkConstants() {
        throw new UnsupportedOperationException("常數類不應被實例化");
    }
}