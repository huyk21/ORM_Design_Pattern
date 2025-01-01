package com.example.schema;

import java.sql.SQLException;
import com.example.connection.DatabaseSession;
import com.example.entity.EntityMetadata;
import com.example.schema.factory.DBMSStrategyFactory;

public class SchemaManager {
    private final DatabaseSession session;
    private final DBMSStrategyFactory factory;

    public SchemaManager(DatabaseSession session, DBMSStrategyFactory factory) {
        this.session = session;
        this.factory = factory;
    }

    public void createTable(Class<?> entityClass) throws SQLException {
        executeDDL(generateCreateTableSQL(entityClass));
    }

    public void dropTable(Class<?> entityClass, boolean cascade) throws SQLException {
        EntityMetadata metadata = new EntityMetadata(entityClass);
        if (!cascade) {
            dropForeignKeyConstraints(metadata);
        }
        executeDDL(generateDropTableSQL(metadata, cascade));
    }

    public void dropTable(Class<?> entityClass) throws SQLException {
        dropTable(entityClass, true); // Default to cascade
    }

    private String generateCreateTableSQL(Class<?> entityClass) throws SQLException {
        DDLGenerator generator = new CreateTableGenerator(factory);
        return generator.generateDDL(new EntityMetadata(entityClass));
    }

    private String generateDropTableSQL(EntityMetadata metadata, boolean cascade) throws SQLException {
        DDLGenerator generator = new DropTableGenerator(factory.createDDLStrategy(), cascade);
        return generator.generateDDL(metadata);
    }

    private void dropForeignKeyConstraints(EntityMetadata metadata) throws SQLException {
        String dropFKSQL = factory.createDDLStrategy().getDropForeignKeySQL(metadata.getTableName());
        if (dropFKSQL != null) {
            executeDDL(dropFKSQL);
        }
    }

    private void executeDDL(String sql) throws SQLException {
        System.out.println("Executing DDL: " + sql);
        session.executeUpdate(sql);
    }
}
