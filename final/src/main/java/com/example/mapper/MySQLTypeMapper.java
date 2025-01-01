package com.example.mapper;

import java.sql.JDBCType;

public class MySQLTypeMapper implements DBMSTypeMapper {
    @Override
    public String getColumnDefinition(JDBCType jdbcType, Integer length, Integer precision) {
        return switch (jdbcType) {
            case VARCHAR -> "VARCHAR(" + (length != null ? length : 255) + ")";
            case INTEGER -> "INT";
            case TIMESTAMP -> "TIMESTAMP";
            case BOOLEAN -> "TINYINT(1)";
            case DECIMAL -> "DECIMAL(" + (precision != null ? precision : 10) + ")";
            case DATE -> "DATE";
            case TIME -> "TIME";
            case BINARY -> "BLOB";
            default -> jdbcType.getName();
        };
    }

    @Override
    public String getAutoIncrementSyntax() {
        return "AUTO_INCREMENT";
    }

    @Override
    public String getCreateTableSQL(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName;
    }

    @Override
    public String getDropTableSQL(String tableName, boolean cascade) {
        return "DROP TABLE " + getIfExistsClause() + " " + tableName +
                (cascade ? " " + getCascadeConstraint() : "");
    }

    @Override
    public String getCascadeConstraint() {
        return "CASCADE";
    }

    @Override
    public String getIfExistsClause() {
        return "IF EXISTS";
    }

    @Override
    public String getDropForeignKeySQL(String tableName) {
        return "SET FOREIGN_KEY_CHECKS = 0";
    }
}
