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
     * 增加操作計數
     * 
     * @param operation 操作名稱
     */
    public void incrementCounter(String operation) {
        counters.computeIfAbsent(operation, k -> new LongAdder()).increment();
    }
    
    /**
     * 記錄操作執行時間
     * 
     * @param operation 操作名稱
     * @param timeMs 執行時間（毫秒）
     */
    public void recordTime(String operation, long timeMs) {
        incrementCounter(operation);
        totalTimes.computeIfAbsent(operation, k -> new AtomicLong()).addAndGet(timeMs);
        
        // 更新最大時間
        maxTimes.computeIfAbsent(operation, k -> new AtomicLong()).updateAndGet(current -> Math.max(current, timeMs));
        
        // 更新最小時間
        minTimes.computeIfAbsent(operation, k -> new AtomicLong(Long.MAX_VALUE)).updateAndGet(current -> Math.min(current, timeMs));
    }
    
    /**
     * 獲取操作計數
     * 
     * @param operation 操作名稱
     * @return 操作計數
     */
    public long getCount(String operation) {
        LongAdder counter = counters.get(operation);
        return counter != null ? counter.sum() : 0;
    }
    
    /**
     * 獲取總執行時間
     * 
     * @param operation 操作名稱
     * @return 總執行時間（毫秒）
     */
    public long getTotalTime(String operation) {
        AtomicLong totalTime = totalTimes.get(operation);
        return totalTime != null ? totalTime.get() : 0;
    }
    
    /**
     * 獲取平均執行時間
     * 
     * @param operation 操作名稱
     * @return 平均執行時間（毫秒）
     */
    public double getAverageTime(String operation) {
        long count = getCount(operation);
        if (count == 0) return 0.0;
        
        long totalTime = getTotalTime(operation);
        return (double) totalTime / count;
    }
    
    /**
     * 獲取最大執行時間
     * 
     * @param operation 操作名稱
     * @return 最大執行時間（毫秒）
     */
    public long getMaxTime(String operation) {
        AtomicLong maxTime = maxTimes.get(operation);
        return maxTime != null ? maxTime.get() : 0;
    }
    
    /**
     * 獲取最小執行時間
     * 
     * @param operation 操作名稱
     * @return 最小執行時間（毫秒）
     */
    public long getMinTime(String operation) {
        AtomicLong minTime = minTimes.get(operation);
        long value = minTime != null ? minTime.get() : 0;
        return value == Long.MAX_VALUE ? 0 : value;
    }
    
    /**
     * 重置指定操作的統計數據
     * 
     * @param operation 操作名稱
     */
    public void reset(String operation) {
        counters.remove(operation);
        totalTimes.remove(operation);
        maxTimes.remove(operation);
        minTimes.remove(operation);
    }
    
    /**
     * 重置所有統計數據
     */
    public void resetAll() {
        counters.clear();
        totalTimes.clear();
        maxTimes.clear();
        minTimes.clear();
    }
    
    /**
     * 生成性能報告
     * 
     * @return 性能報告字符串
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== 性能監控報告 ===\n");
        
        for (String operation : counters.keySet()) {
            long count = getCount(operation);
            double avgTime = getAverageTime(operation);
            long maxTime = getMaxTime(operation);
            long minTime = getMinTime(operation);
            long totalTime = getTotalTime(operation);
            
            report.append(String.format(
                "操作: %s\n" +
                "  計數: %d\n" +
                "  總時間: %d ms\n" +
                "  平均時間: %.2f ms\n" +
                "  最大時間: %d ms\n" +
                "  最小時間: %d ms\n" +
                "---\n",
                operation, count, totalTime, avgTime, maxTime, minTime
            ));
        }
        
        return report.toString();
    }
    
    /**
     * 計時器類，用於自動測量執行時間
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
     * 創建計時器
     * 
     * @param operation 操作名稱
     * @return 計時器實例
     */
    public Timer startTimer(String operation) {
        return new Timer(this, operation);
    }
}