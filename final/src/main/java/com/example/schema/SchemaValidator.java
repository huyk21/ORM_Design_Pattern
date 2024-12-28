package com.example.schema;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.entity.EntityMetadata;

public class SchemaValidator {
    public void validate(Connection connection, EntityMetadata metadata) throws SQLException {
        String tableName = metadata.getTableName();
        ResultSet rs = connection.getMetaData().getTables(null, null, tableName, null);
        if (!rs.next()) {
            throw new IllegalStateException("Table " + tableName + " does not exist in the database!");
        }
        // Further validation logic for columns, types, etc.
    }
}
