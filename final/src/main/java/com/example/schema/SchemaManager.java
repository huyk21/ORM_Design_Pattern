package com.example.schema;

import java.sql.SQLException;
import java.util.List;

import com.example.connection.DatabaseSession;
import com.example.ddl.TableGenerator;
import com.example.entity.EntityMetadata;
import com.example.mapper.DBMSTypeMapper;

public class SchemaManager {
    private final DatabaseSession session;

    public SchemaManager(DatabaseSession session) {
        this.session = session;
    }

    public void createTable(Class<?> entityClass, DBMSTypeMapper dbmsTypeMapper) throws SQLException {
        TableGenerator generator = new TableGenerator(entityClass, dbmsTypeMapper);
        String createTableSQL = generator.generateCreateTableSQL();

        System.out.println("Executing DDL: " + createTableSQL);
        // session.executeUpdate(createTableSQL);
    }

    public void createSchema(List<Class<?>> entityClasses, DBMSTypeMapper dbmsTypeMapper) throws SQLException {
        for (Class<?> entityClass : entityClasses) {
            createTable(entityClass, dbmsTypeMapper);
        }
    }

    public void dropTable(Class<?> entityClass) throws SQLException {
        EntityMetadata metadata = new EntityMetadata(entityClass);
        String dropTableSQL = "DROP TABLE IF EXISTS " + metadata.getTableName();

        System.out.println("Executing DDL: " + dropTableSQL);
        session.executeUpdate(dropTableSQL);
    }
}
