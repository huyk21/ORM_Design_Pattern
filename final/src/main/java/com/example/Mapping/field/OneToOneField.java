package com.example.Mapping.field;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.Mapping.visitor.SQLFieldVisitor;
import com.example.annotation.OneToOne;
import com.example.annotation.Table;

public class OneToOneField extends RelationField {
    public OneToOneField(Field field, ParentInterface parent) {
        super(field, parent);

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
    protected String toFromString() {
        if (!isJoin) {
            return "";
        }
        
        if (field.getAnnotation(OneToOne.class).refInOther()) {
            return " join " + tableName + " " + mappingName 
                + " on " + parent.getMappingName() + "." + parent.getIdColumnName() 
                + " = " + mappingName + "." + getRefColumnName();
        }

        return " join " + tableName + " " + mappingName 
            + " on " + parent.getMappingName() + "." + getRefColumnName() 
            + " = " + mappingName + "." + getIdColumnName();
    }

    @Override
    public void accept(SQLFieldVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getRefColumnName() throws IllegalArgumentException {
        String refName = field.getAnnotation(OneToOne.class).mappedBy();
        if (refName == "") {
            throw new IllegalArgumentException("No reference name found on OneToOne annotation: " + field.getName());
        }
        return refName;
    }
}