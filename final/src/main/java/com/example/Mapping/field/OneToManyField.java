package com.example.Mapping.field;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import com.example.Mapping.visitor.SQLFieldVisitor;
import com.example.annotation.OneToMany;
import com.example.annotation.Table;

public class OneToManyField extends RelationField {
    Class<?> collectionClass;

    public OneToManyField(Field field, ParentInterface parent) {
        super(field, parent);

        collectionClass = field.getType();

        this.clazz = getCollectionGenericClass(field);

        tableName = this.clazz.getAnnotation(Table.class).name();
        if (tableName == "") {
            tableName = clazz.getName().toUpperCase();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setFields(Object parentObject, ResultSet resultSet)
            throws IllegalArgumentException, IllegalAccessException, SQLException {

        if (!isJoin)
            return;

        Collection<Object> collection = (Collection<Object>) field.get(parentObject);
        
        if (collection == null) {
            try {
                collection = (Collection<Object>) collectionClass.getConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Object thisFieldObject = hasObject(collection.toArray(), resultSet);

        boolean toBeAdded = false;

        if (thisFieldObject == null) {
            thisFieldObject = createObject();
            toBeAdded = true;
        }

        for (var f : objectFields) {
            f.setFields(thisFieldObject, resultSet);
        }

        if (toBeAdded == true) {
            collection.add(thisFieldObject);
        }
    }

    private Object hasObject(Object[] objects, ResultSet resultSet)
            throws IllegalArgumentException, IllegalAccessException, SQLException {
        for (int i = 0; i < objects.length; i++) {
            if (hasSameID(objects[i], resultSet))
                return objects[i];
        }
        return null;
    }

    private Class<?> getCollectionGenericClass(Field collection) {
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
        return null;
    }

    @Override
    public void accept(SQLFieldVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getRefColumnName() throws IllegalArgumentException {
        String refName = field.getAnnotation(OneToMany.class).mappedBy();
        if (refName == "") {
            throw new IllegalArgumentException("No reference name found");
        }
        return refName;
    }
}