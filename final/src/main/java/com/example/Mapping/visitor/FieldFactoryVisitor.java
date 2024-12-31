package com.example.Mapping.visitor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.example.Mapping.field.OneToManyField;
import com.example.Mapping.field.OneToOneField;
import com.example.Mapping.field.ParentInterface;
import com.example.Mapping.field.RelationField;
import com.example.Mapping.field.SQLField;
import com.example.Mapping.field.SimpleField;
import com.example.Mapping.field.TableMapping;
import com.example.annotation.OneToMany;
import com.example.annotation.OneToOne;

public class FieldFactoryVisitor implements SQLFieldVisitor {
    private SQLField create(Field field, ParentInterface parent) {
        if (field.isAnnotationPresent(OneToOne.class)) {
            return new OneToOneField(field, parent);
        } else if (field.isAnnotationPresent(OneToMany.class)) {
            if (!Collection.class.isAssignableFrom(field.getType())) {
                throw new IllegalArgumentException(
                        "OneToMany field is not a collection: " + field.getName());
            }
            return new OneToManyField(field, parent);
        } else {
            return new SimpleField(field, parent);
        }
    }

    private List<SQLField> generateField(Class<?> clazz, ParentInterface parent) {
        List<Field> rawFields = Arrays.asList(clazz.getDeclaredFields());
        List<SQLField> sqlFields = new ArrayList<>();

        for (Field f : rawFields) {
            sqlFields.add(create(f, parent));
        }

        Class<?> superClass = clazz.getSuperclass();
        while (superClass != null) {
            rawFields = Arrays.asList(superClass.getDeclaredFields());
            for (Field f : rawFields) {
                sqlFields.add(create(f, parent));
            }

            superClass = superClass.getSuperclass();
        }

        for (var f : sqlFields) {
            f.accept(this);
        }

        return sqlFields;
    }

    private void visit(RelationField field) {
        if (field.getFields().size() > 0) {
            return;
        }

        if (!field.hasJoin()) {
            return;
        }

        List<SQLField> fields = generateField(field.getFieldClass(), field);

        field.setFields(fields);
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

    @Override
    public void visit(TableMapping<?> field) {
        field.setSQLFields(generateField(field.getClazz(), field));
    }
}
