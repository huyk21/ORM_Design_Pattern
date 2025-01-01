package com.example.schema;

import com.example.entity.EntityMetadata;
import com.example.schema.strategy.DDLStrategy;

public class DropTableGenerator extends DDLGenerator {
    private final boolean cascade;

    public DropTableGenerator(DDLStrategy dbmsTypeMapper, boolean cascade) {
        super(dbmsTypeMapper);
        this.cascade = cascade;
    }

    @Override
    protected String generateSQL(EntityMetadata metadata) {
        return dbmsTypeMapper.getDropTableSQL(metadata.getTableName(), cascade);
    }
}
