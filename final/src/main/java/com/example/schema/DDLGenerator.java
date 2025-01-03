package com.example.schema;

import java.sql.SQLException;
import com.example.entity.EntityMetadata;
import com.example.schema.strategy.DDLStrategy;

public abstract class DDLGenerator {
    protected final DDLStrategy dbmsTypeMapper;

    public DDLGenerator(DDLStrategy dbmsTypeMapper) {
        this.dbmsTypeMapper = dbmsTypeMapper;
    }

    // Template method
    public final String generateDDL(EntityMetadata metadata) throws SQLException {
        return generateSQL(metadata);
    }

    protected abstract String generateSQL(EntityMetadata metadata);

}
