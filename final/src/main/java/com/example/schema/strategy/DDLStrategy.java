package com.example.schema.strategy;

public interface DDLStrategy {

    String getCreateTableSQL(String tableName);

    String getDropTableSQL(String tableName, boolean cascade);

    String getCascadeConstraint();

    String getIfExistsClause();

    String getDropForeignKeySQL(String tableName);
}
