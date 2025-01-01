package com.example.schema.factory;

import com.example.schema.strategy.PostgresColumnDefinitionStrategy;
import com.example.schema.strategy.PostgresDDLStrategy;
import com.example.schema.strategy.ColumnDefinitionStrategy;
import com.example.schema.strategy.DDLStrategy;

public class PostgreStrategyFactory implements  DBMSStrategyFactory{
    @Override
    public DDLStrategy createDDLStrategy() {
        return new PostgresDDLStrategy();
    }

    @Override
    public ColumnDefinitionStrategy createColumnDefinitionStrategy() {
        return new PostgresColumnDefinitionStrategy();
    }
}
