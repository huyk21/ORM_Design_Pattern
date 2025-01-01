package com.example.schema.strategy;

public class MySQLDDLStrategy implements DDLStrategy {

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
