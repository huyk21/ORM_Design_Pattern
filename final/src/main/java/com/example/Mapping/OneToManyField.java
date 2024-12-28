package com.example.Mapping;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import com.example.annotation.Table;

public class OneToManyField extends RelationField {
    private List<Object> tempCollection;

    protected OneToManyField(Field field, Class<?> clazz, String parentName) {
        super(field, clazz, parentName);
        clazz = getCollectionGenericClass(field);

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

        Object thisFieldObject;
        int objectIndex = hasObject(resultSet);

        if (objectIndex != -1) {
            thisFieldObject = tempCollection.get(objectIndex);
        } else {
            thisFieldObject = createObject();
        }

        for (var f : objectFields) {
            f.setFields(thisFieldObject, resultSet);
        }

        if (objectIndex == -1) {
            tempCollection.add(thisFieldObject);
        }
    }

    private int hasObject(ResultSet resultSet) throws IllegalArgumentException, IllegalAccessException, SQLException {
        for (int i = 0; i < tempCollection.size(); i++) {
            if (hasSameID(tempCollection.get(i), resultSet))
                return i;
        }
        return -1;
    }

    private Class<?> getCollectionGenericClass(Field collection) {
        if (Collection.class.isAssignableFrom(collection.getClass())) {
            var genericFieldType = collection.getGenericType();

            if (genericFieldType instanceof ParameterizedType) {
                ParameterizedType aType = (ParameterizedType) genericFieldType;
                Type[] fieldArgTypes = aType.getActualTypeArguments();
                for (Type fieldArgType : fieldArgTypes) {
                    var fieldArgClass = (Class<?>) fieldArgType;
                    System.out.println("fieldArgClass = " + fieldArgClass);
                    return fieldArgClass;
                }
            }
        }
        return null;
    }

    @Override
    protected void accept(SQLFieldVisitor visitor) {
        visitor.visit(this);
    }
}