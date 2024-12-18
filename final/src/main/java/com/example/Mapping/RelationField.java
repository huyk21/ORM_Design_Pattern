package com.example.Mapping;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
    }

    private void unloadFields() throws SecurityException {
        List<Field> fields = Arrays.asList(clazz.getDeclaredFields());
        for (Field f : fields) {
            objectFields.add(TableMapping.createSQLField(f, f.getClass()));
        }

        Class<?> superClass = clazz.getSuperclass();
        while (superClass != null) {
            fields = Arrays.asList(superClass.getDeclaredFields());
            for (Field f : fields) {
                objectFields.add(TableMapping.createSQLField(f, f.getClass()));
            }

            superClass = superClass.getSuperclass();
        }
    }

    protected List<SQLField> getFields() throws SecurityException {
        if (objectFields.isEmpty()) {
            unloadFields();
        }
        return objectFields;
    }

    public int getIdFieldIndex() {
        objectFields = getFields();

        int i = 0;
        for (var field : objectFields) {
            // TODO: Id Annotation
            // if (field.getField().isAnnotationPresent(Id.class)) {
            // return field;
            // }
            if (field.getField().getName().toUpperCase() == "ID") {
                return i;
            }
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
        if (objectFields.isEmpty()) {
            unloadFields();
        }

        ArrayList<String> names = new ArrayList<>();
        for (var field : objectFields) {
            names.addAll(field.toQueryString());
        }

        return names;
    }

    @Override
    public void join(String mappingTable, String field, String mappingName) {
        if (this.mappingName == parentName && this.field.getName() == field) {
            this.isJoin = true;
            this.mappingName = mappingName;
            return;
        }
        for (var f : objectFields) {
            f.join(mappingName, field, mappingName);
        }
    }
}