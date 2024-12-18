package com.example.Mapping;

import java.lang.reflect.Field;
import java.util.List;

import com.example.Annotation.Table;

class OneToOneField extends RelationField {
    protected OneToOneField(Field field, Class<?> clazz, String parentName) {
        super(field, clazz, parentName);

        tableName = clazz.getAnnotation(Table.class).name();
        if (tableName == "") {
            tableName = clazz.getName().toUpperCase();
        }
    }

    @Override
    public int setFields(Object thisObject, List<Object> queryObjects, int index)
            throws IllegalArgumentException, IllegalAccessException {

        if (!isJoin)
            return index;

        Object thisFieldObject = createObject();
        for (var f : this.objectFields) {
            index = f.setFields(thisFieldObject, queryObjects, index);
        }
        field.set(thisObject, thisFieldObject);
        return index;
    }
}