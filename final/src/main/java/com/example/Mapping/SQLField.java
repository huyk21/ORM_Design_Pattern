package com.example.Mapping;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

abstract class SQLField {
    protected Class<?> clazz;
    protected Field field;
    protected String parentName;
    protected int index;

    protected SQLField(Field field, Class<?> clazz, String parentName) {
        this.clazz = clazz;
        this.field = field;
        this.parentName = parentName;
    }

    protected final Field getField() {
        return field;
    }

    protected final Class<?> getFieldClass() {
        return clazz;
    }

    protected final int getIndex() {
        return index;
    }

    protected final void setIndex(int index) {
        this.index = index;
    }

    protected Object createObject() {
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    abstract protected int getIndexMapping(int index);

    abstract protected List<String> toQueryString();

    abstract protected void setFields(Object parentObject, ResultSet resultSet)
            throws IllegalArgumentException, IllegalAccessException, SQLException;

    abstract protected void join(String mappingTable, String field, String mappingName);

    abstract protected void accept(SQLFieldVisitor visitor);
}
