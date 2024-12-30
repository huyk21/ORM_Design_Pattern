// File: ColumnMetadata.java
package com.example;

import java.lang.reflect.Field;

import com.example.annotation.Column;
import com.example.annotation.JoinColumn;

/**
 * Holds metadata information about a column.
 * Applies Single Responsibility Principle.
 */
public class ColumnMetadata {

    private final Field field;
    private final String columnName;

    /**
     * Constructor that extracts column information from a field.
     *
     * @param field The Field object.
     */
    public ColumnMetadata(Field field) {
        this.field = field;
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            this.columnName = column.name();
        } else if (field.isAnnotationPresent(JoinColumn.class)) {
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            this.columnName = joinColumn.name();
        } else {
            this.columnName = convertToSnakeCase(field.getName());
        }
    }

    /**
     * Retrieves the associated field.
     *
     * @return The Field object.
     */
    public Field getField() {
        return field;
    }

    /**
     * Retrieves the column name in the database.
     *
     * @return The column name.
     */
    public String getColumnName() {
        return columnName;
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
