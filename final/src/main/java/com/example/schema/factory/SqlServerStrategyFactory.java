package com.example.schema.factory;

import com.example.schema.strategy.SqlServerColumnDefinitionStrategy;
import com.example.schema.strategy.SqlServerDDLStrategy;
import com.example.schema.strategy.ColumnDefinitionStrategy;
import com.example.schema.strategy.DDLStrategy;

public class SqlServerStrategyFactory implements DBMSStrategyFactory {
    @Override
    public DDLStrategy createDDLStrategy() {
        return new SqlServerDDLStrategy();
    }

    @Override
    public ColumnDefinitionStrategy createColumnDefinitionStrategy() {
        return new SqlServerColumnDefinitionStrategy();
    }
}
