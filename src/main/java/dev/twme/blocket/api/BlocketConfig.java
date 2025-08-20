package dev.twme.blocket.api;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration class for BlocketAPI initialization
 */
@Getter
@Setter
public class BlocketConfig {
    private boolean autoInitialize = true;
    private boolean enableStageBoundListener = true;
    private boolean enablePacketListeners = true;
    private int defaultChunksPerTick = 1;
    
    /**
     * Private constructor - use builder methods
     */
    private BlocketConfig() {}
    
    /**
     * Create default configuration
     * 
     * @return Default configuration
     */
    public static BlocketConfig defaultConfig() {
        return new BlocketConfig();
    }
    
    /**
     * Create configuration builder
     * 
     * @return Configuration builder
     */
    public static BlocketConfig builder() {
        return new BlocketConfig();
    }
    
    /**
     * Enable or disable auto initialization of listeners
     * 
     * @param enable true to enable auto initialization
     * @return this configuration for chaining
     */
    public BlocketConfig autoInitialize(boolean enable) {
        this.autoInitialize = enable;
        return this;
    }
    
    /**
     * Enable or disable stage bound listener
     * This listener handles player movement events for stage boundaries
     * 
     * @param enable true to enable
     * @return this configuration for chaining
     */
    public BlocketConfig enableStageBoundListener(boolean enable) {
        this.enableStageBoundListener = enable;
        return this;
    }
    
    /**
     * Enable or disable packet listeners
     * These listeners handle block interactions and chunk loading
     * 
     * @param enable true to enable
     * @return this configuration for chaining
     */
    public BlocketConfig enablePacketListeners(boolean enable) {
        this.enablePacketListeners = enable;
        return this;
    }
    
    /**
     * Set default chunks per tick for all stages
     * 
     * @param chunksPerTick number of chunks to process per tick
     * @return this configuration for chaining
     */
    public BlocketConfig defaultChunksPerTick(int chunksPerTick) {
        if (chunksPerTick <= 0) {
            throw new IllegalArgumentException("Chunks per tick must be positive");
        }
        this.defaultChunksPerTick = chunksPerTick;
        return this;
    }
    
    /**
     * Build the final configuration
     * 
     * @return this configuration
     */
    public BlocketConfig build() {
        return this;
    }
}
