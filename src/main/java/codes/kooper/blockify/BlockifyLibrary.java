package codes.kooper.blockify;

import codes.kooper.blockify.managers.BlockChangeManager;
import codes.kooper.blockify.managers.StageManager;
import lombok.Getter;

/**
 * Main entry point for the Blockify library.
 * This class provides the core functionality without requiring a Bukkit plugin context.
 */
@Getter
public final class BlockifyLibrary {
    private static BlockifyLibrary instance;
    
    private final StageManager stageManager;
    private final BlockChangeManager blockChangeManager;
    private final BlockifyLogger logger;
    private final BlockifyServer server;
    private final BlockifyTaskScheduler taskScheduler;
    private boolean initialized;

    /**
     * Creates a new BlockifyLibrary instance with the specified logger, server, and task scheduler.
     * 
     * @param logger The logger implementation to use
     * @param server The server implementation to use
     * @param taskScheduler The task scheduler implementation to use
     */
    public BlockifyLibrary(BlockifyLogger logger, BlockifyServer server, BlockifyTaskScheduler taskScheduler) {
        if (instance != null) {
            throw new IllegalStateException("BlockifyLibrary is already initialized");
        }
        
        this.logger = logger;
        this.server = server;
        this.taskScheduler = taskScheduler;
        this.stageManager = new StageManager();
        this.blockChangeManager = new BlockChangeManager();
        this.initialized = false;
        
        instance = this;
    }

    /**
     * Initializes the library. Must be called before using any functionality.
     */
    public void initialize() {
        if (initialized) {
            throw new IllegalStateException("BlockifyLibrary is already initialized");
        }
        
        logger.info("Blockify library has been initialized!");
        this.initialized = true;
    }

    /**
     * Shuts down the library and cleans up resources.
     */
    public void shutdown() {
        if (!initialized) {
            return;
        }
        
        blockChangeManager.shutdown();
        logger.info("Blockify library has been shut down!");
        this.initialized = false;
    }

    /**
     * Gets the current library instance.
     * 
     * @return The BlockifyLibrary instance
     * @throws IllegalStateException if the library has not been initialized
     */
    public static BlockifyLibrary getInstance() {
        if (instance == null) {
            throw new IllegalStateException("BlockifyLibrary has not been initialized. Create a new instance first.");
        }
        return instance;
    }
    
    /**
     * Checks if the library has been initialized.
     * 
     * @return true if initialized, false otherwise
     */
    public boolean isInitialized() {
        return initialized;
    }
}