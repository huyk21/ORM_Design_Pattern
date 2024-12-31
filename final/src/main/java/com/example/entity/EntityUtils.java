package com.example.entity;

import java.lang.reflect.Field;

import com.example.annotation.Id;
import com.example.annotation.Table;

public class EntityUtils {
    private EntityUtils() {
    } // Prevent instantiation

    public static String convertToSnakeCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (!result.isEmpty()) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static String getTableName(Class<?> entityClass) {
        if (entityClass.isAnnotationPresent(Table.class)) {
            String name = entityClass.getAnnotation(Table.class).name();
            return name.isEmpty() ? convertToSnakeCase(entityClass.getSimpleName()) : name;
        }
        return convertToSnakeCase(entityClass.getSimpleName());
    }

    public static Field getIdField(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }
        throw new IllegalStateException("No @Id field found in class: " + entityClass.getName());
    }
}
