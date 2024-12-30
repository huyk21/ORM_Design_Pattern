// File: EntityMetadata.java
package com.example;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.example.annotation.Id;
import com.example.annotation.Table;

/**
 * Holds metadata information about an entity.
 * Applies Single Responsibility Principle.
 */
public class EntityMetadata {

    private final Class<?> clazz;
    private final List<ColumnMetadata> columns;
    private final ColumnMetadata idColumn;
    private final String tableName;

    /**
     * Constructor that extracts metadata from the entity class.
     *
     * @param clazz The Class type of the entity.
     */
    public EntityMetadata(Class<?> clazz) {
        this.clazz = clazz;
        this.columns = new ArrayList<>();
        ColumnMetadata tempIdColumn = null;
        String tempTableName = "";

        if (clazz.isAnnotationPresent(Table.class)) {
            Table table = clazz.getAnnotation(Table.class);
            tempTableName = table.name().isEmpty() ? convertToSnakeCase(clazz.getSimpleName()) : table.name();
        } else {
            tempTableName = convertToSnakeCase(clazz.getSimpleName());
        }

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(com.example.annotation.Column.class) ||
                field.isAnnotationPresent(com.example.annotation.JoinColumn.class)) {
                ColumnMetadata column = new ColumnMetadata(field);
                columns.add(column);
                if (field.isAnnotationPresent(Id.class)) {
                    tempIdColumn = column;
                }
            }
        }

        if (tempIdColumn == null) {
            throw new IllegalStateException("No @Id field found in class: " + clazz.getName());
        }

        this.idColumn = tempIdColumn;
        this.tableName = tempTableName;
    }

    /**
     * Retrieves all column metadata.
     *
     * @return List of ColumnMetadata.
     */
    public List<ColumnMetadata> getColumns() {
        return columns;
    }

    /**
     * Retrieves the ID column metadata.
     *
     * @return The ID ColumnMetadata.
     */
    public ColumnMetadata getIdColumn() {
        return idColumn;
    }

    /**
     * Retrieves the table name.
     *
     * @return The table name.
     */
    public String getTableName() {
        return tableName;
    }

    // ----------------------------
    // Helper Methods
    // ----------------------------

    /**
     * Converts a CamelCase string to snake_case.
     *
     * @param input The CamelCase string.
     * @return The snake_case string.
     */
    private String convertToSnakeCase(String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (result.length() > 0) result.append('_');
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
