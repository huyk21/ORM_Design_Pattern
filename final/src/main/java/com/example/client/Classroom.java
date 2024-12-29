package com.example.client;

import java.sql.JDBCType;
import java.util.ArrayList;

import com.example.annotation.Column;
import com.example.annotation.Id;
import com.example.annotation.OneToMany;
import com.example.annotation.Table;


@Table(name = "classes")
public class Classroom {
    public Classroom() {
        students = new ArrayList<>();
    }

    @Id 
    @Column(name = "id", type = JDBCType.INTEGER)
    private int id;
    
    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "year")
    private int year;

    @OneToMany(mappedBy = "class_id")
    private ArrayList<User> students;

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

    public ArrayList<User> getStudents() {
        return students;
    }

    public void setStudents(ArrayList<User> students) {
        this.students = students;
    }
}
