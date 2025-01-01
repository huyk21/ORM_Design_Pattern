package com.example.iterator;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.EntityMapper;

public class ForwardLazyIteratorFactory<T> implements LazyIteratorFactory<T> {
    @Override
    public LazyIterator<T> create(ResultSet resultSet, EntityMapper<T> mapper) throws SQLException {
        return new ForwardLazyIterator<>(resultSet, mapper);
    }
}

