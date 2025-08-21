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
    private final double instantBreakSpeedMultiplier;
    private final long stageCacheExpirationMinutes;
    private final int maxObjectPoolSize;
    private final boolean preserveOriginalLighting;
    
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
        this.instantBreakSpeedMultiplier = builder.instantBreakSpeedMultiplier;
        this.stageCacheExpirationMinutes = builder.stageCacheExpirationMinutes;
        this.maxObjectPoolSize = builder.maxObjectPoolSize;
        this.preserveOriginalLighting = builder.preserveOriginalLighting;
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
     * Get instant break speed multiplier setting
     * This multiplier determines when a block can be instantly broken.
     * If break speed is at least (hardness * multiplier), it's an instant break.
     *
     * @return instant break speed multiplier
     */
    public double getInstantBreakSpeedMultiplier() {
        return instantBreakSpeedMultiplier;
    }
    
    /**
     * Get stage cache expiration time in minutes
     * This determines how long stage information is cached for players.
     *
     * @return stage cache expiration time in minutes
     */
    public long getStageCacheExpirationMinutes() {
        return stageCacheExpirationMinutes;
    }
    
    /**
     * Get maximum object pool size
     * This determines the maximum number of objects that can be pooled.
     *
     * @return maximum object pool size
     */
    public int getMaxObjectPoolSize() {
        return maxObjectPoolSize;
    }
    
    /**
     * Get preserve original lighting setting
     * When enabled, the system will attempt to preserve the original chunk lighting
     * instead of forcing empty lighting that requires client-side recalculation.
     *
     * @return true if original lighting should be preserved
     */
    public boolean isPreserveOriginalLighting() {
        return preserveOriginalLighting;
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
        private double instantBreakSpeedMultiplier = 30.0;
        private long stageCacheExpirationMinutes = 5; // Optimization: Increased from 1 minute to 5 minutes to reduce cache misses
        private int maxObjectPoolSize = 100; // Optimization: Increased object pool size to improve reuse rate
        private boolean preserveOriginalLighting = true;
        
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
         * Set instant break speed multiplier
         * This multiplier determines when a block can be instantly broken.
         * If break speed is at least (hardness * multiplier), it's an instant break.
         *
         * @param multiplier instant break speed multiplier
         * @return this builder for chaining
         * @throws IllegalArgumentException if multiplier is not positive
         */
        public Builder instantBreakSpeedMultiplier(double multiplier) {
            if (multiplier <= 0) {
                throw new IllegalArgumentException("Instant break speed multiplier must be positive");
            }
            this.instantBreakSpeedMultiplier = multiplier;
            return this;
        }
        
        /**
         * Set stage cache expiration time in minutes
         * This determines how long stage information is cached for players.
         *
         * @param minutes stage cache expiration time in minutes
         * @return this builder for chaining
         * @throws IllegalArgumentException if minutes is not positive
         */
        public Builder stageCacheExpirationMinutes(long minutes) {
            if (minutes <= 0) {
                throw new IllegalArgumentException("Stage cache expiration minutes must be positive");
            }
            this.stageCacheExpirationMinutes = minutes;
            return this;
        }
        
        /**
         * Set maximum object pool size
         * This determines the maximum number of objects that can be pooled.
         *
         * @param size maximum object pool size
         * @return this builder for chaining
         * @throws IllegalArgumentException if size is not positive
         */
        public Builder maxObjectPoolSize(int size) {
            if (size <= 0) {
                throw new IllegalArgumentException("Max object pool size must be positive");
            }
            this.maxObjectPoolSize = size;
            return this;
        }
        
        /**
         * Set preserve original lighting setting
         * When enabled, the system will attempt to preserve the original chunk lighting
         * instead of forcing empty lighting that requires client-side recalculation.
         * This can help maintain consistent block brightness across different views.
         *
         * @param preserve true to preserve original lighting
         * @return this builder for chaining
         */
        public Builder preserveOriginalLighting(boolean preserve) {
            this.preserveOriginalLighting = preserve;
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
            if (instantBreakSpeedMultiplier <= 0) {
                throw new IllegalArgumentException("Instant break speed multiplier must be positive");
            }
            if (stageCacheExpirationMinutes <= 0) {
                throw new IllegalArgumentException("Stage cache expiration minutes must be positive");
            }
            if (maxObjectPoolSize <= 0) {
                throw new IllegalArgumentException("Max object pool size must be positive");
            }
            // preserveOriginalLighting does not require validation as it is a boolean value
        }
    }
}
