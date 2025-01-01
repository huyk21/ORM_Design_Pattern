package com.example.cache;

import java.util.Map;

public class FIFOEvictionPolicy<K, V> implements EvictionPolicy<K, V> {
    private final int maxSize;

    public FIFOEvictionPolicy(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public boolean shouldEvict(Map<K, V> cache) {
        System.out.println("Checking eviction: Cache size = " + cache.size() + ", Max size = " + maxSize);
        return cache.size() >= maxSize; // Ensure eviction triggers when cache exceeds maxSize
    }

    @Override
    public K getEvictionKey(Map<K, V> cache) {
        return cache.keySet().iterator().next(); // Returns the first inserted key
    }
}

