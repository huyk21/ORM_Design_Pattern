package com.example.cache;

import java.util.HashMap;
import java.util.Map;

public class CacheManager {
    private final Map<Class<?>, Map<Object, Object>> cache = new HashMap<>();

    public <T> void put(Class<T> entityClass, Object id, T entity) {
        cache.computeIfAbsent(entityClass, k -> new HashMap<>()).put(id, entity);
    }

    public <T> T get(Class<T> entityClass, Object id) {
        return (T) cache.getOrDefault(entityClass, new HashMap<>()).get(id);
    }

    public <T> void evict(Class<T> entityClass, Object id) {
        Map<Object, Object> entities = cache.get(entityClass);
        if (entities != null) {
            entities.remove(id);
        }
    }
}
