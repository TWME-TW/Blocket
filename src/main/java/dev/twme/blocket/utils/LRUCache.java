package dev.twme.blocket.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU Cache implementation using LinkedHashMap.
 * Automatically evicts the least recently used items when capacity is exceeded.
 *
 * @param <K> Key type
 * @param <V> Value type
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    /**
     * The maximum number of entries the cache can hold.
     */
    private final int capacity;

    /**
     * Creates a new LRU cache with the specified capacity
     * 
     * @param capacity The maximum number of entries the cache can hold
     */
    public LRUCache(int capacity) {
        // Initial capacity is set to capacity + 1 to avoid immediate resizing
        // Load factor is set to 0.75f, which is the default for LinkedHashMap
        // Access order is set to true to enable LRU behavior
        super(capacity + 1, 0.75f, true);
        this.capacity = capacity;
    }

    /**
     * Overridden method to implement LRU eviction policy
     * This method is called every time a new entry is added to the map
     * 
     * @param eldest The eldest entry in the map
     * @return true if the eldest entry should be removed
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        // Remove the eldest entry when the size exceeds the capacity
        return size() > capacity;
    }
}