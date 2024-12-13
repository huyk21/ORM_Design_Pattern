package com.example;

import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.Timestamp;

@Table(name = "users")  // Ensure that the @Table annotation is correct and corresponds to the "users" table in your database
public class User extends BaseEntity {

    @Column(name = "id")
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email")
    private String email;

    @Column(name = "full_name")
    private String fullName;  // Changed to camelCase

    @Column(name = "date_of_birth")
    private Date dateOfBirth;  // Changed to camelCase

    @Column(name = "is_active")
    private boolean isActive;  // Changed to camelCase

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Get the table name dynamically from the @Table annotation or fall back to class name
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
        return null;  // Return null or throw an exception if no column annotation is present
    }
}
