package com.example.iterator;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.EntityMapper;

public class ForwardLazyIterator<T> implements LazyIterator<T> {
    private final ResultSet resultSet;
    private final EntityMapper<T> mapper;
    private boolean hasNext;

    public ForwardLazyIterator(ResultSet resultSet, EntityMapper<T> mapper) throws SQLException {
        this.resultSet = resultSet;
        this.mapper = mapper;
        this.hasNext = resultSet.next(); // Initialize cursor
        System.out.println("Initialized iterator: hasNext = " + hasNext); // Debug
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public T next() {
        try {
            T entity = mapper.mapResultSetToEntity(resultSet);
            hasNext = resultSet.next(); // Move to next row
            return entity;
        } catch (SQLException | ReflectiveOperationException e) {
            throw new RuntimeException("Error iterating over ResultSet", e);
        }
    }

    @Override
    public void close() throws SQLException {
        resultSet.close();
    }
}
