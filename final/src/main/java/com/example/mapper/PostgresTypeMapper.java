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
            default -> jdbcType.getName();
        };
    }

    @Override
    public String getAutoIncrementSyntax() {
        return "SERIAL";
    }
}
