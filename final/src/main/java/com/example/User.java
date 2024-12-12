package com.example;

import com.example.annotation.Column;
import com.example.annotation.Table;
import com.example.entity.BaseEntity;

import java.lang.reflect.Field;

@Table(name = "users") // Ensure that the @Table annotation is correct and corresponds to the "users" table in your database
public class User extends BaseEntity {

    @Column(name = "id")
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTableName() {
        Table tableAnnotation = this.getClass().getAnnotation(Table.class);
        if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
            return tableAnnotation.name();
        }
    
        // Fallback to class name and convert to snake_case
        return convertToSnakeCase(this.getClass().getSimpleName());
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
    

    // Override the getColumnName method to reflectively get column names
    @Override
    public String getColumnName(Field field) {
        Column columnAnnotation = field.getAnnotation(Column.class);
        if (columnAnnotation != null) {
            return columnAnnotation.name();
        }
        return null; // Return null or throw an exception if no column annotation is present
    }
}
