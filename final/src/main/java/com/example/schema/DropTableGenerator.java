package com.example.schema;

import com.example.entity.EntityMetadata;
import com.example.mapper.DBMSTypeMapper;

public class DropTableGenerator extends DDLGenerator {
    private final boolean cascade;

    public DropTableGenerator(DBMSTypeMapper dbmsTypeMapper, boolean cascade) {
        super(dbmsTypeMapper);
        this.cascade = cascade;
    }

    @Override
    protected String generateSQL(EntityMetadata metadata) {
        return dbmsTypeMapper.getDropTableSQL(metadata.getTableName(), cascade);
    }
}
