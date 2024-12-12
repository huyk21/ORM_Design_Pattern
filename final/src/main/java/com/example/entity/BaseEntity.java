package com.example.entity;
import com.example.annotation.Column;
import com.example.annotation.Table;

import java.lang.reflect.Field;

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
