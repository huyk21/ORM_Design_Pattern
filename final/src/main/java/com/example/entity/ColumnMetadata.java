package com.example.entity;

import java.lang.reflect.Field;
import java.sql.JDBCType;

public class ColumnMetadata {
    private final Field field;
    private final String columnName;
    private final boolean isId;
    private final JDBCType jdbcType;
    private final boolean isUnique;

    public ColumnMetadata(Field field, String columnName, boolean isId, JDBCType jdbcType, boolean isUnique) {
        this.field = field;
        this.columnName = columnName;
        this.isId = isId;
        this.jdbcType = jdbcType;
        this.isUnique = isUnique;
    }

    public Field getField() { return field; }
    public String getColumnName() { return columnName; }
    public boolean isId() { return isId; }
    public JDBCType getJdbcType() { return jdbcType; }
    public boolean isUnique() { return isUnique; }
}