package com.example;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.Mapping.TableMapping;
import com.example.connection.DatabaseSession;


public class SelectQuery<T> {
    private DatabaseSession session;

    private String sqlFrom;
    private String sqlWhere;

    private TableMapping tableMapping;
    private Class<T> returnType;

    protected SelectQuery(DatabaseSession session, Class<T> returnType) {
        this.session = session;

        tableMapping = new TableMapping(returnType, "root");
        this.returnType = returnType;

        sqlFrom = "";
        sqlWhere = "";
    }

    public SelectQuery<T> join(String table, String field, String name) {
        tableMapping.join(table, field, name);

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
        ResultSet rs = session.executeQuery(sqlString);

        System.out.println(sqlString);

        List<T> results = new ArrayList<>();

        try {
            tableMapping.setFields(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    private String generateSQL() throws SecurityException {
        StringBuilder builder = new StringBuilder();

        builder.append("select ");
        builder.append(tableMapping.getQueryString());
        builder.append("\nfrom ");
        builder.append(sqlFrom);
        builder.append(sqlWhere);

        return builder.toString();
    }
}
