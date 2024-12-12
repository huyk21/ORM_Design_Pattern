package com.example.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgreSQLConnectionFactory implements ConnectionFactory {
    private final DatabaseConfiguration config;

    public PostgreSQLConnectionFactory(DatabaseConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("Database configuration cannot be null");
        }
        this.config = config;
    }

    // Static factory method for common Postgre configurations
    public static PostgreSQLConnectionFactory createDefault(String host, String port, String database, String username,
            String password) {
        DatabaseConfiguration config = new DatabaseConfiguration.Builder(
                host, port, database, username, password)
                .build();
        return new PostgreSQLConnectionFactory(config);
    }

    @Override
    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection(
                getConnectionURL(),
                config.toProperties());
    }

    @Override
    public String getConnectionURL() {
        return config.generateConnectionUrl("jdbc:postgresql");
    }
}
