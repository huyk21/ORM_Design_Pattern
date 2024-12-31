package com.example.Mapping.field;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.Mapping.visitor.SQLFieldVisitor;
import com.example.annotation.Column;

public class SimpleField extends SQLField {
    public SimpleField(Field field, ParentInterface parent) {
        super(field, parent);
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    protected String getName() {
        String name = field.getAnnotation(Column.class).name();
        if (name == "") {
            name = field.getName().toUpperCase();
        }
        return name;
    }

    @Override
    protected String toFromString() {
        return "";
    }
    
    @Override
    public List<String> toQueryString() {
        return new ArrayList<>(Arrays.asList(parent.getMappingName() + "." + getName()));
    }

    @Override
    public void setFields(Object parentObject, ResultSet resultSet)
            throws IllegalArgumentException, IllegalAccessException, SQLException {
        this.field.set(parentObject, resultSet.getObject(index));
    }

    @Override
    public boolean join(String mappingTable, String field, String mappingName) {
        return false;
    }

    @Override
    protected int setIndexMapping(int index) {
        this.index = index;
        return index + 1;
    }

    @Override
    public void accept(SQLFieldVisitor visitor) {
        visitor.visit(this);
    }
}