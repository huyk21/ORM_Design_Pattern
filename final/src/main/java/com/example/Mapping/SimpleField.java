package com.example.Mapping;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    public void setFields(Object parentObject, ResultSet resultSet)
            throws IllegalArgumentException, IllegalAccessException, SQLException {
        this.field.set(parentObject, resultSet.getObject(index));
    }

    @Override
    public void join(String mappingTable, String field, String mappingName) {}

    @Override
    protected int getIndexMapping(int index) {
        return index + 1;
    }

    @Override
    protected void accept(SQLFieldVisitor visitor) {
        visitor.visit(this);
    }
}