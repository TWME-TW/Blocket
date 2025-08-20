package dev.twme.blocket.api;

/**
 * Configuration class for BlocketAPI initialization
 * This class implements the Builder pattern for creating immutable configuration objects.
 */
public class BlocketConfig {
    private final boolean autoInitialize;
    private final boolean enableStageBoundListener;
    private final boolean enablePacketListeners;
    private final int defaultChunksPerTick;
    private final int blockCacheSize;
    
    /**
     * Private constructor - use builder methods
     * Creates an immutable configuration object
     *
     * @param builder The builder containing the configuration values
     */
    private BlocketConfig(Builder builder) {
        this.autoInitialize = builder.autoInitialize;
        this.enableStageBoundListener = builder.enableStageBoundListener;
        this.enablePacketListeners = builder.enablePacketListeners;
        this.defaultChunksPerTick = builder.defaultChunksPerTick;
        this.blockCacheSize = builder.blockCacheSize;
    }
    
    /**
     * Create default configuration
     *
     * @return Default configuration
     */
    public static BlocketConfig defaultConfig() {
        return new Builder().build();
    }
    
    /**
     * Create configuration builder
     *
     * @return Configuration builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Get auto initialization setting
     *
     * @return true if auto initialization is enabled
     */
    public boolean isAutoInitialize() {
        return autoInitialize;
    }
    
    /**
     * Get stage bound listener setting
     *
     * @return true if stage bound listener is enabled
     */
    public boolean isEnableStageBoundListener() {
        return enableStageBoundListener;
    }
    
    /**
     * Get packet listeners setting
     *
     * @return true if packet listeners are enabled
     */
    public boolean isEnablePacketListeners() {
        return enablePacketListeners;
    }
    
    /**
     * Get default chunks per tick setting
     *
     * @return number of chunks to process per tick
     */
    public int getDefaultChunksPerTick() {
        return defaultChunksPerTick;
    }
    
    /**
     * Get block cache size setting
     *
     * @return size of block cache
     */
    public int getBlockCacheSize() {
        return blockCacheSize;
    }
    
    /**
     * Builder class for creating BlocketConfig instances
     * This class implements the Builder pattern for creating immutable configuration objects.
     */
    public static class Builder {
        private boolean autoInitialize = true;
        private boolean enableStageBoundListener = true;
        private boolean enablePacketListeners = true;
        private int defaultChunksPerTick = 1;
        private int blockCacheSize = 1000;
        
        /**
         * Private constructor - use BlocketConfig.builder() to get an instance
         */
        private Builder() {}
        
        /**
         * Enable or disable auto initialization of listeners
         *
         * @param enable true to enable auto initialization
         * @return this builder for chaining
         */
        public Builder autoInitialize(boolean enable) {
            this.autoInitialize = enable;
            return this;
        }
        
        /**
         * Enable or disable stage bound listener
         * This listener handles player movement events for stage boundaries
         *
         * @param enable true to enable
         * @return this builder for chaining
         */
        public Builder enableStageBoundListener(boolean enable) {
            this.enableStageBoundListener = enable;
            return this;
        }
        
        /**
         * Enable or disable packet listeners
         * These listeners handle block interactions and chunk loading
         *
         * @param enable true to enable
         * @return this builder for chaining
         */
        public Builder enablePacketListeners(boolean enable) {
            this.enablePacketListeners = enable;
            return this;
        }
        
        /**
         * Set default chunks per tick for all stages
         *
         * @param chunksPerTick number of chunks to process per tick
         * @return this builder for chaining
         * @throws IllegalArgumentException if chunksPerTick is not positive
         */
        public Builder defaultChunksPerTick(int chunksPerTick) {
            if (chunksPerTick <= 0) {
                throw new IllegalArgumentException("Chunks per tick must be positive");
            }
            this.defaultChunksPerTick = chunksPerTick;
            return this;
        }
        
        /**
         * Set block cache size
         *
         * @param cacheSize size of block cache
         * @return this builder for chaining
         * @throws IllegalArgumentException if cacheSize is not positive
         */
        public Builder blockCacheSize(int cacheSize) {
            if (cacheSize <= 0) {
                throw new IllegalArgumentException("Cache size must be positive");
            }
            this.blockCacheSize = cacheSize;
            return this;
        }
        
        /**
         * Build the final configuration
         * This method creates an immutable BlocketConfig instance with the current settings
         * and validates all configuration parameters.
         *
         * @return immutable BlocketConfig instance
         * @throws IllegalArgumentException if any configuration parameter is invalid
         */
        public BlocketConfig build() {
            // Validate configuration parameters
            validateConfig();
            
            // Create immutable configuration object
            return new BlocketConfig(this);
        }
        
        /**
         * Validate all configuration parameters
         *
         * @throws IllegalArgumentException if any configuration parameter is invalid
         */
        private void validateConfig() {
            if (defaultChunksPerTick <= 0) {
                throw new IllegalArgumentException("Chunks per tick must be positive");
            }
            if (blockCacheSize <= 0) {
                throw new IllegalArgumentException("Cache size must be positive");
            }
        }
    }
}
