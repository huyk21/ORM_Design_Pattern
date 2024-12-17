package com.example.client;

import com.example.annotation.Column;
import com.example.annotation.Id;
import com.example.annotation.Table;

import java.sql.JDBCType;

@Table(name = "users") // Ensure that the @Table annotation is correct and corresponds to the "users" table in your database
public class User {

    @Id 
    @Column(name = "id", type = JDBCType.INTEGER)
    private int id;

    @Column(name = "username", unique = true)
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
}
