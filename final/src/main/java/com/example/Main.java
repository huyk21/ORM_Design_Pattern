package com.example;

import com.example.client.Classroom;
import com.example.client.User;
import com.example.connection.DatabaseSession;
import com.example.connection.MySQLConnectionFactory;

import java.sql.SQLException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // Create the MySQL connection factory using the default configuration
        MySQLConnectionFactory factory = MySQLConnectionFactory.createDefault(
                "localhost", // host
                "3306", // port
                "dam_framework", // database
                "root", // username
                "mysql" // password
        );

        try {
            // Create a DatabaseSession using the factory
            DatabaseSession session = new DatabaseSession(factory);

            // Create a GenericDao for the User entity
            GenericDao<Classroom> dao = new GenericDao<>(session, Classroom.class);

            List<Classroom> classes = dao.select().join("root", "students", "s").get();
            for (var classroom : classes) {
                System.out.println(classroom.getName());
                for (var student : classroom.getStudents()) {
                    System.out.println("\t" + student.getFullName());
                }
            }
            System.out.println("------");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
