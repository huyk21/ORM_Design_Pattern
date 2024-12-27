package com.example;

import java.sql.SQLException;

import com.example.client.User;
import com.example.connection.DatabaseSession;
import com.example.connection.MySQLConnectionFactory;

public class Main {

    public static void main(String[] args) {
        // Create the MySQL connection factory
        MySQLConnectionFactory factory = MySQLConnectionFactory.createDefault(
                "localhost", "3306", "damframework", "root", "ducanh123"
        );

        DatabaseSession session = null;

        try {
            // Initialize the session
            session = new DatabaseSession(factory);

            GenericDao<User> userDao = new GenericDao<>(session, User.class);

            // Lazily load a user
            User lazyUser = userDao.getLazy(User.class, 11);

            // Access properties to trigger lazy loading
            System.out.println(lazyUser.getFullName());
           
    

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Ensure the session is closed
            if (session != null) {
                try {
                    session.closeConnection();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
