package com.example.Mapping;

import java.lang.reflect.Field;
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

    protected Object createObject() {
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    abstract protected int getIndexMapping(int index);

    abstract public List<String> toQueryString();

    abstract public int setFields(Object thisObject, List<Object> queryObjects, int index)
            throws IllegalArgumentException, IllegalAccessException;

    abstract public void join(String mappingTable, String field, String mappingName);
}
