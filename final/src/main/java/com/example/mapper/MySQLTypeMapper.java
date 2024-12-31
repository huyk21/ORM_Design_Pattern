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
            default -> jdbcType.getName();
        };
    }

    @Override
    public String getAutoIncrementSyntax() {
        return "AUTO_INCREMENT";
    }
}
