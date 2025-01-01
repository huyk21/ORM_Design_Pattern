package com.example.schema;

import java.util.List;
import java.util.stream.Collectors;

import com.example.entity.ColumnMetadata;
import com.example.entity.EntityMetadata;
import com.example.mapper.DBMSColumnDefinitionStrategy;
import com.example.mapper.DBMSTypeMapper;
import com.example.mapper.MySQLColumnDefinitionStrategy;
import com.example.mapper.MySQLTypeMapper;
import com.example.mapper.PostgresColumnDefinitionStrategy;
import com.example.mapper.PostgresTypeMapper;
import com.example.mapper.SqlServerColumnDefinitionStrategy;
import com.example.mapper.SqlServerTypeMapper;

public class CreateTableGenerator extends DDLGenerator {
    private final DBMSColumnDefinitionStrategy columnDefinitionStrategy;

    public CreateTableGenerator(DBMSTypeMapper dbmsTypeMapper) {
        super(dbmsTypeMapper);
        this.columnDefinitionStrategy = createColumnDefinitionStrategy(dbmsTypeMapper);

    }

    private DBMSColumnDefinitionStrategy createColumnDefinitionStrategy(DBMSTypeMapper dbmsTypeMapper) {
        if (dbmsTypeMapper instanceof MySQLTypeMapper) {
            return new MySQLColumnDefinitionStrategy();
        } else if (dbmsTypeMapper instanceof PostgresTypeMapper) {
            return new PostgresColumnDefinitionStrategy();
        } else if (dbmsTypeMapper instanceof SqlServerTypeMapper) {
            return new SqlServerColumnDefinitionStrategy();
        }
        throw new UnsupportedOperationException("Unsupported DBMS type");
    }

    @Override
    protected String generateSQL(EntityMetadata metadata) {
        StringBuilder sql = new StringBuilder();
        sql.append(dbmsTypeMapper.getCreateTableSQL(metadata.getTableName()))
                .append(" (\n");

        // Add columns
        List<String> columnDefinitions = metadata.getColumns().stream()
                .map(this::generateColumnDefinition)
                .collect(Collectors.toList());

        sql.append(String.join(",\n", columnDefinitions));

        // Add primary key
        ColumnMetadata idColumn = metadata.getIdColumn();
        if (idColumn != null) {
            sql.append(",\n    PRIMARY KEY (")
                    .append(idColumn.getColumnName())
                    .append(")");
        }

        // Add foreign keys
        List<String> foreignKeys = metadata.getColumns().stream()
                .filter(ColumnMetadata::isForeignKey)
                .map(this::generateForeignKeyConstraint)
                .collect(Collectors.toList());

        if (!foreignKeys.isEmpty()) {
            sql.append(",\n")
                    .append(String.join(",\n", foreignKeys));
        }

        sql.append("\n)");
        return sql.toString();
    }

    private String generateColumnDefinition(ColumnMetadata column) {
        StringBuilder def = new StringBuilder();
        def.append("    ")
                .append(column.getColumnName())
                .append(" ")
                .append(columnDefinitionStrategy.generateColumnDefinition(column));

        if (!column.isNullable()) {
            def.append(" NOT NULL");
        }
        if (column.isUnique()) {
            def.append(" UNIQUE");
        }

        return def.toString();
    }

    // private String generateColumnDefinition(ColumnMetadata column) {
    // StringBuilder def = new StringBuilder();
    // def.append(" ")
    // .append(column.getColumnName())
    // .append(" ");

    // if (column.isId() && column.getIdAnnotation().autoIncrement()) {
    // def.append(dbmsTypeMapper.getColumnDefinition(column.getJdbcType(),
    // column.getLength(), column.getPrecision()))
    // .append(" ")
    // .append(dbmsTypeMapper.getAutoIncrementSyntax());
    // } else {
    // def.append(dbmsTypeMapper.getColumnDefinition(column.getJdbcType(),
    // column.getLength(), column.getPrecision()));
    // }

    // if (!column.isNullable()) {
    // def.append(" NOT NULL");
    // }
    // if (column.isUnique()) {
    // def.append(" UNIQUE");
    // }

    // return def.toString();
    // }

    // private String generateColumnDefinition(ColumnMetadata column) {
    // StringBuilder def = new StringBuilder();
    // def.append(" ")
    // .append(column.getColumnName())
    // .append(" ");
    //
    // if (column.isId() && column.getIdAnnotation().autoIncrement()) {
    // def.append(dbmsTypeMapper.getAutoIncrementSyntax());
    // } else {
    // def.append(dbmsTypeMapper.getColumnDefinition(column.getJdbcType(),
    // column.getLength(), column.getPrecision()));
    // }
    //
    // if (!column.isNullable()) {
    // def.append(" NOT NULL");
    // }
    // if (column.isUnique()) {
    // def.append(" UNIQUE");
    // }
    //
    // return def.toString();
    // }

    private String generateForeignKeyConstraint(ColumnMetadata column) {
        return "    FOREIGN KEY (" + column.getColumnName() + ") " +
                "REFERENCES " + getReferencedTable(column) + "(id)";
    }

    private String getReferencedTable(ColumnMetadata column) {
        Class<?> fieldType = column.getField().getType();
        return new EntityMetadata(fieldType).getTableName();
    }
}