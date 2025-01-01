package com.example.client;


import java.sql.JDBCType;

import com.example.annotation.Column;
import com.example.annotation.Id;
import com.example.annotation.JoinColumn;
import com.example.annotation.ManyToOne;
import com.example.annotation.Table;

@Table(name = "subjects")
public class Subject {
    @Id
    @Column(name = "id", type = JDBCType.INTEGER)
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "credit", type = JDBCType.INTEGER)
    private int credit;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true) // One-to-Many with Users
    private User user;

    // Getters and Setters
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

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Subject{id=" + id + ", name='" + name + '\'' + ", credit=" + credit + '}';
    }
}
