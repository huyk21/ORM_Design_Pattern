package com.example.client;

import java.sql.Date;
import java.sql.JDBCType;
import java.sql.Timestamp;
import java.util.List;

import com.example.annotation.Column;
import com.example.annotation.Id;
import com.example.annotation.JoinColumn;
import com.example.annotation.ManyToOne;
import com.example.annotation.OneToMany;
import com.example.annotation.Table;
import com.example.annotation.validator.Alphanumeric;
import com.example.annotation.validator.NotNull;

@Table(name = "users")
public class User {
    @Id
    @Column(name = "id", type = JDBCType.INTEGER)
    private int id;

    @NotNull(message = "Username cannot be null.")
    @Alphanumeric()
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "date_of_birth", type = JDBCType.DATE)
    private Date dateOfBirth;

    @Column(name = "is_active", type = JDBCType.BOOLEAN)
    private boolean isActive;

    @Column(name = "created_at", type = JDBCType.TIMESTAMP)
    private Timestamp createdAt;

    @Column(name = "updated_at", type = JDBCType.TIMESTAMP)
    private Timestamp updatedAt;

    @JoinColumn(name = "teacher_id", nullable = true) // Self-referencing (One-to-One)
    private User teacher;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = true) // Many-to-One relationship with Classes
    private Class classObject;

    @OneToMany(mappedBy = "user") // One-to-Many relationship with Subjects
    private List<Subject> subjects;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public void setActive(boolean isActive) {
        this.isActive = isActive;
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

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public Class getClassObject() {
        return classObject;
    }

    public void setClassObject(Class classObject) {
        this.classObject = classObject;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    @Override
    public String toString() {
        return "User{id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", teacher=" + (teacher != null ? teacher.getUsername() : "null") +
                ", classObject=" + (classObject != null ? classObject.getName() : "null") +
                ", subjects=" + (subjects != null ? subjects.size() : 0) +
                '}';
    }
}
