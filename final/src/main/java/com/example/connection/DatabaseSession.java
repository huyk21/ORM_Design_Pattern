package com.example.connection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseSession {

    private final Connection connection;

    // Constructor that accepts ConnectionFactory instead of connection parameters
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
    public void beginTransaction() throws SQLException {
        connection.setAutoCommit(false);
    }
    
    public void commitTransaction() throws SQLException {
        connection.commit();
        connection.setAutoCommit(true);
    }
    
    public void rollbackTransaction() throws SQLException {
        connection.rollback();
        connection.setAutoCommit(true);
    }
    public List<Object[]> executeCustomJoinQuery(String query) throws SQLException {
        System.out.println("Executing SQL Query: " + query); // Debug query
    
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            List<Object[]> results = new ArrayList<>();
            int columnCount = rs.getMetaData().getColumnCount();
    
            System.out.println("Number of columns in result: " + columnCount); // Debug column count
    
            // Log column metadata
            for (int i = 1; i <= columnCount; i++) {
                System.out.println("Column " + i + ": " + rs.getMetaData().getColumnName(i));
            }
    
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                results.add(row);
            }
    
            return results;
        } catch (SQLException e) {
            System.err.println("SQL Execution Error: " + e.getMessage());
            throw e; // Rethrow for higher-level handling
        }
    }
    

}
