package dev.twme.blocket.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Performance monitor for tracking and reporting operation statistics in Blocket.
 * Provides counters, timing, and reporting for various operations.
 */
public class PerformanceMonitor {
    
    private final ConcurrentHashMap<String, LongAdder> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> totalTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> maxTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> minTimes = new ConcurrentHashMap<>();
    
    /**
     * Increment operation count
     *
     * @param operation Operation name
     */
    public void incrementCounter(String operation) {
        counters.computeIfAbsent(operation, k -> new LongAdder()).increment();
    }
    
    /**
     * Record operation execution time
     *
     * @param operation Operation name
     * @param timeMs Execution time (milliseconds)
     */
    public void recordTime(String operation, long timeMs) {
        incrementCounter(operation);
        totalTimes.computeIfAbsent(operation, k -> new AtomicLong()).addAndGet(timeMs);
        
        // Update maximum time
        maxTimes.computeIfAbsent(operation, k -> new AtomicLong()).updateAndGet(current -> Math.max(current, timeMs));
        
        // Update minimum time
        minTimes.computeIfAbsent(operation, k -> new AtomicLong(Long.MAX_VALUE)).updateAndGet(current -> Math.min(current, timeMs));
    }
    
    /**
     * Get operation count
     *
     * @param operation Operation name
     * @return Operation count
     */
    public long getCount(String operation) {
        LongAdder counter = counters.get(operation);
        return counter != null ? counter.sum() : 0;
    }
    
    /**
     * Get total execution time
     *
     * @param operation Operation name
     * @return Total execution time (milliseconds)
     */
    public long getTotalTime(String operation) {
        AtomicLong totalTime = totalTimes.get(operation);
        return totalTime != null ? totalTime.get() : 0;
    }
    
    /**
     * Get average execution time
     *
     * @param operation Operation name
     * @return Average execution time (milliseconds)
     */
    public double getAverageTime(String operation) {
        long count = getCount(operation);
        if (count == 0) return 0.0;
        
        long totalTime = getTotalTime(operation);
        return (double) totalTime / count;
    }
    
    /**
     * Get maximum execution time
     *
     * @param operation Operation name
     * @return Maximum execution time (milliseconds)
     */
    public long getMaxTime(String operation) {
        AtomicLong maxTime = maxTimes.get(operation);
        return maxTime != null ? maxTime.get() : 0;
    }
    
    /**
     * Get minimum execution time
     *
     * @param operation Operation name
     * @return Minimum execution time (milliseconds)
     */
    public long getMinTime(String operation) {
        AtomicLong minTime = minTimes.get(operation);
        long value = minTime != null ? minTime.get() : 0;
        return value == Long.MAX_VALUE ? 0 : value;
    }
    
    /**
     * Reset statistics for a specific operation
     *
     * @param operation Operation name
     */
    public void reset(String operation) {
        counters.remove(operation);
        totalTimes.remove(operation);
        maxTimes.remove(operation);
        minTimes.remove(operation);
    }
    
    /**
     * Reset all statistics
     */
    public void resetAll() {
        counters.clear();
        totalTimes.clear();
        maxTimes.clear();
        minTimes.clear();
    }
    
    /**
     * Generate performance report
     *
     * @return Performance report string
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Performance Monitoring Report ===\n");
        
        for (String operation : counters.keySet()) {
            long count = getCount(operation);
            double avgTime = getAverageTime(operation);
            long maxTime = getMaxTime(operation);
            long minTime = getMinTime(operation);
            long totalTime = getTotalTime(operation);
            
            report.append(String.format(
                "Operation: %s\n" +
                "  Count: %d\n" +
                "  Total Time: %d ms\n" +
                "  Average Time: %.2f ms\n" +
                "  Maximum Time: %d ms\n" +
                "  Minimum Time: %d ms\n" +
                "---\n",
                operation, count, totalTime, avgTime, maxTime, minTime
            ));
        }
        
        return report.toString();
    }
    
    /**
     * Timer class for automatically measuring execution time
     */
    public static class Timer implements AutoCloseable {
        private final PerformanceMonitor monitor;
        private final String operation;
        private final long startTime;
        
        /**
         * Constructs a Timer for measuring operation duration.
         * @param monitor The PerformanceMonitor instance
         * @param operation The operation name to track
         */
        public Timer(PerformanceMonitor monitor, String operation) {
            this.monitor = monitor;
            this.operation = operation;
            this.startTime = System.currentTimeMillis();
        }
        
        @Override
        public void close() {
            long duration = System.currentTimeMillis() - startTime;
            monitor.recordTime(operation, duration);
        }
    }
    
    /**
     * Create a timer
     *
     * @param operation Operation name
     * @return Timer instance
     */
    public Timer startTimer(String operation) {
        return new Timer(this, operation);
    }
}