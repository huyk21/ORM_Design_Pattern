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
                "ORMX", // database
                "root", // username
                "mysql" // password
        );

        try {
            // Create a DatabaseSession using the factory
            DatabaseSession session = new DatabaseSession(factory);

            // Create a GenericDao for the User entity
            GenericDao<User> dao = new GenericDao<>(session, User.class);

            // Example: Select Users with id and COUNT(id), GROUP BY id, HAVING COUNT(id) > 0
            GenericDao.SelectBuilder builder = new GenericDao.SelectBuilder(User.class)
            .addColumn("id")
            .addColumn("username")
            .addColumn("email");  // Add columns you want to select

            // Execute the query and get the results as a list of Objects (e.g., Integer)
            List<Object> groupedResults = dao.select(builder);

            // Print the scalar result (count of ids)
            for (Object result : groupedResults) {
                User user = (User) result;  // Cast to User entity
                System.out.println("User ID: " + user.getId() + ", Username: " + user.getUsername() );
            }

        } catch (SQLException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
