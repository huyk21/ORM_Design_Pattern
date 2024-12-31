package com.example.mapper;

import java.sql.JDBCType;

public interface DBMSTypeMapper {
    String getColumnDefinition(JDBCType jdbcType, Integer length, Integer precision);

    String getAutoIncrementSyntax();
}
