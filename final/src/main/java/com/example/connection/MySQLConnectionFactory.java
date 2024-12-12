package com.example.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnectionFactory implements ConnectionFactory {
    private final DatabaseConfiguration config;

    public MySQLConnectionFactory(DatabaseConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("Database configuration cannot be null");
        }
        this.config = config;
    }

    // Static factory method for common MySQL configurations
    public static MySQLConnectionFactory createDefault(String host, String port, String database, String username,
            String password) {
        DatabaseConfiguration config = new DatabaseConfiguration.Builder(
                host, port, database, username, password)
                .build();
        return new MySQLConnectionFactory(config);
    }

    // Create the connection using the configuration
    @Override
    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection(
                getConnectionURL(),
                config.toProperties());
    }

    // Generate the connection URL
    @Override
    public String getConnectionURL() {
        return config.generateConnectionUrl("jdbc:mysql");
    }
}
