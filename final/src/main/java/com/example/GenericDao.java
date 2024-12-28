package com.example;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import com.example.annotation.Column;
import com.example.annotation.Table;
import com.example.connection.DatabaseSession;
import com.example.entity.ColumnMetadata;
import com.example.entity.EntityMetadata;
import com.example.lazyloading.LazyInitializer;

// Generic DAO class for handimport java.util.concurrent.Callable;ling CRUD operations
public class GenericDao<T> {

    private DatabaseSession session;
    private Class<T> clazz;

    // Constructor that takes a DatabaseSession and the Class type
    public GenericDao(DatabaseSession session, Class<T> clazz) {
        this.session = session;
        this.clazz = clazz;
    }

    // Create a new record in the database
    public void create(T entity) throws SQLException, IllegalAccessException {
        String sql = buildInsertQuery(entity);
        session.executeUpdate(sql);
    }
    private T mapResultSetToEntity(ResultSet rs) throws ReflectiveOperationException, SQLException {
        T entity = clazz.getDeclaredConstructor().newInstance();
        EntityMetadata metadata = new EntityMetadata(clazz);
    
        for (ColumnMetadata column : metadata.getColumns()) {
            Field field = column.getField();
            field.setAccessible(true); // Allow access to private fields
    
            String columnName = column.getColumnName(); // Get the database column name
            Object value = rs.getObject(columnName); // Retrieve the value from the ResultSet
    
            if (value != null) {
                
                // Set the value to the corresponding field
                field.set(entity, value);
            }
        }
    
        return entity;
    }
    
    
    // Read records from the database (using a simple SELECT query)
    public List<T> read(String whereCondition) throws SQLException, IllegalAccessException, InstantiationException {
        String query = "SELECT * FROM " + getTableName();
        if (whereCondition != null && !whereCondition.isEmpty()) {
            query += " WHERE " + whereCondition;
        }

        ResultSet rs = session.executeQuery(query);
        List<T> results = new ArrayList<>();

        while (rs.next()) {
            T entity = clazz.newInstance();  // Create a new instance of the entity
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                String columnName = convertToSnakeCase(field.getName());  // Convert to snake_case
                Object columnValue = rs.getObject(columnName);  // Get column value by snake_case column name
                if (columnValue != null) {
                    field.set(entity, columnValue); // Set the value to the field
                }
            }
            results.add(entity);
        }
        return results;
    }

    // Update an existing record in the database
    public void update(T entity, String whereCondition) throws SQLException, IllegalAccessException {
        String sql = buildUpdateQuery(entity, whereCondition);
        session.executeUpdate(sql);
    }

    // Delete a record from the database
    public void delete(String whereCondition) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE " + whereCondition;
        session.executeUpdate(sql);
    }

    public List<Object> select(SelectBuilder builder) throws SQLException, IllegalAccessException, InstantiationException {
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
                Object scalarValue = rs.getObject(1);  // Get the scalar result (e.g., COUNT)
                results.add(scalarValue);  // Add to result list
            }
        } else {
            // For regular query, map rows to entities of type T
            while (rs.next()) {
                T entity = clazz.newInstance();  // Create a new instance of T
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    String columnName = field.getName();
                    try {
                        Object columnValue = rs.getObject(columnName);  // Get column value from ResultSet
                        if (columnValue != null) {
                            field.set(entity, columnValue);  // Set field value using reflection
                        }
                    } catch (SQLException e) {
                        
                    }
                }
                results.add(entity);  // Add the entity to the result list
            }
        }
    
        return results;
    }
    

    // Helper method to build an INSERT query
    private String buildInsertQuery(T entity) throws IllegalAccessException {
        StringBuilder sql = new StringBuilder("INSERT INTO " + getTableName() + " (");
        StringBuilder values = new StringBuilder("VALUES (");

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);

            // Get the column name (using @Column annotation if available)
            String columnName = convertToSnakeCase(field.getName());  // Convert to snake_case
            if (field.isAnnotationPresent(Column.class)) {
                columnName = field.getAnnotation(Column.class).name();
            }

            sql.append(columnName).append(", ");
            // Check if the field is of type Boolean and convert it to 1 or 0
            if (field.getType() == Boolean.class || field.getType() == boolean.class) {
                Boolean value = (Boolean) field.get(entity);
                values.append(value ? "1" : "0").append(", ");
            } else {
                values.append("'").append(field.get(entity)).append("', ");
            }
        }

        sql.delete(sql.length() - 2, sql.length());  // Remove the trailing comma
        values.delete(values.length() - 2, values.length());  // Remove the trailing comma

        sql.append(") ").append(values).append(")");

        return sql.toString();
    }

    // Helper method to build an UPDATE query
    private String buildUpdateQuery(T entity, String whereCondition) throws IllegalAccessException {
        StringBuilder sql = new StringBuilder("UPDATE " + getTableName() + " SET ");

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);

            // Get the column name (using @Column annotation if available)
            String columnName = convertToSnakeCase(field.getName());  // Convert to snake_case
            if (field.isAnnotationPresent(Column.class)) {
                columnName = field.getAnnotation(Column.class).name();
            }

            sql.append(columnName).append(" = '")
                .append(field.get(entity)).append("', ");
        }

        sql.delete(sql.length() - 2, sql.length());  // Remove the trailing comma

        sql.append(" WHERE ").append(whereCondition);
        return sql.toString();
    }

    // Helper method to get table name from the @Table annotation
    private String getTableName() {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
            return tableAnnotation.name();
        }

        // Fallback to class name and convert to snake_case
        return convertToSnakeCase(clazz.getSimpleName());
    }

    // Helper method to convert camelCase or PascalCase to snake_case
    private String convertToSnakeCase(String input) {
        StringBuilder result = new StringBuilder();

        // Iterate over each character in the class name
        for (char c : input.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (result.length() > 0) {
                    result.append('_');  // Add an underscore before uppercase letters
                }
                result.append(Character.toLowerCase(c));  // Convert uppercase to lowercase
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    
public Optional<T> findById(Object id) throws SQLException, ReflectiveOperationException {
    EntityMetadata metadata = new EntityMetadata(clazz);
    ColumnMetadata idColumn = metadata.getIdColumn();

    String sql = "SELECT * FROM " + metadata.getTableName() +
                 " WHERE " + idColumn.getColumnName() + " = ?";

    try (PreparedStatement stmt = session.getConnection().prepareStatement(sql)) {
        stmt.setObject(1, id);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return Optional.of(mapResultSetToEntity(rs));
        }
    }

    return Optional.empty();
}

public T getLazy(Class<T> entityClass, Object id) {
    Callable<T> fetchCallback = () -> {
        Optional<T> optionalEntity = findById(id);
        if (optionalEntity.isPresent()) {
            return optionalEntity.get();
        } else {
            // Print a message if the entity is not found
            System.out.println("Entity not found for ID: " + id);
            return null; // Return null instead of throwing an exception
        }
    };

    LazyInitializer<T> lazyInitializer = new LazyInitializer<>(entityClass, fetchCallback);
    return lazyInitializer.createProxy();
}




    

    public static class SelectBuilder {
        private StringBuilder query;
        private List<String> selectColumns;  // To handle multiple columns in SELECT
        private String groupBy;
        private String having;
        private boolean isScalar = false;
    
        private Class<?> clazz;  // Store the class reference to get the table name
    
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
            this.isScalar = true;  // Set flag indicating this is a scalar query
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
            query.append(String.join(", ", selectColumns));  // Add columns to the SELECT part
    
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
                return tableAnnotation.name();  // Use table name from the annotation
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
                        result.append('_');  // Add an underscore before uppercase letters
                    }
                    result.append(Character.toLowerCase(c));  // Convert uppercase to lowercase
                } else {
                    result.append(c);
                }
            }
            return result.toString();
        }
    }
    
    
}
