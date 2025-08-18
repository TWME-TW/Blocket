package codes.kooper.blockify.plugin;

import codes.kooper.blockify.BlockifyLibrary;
import codes.kooper.blockify.plugin.listeners.StageBoundListener;
import codes.kooper.blockify.plugin.protocol.BlockDigAdapter;
import codes.kooper.blockify.plugin.protocol.BlockPlaceAdapter;
import codes.kooper.blockify.plugin.protocol.ChunkLoadAdapter;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class Blockify extends JavaPlugin {
    private BlockifyLibrary blockifyLibrary;
    private ServerVersion serverVersion;

    @Override
    public void onEnable() {
        serverVersion = PacketEvents.getAPI().getServerManager().getVersion();
        getLogger().info("Blockify plugin has been enabled!");

        // Initialize the library with Bukkit implementations
        BukkitBlockifyLogger logger = new BukkitBlockifyLogger(this);
        BukkitBlockifyServer server = new BukkitBlockifyServer(getServer());
        BukkitBlockifyTaskScheduler taskScheduler = new BukkitBlockifyTaskScheduler(this);
        
        blockifyLibrary = new BlockifyLibrary(logger, server, taskScheduler);
        blockifyLibrary.initialize();

        // Register plugin-specific listeners and adapters
        getServer().getPluginManager().registerEvents(new StageBoundListener(), this);
        PacketEvents.getAPI().getEventManager().registerListeners(new BlockDigAdapter(), new BlockPlaceAdapter(), new ChunkLoadAdapter());
    }

    @Override
    public void onDisable() {
        if (blockifyLibrary != null) {
            blockifyLibrary.shutdown();
        }
        getLogger().info("Blockify plugin has been disabled!");
    }

    public static Blockify getInstance() {
        return Blockify.getPlugin(Blockify.class);
    }
}
