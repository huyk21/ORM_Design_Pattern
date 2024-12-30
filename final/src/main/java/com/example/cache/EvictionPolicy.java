package com.example.cache;

import java.util.Map;

public interface EvictionPolicy<K, V> {
    boolean shouldEvict(Map<K, V> cache);
    K getEvictionKey(Map<K, V> cache); // Defines which key to evict
}
