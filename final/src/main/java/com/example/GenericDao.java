package com.example;

import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import com.example.annotation.Column;
import com.example.annotation.Id;
import com.example.annotation.JoinColumn;
import com.example.annotation.ManyToOne;
import com.example.annotation.OneToMany;
import com.example.annotation.OneToOne;
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
    public void create(T entity) throws SQLException, IllegalAccessException, NoSuchFieldException {
        String sql = buildInsertQuery(entity);

        try (PreparedStatement stmt = session.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.executeUpdate();
    
            // Retrieve the generated keys
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Field idField = getIdField(); // Get the @Id field
                    idField.setAccessible(true);
                    Object key = generatedKeys.getObject(1);
                
                    if (idField.getType() == int.class || idField.getType() == Integer.class) {
                        idField.set(entity, ((Number) key).intValue()); // Convert to int
                    } else if (idField.getType() == long.class || idField.getType() == Long.class) {
                        idField.set(entity, ((Number) key).longValue()); // Convert to long
                    } else {
                        idField.set(entity, key); // Directly set if already compatible
                    }
                }
                
            }
        }
        
        session.executeUpdate(sql);
    }
    private Field getIdField() {
    for (Field field : clazz.getDeclaredFields()) {
        if (field.isAnnotationPresent(Id.class)) {
            return field;
        }
    }
    throw new IllegalStateException("No @Id field found in class: " + clazz.getName());
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
    public List<T> read(String whereCondition) throws SQLException, ReflectiveOperationException {
        String query = "SELECT * FROM " + getTableName();
        if (whereCondition != null && !whereCondition.isEmpty()) {
            query += " WHERE " + whereCondition;
        }
    
        System.out.println("Executing SQL Query: " + query); // Debug SQL query
    
        ResultSet rs = session.executeQuery(query);
        List<T> results = new ArrayList<>();
    
        while (rs.next()) {
            T entity = clazz.getDeclaredConstructor().newInstance();  // Create a new instance of the entity
            System.out.println("Mapping ResultSet to Entity: " + clazz.getSimpleName()); // Debug entity mapping
    
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
    
                // Handle @JoinColumn for relationships
                if (field.isAnnotationPresent(JoinColumn.class)) {
                    JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                    String joinColumnName = joinColumn.name();
                    Object foreignKeyValue = rs.getObject(joinColumnName);
    
                    if (foreignKeyValue != null) {
                        if (field.getType() == Integer.class || field.getType() == int.class) {
                            field.set(entity, foreignKeyValue); // Store foreign key
                        } else {
                            // Eagerly fetch related entity
                            GenericDao<?> relatedDao = new GenericDao<>(session, field.getType());
                            Object relatedEntity = relatedDao.findById(foreignKeyValue).orElse(null);
                            field.set(entity, relatedEntity);
                        }
                    }
                    continue;
                }
    
                // Skip @OneToMany
                if (field.isAnnotationPresent(OneToMany.class)) {
                    continue; // ORM will handle this automatically if required
                }
    
                // Map regular columns
                if (field.isAnnotationPresent(Column.class)) {
                    String columnName = field.getAnnotation(Column.class).name();
                    Object columnValue = rs.getObject(columnName);
                    if (columnValue != null) {
                        field.set(entity, columnValue);
                    }
                }
            }
    
            results.add(entity);
        }
        return results;
    }
    public void update(T entity, String whereCondition) throws SQLException, IllegalAccessException {
        String sql = buildUpdateQuery(entity, whereCondition);
        session.executeUpdate(sql);
    }
    
    private String buildUpdateQuery(T entity, String whereCondition) throws IllegalAccessException {
        StringBuilder sql = new StringBuilder("UPDATE " + getTableName() + " SET ");
    
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
    
            // Skip @OneToMany relationships or fields without annotations
            if (field.isAnnotationPresent(OneToMany.class)) {
                continue;
            }
    
            String columnName = null;
            Object value = field.get(entity);
    
            // Handle @JoinColumn for relationships (e.g., teacher_id)
            if (field.isAnnotationPresent(JoinColumn.class)) {
                JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                columnName = joinColumn.name(); // Get the foreign key column name
            } else if (field.isAnnotationPresent(Column.class)) {
                // Handle regular columns
                Column column = field.getAnnotation(Column.class);
                columnName = column.name(); // Get the column name from the annotation
            }
    
            // If no column name was determined, skip this field
            if (columnName == null) {
                continue;
            }
    
            // Append column name and value to the SQL query
            sql.append(columnName).append(" = ");
            appendValue(sql, value);
            sql.append(", ");
        }
    
        // Remove trailing comma
        if (sql.toString().endsWith(", ")) {
            sql.delete(sql.length() - 2, sql.length());
        }
    
        // Add WHERE condition
        if (whereCondition != null && !whereCondition.isEmpty()) {
            sql.append(" WHERE ").append(whereCondition);
        } else {
            throw new IllegalArgumentException("WHERE condition cannot be null or empty for UPDATE query");
        }
    
        return sql.toString();
    }
    
    // Helper method to append values to the SQL query
    private void appendValue(StringBuilder sql, Object value) {
        if (value == null) {
            sql.append("NULL");
        } else if (value instanceof String || value instanceof Date || value instanceof Timestamp) {
            sql.append("'").append(value).append("'");
        } else {
            sql.append(value);
        }
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
    

    private String buildInsertQuery(T entity) throws IllegalAccessException, SecurityException, NoSuchFieldException {
        StringBuilder sql = new StringBuilder("INSERT INTO " + getTableName() + " (");
        StringBuilder values = new StringBuilder("VALUES (");
    
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
    
            // Skip fields annotated with @OneToMany or @OneToOne
            if (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(OneToOne.class)) {
                continue;
            }
    
            // Process fields annotated with @Column
            if (field.isAnnotationPresent(Column.class)) {
                Column columnAnnotation = field.getAnnotation(Column.class);
                String columnName = columnAnnotation.name();
                sql.append(columnName).append(", ");
    
                Object fieldValue = field.get(entity);
                if (fieldValue == null) {
                    values.append("NULL").append(", ");
                } else if (fieldValue instanceof String || fieldValue instanceof Date || fieldValue instanceof Timestamp) {
                    values.append("'").append(fieldValue).append("', ");
                } else {
                    values.append(fieldValue).append(", ");
                }
            }
    
            // Process fields annotated with @ManyToOne
            if (field.isAnnotationPresent(ManyToOne.class)) {
                JoinColumn joinColumn = field.getAnnotation(JoinColumn.class); // Ensure @JoinColumn is present
                String columnName = joinColumn.name(); // Get the name of the foreign key column
                sql.append(columnName).append(", ");
    
                Object relatedEntity = field.get(entity);
                if (relatedEntity == null) {
                    values.append("NULL").append(", ");
                } else {
                    // Assume the related entity has an ID field annotated with @Id
                    Field idField = relatedEntity.getClass().getDeclaredField("id");
                    idField.setAccessible(true);
                    Object relatedId = idField.get(relatedEntity);
                    values.append(relatedId).append(", ");
                }
            }
        }
    
        // Remove trailing commas
        sql.delete(sql.length() - 2, sql.length());
        values.delete(values.length() - 2, values.length());
    
        sql.append(") ").append(values).append(")");
    
        // Debugging: Print the generated SQL
        System.out.println("Generated INSERT SQL: " + sql);
    
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
public SelectBuilder dynamicJoinBuilder() {
    SelectBuilder builder = new SelectBuilder(clazz);

    // Process relationships for JOINs only
    for (Field field : clazz.getDeclaredFields()) {
        if (field.isAnnotationPresent(ManyToOne.class)) {
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            if (joinColumn != null) {
                Class<?> relatedClass = field.getType();
                if (!relatedClass.isAnnotationPresent(Table.class)) {
                    throw new IllegalStateException("Class " + relatedClass.getName() + " is not annotated with @Table");
                }

                String relatedTableName = relatedClass.getAnnotation(Table.class).name();
                String alias = field.getName();
                builder.addJoin(relatedClass, alias,
                        clazz.getAnnotation(Table.class).name() + "." + joinColumn.name() + " = " + alias + ".id");
            }
        }
    }

    return builder;
}










    

public static class SelectBuilder {
    private StringBuilder query;
    private List<String> selectColumns;  // Columns to SELECT
    private List<String> joins;         // JOIN clauses
    private String whereClause;
    private String groupBy;
    private String having;
    private boolean isScalar = false;   // Flag for scalar queries (e.g., COUNT, MAX, etc.)

    private Class<?> clazz;  // Main class for building the query

    public SelectBuilder(Class<?> clazz) {
        this.clazz = clazz;
        query = new StringBuilder("SELECT ");
        this.selectColumns = new ArrayList<>();
        this.joins = new ArrayList<>();
    }

    public SelectBuilder addColumn(String column) {
        if (!column.contains(".")) {
            column = clazz.getAnnotation(Table.class).name() + "." + column;
        }
        selectColumns.add(column); // Add column to SELECT list
        return this;
    }
    
    public SelectBuilder addScalar(String function, String column, String alias) {
        String scalar = function + "(" + column + ")";
        if (alias != null && !alias.isEmpty()) {
            scalar += " AS " + alias;
        }
        selectColumns.add(scalar);
        this.isScalar = true;
        return this;
    }
    

    // Add a JOIN clause
    public SelectBuilder addJoin(Class<?> joinClass, String alias, String onCondition) {
        Table tableAnnotation = joinClass.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new IllegalStateException("Class " + joinClass.getName() + " is not annotated with @Table");
        }

        String tableName = tableAnnotation.name();
        if (joins.stream().noneMatch(join -> join.contains(alias))) { // Avoid duplicate joins
            joins.add("JOIN " + tableName + " " + alias + " ON " + onCondition);
        }
        return this;
    }

    // Add a WHERE clause
    public SelectBuilder where(String whereCondition) {
        this.whereClause = whereCondition;
        return this;
    }

    // Add a GROUP BY clause
    public SelectBuilder groupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    // Add a HAVING clause
    public SelectBuilder having(String having) {
        this.having = having;
        return this;
    }

    // Build the final SELECT query
    public String buildSelectQuery() {
        if (selectColumns.isEmpty()) {
            throw new IllegalStateException("No columns selected.");
        }
     // Validate selectColumns compatibility with GROUP BY
     if (groupBy != null && !groupBy.isEmpty()) {
        for (String column : selectColumns) {
            if (!column.contains("COUNT") && !groupBy.contains(column.split("\\.")[1])) {
                throw new IllegalStateException("Column " + column + " is not valid for GROUP BY query.");
            }
        }
    }
    
        // Initialize a new StringBuilder for each query
        StringBuilder query = new StringBuilder("SELECT ");
    
        // Add SELECT columns
        query.append(String.join(", ", selectColumns)).append(" FROM ")
             .append(getTableName(clazz)).append(" ");
    
        // Add JOIN clauses
        for (String join : joins) {
            query.append(join).append(" ");
        }
    
        // Add WHERE clause
        if (whereClause != null && !whereClause.isEmpty()) {
            query.append("WHERE ").append(whereClause).append(" ");
        }
    
        // Add GROUP BY clause
        if (groupBy != null && !groupBy.isEmpty()) {
            query.append("GROUP BY ").append(groupBy).append(" ");
        }
    
        // Add HAVING clause
        if (having != null && !having.isEmpty()) {
            query.append("HAVING ").append(having).append(" ");
        }
    
        return query.toString();
    }
    

    // Helper to get table name from @Table annotation
    private String getTableName(Class<?> clazz) {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
            return tableAnnotation.name();
        }
        return convertToSnakeCase(clazz.getSimpleName());
    }

    // Helper to convert camelCase to snake_case
    private String convertToSnakeCase(String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (result.length() > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public boolean isScalar() {
        return isScalar;
    }
}

    
    
}
