package codes.kooper.blockify.plugin;

import codes.kooper.blockify.BlockifyServer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Bukkit implementation of the BlockifyServer interface.
 */
public class BukkitBlockifyServer implements BlockifyServer {
    private final Server server;
    
    public BukkitBlockifyServer(Server server) {
        this.server = server;
    }
    
    @Override
    public Player getPlayer(UUID playerId) {
        return server.getPlayer(playerId);
    }
    
    @Override
    public boolean isRunning() {
        return server != null;
    }
}