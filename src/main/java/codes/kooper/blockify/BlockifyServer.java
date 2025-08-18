package codes.kooper.blockify;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Interface for server operations needed by the Blockify library.
 * This allows the library to work with different server implementations.
 */
public interface BlockifyServer {
    
    /**
     * Get a player by their UUID.
     * 
     * @param playerId The player's UUID
     * @return The player, or null if not found or offline
     */
    Player getPlayer(UUID playerId);
    
    /**
     * Check if the server is running (optional operation).
     * 
     * @return true if the server is running
     */
    default boolean isRunning() {
        return true;
    }
}