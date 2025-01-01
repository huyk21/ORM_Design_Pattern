// File: GenericDaoImpl.java
package com.example.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.SelectBuilder;
import com.example.connection.DatabaseSession;

/**
 * GenericDao implementation using the DAO Pattern.
 * Applies Dependency Inversion by depending on abstractions (Dao interface).
 *
 * @param <T> The type of the entity.
 */
public class GenericDaoImpl<T> extends AbstractGenericDao<T> {

    /**
     * Constructor with Dependency Injection.
     *
     * @param session The database session.
     * @param clazz   The Class type of the entity.
     */
    public GenericDaoImpl(DatabaseSession session, Class<T> clazz) {
        super(session, clazz);
    }

    @Override
    protected String buildInsertQuery(T entity) throws IllegalAccessException, NoSuchFieldException {
        return queryBuilder.buildInsertQuery(entity);
    }

    @Override
    protected String buildSelectByIdQuery(Object id) {
        return queryBuilder.buildSelectByIdQuery(id);
    }

    @Override
    protected String buildReadQuery(String whereCondition) {
        return queryBuilder.buildReadQuery(whereCondition);
    }

    @Override
    protected String buildUpdateQuery(T entity, String whereCondition) throws IllegalAccessException {
        return queryBuilder.buildUpdateQuery(entity, whereCondition);
    }

    @Override
    protected String buildDeleteQuery(String whereCondition) {
        return queryBuilder.buildDeleteQuery(whereCondition);
    }

    @Override
    protected T mapResultSetToEntity(ResultSet rs) throws ReflectiveOperationException, SQLException {
        return mapper.mapResultSetToEntity(rs);
    }

    @Override
    protected SelectBuilder<T> buildDynamicJoinBuilder() {
        return queryBuilder.buildDynamicJoinBuilder();
    }

    @Override
    protected void postCreate(T entity) {
        // Optional: Implement any post-create operations here
    }

}
