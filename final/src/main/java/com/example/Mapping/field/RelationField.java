package com.example.Mapping.field;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.annotation.Id;
import com.example.annotation.Table;

public abstract class RelationField extends SQLField implements ParentInterface {
    protected List<SQLField> objectFields;

    protected String refName;
    protected String tableName;
    protected String mappingName;
    protected boolean isJoin;

    protected RelationField(Field field, Class<?> clazz, ParentInterface parent) {
        super(field, clazz, parent);
        this.mappingName = "";
        this.isJoin = false;
        this.objectFields = new ArrayList<>();
    }

    public List<SQLField> getFields() throws SecurityException {
        return objectFields;
    }

    public void setFields(List<SQLField> fields) throws SecurityException {
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

    public String getName() {
        return tableName;
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

    protected abstract String getRefColumnName() throws IllegalArgumentException;

    @Override
    protected int setIndexMapping(int index) {
        if (!isJoin) {
            return index;
        }
        this.index = index;
        objectFields = getFields();
        for (var field : objectFields) {
            index = field.setIndexMapping(index);
        }
        return index;
    }

    @Override
    public String getIdColumnName() {
        return objectFields.get(getIdFieldIndex()).getName();
    }

    @Override
    protected String toFromString() {
        if (!isJoin) {
            return "";
        }
        
        return " join " + tableName + " " + mappingName 
        + " on " + parent.getMappingName() + "." + parent.getIdColumnName() + " = " + mappingName + "." + getRefColumnName();
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
    public boolean join(String mappingTable, String field, String mappingName) {
        if (mappingTable == parent.getMappingName() && this.field.getName() == field) {
            this.isJoin = true;
            this.mappingName = mappingName;
            return true;
        }
        for (var f : objectFields) {
            if (f.join(mappingTable, field, mappingName)) {
                return true;
            }
        }
        return false;
    }
}