package com.example.connection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConnectionPoolManager {

    // Thread-safe connection pool registry
    private final ConcurrentHashMap<String, HikariDataSource> connectionPools = new ConcurrentHashMap<>();

    // Singleton instance
    private static volatile ConnectionPoolManager instance;

    // Private constructor to prevent direct instantiation
    private ConnectionPoolManager() {
    }

    // Thread-safe singleton with double-checked locking
    public static ConnectionPoolManager getInstance() {
        if (instance == null) {
            synchronized (ConnectionPoolManager.class) {
                if (instance == null) {
                    instance = new ConnectionPoolManager();
                }
            }
        }
        return instance;
    }

    public HikariDataSource getOrCreateConnectionPool(ConnectionFactory factory) {
        // Use a unique key based on configuration
        DatabaseConfiguration config = factory.getConfig();
        String poolKey = generatePoolKey(config);

        // Check if pool already exists
        return connectionPools.computeIfAbsent(poolKey, k -> {
            HikariConfig hikariConfig = new HikariConfig();

            // Database connection details
            hikariConfig.setJdbcUrl(factory.getConnectionURL());
            hikariConfig.setUsername(config.getUsername());
            hikariConfig.setPassword(config.getPassword());

            // Connection pool configuration
            hikariConfig.setMaximumPoolSize(config.getMaxConnections());
            hikariConfig.setMinimumIdle(config.getMinConnections());
            hikariConfig.setConnectionTimeout(config.getConnectionTimeoutMs());
            hikariConfig.setIdleTimeout(config.getIdleTimeoutMs());
            hikariConfig.setMaxLifetime(config.getMaxLifetimeMs());

            // Optional: Driver-specific properties
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            // Create and return the connection pool
            return new HikariDataSource(hikariConfig);
        });
    }

    public Connection getConnection(ConnectionFactory factory) throws SQLException {
        HikariDataSource dataSource = getOrCreateConnectionPool(factory);
        return dataSource.getConnection();
    }

    public void closeAllPools() {
        connectionPools.values().forEach(HikariDataSource::close);
        connectionPools.clear();
    }

    private String generatePoolKey(DatabaseConfiguration config) {
        return config.getHost() + ":" +
                config.getPort() + "/" +
                config.getDatabase() + "@" +
                config.getUsername();
    }

    // Prevent cloning
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Singleton, cannot be cloned");
    }
}