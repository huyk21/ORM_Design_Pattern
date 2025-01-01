package com.example.schema.strategy;

public class PostgresDDLStrategy implements DDLStrategy {

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
