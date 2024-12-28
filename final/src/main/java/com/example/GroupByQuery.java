package com.example;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.example.connection.DatabaseSession;

public class GroupByQuery<T> {
    private String sqlString;
    private List<Field> returnList;

    protected GroupByQuery(DatabaseSession session) {}

    public void having(String field) {

    }
    
    public List<T> get() {
        return new ArrayList<>();
    }
}
