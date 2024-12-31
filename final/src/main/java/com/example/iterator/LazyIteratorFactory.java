package com.example.iterator;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.EntityMapper;

@FunctionalInterface
public interface LazyIteratorFactory<T> {
    LazyIterator<T> create(ResultSet resultSet, EntityMapper<T> mapper) throws SQLException;
}
