package com.example.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.entity.ColumnMetadata;
import com.example.entity.EntityMetadata;
import com.example.schema.factory.DBMSStrategyFactory;
import com.example.schema.strategy.*;

public class CreateTableGenerator extends DDLGenerator {
    private final ColumnDefinitionStrategy columnDefinitionStrategy;

    public CreateTableGenerator(DBMSStrategyFactory factory) {
        super(factory.createDDLStrategy());
        this.columnDefinitionStrategy = factory.createColumnDefinitionStrategy();
    }

    @Override
    protected String generateSQL(EntityMetadata metadata) {
        StringBuilder sql = new StringBuilder();
        sql.append(dbmsTypeMapper.getCreateTableSQL(metadata.getTableName()))
                .append(" (\n")
                .append(String.join(",\n", generateColumnDefinitions(metadata)))
                .append("\n)");
        return sql.toString();
    }


    private List<String> generateColumnDefinitions(EntityMetadata metadata) {
        List<String> definitions = new ArrayList<>();
        definitions.addAll(generateColumns(metadata));
        definitions.addAll(generateConstraints(metadata));
        return definitions;
    }

    private List<String> generateColumns(EntityMetadata metadata) {
        return metadata.getColumns().stream()
                .map(this::generateColumnDefinition)
                .collect(Collectors.toList());
    }

    private String generateColumnDefinition(ColumnMetadata column) {
        StringBuilder definition = new StringBuilder();
        definition.append("    ")
                .append(column.getColumnName())
                .append(" ")
                .append(columnDefinitionStrategy.generateColumnDefinition(column))
                .append(column.isNullable() ? "" : " NOT NULL")
                .append(column.isUnique() ? " UNIQUE" : "");
        return definition.toString();
    }

    private List<String> generateConstraints(EntityMetadata metadata) {
        List<String> constraints = new ArrayList<>();

        // Add primary key
        ColumnMetadata idColumn = metadata.getIdColumn();
        if (idColumn != null) {
            constraints.add("    PRIMARY KEY (" + idColumn.getColumnName() + ")");
        }

        // Add foreign keys
        List<String> foreignKeys = metadata.getColumns().stream()
                .filter(ColumnMetadata::isForeignKey)
                .map(this::generateForeignKeyConstraint)
                .collect(Collectors.toList());

        constraints.addAll(foreignKeys);
        return constraints;
    }

    private String generateForeignKeyConstraint(ColumnMetadata column) {
        return "    FOREIGN KEY (" + column.getColumnName() + ") " +
                "REFERENCES " + getReferencedTable(column) + "(id)";
    }

    private String getReferencedTable(ColumnMetadata column) {
        Class<?> fieldType = column.getField().getType();
        return new EntityMetadata(fieldType).getTableName();
    }
}