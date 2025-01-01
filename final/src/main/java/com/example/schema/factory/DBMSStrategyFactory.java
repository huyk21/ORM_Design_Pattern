package com.example.schema.factory;

import com.example.schema.strategy.ColumnDefinitionStrategy;
import com.example.schema.strategy.DDLStrategy;

public interface DBMSStrategyFactory {
    DDLStrategy createDDLStrategy();
    ColumnDefinitionStrategy createColumnDefinitionStrategy();
}
