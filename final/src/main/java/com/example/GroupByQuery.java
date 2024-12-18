package com.example;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class GroupByQuery<T> {
    private String sqlString;
    private List<Field> returnList;

    protected GroupByQuery(DatabaseSession session) {}

    public void having(String field) {}
    public List<T> get() {
        return new ArrayList<>();
    }
}
