package com.example.mapper;

import java.sql.JDBCType;

public class PostgresTypeMapper implements DBMSTypeMapper {
    @Override
    public String getColumnDefinition(JDBCType jdbcType, Integer length, Integer precision) {
        return switch (jdbcType) {
            case VARCHAR -> "VARCHAR(" + (length != null ? length : 255) + ")";
            case INTEGER -> "INTEGER";
            case TIMESTAMP -> "TIMESTAMP";
            case BOOLEAN -> "BOOLEAN";
            case DECIMAL -> "DECIMAL(" + (precision != null ? precision : 10) + ")";
            case DATE -> "DATE";
            case TIME -> "TIME";
            case BINARY -> "BYTEA";
            default -> jdbcType.getName();
        };
    }

    @Override
    public String getAutoIncrementSyntax() {
        return "SERIAL";
    }

    @Override
    public String getCreateTableSQL(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName;
    }

    @Override
    public String getDropTableSQL(String tableName, boolean cascade) {
        return "DROP TABLE IF EXISTS " + tableName +
                (cascade ? " " + getCascadeConstraint() : "") + ";";
    }

    @Override
    public String getCascadeConstraint() {
        return "CASCADE";
    }

    @Override
    public String getDropForeignKeySQL(String tableName) {
        return null; // Postgres handles this with CASCADE
    }

    @Override 
    public String getIfExistsClause() {
        return "IF EXISTS";
    }
}
