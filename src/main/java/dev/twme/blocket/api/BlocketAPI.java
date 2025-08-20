package dev.twme.blocket.api;

import org.bukkit.plugin.Plugin;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

import dev.twme.blocket.listeners.StageBoundListener;
import dev.twme.blocket.managers.BlockChangeManager;
import dev.twme.blocket.managers.StageManager;
import dev.twme.blocket.protocol.BlockDigAdapter;
import dev.twme.blocket.protocol.BlockPlaceAdapter;
import dev.twme.blocket.protocol.ChunkLoadAdapter;
import lombok.Getter;

/**
 * Main API class for Blocket Library
 * 
 * Usage:
 * BlocketAPI api = BlocketAPI.initialize(plugin);
 * StageManager stageManager = api.getStageManager();
 */
@Getter
public class BlocketAPI {
    private static BlocketAPI instance;
    
    private final Plugin ownerPlugin;
    private final StageManager stageManager;
    private final BlockChangeManager blockChangeManager;
    private final ServerVersion serverVersion;
    private final BlocketConfig config;
    
    // Protocol listeners
    private BlockDigAdapter blockDigAdapter;
    private BlockPlaceAdapter blockPlaceAdapter;
    private ChunkLoadAdapter chunkLoadAdapter;
    private StageBoundListener stageBoundListener;
    
    /**
     * Private constructor - use initialize() methods
     */
    private BlocketAPI(Plugin plugin, BlocketConfig config) {
        this.ownerPlugin = plugin;
        this.config = config;
        this.serverVersion = PacketEvents.getAPI().getServerManager().getVersion();
        
        // Initialize managers
        this.stageManager = new StageManager(this);
        this.blockChangeManager = new BlockChangeManager(this);
        
        // Auto-initialize if configured
        if (config.isAutoInitialize()) {
            initializeListeners();
        }
        
        plugin.getLogger().info("Blocket API initialized for plugin: " + plugin.getName());
    }
    
    /**
     * Initialize BlocketAPI with default configuration
     * 
     * @param plugin The plugin that will own this BlocketAPI instance
     * @return BlocketAPI instance
     * @throws IllegalStateException if already initialized
     */
    public static BlocketAPI initialize(Plugin plugin) {
        return initialize(plugin, BlocketConfig.defaultConfig());
    }
    
    /**
     * Initialize BlocketAPI with custom configuration
     * 
     * @param plugin The plugin that will own this BlocketAPI instance
     * @param config Custom configuration
     * @return BlocketAPI instance
     * @throws IllegalStateException if already initialized
     */
    public static BlocketAPI initialize(Plugin plugin, BlocketConfig config) {
        if (instance != null) {
            throw new IllegalStateException("BlocketAPI is already initialized by plugin: " + 
                instance.ownerPlugin.getName());
        }
        
        instance = new BlocketAPI(plugin, config);
        return instance;
    }
    
    /**
     * Get the current BlocketAPI instance
     * 
     * @return BlocketAPI instance
     * @throws IllegalStateException if not initialized
     */
    public static BlocketAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("BlocketAPI is not initialized! Call initialize() first.");
        }
        return instance;
    }
    
    /**
     * Check if BlocketAPI is initialized
     * 
     * @return true if initialized
     */
    public static boolean isInitialized() {
        return instance != null;
    }
    
    /**
     * Initialize all listeners (can be called manually if autoInitialize is false)
     */
    public void initializeListeners() {
        if (config.isEnablePacketListeners()) {
            // Initialize packet listeners
            blockDigAdapter = new BlockDigAdapter();
            blockPlaceAdapter = new BlockPlaceAdapter();
            chunkLoadAdapter = new ChunkLoadAdapter();
            
            PacketEvents.getAPI().getEventManager().registerListeners(
                blockDigAdapter, blockPlaceAdapter, chunkLoadAdapter
            );
        }
        
        if (config.isEnableStageBoundListener()) {
            // Initialize Bukkit event listener
            stageBoundListener = new StageBoundListener();
            ownerPlugin.getServer().getPluginManager().registerEvents(stageBoundListener, ownerPlugin);
        }
    }
    
    /**
     * Shutdown and cleanup all resources
     * This should be called in the plugin's onDisable() method
     */
    public void shutdown() {
        ownerPlugin.getLogger().info("Shutting down Blocket API...");
        
        // Shutdown managers
        if (blockChangeManager != null) {
            blockChangeManager.shutdown();
        }
        
        // Unregister packet listeners
        if (config.isEnablePacketListeners()) {
            if (blockDigAdapter != null) {
                PacketEvents.getAPI().getEventManager().unregisterListener(blockDigAdapter);
            }
            if (blockPlaceAdapter != null) {
                PacketEvents.getAPI().getEventManager().unregisterListener(blockPlaceAdapter);
            }
            if (chunkLoadAdapter != null) {
                PacketEvents.getAPI().getEventManager().unregisterListener(chunkLoadAdapter);
            }
        }
        
        // Clear static instance
        instance = null;
        
        ownerPlugin.getLogger().info("Blocket API shutdown complete.");
    }
    
    /**
     * Get the plugin that owns this BlocketAPI instance
     * 
     * @return Owner plugin
     */
    public Plugin getOwnerPlugin() {
        return ownerPlugin;
    }
    
    /**
     * Get configuration used to initialize this API
     * 
     * @return Configuration object
     */
    public BlocketConfig getConfig() {
        return config;
    }
}
