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
    private static volatile BlocketAPI instance;
    
    private final Plugin ownerPlugin;
    private final StageManager stageManager;
    private final BlockChangeManager blockChangeManager;
    private final ServerVersion serverVersion;
    private final BlocketConfig config;
    
    // Initialization state tracking
    private volatile boolean listenersInitialized = false;
    private final Object initializationLock = new Object();
    
    // Protocol listeners
    private BlockDigAdapter blockDigAdapter;
    private BlockPlaceAdapter blockPlaceAdapter;
    private ChunkLoadAdapter chunkLoadAdapter;
    private StageBoundListener stageBoundListener;
    
    /**
     * Private constructor - use initialize() methods
     * Phase 1: Core initialization only, no listeners
     */
    private BlocketAPI(Plugin plugin, BlocketConfig config) {
        this.ownerPlugin = plugin;
        this.config = config;
        this.serverVersion = PacketEvents.getAPI().getServerManager().getVersion();
        
        // Initialize managers
        this.stageManager = new StageManager(this);
        this.blockChangeManager = new BlockChangeManager(this);
        
        plugin.getLogger().info("Blocket API core initialized for plugin: " + plugin.getName());
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
     * Implements two-phase initialization to avoid circular dependencies
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
        
        // Double-checked locking pattern for thread-safe singleton initialization
        if (instance == null) {
            synchronized (BlocketAPI.class) {
                if (instance == null) {
                    // Phase 1: Create instance and set static reference
                    instance = new BlocketAPI(plugin, config);
                    
                    // Phase 2: Complete initialization if auto-initialize is enabled
                    if (config.isAutoInitialize()) {
                        instance.completeInitialization();
                    }
                }
            }
        }
        return instance;
    }
    
    /**
     * Get the current BlocketAPI instance
     * 
     * @return BlocketAPI instance
     * @throws IllegalStateException if not initialized
     */
    public static BlocketAPI getInstance() {
        // Double-checked locking pattern for thread-safe singleton access
        if (instance == null) {
            synchronized (BlocketAPI.class) {
                if (instance == null) {
                    throw new IllegalStateException("BlocketAPI is not initialized! Call initialize() first.");
                }
            }
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
     * Complete the initialization process (Phase 2)
     * This method is called after the static instance is set to avoid circular dependencies
     */
    private void completeInitialization() {
        synchronized (initializationLock) {
            if (!listenersInitialized) {
                initializeListeners();
                listenersInitialized = true;
                ownerPlugin.getLogger().info("Blocket API listeners initialized for plugin: " + ownerPlugin.getName());
            }
        }
    }
    
    /**
     * Initialize all listeners (can be called manually if autoInitialize is false)
     */
    public void initializeListeners() {
        synchronized (initializationLock) {
            if (listenersInitialized) {
                ownerPlugin.getLogger().warning("Listeners are already initialized, skipping...");
                return;
            }
            
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
            
            listenersInitialized = true;
        }
    }
    
    /**
     * Shutdown and cleanup all resources
     * This should be called in the plugin's onDisable() method
     * Ensure all resources are properly released when the plugin is disabled, including thread pools and other managers
     */
    public void shutdown() {
        ownerPlugin.getLogger().info("Shutting down Blocket API...");
        
        // Shutdown managers
        // Shutdown BlockChangeManager, releasing its thread pool and related resources
        if (blockChangeManager != null) {
            blockChangeManager.shutdown();
        }
        
        // Unregister packet listeners
        // Unregister all packet listeners
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
