// File: QueryBuilder.java
package com.example;

import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.Timestamp;

import com.example.annotation.Column;
import com.example.annotation.Id;
import com.example.annotation.JoinColumn;
import com.example.annotation.ManyToOne;
import com.example.annotation.OneToMany;
import com.example.annotation.OneToOne;
import com.example.annotation.Table;

/**
 * Responsible for building SQL queries for CRUD operations.
 *
 * @param <T> The type of the entity.
 */
public class QueryBuilder<T> {

    private final Class<T> clazz;

    /**
     * Constructor initializing the QueryBuilder with the entity class.
     *
     * @param clazz The Class type of the entity.
     */
    public QueryBuilder(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * Builds an INSERT SQL query for the given entity.
     *
     * @param entity The entity to insert.
     * @return The SQL INSERT query string.
     * @throws IllegalAccessException If field access fails.
     */
    public String buildInsertQuery(T entity) throws IllegalAccessException, NoSuchFieldException {
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

    /**
     * Builds a SELECT BY ID SQL query.
     *
     * @param id The ID of the entity.
     * @return The SQL SELECT query string.
     */
    public String buildSelectByIdQuery(Object id) {
        String idColumn = getIdColumn();
        return "SELECT * FROM " + getTableName() + " WHERE " + idColumn + " = " + id;
    }

    /**
     * Builds a READ SQL query with a WHERE condition.
     *
     * @param whereCondition The WHERE condition.
     * @return The SQL SELECT query string.
     */
    public String buildReadQuery(String whereCondition) {
        if (whereCondition == null || whereCondition.trim().isEmpty()) {
            return "SELECT * FROM " + getTableName();
        }
        return "SELECT * FROM " + getTableName() + " WHERE " + whereCondition;
    }
    

    /**
     * Builds an UPDATE SQL query.
     *
     * @param entity         The entity to update.
     * @param whereCondition The WHERE condition.
     * @return The SQL UPDATE query string.
     * @throws IllegalAccessException If field access fails.
     */
    public String buildUpdateQuery(T entity, String whereCondition) throws IllegalAccessException {
        StringBuilder sql = new StringBuilder("UPDATE " + getTableName() + " SET ");
        System.err.println(sql);
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
    private void appendValue(StringBuilder sql, Object value) {
        // Debug: Log the incoming value and its type
        System.out.println("appendValue: Processing value = " + value + " (Type: " + (value != null ? value.getClass().getName() : "null") + ")");
    
        if (value == null) {
            sql.append("NULL");
            System.out.println("appendValue: Appended NULL");
        } else if (value instanceof String || value instanceof Date || value instanceof Timestamp) {
            sql.append("'").append(value).append("'");
            System.out.println("appendValue: Appended quoted value = '" + value + "'");
        } else if (value instanceof Number || value instanceof Boolean) {
            sql.append(value);
            System.out.println("appendValue: Appended numeric/boolean value = " + value);
        } else {
            // Handle objects (e.g., related entities)
            try {
                Field idField = value.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                Object idValue = idField.get(value);
    
                if (idValue != null) {
                    sql.append(idValue);
                    System.out.println("appendValue: Appended related entity ID = " + idValue);
                } else {
                    sql.append("NULL");
                    System.out.println("appendValue: Related entity ID is NULL");
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // Debug: Log the error with context
                System.err.println("appendValue: Error extracting ID from related entity: " + value);
                e.printStackTrace();
                throw new IllegalStateException("Unable to extract ID from related entity: " + value, e);
            }
        }
    }
    
    /**
     * Builds a DELETE SQL query with a WHERE condition.
     *
     * @param whereCondition The WHERE condition.
     * @return The SQL DELETE query string.
     */
    public String buildDeleteQuery(String whereCondition) {
        return "DELETE FROM " + getTableName() + " WHERE " + whereCondition;
    }

    /**
     * Builds a dynamic SelectBuilder for complex JOIN operations.
     *
     * @return A SelectBuilder instance.
     */
    public SelectBuilder<T> buildDynamicJoinBuilder() {
        return new SelectBuilder<>(clazz);
    }

    /**
     * Retrieves the table name from the @Table annotation or converts the class name to snake_case.
     *
     * @return The table name.
     */
    private String getTableName() {
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
            return tableAnnotation.name();
        }
        return convertToSnakeCase(clazz.getSimpleName());
    }

    /**
     * Retrieves the column name of the field annotated with @Id.
     *
     * @return The ID column name.
     */
    private String getIdColumn() {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class) && field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                return column.name();
            }
        }
        throw new IllegalStateException("No field annotated with @Id in class " + clazz.getName());
    }

    /**
     * Converts CamelCase to snake_case.
     *
     * @param input The CamelCase string.
     * @return The snake_case string.
     */
    private String convertToSnakeCase(String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (result.length() > 0)
                    result.append("_");
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
