package com.example.schema.factory;

import com.example.schema.strategy.MySQLColumnDefinitionStrategy;
import com.example.schema.strategy.MySQLDDLStrategy;
import com.example.schema.strategy.ColumnDefinitionStrategy;
import com.example.schema.strategy.DDLStrategy;

public class MySQLStrategyFactory implements DBMSStrategyFactory{
    @Override
    public DDLStrategy createDDLStrategy() {
        return new MySQLDDLStrategy();
    }

    @Override
    public ColumnDefinitionStrategy createColumnDefinitionStrategy() {
        return new MySQLColumnDefinitionStrategy();
    }
}
