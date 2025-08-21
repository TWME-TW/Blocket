package dev.twme.blocket.utils;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * 通用對象池
 * 用於重用昂貴的對象，減少垃圾回收壓力
 * 
 * <p>特點：
 * <ul>
 *   <li>線程安全的對象池實現</li>
 *   <li>支援自定義對象工廠</li>
 *   <li>自動清理和重置機制</li>
 *   <li>可配置的最大池大小</li>
 * </ul>
 * 
 * @param <T> 池化對象的類型
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
public class ObjectPool<T> {
    
    private final ConcurrentLinkedQueue<T> pool;
    private final Supplier<T> objectFactory;
    private final int maxSize;
    
    /**
     * 建構子
     * 
     * @param objectFactory 對象工廠，用於創建新對象
     * @param maxSize 池的最大大小
     */
    public ObjectPool(Supplier<T> objectFactory, int maxSize) {
        this.pool = new ConcurrentLinkedQueue<>();
        this.objectFactory = objectFactory;
        this.maxSize = maxSize;
    }
    
    /**
     * 從池中獲取對象
     * 如果池為空，則創建新對象
     * 
     * @return 池化對象
     */
    public T acquire() {
        T object = pool.poll();
        if (object == null) {
            object = objectFactory.get();
        }
        return object;
    }
    
    /**
     * 將對象歸還到池中
     * 如果池已滿，則丟棄對象
     * 
     * @param object 要歸還的對象
     */
    public void release(T object) {
        if (object != null && pool.size() < maxSize) {
            pool.offer(object);
        }
    }
    
    /**
     * 清空池
     */
    public void clear() {
        pool.clear();
    }
    
    /**
     * 獲取當前池大小
     * 
     * @return 池中對象數量
     */
    public int size() {
        return pool.size();
    }
    
    /**
     * 檢查池是否為空
     * 
     * @return true如果池為空，false否則
     */
    public boolean isEmpty() {
        return pool.isEmpty();
    }
}