package com.example.Mapping;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class SimpleField extends SQLField {
    protected SimpleField(Field field, Class<?> clazz, String parentName) {
        super(field, clazz, parentName);
    }

    @Override
    public List<String> toQueryString() {
        return new ArrayList<>(Arrays.asList(parentName + "." + field.getName().toUpperCase()));
    }

    @Override
    public int setFields(Object thisObject, List<Object> queryObjects, int index)
            throws IllegalArgumentException, IllegalAccessException {
        this.field.set(thisObject, queryObjects.get(index));
        return index + 1;
    }

    @Override
    public void join(String mappingTable, String field, String mappingName) {
    }

    @Override
    protected int getIndexMapping(int index) {
        return index + 1;
    }
}