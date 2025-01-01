package com.example.schema.strategy;

import java.sql.JDBCType;

import com.example.entity.ColumnMetadata;

public class MySQLColumnDefinitionStrategy implements ColumnDefinitionStrategy {
    @Override
    public String generateColumnDefinition(ColumnMetadata column) {
        StringBuilder def = new StringBuilder();

        if (column.isId() && column.getIdAnnotation().autoIncrement()) {
            def.append("INT AUTO_INCREMENT");
        } else if (column.getJdbcType() == JDBCType.VARCHAR) {
            def.append("VARCHAR(").append(column.getLength()).append(")");
        } else if (column.getJdbcType() == JDBCType.INTEGER) {
            def.append("INT");
        } else if (column.getJdbcType() == JDBCType.TIMESTAMP) {
            def.append("TIMESTAMP");
        } else if (column.getJdbcType() == JDBCType.BOOLEAN) {
            def.append("TINYINT(1)");
        } else {
            def.append(column.getJdbcType().getName());
        }

        return def.toString();
    }
}
