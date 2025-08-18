package codes.kooper.blockify.plugin;

import codes.kooper.blockify.BlockifyTaskScheduler;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Bukkit implementation of the BlockifyTaskScheduler interface.
 */
public class BukkitBlockifyTaskScheduler implements BlockifyTaskScheduler {
    private final Plugin plugin;
    private final BukkitScheduler scheduler;
    private final ConcurrentHashMap<Integer, BukkitTask> tasks = new ConcurrentHashMap<>();
    private int nextTaskId = 1;
    
    public BukkitBlockifyTaskScheduler(Plugin plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
    }
    
    @Override
    public void runTask(Runnable task) {
        scheduler.runTask(plugin, task);
    }
    
    @Override
    public int scheduleRepeatingTask(Runnable task, long delay, long period) {
        BukkitTask bukkitTask = scheduler.runTaskTimer(plugin, task, delay, period);
        int taskId = nextTaskId++;
        tasks.put(taskId, bukkitTask);
        return taskId;
    }
    
    @Override
    public void cancelTask(int taskId) {
        BukkitTask task = tasks.remove(taskId);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }
}