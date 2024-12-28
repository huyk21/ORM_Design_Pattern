package com.example.Mapping;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.annotation.Id;

abstract class RelationField extends SQLField {
    protected List<SQLField> objectFields;

    protected String tableName;
    protected String mappingName;
    protected boolean isJoin;

    protected RelationField(Field field, Class<?> clazz, String parentName) {
        super(field, clazz, parentName);
        this.tableName = "";
        this.mappingName = "";
        this.isJoin = false;
        this.objectFields = new ArrayList<>();
    }

    protected List<SQLField> getFields() throws SecurityException {
        return objectFields;
    }

    protected void setFields(List<SQLField> fields) throws SecurityException {
        objectFields = fields;
    }

    protected int getIdFieldIndex() {
        objectFields = getFields();

        int i = 0;
        for (var field : objectFields) {
            if (field.getField().isAnnotationPresent(Id.class)) {
                return i;
            }
            i++;
        }

        // Table has no ID, throw
        return -1;
    }

    public String getTableName() {
        return tableName;
    }

    public String getMappingName() {
        return mappingName;
    }

    public boolean hasJoin() {
        return isJoin;
    }

    protected boolean hasSameID(Object parentObject, ResultSet resultSet)
            throws IllegalArgumentException, IllegalAccessException, SQLException {
        
        if (parentObject == null) {
            return false;
        }

        int idIndex = getIdFieldIndex();
        SQLField idField = objectFields.get(idIndex);

        Object parentObjectId = idField.getField().get(parentObject);
        Object newObjectId = resultSet.getObject(idField.getIndex());

        return parentObjectId.equals(newObjectId);
    }

    @Override
    protected int getIndexMapping(int index) {
        this.index = index;
        objectFields = getFields();
        for (var field : objectFields) {
            index = field.getIndexMapping(index);
        }
        return index;
    }

    @Override
    public List<String> toQueryString() {
        ArrayList<String> names = new ArrayList<>();
        for (var field : objectFields) {
            names.addAll(field.toQueryString());
        }

        return names;
    }

    @Override
    public void join(String mappingTable, String field, String mappingName) {
        if (mappingTable == parentName && this.field.getName() == field) {
            this.isJoin = true;
            this.mappingName = mappingName;
            return;
        }
        for (var f : objectFields) {
            f.join(mappingTable, field, mappingName);
        }
    }
}


// "From User u join User t on u.Id = t.TeacherId"

/*  class User {

        @OneToOne(column = "teacherId")
        User teacher;

        @OneToOne
        Group group;
    } 
*/