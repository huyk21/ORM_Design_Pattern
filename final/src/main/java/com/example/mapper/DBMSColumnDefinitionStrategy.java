package com.example.mapper;

import com.example.entity.ColumnMetadata;

public interface DBMSColumnDefinitionStrategy {
    String generateColumnDefinition(ColumnMetadata column);
}
