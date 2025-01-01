package com.example.client;


import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;

import com.example.annotation.Column;
import com.example.annotation.Id;
import com.example.annotation.OneToMany;
import com.example.annotation.Table;
@Table(name = "classes")
public class Class {
    @Id
    @Column(name = "id", type = JDBCType.INTEGER)
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "year", type = JDBCType.INTEGER)
    private int year;

    @OneToMany(mappedBy = "classObject") // Back reference for Users
    private List<User> users = new ArrayList<>(); // Initialize to avoid null issues

    //Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "Class{id=" + id + ", name='" + name + '\'' + ", year=" + year + '}';
    }
}

