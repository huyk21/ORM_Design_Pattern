package com.example.client;

import java.sql.Date;
import java.sql.JDBCType;
import java.sql.Timestamp;

import com.example.annotation.Column;
import com.example.annotation.Id;
import com.example.annotation.Table;

@Table(name = "users") // Ensure that the @Table annotation is correct and corresponds to the "users" table in your database
public class User implements UserProxy {
    public User() {
        // Default constructor
    }
    @Id 
    @Column(name = "id", type = JDBCType.INTEGER)
    private int id;

    @Column(name = "username", unique = true)
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
    @Override
public String toString() {
    return "User{id=" + id +
           ", username='" + username + '\'' +
           ", password='" + password + '\'' +
           ", email='" + email + '\'' +
           ", fullName='" + fullName + '\'' +
           ", dateOfBirth=" + dateOfBirth +
           ", isActive=" + isActive +
           ", createdAt=" + createdAt +
           ", updatedAt=" + updatedAt +
           '}';
}

}
