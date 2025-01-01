// File: SelectBuilder.java
package com.example;

import java.util.ArrayList;
import java.util.List;

import com.example.annotation.Table;
import com.example.entity.EntityUtils;

/**
 * Builder class for constructing SQL SELECT queries.
 *
 * @param <T> The type of the entity.
 */
public class SelectBuilder<T> {
    private final Class<T> clazz;
    private final List<String> selectColumns = new ArrayList<>();
    private final List<String> joins = new ArrayList<>();
    private String whereClause;
    private String groupByClause;
    private String havingClause;

    /**
     * Constructor initializing the builder with the entity class.
     *
     * @param clazz The Class type of the entity.
     */
    public SelectBuilder(Class<T> clazz) {
        this.clazz = clazz;
    }

    public SelectBuilder<T> addScalar(String function, String column, String alias) {
        String scalar = function + "(" + column + ")";
        if (alias != null && !alias.isEmpty()) {
            scalar += " AS " + alias;
        }
        selectColumns.add(scalar);
        return this;
    }

    public SelectBuilder<T> addColumn(String column) {
        selectColumns.add(column);
        return this;
    }

    public SelectBuilder<T> addJoin(String joinType, String table, String alias, String onCondition) {
        String join = joinType + " " + table + " " + alias + " ON " + onCondition;
        joins.add(join);
        return this;
    }

    public SelectBuilder<T> where(String whereClause) {
        this.whereClause = whereClause;
        return this;
    }

    public SelectBuilder<T> groupBy(String groupByClause) {
        this.groupByClause = groupByClause;
        return this;
    }

    public SelectBuilder<T> having(String havingClause) {
        this.havingClause = havingClause;
        return this;
    }

    public String buildSelectQuery() {
        StringBuilder query = new StringBuilder("SELECT ");
        if (selectColumns.isEmpty()) {
            query.append("*");
        } else {
            query.append(String.join(", ", selectColumns));
        }

        // Determine main table from @Table annotation or class name
        String mainTable = EntityUtils.getTableName(clazz);
        query.append(" FROM ").append(mainTable).append(" ");

        // Append JOIN clauses
        if (!joins.isEmpty()) {
            for (String join : joins) {
                query.append(join).append(" ");
            }
        }

        // Append WHERE clause
        if (whereClause != null && !whereClause.isEmpty()) {
            query.append("WHERE ").append(whereClause).append(" ");
        }

        // Append GROUP BY clause
        if (groupByClause != null && !groupByClause.isEmpty()) {
            query.append("GROUP BY ").append(groupByClause).append(" ");
        }

        // Append HAVING clause
        if (havingClause != null && !havingClause.isEmpty()) {
            query.append("HAVING ").append(havingClause).append(" ");
        }

        return query.toString().trim();
    }

    public boolean hasJoins() {
        return !joins.isEmpty();
    }
}
