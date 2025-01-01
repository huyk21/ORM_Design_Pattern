package com.example.schema;

import java.sql.SQLException;
import com.example.entity.EntityMetadata;
import com.example.mapper.DBMSTypeMapper;

public abstract class DDLGenerator {
    protected final DBMSTypeMapper dbmsTypeMapper;

    public DDLGenerator(DBMSTypeMapper dbmsTypeMapper) {
        this.dbmsTypeMapper = dbmsTypeMapper;
    }

    // Template method
    public final String generateDDL(EntityMetadata metadata) throws SQLException {
        return generateSQL(metadata);
    }

    protected abstract String generateSQL(EntityMetadata metadata);

}
