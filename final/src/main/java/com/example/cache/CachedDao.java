package com.example.cache;

import java.sql.SQLException;
import java.util.Optional;

import com.example.Dao;

public class CachedDao<T> {
    private final Dao<T> dao; // Underlying DAO
    private final Cache<Integer, T> cache; // Cache instance

    public CachedDao(Dao<T> dao, EvictionPolicy<Integer, T> evictionPolicy) {
        this.dao = dao;
        this.cache = new Cache<>(evictionPolicy);
    }

    // Caching for findById
    public Optional<T> findById(Object id) throws SQLException, ReflectiveOperationException {
        if (cache.containsKey((Integer) id)) {
            System.out.println("Cache hit for ID: " + id);
            return Optional.of(cache.get((Integer) id));
        }

        // Fetch from database and cache the result
        Optional<T> result = dao.findById(id);
        result.ifPresent(entity -> cache.put((Integer) id, entity));
        return result;
    }

    public void create(T entity) throws SQLException, IllegalAccessException, NoSuchFieldException {
        dao.create(entity);
    }

    public void update(T entity, String whereCondition) throws SQLException, IllegalAccessException {
        dao.update(entity, whereCondition);
    }

    public void delete(String whereCondition) throws SQLException {
        dao.delete(whereCondition);
    }
}
