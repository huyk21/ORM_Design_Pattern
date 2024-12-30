package com.example.connection;

import java.util.Properties;

public class DatabaseConfiguration {

    private final String host;
    private final String port;
    private final String database;
    private final String username;
    private final String password;
    // Optional fields
    private final int maxConnections;
    private final int minConnections;
    private final boolean ssl;
    private final long connectionTimeoutMs;
    private final long idleTimeoutMs;
    private final long maxLifetimeMs;
    private final int minPoolSize;
    private final int maxPoolSize;

    // Private constructor to enforce builder usage
    private DatabaseConfiguration(Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.database = builder.database;
        this.username = builder.username;
        this.password = builder.password;
        this.maxConnections = builder.maxConnections;
        this.minConnections = builder.minConnections;
        this.ssl = builder.ssl;
        this.connectionTimeoutMs = builder.connectionTimeoutMs;
        this.idleTimeoutMs = builder.idleTimeoutMs;
        this.maxLifetimeMs = builder.maxLifetimeMs;
        this.minPoolSize = builder.minPoolSize;
        this.maxPoolSize = builder.maxPoolSize;
    }

    // Getters (no setters to maintain immutability)
    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public int getMinConnections() { return minConnections;}

    public boolean isSsl() {
        return ssl;
    }

    public long getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public long getIdleTimeoutMs() {
        return idleTimeoutMs;
    }

    public long getMaxLifetimeMs() {
        return maxLifetimeMs;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    // Convert to Properties for JDBC connection
    public Properties toProperties() {
        Properties props = new Properties();
        if (username != null && !username.isEmpty()) {
            props.setProperty("user", username);
        }
        if (password != null && !password.isEmpty()) {
            props.setProperty("password", password);
        }
        if (connectionTimeoutMs > 0) {
            props.setProperty("connectTimeout", String.valueOf(connectionTimeoutMs));
        }
        if (idleTimeoutMs > 0) {
            props.setProperty("idleTimeout", String.valueOf(idleTimeoutMs));
        }
        if (maxLifetimeMs > 0) {
            props.setProperty("maxLifetime", String.valueOf(maxLifetimeMs));
        }
        props.setProperty("maxConnections", String.valueOf(maxConnections));
        props.setProperty("minConnections", String.valueOf(minConnections));
        props.setProperty("ssl", String.valueOf(ssl));
        props.setProperty("minimumIdle", String.valueOf(minPoolSize));
        props.setProperty("maximumPoolSize", String.valueOf(maxPoolSize));
        return props;
    }

    // Builder Pattern
    public static class Builder {
        // Required parameters
        private final String host;
        private final String port;
        private final String database;
        private final String username;
        private final String password;

        // Optional parameters with default values
        private int maxConnections = 10;
        private int minConnections = 2;
        private boolean ssl = false;
        private long connectionTimeoutMs = 5000;
        private long idleTimeoutMs = 600000;  // 10 minutes
        private long maxLifetimeMs = 1800000; // 30 minutes
        private int minPoolSize = 1;
        private int maxPoolSize = 10;

        // Constructor with required parameters
        public Builder(String host, String port, String database,
                String username, String password) {
            this.host = host;
            this.port = port;
            this.database = database;
            this.username = username;
            this.password = password;
        }

        // Fluent setters for optional parameters
        public Builder maxConnections(int maxConnections) {
            if (maxConnections <= 0) {
                throw new IllegalArgumentException("Max connections must be positive");
            }
            this.maxConnections = maxConnections;
            return this;
        }

        public Builder minConnections(int minConnections) {
            if (minConnections <= 0) {
                throw new IllegalArgumentException("Min connections must be positive");
            }
            this.minConnections = minConnections;
            return this;
        }

        public Builder enableSsl(boolean ssl) {
            this.ssl = ssl;
            return this;
        }

        public Builder connectionTimeout(long timeoutMs) {
            if (timeoutMs < 0) {
                throw new IllegalArgumentException("Timeout must be non-negative");
            }
            this.connectionTimeoutMs = timeoutMs;
            return this;
        }

        public Builder idleTimeout(long timeoutMs) {
            if (timeoutMs < 0) {
                throw new IllegalArgumentException("Timeout must be non-negative");
            }
            this.idleTimeoutMs = timeoutMs;
            return this;
        }

        public Builder maxLifetime(long lifetimeMs) {
            if (lifetimeMs < 0) {
                throw new IllegalArgumentException("Lifetime must be non-negative");
            }
            this.maxLifetimeMs = lifetimeMs;
            return this;
        }

        public Builder poolSize(int minSize, int maxSize) {
            if (minSize < 0 || maxSize < minSize) {
                throw new IllegalArgumentException("Invalid pool size configuration");
            }
            this.minPoolSize = minSize;
            this.maxPoolSize = maxSize;
            return this;
        }

        // Build method to create the immutable configuration
        public DatabaseConfiguration build() {
            validate();
            return new DatabaseConfiguration(this);
        }

        // Validation method
        private void validate() {
            if (host == null || host.isEmpty()) {
                throw new IllegalArgumentException("Host is required");
            }
            if (port == null || port.isEmpty()) {
                throw new IllegalArgumentException("Port is required");
            }
            if (database == null || database.isEmpty()) {
                throw new IllegalArgumentException("Database name is required");
            }
            if (username == null || username.isEmpty()) {
                throw new IllegalArgumentException("Username is required");
            }
            if (password == null) {
                throw new IllegalArgumentException("Password is required");
            }
        }

    }


}
