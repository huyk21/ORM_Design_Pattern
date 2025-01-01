package com.example.schema.strategy;

import com.example.entity.ColumnMetadata;

public interface ColumnDefinitionStrategy {
    String generateColumnDefinition(ColumnMetadata column);
}
