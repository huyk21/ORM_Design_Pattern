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
            case DECIMAL -> "DECIMAL(" + (precision != null ? precision : 10) + ")";
            case DATE -> "DATE";
            case TIME -> "TIME";
            case BINARY -> "VARBINARY(MAX)";
            default -> jdbcType.getName();
        };
    }

    @Override
    public String getAutoIncrementSyntax() {
        return "IDENTITY(1,1)";
    }

    @Override
    public String getCreateTableSQL(String tableName) {
        return "CREATE TABLE " + tableName;
    }

    @Override
    public String getDropTableSQL(String tableName, boolean cascade) {
        StringBuilder sql = new StringBuilder();
        if (cascade) {
            sql.append("DECLARE @sql NVARCHAR(MAX); ")
                    .append("SELECT @sql = COALESCE(@sql + ' ', '') + ")
                    .append("'ALTER TABLE ' + QUOTENAME(OBJECT_SCHEMA_NAME(parent_object_id)) + '.' + ")
                    .append("QUOTENAME(OBJECT_NAME(parent_object_id)) + ' DROP CONSTRAINT ' + QUOTENAME(name) + ';' ")
                    .append("FROM sys.foreign_keys WHERE referenced_object_id = OBJECT_ID('").append(tableName)
                    .append("'); ")
                    .append("EXEC sp_executesql @sql; ");
        }
        sql.append("DROP TABLE ").append(getIfExistsClause()).append(" ").append(tableName);
        return sql.toString();
    }

    @Override
    public String getCascadeConstraint() {
        return ""; // SQL Server handles cascade differently
    }

    @Override
    public String getDropForeignKeySQL(String tableName) {
        return String.format(
                "DECLARE @sql NVARCHAR(MAX); " +
                        "SELECT @sql = COALESCE(@sql + ' ', '') + " +
                        "'ALTER TABLE ' + QUOTENAME(OBJECT_SCHEMA_NAME(parent_object_id)) + '.' + " +
                        "QUOTENAME(OBJECT_NAME(parent_object_id)) + ' DROP CONSTRAINT ' + QUOTENAME(name) + ';' " +
                        "FROM sys.foreign_keys WHERE referenced_object_id = OBJECT_ID('%s'); " +
                        "EXEC sp_executesql @sql;",
                tableName);
    }

    @Override
    public String getIfExistsClause() {
        return "IF EXISTS";
    }
}
