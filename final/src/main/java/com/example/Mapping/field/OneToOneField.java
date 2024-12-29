package com.example.Mapping.field;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.Mapping.visitor.SQLFieldVisitor;
import com.example.annotation.OneToOne;
import com.example.annotation.Table;

public class OneToOneField extends RelationField {
    public OneToOneField(Field field, Class<?> clazz, ParentInterface parent) {
        super(field, clazz, parent);

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
    public void accept(SQLFieldVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getRefColumnName() throws IllegalArgumentException {
        String refName = field.getAnnotation(OneToOne.class).mappedBy();
        if (refName == "") {
            throw new IllegalArgumentException("No reference name found");
        }
        return refName;
    }
}