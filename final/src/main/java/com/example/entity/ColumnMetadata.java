package com.example.entity;

import com.example.annotation.Column;
import com.example.annotation.Id;
import com.example.annotation.JoinColumn;

import java.lang.reflect.Field;
import java.sql.JDBCType;

public class ColumnMetadata {
    private final Field field;
    private final String columnName;
    private final boolean isId;
    private final JDBCType jdbcType;
    private final boolean isUnique;
    private final boolean isNullable;
    private final boolean isForeignKey;
    private final Integer length;
    private final Integer precision;
    private final Id idAnnotation;

    /**
     * Constructor that extracts column information from a field.
     *
     * @param field The Field object.
     */

    public ColumnMetadata(Field field) {
        this.field = field;
        this.isId = field.isAnnotationPresent(Id.class);
        this.isForeignKey = field.isAnnotationPresent(JoinColumn.class);
        this.idAnnotation = field.getAnnotation(Id.class);

        Column column = field.getAnnotation(Column.class);
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);

        if (column != null) {
            this.columnName = resolveColumnName(field, column.name());
            this.jdbcType = column.type();
            this.isUnique = column.unique();
            this.isNullable = column.nullable();
            this.length = column.length();
            this.precision = column.precision();
        } else if (joinColumn != null) {
            this.columnName = joinColumn.name();
            this.jdbcType = JDBCType.INTEGER; // Assuming foreign key is an integer
            this.isUnique = false;
            this.isNullable = joinColumn.nullable();
            this.length = null;
            this.precision = null;
        } else {
            throw new IllegalStateException("Field must have @Column or @JoinColumn");
        }
    }

    private String resolveColumnName(Field field, String annotationName) {
        return annotationName.isEmpty() ? EntityUtils.convertToSnakeCase(field.getName()) : annotationName;
    }

    // Add value handling methods
    public Object getValueFromEntity(Object entity) throws IllegalAccessException {
        field.setAccessible(true);
        return field.get(entity);
    }

    public void setValueToEntity(Object entity, Object value) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(entity, value);
    }

    // SQL generation helpers
    public String getColumnDefinition() {
        StringBuilder def = new StringBuilder(columnName)
                .append(" ")
                .append(jdbcType);

        if (!isNullable)
            def.append(" NOT NULL");
        if (isUnique)
            def.append(" UNIQUE");
        if (isId)
            def.append(" PRIMARY KEY");

        return def.toString();
    }

    /**
     * Retrieves the associated field.
     *
     * @return The Field object.
     */

    public Field getField() {
        return field;
    }

    /**
     * Retrieves the column name in the database.
     *
     * @return The column name.
     */
    public String getColumnName() {
        return columnName;
    }

    public boolean isId() {
        return isId;
    }

    public Id getIdAnnotation() {
        return idAnnotation;
    }

    public Integer getLength() {
        return length;
    }

    public Integer getPrecision() {
        return precision;
    }

    public JDBCType getJdbcType() {
        return jdbcType;
    }

    public boolean isUnique() {
        return isUnique;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public boolean isForeignKey() {
        return isForeignKey;
    }
}