package com.example.schema.strategy;

public class SqlServerDDLStrategy implements DDLStrategy {

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
