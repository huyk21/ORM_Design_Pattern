package com.example.cache;

import java.util.LinkedHashMap;
import java.util.Map;

public class Cache<K, V> {
    private static Cache instance;
    private final Map<K, V> cache;
    private final int maxSize;

    // Private constructor to enforce Singleton
    private Cache(int maxSize) {
        this.maxSize = maxSize;

        // Use LinkedHashMap for LRU eviction policy
        this.cache = new LinkedHashMap<>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > Cache.this.maxSize;
            }
        };
    }

    // Public method to get the Singleton instance
    public static synchronized <K, V> Cache<K, V> getInstance(int maxSize) {
        if (instance == null) {
            instance = new Cache<>(maxSize);
        }
        return instance;
    }

    // Add an item to the cache
    public synchronized void put(K key, V value) {
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

