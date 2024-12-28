package com.example.entity;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.connection.DatabaseSession;


public class GenericDao<T> {
    private final DatabaseSession session;
    private final Class<T> entityClass;
    private final EntityMetadata metadata;

    public GenericDao(DatabaseSession session, Class<T> entityClass) {
        this.session = session;
        this.entityClass = entityClass;
        this.metadata = new EntityMetadata(entityClass);
    }

    public void save(T entity) throws SQLException, IllegalAccessException {
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(metadata.getTableName()).append(" (");
        StringBuilder values = new StringBuilder();

        List<ColumnMetadata> columns = metadata.getColumns();
        for (ColumnMetadata column : columns) {
            sql.append(column.getColumnName()).append(",");
            values.append("?,");
        }

        sql.setLength(sql.length() - 1);
        values.setLength(values.length() - 1);
        sql.append(") VALUES (").append(values).append(")");

        try (PreparedStatement stmt = session.getConnection().prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (ColumnMetadata column : columns) {
                Field field = column.getField();
                field.setAccessible(true);
                stmt.setObject(paramIndex++, field.get(entity));
            }
            stmt.executeUpdate();
        }
    }

    public void update(T entity) throws SQLException, IllegalAccessException {
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(metadata.getTableName()).append(" SET ");

        List<ColumnMetadata> columns = metadata.getColumns();
        for (ColumnMetadata column : columns) {
            if (!column.isId()) {
                sql.append(column.getColumnName()).append(" = ?,");
            }
        }
        sql.setLength(sql.length() - 1);

        ColumnMetadata idColumn = metadata.getIdColumn();
        sql.append(" WHERE ").append(idColumn.getColumnName()).append(" = ?");

        try (PreparedStatement stmt = session.getConnection().prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (ColumnMetadata column : columns) {
                if (!column.isId()) {
                    Field field = column.getField();
                    field.setAccessible(true);
                    stmt.setObject(paramIndex++, field.get(entity));
                }
            }
            Field idField = idColumn.getField();
            idField.setAccessible(true);
            stmt.setObject(paramIndex, idField.get(entity));
            stmt.executeUpdate();
        }
    }

    public void delete(Object id) throws SQLException {
        String sql = "DELETE FROM " + metadata.getTableName() +
                " WHERE " + metadata.getIdColumn().getColumnName() + " = ?";
        try (PreparedStatement stmt = session.getConnection().prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
        }
    }

    public Optional<T> findById(Object id) throws SQLException, ReflectiveOperationException {
        String sql = "SELECT * FROM " + metadata.getTableName() +
                " WHERE " + metadata.getIdColumn().getColumnName() + " = ?";
        try (PreparedStatement stmt = session.getConnection().prepareStatement(sql)) {
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? Optional.of(mapResultSetToEntity(rs)) : Optional.empty();
        }
    }

    public List<T> findAll() throws SQLException, ReflectiveOperationException {
        return select(null, null, null);
    }

    public List<T> select(String whereClause, String groupBy, String having)
            throws SQLException, ReflectiveOperationException {
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(metadata.getTableName());

        if (whereClause != null && !whereClause.isEmpty()) {
            sql.append(" WHERE ").append(whereClause);
        }
        if (groupBy != null && !groupBy.isEmpty()) {
            sql.append(" GROUP BY ").append(groupBy);
        }
        if (having != null && !having.isEmpty()) {
            sql.append(" HAVING ").append(having);
        }

        try (PreparedStatement stmt = session.getConnection().prepareStatement(sql.toString())) {
            ResultSet rs = stmt.executeQuery();
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(mapResultSetToEntity(rs));
            }
            return results;
        }
    }

    private T mapResultSetToEntity(ResultSet rs) throws ReflectiveOperationException, SQLException {
        T entity = entityClass.getDeclaredConstructor().newInstance();
        for (ColumnMetadata column : metadata.getColumns()) {
            Field field = column.getField();
            field.setAccessible(true);
            field.set(entity, rs.getObject(column.getColumnName()));
        }
        return entity;
    }
   
    

    public DatabaseSession getSession() {
        return session;
    }
}