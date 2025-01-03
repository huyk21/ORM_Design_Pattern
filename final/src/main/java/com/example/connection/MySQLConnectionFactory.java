package com.example.connection;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLConnectionFactory implements ConnectionFactory {
    private final DatabaseConfiguration config;
    private final ConnectionPoolManager poolManager;

    public MySQLConnectionFactory(DatabaseConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("Database configuration cannot be null");
        }
        this.config = config;
        this.poolManager = ConnectionPoolManager.getInstance();
    }

    // Static factory method for common MySQL configurations
    public static MySQLConnectionFactory createDefault(String host, String port, String database, String username,
            String password) {
        DatabaseConfiguration config = new DatabaseConfiguration.Builder(
                host, port, database, username, password)
                .build();
        return new MySQLConnectionFactory(config);
    }

    @Override
    public Connection createConnection() throws SQLException {
        return poolManager.getConnection(this);
    }


    // Generate the connection URL
    @Override
    public String getConnectionURL() {
        return generateConnectionUrl();
    }

    @Override
    public DatabaseConfiguration getConfig() {
        return config;
    }

    private String generateConnectionUrl() {
        return new StringBuilder("jdbc:mysql://")
                .append(config.getHost())
                .append(":")
                .append(config.getPort())
                .append("/")
                .append(config.getDatabase())
                .append("?useSSL=")
                .append(config.isSsl())
                .toString();
    }
}
