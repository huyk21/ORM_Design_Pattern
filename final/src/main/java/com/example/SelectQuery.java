package com.example;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.example.Mapping.TableMapping;
import com.example.annotation.OneToMany;
import com.example.annotation.OneToOne;
import com.example.annotation.Table;

class JoinTableMapping {
    protected String thisTable;
    protected String refTable;
    protected String thisField;
    protected String refField;

    public JoinTableMapping(String thisTable, String refTable, String thisField, String refField) {
        this.thisTable = thisTable;
        this.refTable = refTable;
        this.thisField = thisField;
        this.refField = refField;
    }

    public String getThisTable() {
        return thisTable;
    }

    public String getRefTable() {
        return refTable;
    }

    public String getThisField() {
        return thisField;
    }

    public String getRefField() {
        return refField;
    }
}

class JoinIdMapping {
    protected int thisIndex;
    protected int refIndex;

    public JoinIdMapping(int thisIndex, int refIndex) {
        this.thisIndex = thisIndex;
        this.refIndex = refIndex;
    }

    public int getThisIndex() {
        return thisIndex;
    }

    public int getRefIndex() {
        return refIndex;
    }
}

public class SelectQuery<T> {
    private DatabaseSession session;

    private String sqlWhere;

    private List<TableMapping> tableMappings;
    private List<JoinTableMapping> joinMappings;
    private Class<T> returnType;

    protected SelectQuery(DatabaseSession session, Class<T> returnType) {
        this.session = session;

        tableMappings = new ArrayList<>();
        tableMappings.add(new TableMapping(returnType, returnType.getName().toUpperCase()));

        joinMappings = new ArrayList<>();
        this.returnType = returnType;

        sqlWhere = "";
    }

    public SelectQuery<T> join(String table, String field) {
        List<Field> fields = new ArrayList<>();
        for (var t : tableMappings) {
            if (t.getMappingName() == table) {
                fields = t.getFields();
            }
        }

        if (fields.size() == 0) {
            return this;
        }

        for (var f : fields) {
            if (f.getName() == field) {
                if (f.isAnnotationPresent(OneToOne.class)) {
                    tableMappings.add(new TableMapping(f.getClass(), f.getName()));

                    var m = tableMappings.get(tableMappings.size() - 1);
                    joinMappings.add(new JoinTableMapping(table, m.getMappingName(), field, m.getIdField()));
                } else if (f.isAnnotationPresent(OneToMany.class)) {
                    Class<?> type = getCollectionGenericType(f);
                    tableMappings.add(new TableMapping(type, f.getName()));

                    var m = tableMappings.get(tableMappings.size() - 1);
                    joinMappings.add(new JoinTableMapping(table, m.getMappingName(), field, m.getIdField()));
                }

            }
        }

        return this;
    }

    public SelectQuery<T> where(String whereClause) {
        sqlWhere = "\nwhere ";
        sqlWhere += whereClause;
        return this;
    }

    public SelectQuery<T> where(QueryStrategy strategy) {
        sqlWhere = "\nwhere ";
        sqlWhere += strategy.toString(tableMappings);
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

        List<JoinIdMapping> idMapping = new ArrayList<>();
        List<Field> columns = new ArrayList<>();

        for (var mapping : tableMappings) {
            columns.addAll(mapping.getFields());
        }

        for (var mapping : joinMappings) {
            int thisIndex = -1;
            int refIndex = -1;
            int i = 0;

            for (var table : tableMappings) {
                var fields = table.getFieldsName();
                for (var field : fields) {
                    if (table.getMappingName() == mapping.getThisTable() && 
                        field == mapping.getThisField()) {
                        thisIndex = i;
                    }
                    if (table.getMappingName() == mapping.getRefTable() && 
                        field == mapping.getRefField()) {
                        refIndex = i;
                    }
                }
            }

            idMapping.add(new JoinIdMapping(thisIndex, refIndex));
        }

        List<Object> lastRow = new ArrayList<>();

        List<T> results = new ArrayList<>();

        try {
            T obj = null;

            while (rs.next()) {
                if (lastRow.isEmpty()) {
                    obj = returnType.getDeclaredConstructor().newInstance();
                }

                int i = 1;
                for (var table : tableMappings) {
                    List<Field> fields = table.getFields();

                    for (var field : fields) {
                        
                        field.set(obj, rs.getObject(i));
                        i++;
                    }
                }

                lastRow.clear();
                for (i = 1; i <= columns.size(); i++) {
                    lastRow.add(rs.getObject(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    private String generateSQL() throws SecurityException {
        StringBuilder builder = new StringBuilder();

        builder.append("select ");

        for (var mapping : tableMappings) {
            List<String> fields = mapping.getFieldsName();

            for (var field : fields) {
                builder.append(mapping.getMappingName());
                builder.append(".");
                builder.append(field);
                builder.append(", ");
            }
        }

        builder.delete(builder.length() - 2, builder.length());

        builder.append(" from ");

        builder.append(tableMappings.get(0).getTableName());
        builder.append(" ");
        builder.append(tableMappings.get(0).getMappingName());

        for (int i = 1; i < tableMappings.size(); i++) {
            builder.append(" join ");
            builder.append(tableMappings.get(i).getTableName());
            builder.append(" ");
            builder.append(tableMappings.get(i).getMappingName());
            builder.append(" on ");
            builder.append(tableMappings.get(i).getMappingName());
            builder.append(".");
            // builder.append("var");
            builder.append(" = ");
            builder.append(tableMappings.get(i - 1).getMappingName());
            builder.append(".");
            // builder.append("var");
        }

        builder.append(sqlWhere);

        return builder.toString();
    }

    private Class<?> getCollectionGenericType(Field collection) {
        if (Collection.class.isAssignableFrom(collection.getClass())) {
            var genericFieldType = collection.getGenericType();

            if (genericFieldType instanceof ParameterizedType) {
                ParameterizedType aType = (ParameterizedType) genericFieldType;
                Type[] fieldArgTypes = aType.getActualTypeArguments();
                for (Type fieldArgType : fieldArgTypes) {
                    var fieldArgClass = (Class<?>) fieldArgType;
                    System.out.println("fieldArgClass = " + fieldArgClass);
                    return fieldArgClass;
                }
            }
        }
        return null;
    }
}
