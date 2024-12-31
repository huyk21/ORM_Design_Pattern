package com.example.ddl;

import java.util.List;
import java.util.stream.Collectors;

import com.example.annotation.Id;
import com.example.entity.ColumnMetadata;
import com.example.entity.EntityMetadata;
import com.example.mapper.DBMSTypeMapper;

public class TableGenerator {
    private final EntityMetadata metadata;
    private final DBMSTypeMapper dbmsTypeMapper;

    public TableGenerator(Class<?> entityClass, DBMSTypeMapper dbmsTypeMapper) {
        this.metadata = new EntityMetadata(entityClass);
        this.dbmsTypeMapper = dbmsTypeMapper;
    }

    public String generateCreateTableSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ")
                .append(metadata.getTableName())
                .append(" (\n");

        // Add columns
        List<String> columnDefinitions = metadata.getColumns().stream()
                .map(this::generateColumnDefinition)
                .collect(Collectors.toList());

        sql.append(String.join(",\n", columnDefinitions));

        // Add primary key
        ColumnMetadata idColumn = metadata.getIdColumn();
        if (idColumn != null) {
            sql.append(",\n    PRIMARY KEY (").append(idColumn.getColumnName()).append(")");
        }

        // Add foreign key constraints
        List<String> foreignKeys = metadata.getColumns().stream()
                .filter(ColumnMetadata::isForeignKey)
                .map(this::generateForeignKeyConstraint)
                .collect(Collectors.toList());

        if (!foreignKeys.isEmpty()) {
            sql.append(",\n");
            sql.append(String.join(",\n", foreignKeys));
        }

        sql.append("\n)");
        return sql.toString();
    }

    private String generateColumnDefinition(ColumnMetadata column) {
        StringBuilder def = new StringBuilder("    ")
            .append(column.getColumnName())
            .append(" ");

        if (column.isId() && column.getIdAnnotation().autoIncrement()) {
            if (column.getIdAnnotation().strategy() == Id.Strategy.IDENTITY) {
                def.append(dbmsTypeMapper.getColumnDefinition(column.getJdbcType(), 
                    column.getLength(), column.getPrecision()))
                   .append(" ")
                   .append(dbmsTypeMapper.getAutoIncrementSyntax());
            }
        } else {
            def.append(dbmsTypeMapper.getColumnDefinition(column.getJdbcType(), 
                column.getLength(), column.getPrecision()));
        }

        if (!column.isNullable()) {
            def.append(" NOT NULL");
        }
        if (column.isUnique()) {
            def.append(" UNIQUE");
        }

        return def.toString();
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
