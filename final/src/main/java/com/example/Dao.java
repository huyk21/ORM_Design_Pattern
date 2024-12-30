// File: Dao.java
package com.example;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Generic DAO Interface defining CRUD operations and select functionality.
 *
 * @param <T> The type of the entity.
 */
public interface Dao<T> {
    void create(T entity) throws SQLException, IllegalAccessException;

    Optional<T> findById(Object id) throws SQLException, ReflectiveOperationException;

    List<T> read(String whereCondition) throws SQLException, ReflectiveOperationException;

    void update(T entity, String whereCondition) throws SQLException, IllegalAccessException;

    void delete(String whereCondition) throws SQLException;

    /**
     * Select method using a generic SelectBuilder.
     *
     * @param builder The SelectBuilder instance for building the query.
     * @return A list of Object arrays representing the query results.
     * @throws SQLException If a database access error occurs.
     */
    List<Object[]> select(SelectBuilder<T> builder) throws SQLException;

    T getLazy(Class<T> entityClass, Object id);

    SelectBuilder<T> dynamicJoinBuilder();
}
