package com.example.schema;

import java.sql.SQLException;
import com.example.connection.DatabaseSession;
import com.example.entity.EntityMetadata;
import com.example.mapper.DBMSTypeMapper;

public class SchemaManager {
    private final DatabaseSession session;
    private final DBMSTypeMapper dbmsTypeMapper;

    public SchemaManager(DatabaseSession session, DBMSTypeMapper dbmsTypeMapper) {
        this.session = session;
        this.dbmsTypeMapper = dbmsTypeMapper;
    }

    public void createTable(Class<?> entityClass) throws SQLException {
        EntityMetadata metadata = new EntityMetadata(entityClass);
        DDLGenerator generator = new CreateTableGenerator(dbmsTypeMapper);
        String createTableSQL = generator.generateDDL(metadata);
        System.out.println("Executing DDL: " + createTableSQL);
        session.executeUpdate(createTableSQL);
    }

    public void dropTable(Class<?> entityClass, boolean cascade) throws SQLException {
        EntityMetadata metadata = new EntityMetadata(entityClass);

        // Drop foreign key constraints if not cascading
        if (!cascade) {
            dropForeignKeyConstraints(metadata);
        }

        DDLGenerator generator = new DropTableGenerator(dbmsTypeMapper, cascade);
        String dropTableSQL = generator.generateDDL(metadata);

        System.out.println("Executing DDL: " + dropTableSQL);
        session.executeUpdate(dropTableSQL);
    }

    public void dropTable(Class<?> entityClass) throws SQLException {
        dropTable(entityClass, true); // Default to cascade
    }

    private void dropForeignKeyConstraints(EntityMetadata metadata) throws SQLException {
        String dropFKSQL = dbmsTypeMapper.getDropForeignKeySQL(metadata.getTableName());
        if (dropFKSQL != null) {
            System.out.println("Executing DDL: " + dropFKSQL);
            session.executeUpdate(dropFKSQL);
        }
    }
}
