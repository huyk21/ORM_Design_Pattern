package com.example.cache;

import java.util.Map;

public class LRUEvictionPolicy<K, V> implements EvictionPolicy<K, V> {
    private final int maxSize;

    public LRUEvictionPolicy(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public boolean shouldEvict(Map<K, V> cache) {
        return cache.size() > maxSize;
    }

    @Override
    public K getEvictionKey(Map<K, V> cache) {
        return cache.keySet().iterator().next(); // Returns the least recently used key
    }
}
