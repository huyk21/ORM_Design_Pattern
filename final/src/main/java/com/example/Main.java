package com.example;

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
            GenericDao<User> dao = new GenericDao<>(session, User.class);

            List<User> users = dao.select().join("root", "teacher", "t").get();
            for (var user : users) {
                System.out.println(user.getFullName());
                System.out.println(user.getTeacher().getFullName());
                System.out.println("------");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
