package com.example.entity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.annotation.Column;
import com.example.annotation.Id;
import com.example.annotation.Table;

public class EntityMetadata {
    private final Class<?> entityClass;
    private final String tableName;
    private final List<ColumnMetadata> columns;
    private final ColumnMetadata idColumn;

    public EntityMetadata(Class<?> entityClass) {
        this.entityClass = entityClass;
        this.tableName = resolveTableName();
        this.columns = resolveColumns();
        this.idColumn = findIdColumn();

        
    }

    private String resolveTableName() {
        if (entityClass.isAnnotationPresent(Table.class)) {
            String name = entityClass.getAnnotation(Table.class).name();
            return name.isEmpty() ? convertToSnakeCase(entityClass.getSimpleName()) : name;
        }
        return convertToSnakeCase(entityClass.getSimpleName());
    }

    private List<ColumnMetadata> resolveColumns() {
        List<ColumnMetadata> cols = new ArrayList<>();
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column col = field.getAnnotation(Column.class);
                cols.add(new ColumnMetadata(
                        field,
                        col.name().isEmpty() ? convertToSnakeCase(field.getName()) : col.name(),
                        field.isAnnotationPresent(Id.class),
                        col.type(),
                        col.unique()
                ));
            }
        }
        return cols;
    }

    private ColumnMetadata findIdColumn() {
        Optional<ColumnMetadata> idCol = columns.stream()
                .filter(ColumnMetadata::isId)
                .findFirst();
        if (idCol.isEmpty()) {
            throw new IllegalStateException("No @Id field found in " + entityClass.getName());
        }
        return idCol.get();
    }

    public String getTableName() {
        return tableName;
    }

    public List<ColumnMetadata> getColumns() {
        return columns;
    }

    public ColumnMetadata getIdColumn() {
        return idColumn;
    }

    private String convertToSnakeCase(String input) {
        return input.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
