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
    private final boolean ssl;
    private final String connectionTimeoutMs;
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
        this.ssl = builder.ssl;
        this.connectionTimeoutMs = builder.connectionTimeoutMs;
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

    public boolean isSsl() {
        return ssl;
    }

    public String getConnectionTimeoutMs() {
        return connectionTimeoutMs;
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
        if (connectionTimeoutMs != null && !connectionTimeoutMs.isEmpty()) {
            props.setProperty("connectTimeout", connectionTimeoutMs);
        }
        props.setProperty("maxConnections", String.valueOf(maxConnections));
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
        private boolean ssl = false;
        private String connectionTimeoutMs = "5000";
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

        public Builder enableSsl(boolean ssl) {
            this.ssl = ssl;
            return this;
        }

        public Builder connectionTimeout(String timeoutMs) {
            if (Integer.parseInt(timeoutMs) < 0) {
                throw new IllegalArgumentException("Timeout must be non-negative");
            }
            this.connectionTimeoutMs = timeoutMs;
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

    // Utility method to generate connection URL
    public String generateConnectionUrl(String jdbcPrefix) {
        StringBuilder urlBuilder = new StringBuilder(jdbcPrefix)
                .append("://")
                .append(host)
                .append(":")
                .append(port)
                .append("/")
                .append(database);

        // Optional parameters can be added here
        if (ssl) {
            urlBuilder.append("?ssl=true");
        }

        return urlBuilder.toString();
    }
}
