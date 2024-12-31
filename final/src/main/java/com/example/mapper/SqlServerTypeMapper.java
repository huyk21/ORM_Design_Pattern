package com.example.mapper;

import java.sql.JDBCType;

public class SqlServerTypeMapper implements DBMSTypeMapper {
    @Override
    public String getColumnDefinition(JDBCType jdbcType, Integer length, Integer precision) {
        return switch (jdbcType) {
            case VARCHAR -> "NVARCHAR(" + (length != null ? length : 255) + ")";
            case INTEGER -> "INT";
            case TIMESTAMP -> "DATETIME";
            case BOOLEAN -> "BIT";
            default -> jdbcType.getName();
        };
    }

    @Override
    public String getAutoIncrementSyntax() {
        return "IDENTITY(1,1)";
    }
}
