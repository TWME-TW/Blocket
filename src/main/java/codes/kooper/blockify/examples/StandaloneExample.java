package codes.kooper.blockify.examples;

import codes.kooper.blockify.BlockifyLibrary;
import codes.kooper.blockify.BlockifyLogger;
import codes.kooper.blockify.BlockifyServer;
import codes.kooper.blockify.BlockifyTaskScheduler;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Example usage of Blockify as a standalone library.
 * This shows how to use the library without the Bukkit plugin.
 */
public class StandaloneExample {
    
    public static void main(String[] args) {
        // Create simple implementations for standalone use
        BlockifyLogger logger = new SimpleLogger();
        BlockifyServer server = new SimpleServer();
        BlockifyTaskScheduler taskScheduler = new SimpleTaskScheduler();
        
        // Initialize the library
        BlockifyLibrary library = new BlockifyLibrary(logger, server, taskScheduler);
        library.initialize();
        
        // Now you can use the library
        // library.getStageManager().createStage(...);
        // library.getBlockChangeManager().applyBlockChange(...);
        
        // Don't forget to shutdown when done
        library.shutdown();
    }
    
    /**
     * Simple logger implementation for standalone use
     */
    static class SimpleLogger implements BlockifyLogger {
        @Override
        public void info(String message) {
            System.out.println("[INFO] " + message);
        }
        
        @Override
        public void warning(String message) {
            System.out.println("[WARN] " + message);
        }
        
        @Override
        public void error(String message) {
            System.err.println("[ERROR] " + message);
        }
        
        @Override
        public void debug(String message) {
            System.out.println("[DEBUG] " + message);
        }
    }
    
    /**
     * Simple server implementation for standalone use
     */
    static class SimpleServer implements BlockifyServer {
        @Override
        public Player getPlayer(UUID playerId) {
            // In standalone mode, you might return null or mock players
            return null;
        }
        
        @Override
        public boolean isRunning() {
            return true;
        }
    }
    
    /**
     * Simple task scheduler for standalone use
     */
    static class SimpleTaskScheduler implements BlockifyTaskScheduler {
        @Override
        public void runTask(Runnable task) {
            // Run immediately in standalone mode
            task.run();
        }
        
        @Override
        public int scheduleRepeatingTask(Runnable task, long delay, long period) {
            // In standalone mode, you might use a Timer or ScheduledExecutorService
            // For simplicity, this example just runs the task once
            task.run();
            return 1;
        }
        
        @Override
        public void cancelTask(int taskId) {
            // No-op in this simple implementation
        }
    }
}