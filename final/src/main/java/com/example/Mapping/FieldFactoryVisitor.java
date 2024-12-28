package com.example.Mapping;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.annotation.OneToMany;
import com.example.annotation.OneToOne;

public class FieldFactoryVisitor implements SQLFieldVisitor {
    private SQLField create(Field field, Class<?> clazz, String parentName) {
        if (field.isAnnotationPresent(OneToOne.class)) {
            return new OneToOneField(field, clazz, parentName);
        } else if (field.isAnnotationPresent(OneToMany.class)) {
            return new OneToManyField(field, clazz, parentName);
        } else {
            return new SimpleField(field, clazz, parentName);
        }
    }

    private void visit(RelationField field) {
        if (field.getFields().size() > 0) {
            return;
        }

        if (!field.hasJoin()) {
            return;
        }

        Class<?> clazz = field.getFieldClass();
        List<Field> rawFields = Arrays.asList(clazz.getDeclaredFields());
        List<SQLField> sqlFields = new ArrayList<>();
        
        for (Field f : rawFields) {
            sqlFields.add(create(f, f.getClass(), field.mappingName));
        }

        Class<?> superClass = clazz.getSuperclass();
        while (superClass != null) {
            rawFields = Arrays.asList(superClass.getDeclaredFields());
            for (Field f : rawFields) {
                sqlFields.add(create(f, f.getClass(), field.mappingName));
            }

            superClass = superClass.getSuperclass();
        }

        field.setFields(sqlFields);
    }

    @Override
    public void visit(SimpleField field) {
        return;
    }

    @Override
    public void visit(OneToOneField field) {
        visit((RelationField) field);
    }

    @Override
    public void visit(OneToManyField field) {
        visit((RelationField) field);
    }
}
