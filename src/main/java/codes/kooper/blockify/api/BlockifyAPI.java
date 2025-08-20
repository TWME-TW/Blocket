package codes.kooper.blockify.api;

import org.bukkit.plugin.Plugin;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

import codes.kooper.blockify.listeners.StageBoundListener;
import codes.kooper.blockify.managers.BlockChangeManager;
import codes.kooper.blockify.managers.StageManager;
import codes.kooper.blockify.protocol.BlockDigAdapter;
import codes.kooper.blockify.protocol.BlockPlaceAdapter;
import codes.kooper.blockify.protocol.ChunkLoadAdapter;
import lombok.Getter;

/**
 * Main API class for Blockify Library
 * 
 * Usage:
 * BlockifyAPI api = BlockifyAPI.initialize(plugin);
 * StageManager stageManager = api.getStageManager();
 */
@Getter
public class BlockifyAPI {
    private static BlockifyAPI instance;
    
    private final Plugin ownerPlugin;
    private final StageManager stageManager;
    private final BlockChangeManager blockChangeManager;
    private final ServerVersion serverVersion;
    private final BlockifyConfig config;
    
    // Protocol listeners
    private BlockDigAdapter blockDigAdapter;
    private BlockPlaceAdapter blockPlaceAdapter;
    private ChunkLoadAdapter chunkLoadAdapter;
    private StageBoundListener stageBoundListener;
    
    /**
     * Private constructor - use initialize() methods
     */
    private BlockifyAPI(Plugin plugin, BlockifyConfig config) {
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
        
        plugin.getLogger().info("Blockify API initialized for plugin: " + plugin.getName());
    }
    
    /**
     * Initialize BlockifyAPI with default configuration
     * 
     * @param plugin The plugin that will own this BlockifyAPI instance
     * @return BlockifyAPI instance
     * @throws IllegalStateException if already initialized
     */
    public static BlockifyAPI initialize(Plugin plugin) {
        return initialize(plugin, BlockifyConfig.defaultConfig());
    }
    
    /**
     * Initialize BlockifyAPI with custom configuration
     * 
     * @param plugin The plugin that will own this BlockifyAPI instance
     * @param config Custom configuration
     * @return BlockifyAPI instance
     * @throws IllegalStateException if already initialized
     */
    public static BlockifyAPI initialize(Plugin plugin, BlockifyConfig config) {
        if (instance != null) {
            throw new IllegalStateException("BlockifyAPI is already initialized by plugin: " + 
                instance.ownerPlugin.getName());
        }
        
        instance = new BlockifyAPI(plugin, config);
        return instance;
    }
    
    /**
     * Get the current BlockifyAPI instance
     * 
     * @return BlockifyAPI instance
     * @throws IllegalStateException if not initialized
     */
    public static BlockifyAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("BlockifyAPI is not initialized! Call initialize() first.");
        }
        return instance;
    }
    
    /**
     * Check if BlockifyAPI is initialized
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
        ownerPlugin.getLogger().info("Shutting down Blockify API...");
        
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
        
        ownerPlugin.getLogger().info("Blockify API shutdown complete.");
    }
    
    /**
     * Get the plugin that owns this BlockifyAPI instance
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
    public BlockifyConfig getConfig() {
        return config;
    }
}
