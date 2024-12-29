package com.example;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.Mapping.field.TableMapping;
import com.example.connection.DatabaseSession;


public class SelectQuery<T> {
    private DatabaseSession session;

    private String sqlFrom;
    private String sqlWhere;

    private TableMapping<T> tableMapping;
    private Class<T> returnType;

    protected SelectQuery(DatabaseSession session, Class<T> returnType) {
        this.session = session;

        tableMapping = new TableMapping<T>(returnType, "root");
        this.returnType = returnType;

        sqlFrom = "";
        sqlWhere = "";
    }

    public SelectQuery<T> join(String table, String field, String name) {
        tableMapping.join(table, field, name);

        sqlFrom += " " + "tableName" + " on " + field + " = " + name;

        return this;
    }

    public SelectQuery<T> where(String whereClause) {
        sqlWhere = "\nwhere ";
        sqlWhere += tableMapping.getQueryString();
        return this;
    }

    public SelectQuery<T> orderBy() {
        return this;
    }

    public GroupByQuery<T> groupBy() {
        return new GroupByQuery<T>(session);
    }

    public List<T> get() throws SQLException {
        String sqlString = generateSQL();
        System.out.println(sqlString);

        ResultSet rs = session.executeQuery(sqlString);

        List<T> results = new ArrayList<>();

        while (rs.next()) {
            try {
                for (T item : results) {
                    if (tableMapping.hasSameID(item, rs)) {
                        tableMapping.setFields(item, rs);
                        continue;
                    }
                }
                T newObj = tableMapping.createObject();

                tableMapping.setFields(newObj, rs);
                results.add(newObj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return results;
    }

    private String generateSQL() throws SecurityException {
        StringBuilder builder = new StringBuilder();

        builder.append(tableMapping.getQueryString());
        builder.append(tableMapping.getFromString());
        builder.append(sqlWhere);

        return builder.toString();
    }
}
