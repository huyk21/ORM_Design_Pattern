package com.example.Mapping;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.example.annotation.Table;

class OneToOneField extends RelationField {
    protected OneToOneField(Field field, Class<?> clazz, String parentName) {
        super(field, clazz, parentName);

        tableName = clazz.getAnnotation(Table.class).name();
        if (tableName == "") {
            tableName = clazz.getName().toUpperCase();
        }
    }

    @Override
    public void setFields(Object parentObject, ResultSet resultSet)
            throws IllegalArgumentException, IllegalAccessException, SQLException {

        if (!isJoin)
            return;

        Object thisFieldObject = null;

        if (!hasSameID(parentObject, resultSet)) {
            thisFieldObject = createObject();
        }
        else {
            thisFieldObject = field.get(thisFieldObject);
        }

        for (var f : this.objectFields) {
            f.setFields(thisFieldObject, resultSet);
        }
        
        field.set(parentObject, thisFieldObject);
    }

    @Override
    protected void accept(SQLFieldVisitor visitor) {
        visitor.visit(this);
    }
}