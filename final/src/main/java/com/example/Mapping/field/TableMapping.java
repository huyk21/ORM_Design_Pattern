package com.example.Mapping.field;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.Mapping.visitor.FieldFactoryVisitor;
import com.example.Mapping.visitor.SQLFieldVisitor;
import com.example.annotation.Id;
import com.example.annotation.Table;

public class TableMapping<T> implements ParentInterface {
    protected Class<T> clazz;
    protected String tableName;
    protected String mappingName;

    protected List<SQLField> objectFields;

    public TableMapping(Class<T> clazz, String mappingName) {
        this.clazz = clazz;
        this.mappingName = mappingName;

        this.tableName = clazz.getAnnotation(Table.class).name();
        if (tableName == "") {
            tableName = clazz.getSimpleName();
        }

        accept(new FieldFactoryVisitor());
    }

    public String getFromString() {
        String result = "\nfrom " + tableName + " " + mappingName;
        for (var f : objectFields) {
            result += f.toFromString();
        }
        return result;
    }

    public String getQueryString() {
        List<String> fieldString = new ArrayList<>();

        for (var f : objectFields) {
            fieldString.addAll(f.toQueryString());
        }

        String queryString = "select ";
        for (int i = 0; i < fieldString.size() - 1; i++) {
            queryString += fieldString.get(i) + ", ";
        }
        queryString += fieldString.get(fieldString.size() - 1);

        return queryString;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public int getIdFieldIndex() {
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

    public String getMappingName() {
        return mappingName;
    }

    public List<SQLField> getSQLFields() {
        return objectFields;
    }

    public void setSQLFields(List<SQLField> fields) {
        objectFields = fields;
    }

    public void setFields(T object, ResultSet resultSet)
            throws IllegalArgumentException, IllegalAccessException, SQLException {
        int index = 1;
        for (var f : objectFields) {
            index = f.setIndexMapping(index);
        }

        for (var f : objectFields) {
            f.setFields(object, resultSet);
        }
    }

    public boolean hasSameID(Object parentObject, ResultSet resultSet)
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

    public void join(String mappingTable, String field, String mappingName) {
        for (var f : objectFields) {
            if (f.join(mappingTable, field, mappingName)) {
                f.accept(new FieldFactoryVisitor());
            }
        }
    }

    public void accept(SQLFieldVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public String getIdColumnName() {
        return objectFields.get(getIdFieldIndex()).getName();
    }

    public T createObject() {
        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
    }
}
