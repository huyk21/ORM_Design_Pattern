package com.example;
import java.lang.reflect.Field;

import com.example.annotation.Column;
import com.example.annotation.Table;

public abstract class BaseEntity {
    
    public String getTableName() {
        // Get the table name using reflection
        if (this.getClass().isAnnotationPresent(Table.class)) {
            Table tableAnnotation = this.getClass().getAnnotation(Table.class);
            return tableAnnotation.name();
        }
        return this.getClass().getSimpleName().toLowerCase(); // Default to class name
    }

    public String getColumnName(Field field) {
        // Get the column name using reflection
        if (field.isAnnotationPresent(Column.class)) {
            Column columnAnnotation = field.getAnnotation(Column.class);
            return columnAnnotation.name();
        }
        return field.getName(); // Default to field name
    }
}
