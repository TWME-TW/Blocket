package codes.kooper.blockify.plugin;

import codes.kooper.blockify.BlockifyLogger;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Bukkit implementation of the BlockifyLogger interface.
 */
public class BukkitBlockifyLogger implements BlockifyLogger {
    private final Logger bukkitLogger;
    
    public BukkitBlockifyLogger(Plugin plugin) {
        this.bukkitLogger = plugin.getLogger();
    }
    
    @Override
    public void info(String message) {
        bukkitLogger.info(message);
    }
    
    @Override
    public void warning(String message) {
        bukkitLogger.warning(message);
    }
    
    @Override
    public void error(String message) {
        bukkitLogger.severe(message);
    }
    
    @Override
    public void debug(String message) {
        bukkitLogger.fine(message);
    }
}