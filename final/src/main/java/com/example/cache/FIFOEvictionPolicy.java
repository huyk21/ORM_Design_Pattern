package com.example.cache;

import java.util.Map;

public class FIFOEvictionPolicy<K, V> implements EvictionPolicy<K, V> {
    private final int maxSize;

    public FIFOEvictionPolicy(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public boolean shouldEvict(Map<K, V> cache) {
        return cache.size() > maxSize;
    }

    @Override
    public K getEvictionKey(Map<K, V> cache) {
        return cache.keySet().iterator().next(); // Returns the first inserted key
    }
}

