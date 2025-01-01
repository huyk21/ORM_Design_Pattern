package com.example.cache;

import java.util.LinkedHashMap;
import java.util.Map;

public class Cache<K, V> {
    private final Map<K, V> cache;
    private final EvictionPolicy<K, V> evictionPolicy;

    public Cache(EvictionPolicy<K, V> evictionPolicy) {
        this.evictionPolicy = evictionPolicy;

        // LinkedHashMap to maintain insertion or access order (for LRU/FIFO)
        this.cache = new LinkedHashMap<>(16, 0.75f, true);
    }

    // Add an item to the cache
    public synchronized void put(K key, V value) {
        if (evictionPolicy.shouldEvict(cache)) {
            K evictionKey = evictionPolicy.getEvictionKey(cache);
            cache.remove(evictionKey);
            System.out.println("Evicted key: " + evictionKey);
        }
        cache.put(key, value);
       
    }

    // Get an item from the cache
    public synchronized V get(K key) {
        return cache.get(key);
    }

    // Remove an item from the cache
    public synchronized void remove(K key) {
        cache.remove(key);
    }

    // Check if the cache contains a key
    public synchronized boolean containsKey(K key) {
        return cache.containsKey(key);
    }

    // Clear the cache
    public synchronized void clear() {
        cache.clear();
    }
}
