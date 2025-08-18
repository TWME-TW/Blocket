package codes.kooper.blockify;

import java.util.UUID;

/**
 * Interface for task scheduling operations needed by the Blockify library.
 * This allows the library to work with different task scheduling implementations.
 */
public interface BlockifyTaskScheduler {
    
    /**
     * Run a task synchronously.
     * 
     * @param task The task to run
     */
    void runTask(Runnable task);
    
    /**
     * Schedule a repeating task.
     * 
     * @param task The task to run
     * @param delay Initial delay in ticks
     * @param period Period between executions in ticks  
     * @return A task ID that can be used to cancel the task
     */
    int scheduleRepeatingTask(Runnable task, long delay, long period);
    
    /**
     * Cancel a scheduled task.
     * 
     * @param taskId The task ID to cancel
     */
    void cancelTask(int taskId);
}