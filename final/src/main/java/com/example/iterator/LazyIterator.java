package com.example.iterator;

import java.sql.SQLException;

public interface LazyIterator<T> {
    boolean hasNext();

    T next();

    void close() throws SQLException; // Ensure proper resource management
}
