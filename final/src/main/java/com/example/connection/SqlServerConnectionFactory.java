package com.example.connection;

import java.sql.Connection;
import java.sql.SQLException;

public class SqlServerConnectionFactory implements ConnectionFactory{
    private final DatabaseConfiguration config;
    private final ConnectionPoolManager poolManager;

    public SqlServerConnectionFactory(DatabaseConfiguration config) {
        if (config == null) {
            throw new IllegalArgumentException("Database configuration cannot be null");
        }
        this.config = config;
        this.poolManager = ConnectionPoolManager.getInstance();
    }

    // Static factory method for common Sql Server configurations
    public static SqlServerConnectionFactory createDefault(String host, String port, String database, String username, String password) {
        DatabaseConfiguration config = new DatabaseConfiguration.Builder(host, port, database, username, password).build();
        return new SqlServerConnectionFactory(config);
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
        return new StringBuilder("jdbc:sqlserver://")
                .append(config.getHost())
                .append(":")
                .append(config.getPort())
                .append(";databaseName=")
                .append(config.getDatabase())
                .append(";trustServerCertificate=true")
                .toString();
    }
}
