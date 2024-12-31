package com.example.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgreSQLConnectionFactory implements ConnectionFactory {
    private final DatabaseConfiguration config;
    private final ConnectionPoolManager poolManager;


    public PostgreSQLConnectionFactory(DatabaseConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("Database configuration cannot be null");
        }
        this.config = config;
        this.poolManager = ConnectionPoolManager.getInstance();
    }

    // Static factory method for common Postgre configurations
    public static PostgreSQLConnectionFactory createDefault(String host, String port, String database, String username, String password) {
        DatabaseConfiguration config = new DatabaseConfiguration.Builder(host, port, database, username, password).build();
        return new PostgreSQLConnectionFactory(config);
    }

    @Override
    public Connection createConnection() throws SQLException {
        return poolManager.getConnection(this);
    }

    @Override
    public String getConnectionURL() {
        return generateConnectionUrl();
    }

    @Override
    public DatabaseConfiguration getConfig() {
        return config;
    }

    private String generateConnectionUrl() {
        return new StringBuilder("jdbc:postgresql://")
                .append(config.getHost())
                .append(":")
                .append(config.getPort())
                .append("/")
                .append(config.getDatabase())
                .append("?ssl=")
                .append(config.isSsl())
                .toString();
    }
}
