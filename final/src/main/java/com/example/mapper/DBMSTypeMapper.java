package com.example.mapper;

import java.sql.JDBCType;

public interface DBMSTypeMapper {
    String getColumnDefinition(JDBCType jdbcType, Integer length, Integer precision);

    String getAutoIncrementSyntax();

    String getCreateTableSQL(String tableName);

    String getDropTableSQL(String tableName, boolean cascade);

    String getCascadeConstraint();

    String getIfExistsClause();

    String getDropForeignKeySQL(String tableName);
}
