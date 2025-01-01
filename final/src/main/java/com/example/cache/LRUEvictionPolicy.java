package com.example.cache;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUEvictionPolicy<K, V> implements EvictionPolicy<K, V> {
    private final int maxSize;

    public LRUEvictionPolicy(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public boolean shouldEvict(Map<K, V> cache) {
        return cache.size() >= maxSize;
    }

    @Override
    public K getEvictionKey(Map<K, V> cache) {
         if (cache instanceof LinkedHashMap) {
            // Access-order LinkedHashMap maintains LRU as the last element
            return cache.keySet().iterator().next(); // Returns the least recently used key
        }
        throw new IllegalArgumentException("Cache must be a LinkedHashMap with access-order enabled for LRU to work");
    }
    
}
