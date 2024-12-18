package com.example.Mapping;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.example.annotation.OneToMany;
import com.example.annotation.OneToOne;
import com.example.annotation.Table;

public class TableMapping {
    private Class<?> clazz;

    private String tableName;
    private String mappingName;

    private List<SQLField> fields = new ArrayList<>();

    protected TableMapping(Class<?> clazz, String mappingName) {
        this.clazz = clazz;
        this.mappingName = mappingName;
        this.fields = new ArrayList<>();

        Table annotation = clazz.getAnnotation(Table.class);
        if (annotation != null) {
            this.tableName = annotation.name();
            if (this.tableName == "") {
                this.tableName = clazz.getSimpleName().toUpperCase();
            }
        }
    }

    public String getTableName() {
        return tableName;
    }

    public String getMappingName() {
        return mappingName;
    }

    public static SQLField createSQLField(Field field, Class<?> clazz) {
        if (field.isAnnotationPresent(OneToOne.class)) {
            return new OneToOneField(field, clazz, "");
        } else if (field.isAnnotationPresent(OneToMany.class)) {
            return new OneToManyField(field, clazz, "");
        } else {
            return new SimpleField(field, clazz, "");
        }
    }
}
