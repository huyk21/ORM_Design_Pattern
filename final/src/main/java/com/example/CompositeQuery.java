package com.example;
import java.util.ArrayList;
import java.util.List;

interface Clause {
    String build();
}

class WhereClause implements Clause {
    private String condition;

    public WhereClause(String condition) {
        this.condition = condition;
    }

    @Override
    public String build() {
        return "WHERE " + condition;
    }
}

class GroupByClause implements Clause {
    private String column;

    public GroupByClause(String column) {
        this.column = column;
    }

    @Override
    public String build() {
        return "GROUP BY " + column;
    }
}

class HavingClause implements Clause {
    private String condition;

    public HavingClause(String condition) {
        this.condition = condition;
    }

    @Override
    public String build() {
        return "HAVING " + condition;
    }
}

public class CompositeQuery {
    private String tableName;
    private List<Clause> clauses = new ArrayList<>();
    private List<String> selectedColumns = new ArrayList<>();

    public CompositeQuery(String tableName) {
        this.tableName = tableName;
    }

    // Allow columns to be selected explicitly
    public void selectColumns(String... columns) {
        for (String column : columns) {
            selectedColumns.add(column);
        }
    }

    // Add clause methods
    public void addClause(Clause clause) {
        clauses.add(clause);
    }

    public String buildQuery() {
        // Ensure we don't select * if columns are specified
        String columns = selectedColumns.isEmpty() ? "*" : String.join(", ", selectedColumns);
        StringBuilder query = new StringBuilder("SELECT ").append(columns).append(" FROM ").append(tableName);
        
        for (Clause clause : clauses) {
            query.append(" ").append(clause.build());
        }
        
        return query.toString();
    }
}
