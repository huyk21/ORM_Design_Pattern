// File: AbstractGenericDao.java
package com.example;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import com.example.annotation.Column;
import com.example.annotation.Id;
import com.example.connection.DatabaseSession;
import com.example.lazyloading.LazyInitializer;

/**
 * Abstract Generic DAO defining template methods for CRUD operations.
 *
 * @param <T> The type of the entity.
 */
public abstract class AbstractGenericDao<T> implements Dao<T> {

    protected final DatabaseSession session;
    protected final Class<T> clazz;
    protected final EntityMapper<T> mapper;
    protected final QueryBuilder<T> queryBuilder;

    /**
     * Constructor initializing the DAO with a database session and entity class.
     *
     * @param session The database session.
     * @param clazz   The Class type of the entity.
     */
    public AbstractGenericDao(DatabaseSession session, Class<T> clazz) {
        this.session = session;
        this.clazz = clazz;
        this.mapper = new EntityMapper<>(clazz, session);
        this.queryBuilder = new QueryBuilder<>(clazz);
    }

    @Override
    public void create(T entity) throws SQLException, IllegalAccessException {
        String sql = buildInsertQuery(entity);
        executeUpdate(sql, entity);
        postCreate(entity);
    }

    @Override
    public Optional<T> findById(Object id) throws SQLException, ReflectiveOperationException {
        String sql = buildSelectByIdQuery(id);
        try (PreparedStatement stmt = session.getConnection().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                T entity = mapResultSetToEntity(rs);
                return Optional.of(entity);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<T> read(String whereCondition) throws SQLException, ReflectiveOperationException {
        String sql = buildReadQuery(whereCondition);
        System.out.println("Executing SQL Query: " + sql); // Debug SQL query
        ResultSet rs = session.executeQuery(sql);
        List<T> results = new ArrayList<>();

        while (rs.next()) {
            T entity = mapResultSetToEntity(rs);
            results.add(entity);
        }
        return results;
    }

    @Override
    public void update(T entity, String whereCondition) throws SQLException, IllegalAccessException {
        String sql = buildUpdateQuery(entity, whereCondition);
        executeUpdate(sql, entity);
    }

    @Override
    public void delete(String whereCondition) throws SQLException {
        String sql = buildDeleteQuery(whereCondition);
        try {
            executeUpdate(sql, null);
        } catch (IllegalAccessException ex) {
        }
    }

    @Override
    public List<Object[]> select(SelectBuilder<T> builder) throws SQLException {
        String selectQuery = builder.buildSelectQuery();
        System.out.println("Generated SQL Query: " + selectQuery); // Debugging

        List<Object[]> results = new ArrayList<>();

        // Determine if the query involves JOINs or is a simple select
        if (builder.hasJoins()) {
            // Use executeCustomJoinQuery for JOIN operations
            results = session.executeCustomJoinQuery(selectQuery);
        } else {
            // Use executeQuery for simple SELECT operations
            try (PreparedStatement stmt = session.getConnection().prepareStatement(selectQuery);
                 ResultSet rs = stmt.executeQuery()) {
                int columnCount = rs.getMetaData().getColumnCount();

                while (rs.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = rs.getObject(i);
                    }
                    results.add(row);
                }
            }
        }

        return results;
    }

    @Override
    public T getLazy(Class<T> entityClass, Object id) {
        Callable<T> fetchCallback = () -> {
            Optional<T> optionalEntity = findById(id);
            if (optionalEntity.isPresent()) {
                return optionalEntity.get();
            } else {
                System.out.println("Entity not found for ID: " + id);
                return null;
            }
        };

        LazyInitializer<T> lazyInitializer = new LazyInitializer<>(entityClass, fetchCallback);
        return lazyInitializer.createProxy();
    }

    @Override
    public SelectBuilder<T> dynamicJoinBuilder() {
        return queryBuilder.buildDynamicJoinBuilder();
    }

    /**
     * Executes an update operation (INSERT, UPDATE, DELETE).
     *
     * @param sql    The SQL statement to execute.
     * @param entity The entity to bind parameters (can be null for DELETE).
     * @throws SQLException If a database access error occurs.
     * @throws IllegalAccessException If field access fails.
     */
    protected void executeUpdate(String sql, T entity) throws SQLException, IllegalAccessException {
        try (PreparedStatement stmt = session.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (entity != null) {
                bindParameters(stmt, entity);
            }
            stmt.executeUpdate();

            if (entity != null) {
                // Retrieve and set generated keys if any
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Field idField = mapper.getIdField();
                        idField.setAccessible(true);
                        Object key = generatedKeys.getObject(1);

                        if (idField.getType() == int.class || idField.getType() == Integer.class) {
                            idField.set(entity, ((Number) key).intValue());
                        } else if (idField.getType() == long.class || idField.getType() == Long.class) {
                            idField.set(entity, ((Number) key).longValue());
                        } else {
                            idField.set(entity, key);
                        }
                    }
                }
            }
        }
    }

    /**
     * Binds entity fields to the PreparedStatement parameters.
     *
     * @param stmt   The PreparedStatement.
     * @param entity The entity whose fields are to be bound.
     * @throws SQLException If a database access error occurs.
     * @throws IllegalAccessException If field access fails.
     */
    protected void bindParameters(PreparedStatement stmt, T entity) throws SQLException, IllegalAccessException {
        int index = 1;
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent((Class<? extends Annotation>) Column.class) && !field.isAnnotationPresent((Class<? extends Annotation>) Id.class)) {
                field.setAccessible(true);
                Object value = field.get(entity);
                stmt.setObject(index++, value);
            }
        }
    }

    /**
     * Hook method for any post-create operations. Can be overridden by subclasses.
     *
     * @param entity The entity that was created.
     */
    protected void postCreate(T entity) {
        // Default implementation does nothing. Subclasses can override.
    }

    /**
     * Builds the INSERT SQL query. Must be implemented by subclasses.
     *
     * @param entity The entity to insert.
     * @return The SQL INSERT query string.
     * @throws IllegalAccessException If field access fails.
     */
    protected abstract String buildInsertQuery(T entity) throws IllegalAccessException;

    /**
     * Builds the SELECT BY ID SQL query. Must be implemented by subclasses.
     *
     * @param id The ID of the entity.
     * @return The SQL SELECT query string.
     */
    protected abstract String buildSelectByIdQuery(Object id);

    /**
     * Builds the READ SQL query with a WHERE condition. Must be implemented by subclasses.
     *
     * @param whereCondition The WHERE condition.
     * @return The SQL SELECT query string.
     */
    protected abstract String buildReadQuery(String whereCondition);

    /**
     * Builds the UPDATE SQL query. Must be implemented by subclasses.
     *
     * @param entity         The entity to update.
     * @param whereCondition The WHERE condition.
     * @return The SQL UPDATE query string.
     * @throws IllegalAccessException If field access fails.
     */
    protected abstract String buildUpdateQuery(T entity, String whereCondition) throws IllegalAccessException;

    /**
     * Builds the DELETE SQL query with a WHERE condition. Must be implemented by subclasses.
     *
     * @param whereCondition The WHERE condition.
     * @return The SQL DELETE query string.
     */
    protected abstract String buildDeleteQuery(String whereCondition);

    /**
     * Maps a ResultSet row to an entity instance. Must be implemented by subclasses.
     *
     * @param rs The ResultSet.
     * @return The mapped entity.
     * @throws ReflectiveOperationException If instantiation fails.
     * @throws SQLException                 If ResultSet access fails.
     */
    protected abstract T mapResultSetToEntity(ResultSet rs) throws ReflectiveOperationException, SQLException;

    /**
     * Builds a dynamic SelectBuilder for complex JOIN operations. Must be implemented by subclasses.
     *
     * @return A SelectBuilder instance.
     */
    protected abstract SelectBuilder<T> buildDynamicJoinBuilder();
}
