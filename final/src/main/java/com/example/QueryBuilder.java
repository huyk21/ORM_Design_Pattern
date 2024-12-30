// File: QueryBuilder.java
package com.example;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.example.annotation.Column;
import com.example.annotation.Id;
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
    public String buildInsertQuery(T entity) throws IllegalAccessException {
        List<String> columns = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                columns.add(column.name());
                placeholders.add("?");
            }
        }

        String columnsPart = String.join(", ", columns);
        String placeholdersPart = String.join(", ", placeholders);

        return "INSERT INTO " + getTableName() + " (" + columnsPart + ") VALUES (" + placeholdersPart + ")";
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
        List<String> setClauses = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class) && !field.isAnnotationPresent(Id.class)) {
                Column column = field.getAnnotation(Column.class);
                setClauses.add(column.name() + " = ?");
            }
        }

        String setPart = String.join(", ", setClauses);
        return "UPDATE " + getTableName() + " SET " + setPart + " WHERE " + whereCondition;
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
