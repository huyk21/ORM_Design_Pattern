package com.example.mapper;

import com.example.entity.ColumnMetadata;

import java.sql.JDBCType;

public class SqlServerColumnDefinitionStrategy implements DBMSColumnDefinitionStrategy {
    @Override
    public String generateColumnDefinition(ColumnMetadata column) {
        StringBuilder def = new StringBuilder();

        if (column.isId() && column.getIdAnnotation().autoIncrement()) {
            def.append("INT IDENTITY(1,1)");
        } else if (column.getJdbcType() == JDBCType.VARCHAR) {
            def.append("VARCHAR(").append(column.getLength()).append(")");
        } else if (column.getJdbcType() == JDBCType.INTEGER) {
            def.append("INT");
        } else if (column.getJdbcType() == JDBCType.TIMESTAMP) {
            def.append("DATETIME");
        } else if (column.getJdbcType() == JDBCType.BOOLEAN) {
            def.append("BIT");
        } else {
            def.append(column.getJdbcType().getName());
        }

        return def.toString();
    }
}
