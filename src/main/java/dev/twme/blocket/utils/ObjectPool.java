package dev.twme.blocket.utils;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * General object pool
 * Used to reuse expensive objects and reduce garbage collection pressure
 *
 * <p>Features:
 * <ul>
 *   <li>Thread-safe object pool implementation</li>
 *   <li>Supports custom object factories</li>
 *   <li>Automatic cleaning and resetting mechanism</li>
 *   <li>Configurable maximum pool size</li>
 * </ul>
 *
 * @param <T> Type of pooled objects
 * @author TWME-TW
 * @version 1.0.0
 * @since 1.0.0
 */
public class ObjectPool<T> {
    
    private final ConcurrentLinkedQueue<T> pool;
    private final Supplier<T> objectFactory;
    private final int maxSize;
    
    /**
     * Constructor
     *
     * @param objectFactory Object factory for creating new objects
     * @param maxSize Maximum pool size
     */
    public ObjectPool(Supplier<T> objectFactory, int maxSize) {
        this.pool = new ConcurrentLinkedQueue<>();
        this.objectFactory = objectFactory;
        this.maxSize = maxSize;
    }
    
    /**
     * Acquire an object from the pool
     * If the pool is empty, create a new object
     *
     * @return Pooled object
     */
    public T acquire() {
        T object = pool.poll();
        if (object == null) {
            object = objectFactory.get();
        }
        return object;
    }
    
    /**
     * Return an object to the pool
     * If the pool is full, discard the object
     *
     * @param object Object to return
     */
    public void release(T object) {
        if (object != null && pool.size() < maxSize) {
            pool.offer(object);
        }
    }
    
    /**
     * Clear the pool
     */
    public void clear() {
        pool.clear();
    }
    
    /**
     * Get current pool size
     *
     * @return Number of objects in the pool
     */
    public int size() {
        return pool.size();
    }
    
    /**
     * Check if the pool is empty
     *
     * @return true if the pool is empty, false otherwise
     */
    public boolean isEmpty() {
        return pool.isEmpty();
    }
}