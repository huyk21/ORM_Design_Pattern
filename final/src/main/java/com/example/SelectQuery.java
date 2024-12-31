package com.example;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.Mapping.field.TableMapping;
import com.example.connection.DatabaseSession;


public class SelectQuery<T> {
    private DatabaseSession session;

    private String sqlWhere;
    private String sqlOrderBy;

    private TableMapping<T> tableMapping;

    protected SelectQuery(DatabaseSession session, Class<T> returnType, String mappingName) {
        this.session = session;

        tableMapping = new TableMapping<T>(returnType, mappingName);

        sqlWhere = "";
    }

    public SelectQuery<T> join(String table, String field, String mappingName) {
        tableMapping.join(table, field, mappingName);

        return this;
    }

    public SelectQuery<T> where(String whereClause) {
        sqlWhere = "\nwhere ";
        sqlWhere += tableMapping.getQueryString();
        return this;
    }

    public SelectQuery<T> orderBy(String orderBy) {
        sqlOrderBy = "\norder by " + orderBy;
        return this;
    }

    public GroupByQuery<T> groupBy() {
        return new GroupByQuery<T>(session);
    }

    public List<T> get() throws SQLException {
        String sqlString = generateSQL();
        System.out.println("\n*** Execute SELECT statement:\n" + sqlString + " ***\n");

        ResultSet rs = session.executeQuery(sqlString);

        List<T> results = new ArrayList<>();

        boolean hasObject = false;

        while (rs.next()) {
            try {
                for (T item : results) {
                    if (tableMapping.hasSameID(item, rs)) {
                        tableMapping.setFields(item, rs);
                        hasObject = true;
                        break;
                    }
                }
                
                if (hasObject) {
                    continue;
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
        builder.append(sqlOrderBy);
        builder.append(";");

        return builder.toString();
    }
}
