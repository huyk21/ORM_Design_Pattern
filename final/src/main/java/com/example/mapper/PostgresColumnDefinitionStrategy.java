package com.example.mapper;

import java.sql.JDBCType;

import com.example.entity.ColumnMetadata;

public class PostgresColumnDefinitionStrategy implements DBMSColumnDefinitionStrategy {
    @Override
    public String generateColumnDefinition(ColumnMetadata column) {
        StringBuilder def = new StringBuilder();
        
        if (column.isId() && column.getIdAnnotation().autoIncrement()) {
            def.append("SERIAL");
        } else if (column.getJdbcType() == JDBCType.VARCHAR) {
            def.append("VARCHAR(").append(column.getLength()).append(")");
        } else if (column.getJdbcType() == JDBCType.INTEGER) {
            def.append("INTEGER");
        } else if (column.getJdbcType() == JDBCType.TIMESTAMP) {
            def.append("TIMESTAMP");
        } else if (column.getJdbcType() == JDBCType.BOOLEAN) {
            def.append("BOOLEAN");
        } else {
            def.append(column.getJdbcType().getName());
        }
        
        return def.toString();
    }
}
