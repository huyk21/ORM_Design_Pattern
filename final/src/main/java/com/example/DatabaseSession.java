package com.example;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSession {

    private Connection connection;

    // Constructor that accepts MySQLConnectionFactory instead of connection parameters
    public DatabaseSession(ConnectionFactory factory) throws SQLException {
        // Create the connection using the factory
        this.connection = factory.createConnection();
    }

    // Getter for connection
    public Connection getConnection() {
        return connection;
    }

    // Method to close the connection
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // Method to perform a generic SELECT query
    public ResultSet executeQuery(String query) throws SQLException {
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(query);
    }

    // Method to perform INSERT, UPDATE, DELETE queries
    public int executeUpdate(String query) throws SQLException {
        Statement stmt = connection.createStatement();
        return stmt.executeUpdate(query);
    }
}
