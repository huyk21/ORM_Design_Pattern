package com.example.Mapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class TableMapping {
    private OneToManyField field;

    public TableMapping(Class<?> clazz, String mappingName) {
        this.field = new OneToManyField(null, clazz, mappingName);
        this.field.isJoin = true;
    }

    public String getQueryString() {
        List<String> fieldString = field.toQueryString();

        String queryString = "";
        for (int i = 0; i < fieldString.size() - 1; i++) {
            queryString += fieldString.get(i) + ", ";
        }
        queryString += fieldString.get(fieldString.size() - 1);

        return queryString;
    }

    public void join(String mappingTable, String field, String mappingName) {
        this.field.join(mappingName, field, mappingName);
    }

    public void setFields(ResultSet resultSet) throws SQLException {
        this.field.setIndex(1);
        try {
            field.setFields(null, resultSet);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
