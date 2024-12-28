package com.example.client;


import java.sql.JDBCType;

import com.example.annotation.Column;
import com.example.annotation.Id;
import com.example.annotation.Table;

@Table(name = "subjects") // Bảng "subjects"
public class Subject {
    @Id
    @Column(name = "id", type = JDBCType.INTEGER)
    private int id;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "credit")
    private int credit;

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

    @Override
    public String toString() {
        return "Subject{id=" + id + ", name='" + name + '\'' + ", credit=" + credit + '}';
    }
}

