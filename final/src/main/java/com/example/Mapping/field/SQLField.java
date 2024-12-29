package com.example.Mapping.field;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.example.Mapping.visitor.SQLFieldVisitor;

public abstract class SQLField {
    protected Class<?> clazz;
    protected Field field;
    protected int index;
    protected ParentInterface parent;

    protected SQLField(Field field, Class<?> clazz, ParentInterface parent) {
        this.clazz = clazz;
        this.field = field;
        this.parent = parent;
        this.field.setAccessible(true);
    }

    public final Field getField() {
        return field;
    }

    public final Class<?> getFieldClass() {
        return clazz;
    }

    public final int getIndex() {
        return index;
    }

    public final void setIndex(int index) {
        this.index = index;
    }

    public Object createObject() {
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    abstract protected String getName();

    abstract protected int setIndexMapping(int index);

    abstract protected List<String> toQueryString();

    abstract protected String toFromString();

    abstract protected void setFields(Object parentObject, ResultSet resultSet)
            throws IllegalArgumentException, IllegalAccessException, SQLException;

    abstract protected boolean join(String mappingTable, String field, String mappingName);

    abstract public void accept(SQLFieldVisitor visitor);
}
