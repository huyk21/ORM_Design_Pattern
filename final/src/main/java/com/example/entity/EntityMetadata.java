package com.example.entity;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.annotation.Column;
import com.example.annotation.Id;
import com.example.annotation.JoinColumn;

/**
 * Holds metadata information about an entity.
 * Applies Single Responsibility Principle.
 */
public class EntityMetadata {
    private final Class<?> entityClass;
    private final String tableName;
    private final List<ColumnMetadata> columns;
    private final ColumnMetadata idColumn;
    private final Map<String, ColumnMetadata> columnMap;

    public EntityMetadata(Class<?> entityClass) {
        this.entityClass = entityClass;
        this.tableName = EntityUtils.getTableName(entityClass);
        this.columns = resolveColumns();
        this.columnMap = createColumnMap();
        this.idColumn = findIdColumn();
    }

    private Map<String, ColumnMetadata> createColumnMap() {
        return columns.stream()
                .collect(Collectors.toMap(
                        ColumnMetadata::getColumnName,
                        column -> column));
    }

    private List<ColumnMetadata> resolveColumns() {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(this::isValidColumn)
                .map(ColumnMetadata::new)
                .collect(Collectors.toList());
    }

    private boolean isValidColumn(Field field) {
        return field.isAnnotationPresent(Column.class) ||
                field.isAnnotationPresent(JoinColumn.class);
    }


    public String getColumnNames() {
        return columns.stream()
                .map(ColumnMetadata::getColumnName)
                .collect(Collectors.joining(", "));
    }

    private ColumnMetadata findIdColumn() {
        return columns.stream()
                .filter(ColumnMetadata::isId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No @Id field found in " + entityClass.getName()));
    }

    /**
     * Get the ID field of the entity
     * 
     * @return Field annotated with @Id
     * @throws IllegalStateException if no ID field is found
     */
    public Field getIdField() {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }
        throw new IllegalStateException("No @Id field found in class: " + entityClass.getName());
    }

    public void validateEntity(Object entity) {
        if (!entityClass.isInstance(entity)) {
            throw new IllegalArgumentException("Invalid entity type");
        }
    }

    public Optional<ColumnMetadata> findColumn(String columnName) {
        return Optional.ofNullable(columnMap.get(columnName));
    }

    /**
     * Retrieves the table name.
     *
     * @return The table name.
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Retrieves all column metadata.
     *
     * @return List of ColumnMetadata.
     */
    public List<ColumnMetadata> getColumns() {
        return columns;
    }

    /**
     * Retrieves the ID column metadata.
     *
     * @return The ID ColumnMetadata.
     */
    public ColumnMetadata getIdColumn() {
        return idColumn;
    }

}
