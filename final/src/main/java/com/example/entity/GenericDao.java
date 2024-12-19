package com.example.entity;

import com.example.annotation.Table;
import com.example.connection.DatabaseSession;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenericDao<T> {
    private final DatabaseSession session;
    private final Class<T> entityClass;
    private final EntityMetadata metadata;

    public GenericDao(DatabaseSession session, Class<T> entityClass) {
        this.session = session;
        this.entityClass = entityClass;
        this.metadata = new EntityMetadata(entityClass);
    }

    /**
     * Inserts a new entity into the database
     * 
     * @param entity The entity to insert
     * @throws SQLException           If a database error occurs
     * @throws IllegalAccessException If field access is denied
     */
    // Create a new record in the database
    public void create(T entity) throws SQLException, IllegalAccessException {
        String sql = buildInsertQuery();

        try (PreparedStatement stmt = session.getConnection().prepareStatement(sql)) {
            setInsertParameters(stmt, entity);
            stmt.executeUpdate();
        }
    }

    /**
     * Builds the INSERT query string
     * 
     * @return SQL INSERT statement with parameter placeholders
     */
    // Helper method to build an INSERT query
    private String buildInsertQuery() {
        // sql: INSERT INTO table_name (
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(metadata.getTableName()).append(" (");
        StringBuilder values = new StringBuilder();

        // Get the list of columns in the entity
        List<ColumnMetadata> columns = metadata.getColumns();
        for (ColumnMetadata column : columns) {
            // Get the column name
            // sql: INSERT INTO table_name (column1, column2, ...)
            sql.append(column.getColumnName()).append(",");
            values.append("?,");
        }

        // Remove trailing commas
        sql.setLength(sql.length() - 1);
        values.setLength(values.length() - 1);

        // sql: INSERT INTO table_name (column1, column2, ...) VALUES (?, ?, ...)
        sql.append(") VALUES (").append(values).append(")");
        return sql.toString();
    }

    /**
     * Sets parameters for the INSERT statement based on entity field types
     * 
     * @param stmt   PreparedStatement to set parameters on
     * @param entity Entity containing the values
     */
    private void setInsertParameters(PreparedStatement stmt, T entity)
            throws SQLException, IllegalAccessException {
        int paramIndex = 1;
        for (ColumnMetadata column : metadata.getColumns()) {
            Field field = column.getField();
            field.setAccessible(true);
            Object value = field.get(entity);

            if (value == null) {
                stmt.setNull(paramIndex++, column.getJdbcType().getVendorTypeNumber());
                continue;
            }

            // Handle different types
            switch (column.getJdbcType()) {
                case BOOLEAN:
                    stmt.setBoolean(paramIndex, (Boolean) value);
                    break;
                case TINYINT:
                case SMALLINT:
                case INTEGER:
                    stmt.setInt(paramIndex, (Integer) value);
                    break;
                case BIGINT:
                    stmt.setLong(paramIndex, (Long) value);
                    break;
                case FLOAT:
                    stmt.setFloat(paramIndex, (Float) value);
                    break;
                case DOUBLE:
                    stmt.setDouble(paramIndex, (Double) value);
                    break;
                case DECIMAL:
                case NUMERIC:
                    stmt.setBigDecimal(paramIndex, (java.math.BigDecimal) value);
                    break;
                case DATE:
                    stmt.setDate(paramIndex, (java.sql.Date) value);
                    break;
                case TIME:
                    stmt.setTime(paramIndex, (java.sql.Time) value);
                    break;
                case TIMESTAMP:
                    stmt.setTimestamp(paramIndex, (java.sql.Timestamp) value);
                    break;
                default:
                    stmt.setObject(paramIndex, value);
            }
            paramIndex++;
        }
    }

    /**
     * Debug method to preview the INSERT query that would be generated
     * 
     * @param entity The entity to insert
     * @throws SQLException           If a database error occurs
     * @throws IllegalAccessException If field access is denied
     * @return The INSERT query string with placeholders
     */
    public String previewInsertQuery(T entity) throws SQLException, IllegalAccessException {
        String sql = buildInsertQuery();

        try (PreparedStatement stmt = session.getConnection().prepareStatement(sql)) {
            setInsertParameters(stmt, entity);
            return stmt.toString();
        }
    }

    /**
     * Updates an existing entity in the database
     *
     * @param entity The entity to update
     * @throws SQLException           If a database error occurs
     * @throws IllegalAccessException If field access is denied
     */

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
            // DEBUG: Print the generated SQL query
            System.out.println("Generated SQL Query: " + stmt.toString());
            stmt.executeUpdate();
        }
    }

    /**
     * Delete an existing entity in the database
     *
     * @param entity The entity to delete
     * @throws SQLException           If a database error occurs
     * @throws IllegalAccessException If field access is denied
     */
    public void delete(Object id) throws SQLException, IllegalAccessException {
        String sql = "DELETE FROM " + metadata.getTableName() +
                " WHERE " + metadata.getIdColumn().getColumnName() + " = ?";
        try (PreparedStatement stmt = session.getConnection().prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
        }
    }

    // Read records from the database (using a simple SELECT query)
    public List<T> read(String whereCondition) throws SQLException, IllegalAccessException, InstantiationException {
        String query = "SELECT * FROM " + metadata.getTableName();
        if (whereCondition != null && !whereCondition.isEmpty()) {
            query += " WHERE " + whereCondition;
        }

        ResultSet rs = session.executeQuery(query);
        List<T> results = new ArrayList<>();

        while (rs.next()) {
            T entity = entityClass.newInstance(); // Create a new instance of the entity
            // Use metadata columns instead of reflection directly
            for (ColumnMetadata column : metadata.getColumns()) {
                Field field = column.getField();
                field.setAccessible(true);
                Object columnValue = rs.getObject(column.getColumnName());
                if (columnValue != null) {
                    field.set(entity, columnValue); // Set the value to the field
                }
            }
            results.add(entity);

        }
        return results;
    }

    public List<Object> select(SelectBuilder builder)
            throws SQLException, IllegalAccessException, InstantiationException {
        // Build the select query using SelectBuilder
        String selectQuery = builder.buildSelectQuery();

        // Log the query before executing it (for debugging)
        System.out.println("Generated SQL Query: " + selectQuery);

        // Execute the query using the session
        ResultSet rs = session.executeQuery(selectQuery);

        List<Object> results = new ArrayList<>();

        // Check if it's a scalar query (like COUNT or MAX)
        if (builder.isScalar()) {
            // For scalar query, return single value as the result
            if (rs.next()) {
                Object scalarValue = rs.getObject(1); // Get the scalar result (e.g., COUNT)
                results.add(scalarValue); // Add to result list
            }
        } else {
            // For regular query, map rows to entities of type T
            while (rs.next()) {
                T entity = entityClass.newInstance(); // Create a new instance of T
                for (Field field : entityClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    String columnName = field.getName();
                    try {
                        Object columnValue = rs.getObject(columnName); // Get column value from ResultSet
                        if (columnValue != null) {
                            field.set(entity, columnValue); // Set field value using reflection
                        }
                    } catch (SQLException e) {

                    }
                }
                results.add(entity); // Add the entity to the result list
            }
        }

        return results;
    }

    public static class SelectBuilder {
        private StringBuilder query;
        private List<String> selectColumns; // To handle multiple columns in SELECT
        private String groupBy;
        private String having;
        private boolean isScalar = false;

        private Class<?> clazz; // Store the class reference to get the table name

        public SelectBuilder(Class<?> clazz) {
            this.clazz = clazz;
            query = new StringBuilder("SELECT ");
            this.selectColumns = new ArrayList<>();
        }

        // Add a regular column to the SELECT clause
        public SelectBuilder addColumn(String column) {
            selectColumns.add(column);
            return this;
        }

        // Add a scalar function like COUNT(id) to the SELECT clause
        public SelectBuilder addScalar(String function, String field) {
            selectColumns.add(function + "(" + field + ")");
            this.isScalar = true; // Set flag indicating this is a scalar query
            return this;
        }

        // Add GROUP BY clause
        public SelectBuilder groupBy(String groupBy) {
            this.groupBy = groupBy;
            return this;
        }

        // Add HAVING clause
        public SelectBuilder having(String having) {
            this.having = having;
            return this;
        }

        // Build the final SELECT query
        public String buildSelectQuery() {
            // If columns were added, join them with commas
            if (selectColumns.isEmpty()) {
                throw new IllegalStateException("No columns selected.");
            }

            // Append SELECT columns (scalar or regular)
            query.append(String.join(", ", selectColumns)); // Add columns to the SELECT part

            // Get the table name using the getTableName() method
            query.append(" FROM ").append(getTableName());

            // Add GROUP BY if specified
            if (groupBy != null && !groupBy.isEmpty()) {
                query.append(" GROUP BY ").append(groupBy);
            }

            // Add HAVING if specified
            if (having != null && !having.isEmpty()) {
                query.append(" HAVING ").append(having);
            }

            return query.toString();
        }

        // Check if the query is scalar (like COUNT, MAX, etc.)
        public boolean isScalar() {
            return isScalar;
        }

        // Helper method to get table name for the SELECT query
        private String getTableName() {
            // Assuming the class is annotated with @Table to define the table name
            Table tableAnnotation = clazz.getAnnotation(Table.class);
            if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
                return tableAnnotation.name(); // Use table name from the annotation
            }

            // If no annotation is found, convert the class name to snake_case
            return convertToSnakeCase(clazz.getSimpleName());
        }

        // Helper method to convert camelCase or PascalCase to snake_case
        private String convertToSnakeCase(String input) {
            StringBuilder result = new StringBuilder();

            for (char c : input.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    if (result.length() > 0) {
                        result.append('_'); // Add an underscore before uppercase letters
                    }
                    result.append(Character.toLowerCase(c)); // Convert uppercase to lowercase
                } else {
                    result.append(c);
                }
            }
            return result.toString();
        }
    }

    
    public Optional<T> findById(Object id) throws SQLException,
    ReflectiveOperationException {
    String sql = "SELECT * FROM " + metadata.getTableName() +
    " WHERE " + metadata.getIdColumn().getColumnName() + " = ?";
    try (PreparedStatement stmt = session.getConnection().prepareStatement(sql))
    {
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

    try (PreparedStatement stmt =
    session.getConnection().prepareStatement(sql.toString())) {
    ResultSet rs = stmt.executeQuery();
    List<T> results = new ArrayList<>();
    while (rs.next()) {
    results.add(mapResultSetToEntity(rs));
    }
    return results;
    }
    }

    private T mapResultSetToEntity(ResultSet rs) throws
    ReflectiveOperationException, SQLException {
    T entity = entityClass.getDeclaredConstructor().newInstance();
    for (ColumnMetadata column : metadata.getColumns()) {
    Field field = column.getField();
    field.setAccessible(true);
    field.set(entity, rs.getObject(column.getColumnName()));
    }
    return entity;
    }

}