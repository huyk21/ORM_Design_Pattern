package com.example.Mapping;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.example.annotation.Table;

class OneToManyField extends RelationField {
    int idColumnIndex;
    List<Object> tempCollection;

    protected OneToManyField(Field field, Class<?> clazz, String parentName) {
        super(field, clazz, parentName);
        clazz = getCollectionGenericClass(field);
        idColumnIndex = getIdFieldIndex();
        tempCollection = new ArrayList<>();

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

        objectFields = getFields();
        
        int idIndex = getIdFieldIndex();
        objectFields.get(idIndex);

        Object thisFieldObject;

        int objectIndex = hasObject(queryObjects);

        if (objectIndex != -1) {
            thisFieldObject = tempCollection.get(objectIndex);
        } else {
            thisFieldObject = createObject();
        }

        for (var f : objectFields) {
            // if (f.getField().isAnnotationPresent(Id.class)) {
            // IdColumnIndex = index;
            // }
            if (f.getField().getName().toUpperCase() == "ID") {
                idColumnIndex = index;
            }
            index = f.setFields(thisFieldObject, queryObjects, index);
        }

        if (objectIndex == -1) {
            tempCollection.add(thisFieldObject);
        }

        return index;
    }

    private int hasObject(List<Object> queryObjects) throws IllegalArgumentException, IllegalAccessException {
        if (idColumnIndex == 0)
            return -1;

        SQLField idField = objectFields.get(idColumnIndex);

        for (int i = 0; i < tempCollection.size(); i++) {
            if (idField.getField().get(tempCollection.get(i)) == queryObjects.get(idField.getIndex()))
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
}