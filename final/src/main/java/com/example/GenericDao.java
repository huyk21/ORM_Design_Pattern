package com.example;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.Annotation.Column;
import com.example.Annotation.Table;

public class GenericDao<T extends BaseEntity> {

    private DatabaseSession session;
    private Class<T> clazz;

    public GenericDao(DatabaseSession session, Class<T> clazz) {
        this.session = session;
        this.clazz = clazz;
    }

    // Create (Insert)
    public void save(T entity) throws SQLException, IllegalAccessException {
        // Debug: Print the table name used for the insert
        System.out.println("Table Name for Insert: " + entity.getTableName()); // Print table name

        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(entity.getTableName()).append(" (");
        StringBuilder values = new StringBuilder();

        // Get fields and values using reflection
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Column.class)) {
                String columnName = entity.getColumnName(field);
                sql.append(columnName).append(",");
                values.append("'").append(field.get(entity)).append("',");
            }
        }

        // Remove trailing commas
        sql.setLength(sql.length() - 1);
        values.setLength(values.length() - 1);

        sql.append(") VALUES (").append(values).append(")");

        // Execute the insert query
        session.executeUpdate(sql.toString());
    }

    public void update(T entity) throws SQLException, IllegalAccessException {
        // Debug: Print the table name used for the update
        String tableName = getTableNameFromAnnotation(); // Get the table name
        System.out.println("Table Name for Update: " + tableName); // Print table name

        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(tableName).append(" SET ");

        // Get all fields and their values
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Column.class)) {
                String columnName = entity.getColumnName(field);
                sql.append(columnName).append(" = '").append(field.get(entity)).append("', ");
            }
        }

        sql.setLength(sql.length() - 2); // Remove the last comma

        // Get the 'id' field dynamically (if exists)
        Field idField = getIdField(clazz);
        if (idField == null) {
            throw new IllegalArgumentException("No id field found for class: " + clazz.getName());
        }

        idField.setAccessible(true); // Make sure the id field is accessible
        sql.append(" WHERE id = ?");

        // Execute the update query
        PreparedStatement stmt = session.getConnection().prepareStatement(sql.toString());
        stmt.setInt(1, (Integer) idField.get(entity)); // Set the id field value
        stmt.executeUpdate();
    }

    public void delete(int id) throws SQLException {
        // Get the table name dynamically from the @Table annotation
        String tableName = getTableNameFromAnnotation(); // Get the table name
        System.out.println("Table Name for Delete: " + tableName); // Print table name

        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        PreparedStatement stmt = session.getConnection().prepareStatement(sql);
        stmt.setInt(1, id); // Set the id for deletion
        stmt.executeUpdate();
    }

    // Select with WHERE, GROUP BY, HAVING
    public List<T> select(String whereClause, String groupBy, String having)
            throws SQLException, IllegalAccessException {
        // Get the table name using the @Table annotation
        String tableName = getTableNameFromAnnotation();
        System.out.println("Table Name for Select: " + tableName); // Print table name for debugging

        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(tableName); // Use the table name from the annotation

        if (whereClause != null) {
            sql.append(" WHERE ").append(whereClause);
        }
        if (groupBy != null) {
            sql.append(" GROUP BY ").append(groupBy);
        }
        if (having != null) {
            sql.append(" HAVING ").append(having);
        }

        ResultSet rs = session.executeQuery(sql.toString());
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            T obj;

            try {
                obj = clazz.getDeclaredConstructor().newInstance();

                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    String columnName = obj.getColumnName(field);
                    field.set(obj, rs.getObject(columnName));
                }
                results.add(obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    // Helper method to get the table name from the @Table annotation
    private String getTableNameFromAnnotation() {
        if (clazz.isAnnotationPresent(Table.class)) {
            Table tableAnnotation = clazz.getAnnotation(Table.class);
            return tableAnnotation.name().isEmpty() ? clazz.getSimpleName().toLowerCase() : tableAnnotation.name();
        } else {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with @Table");
        }
    }

    // Helper method to get the 'id' field dynamically (if it exists)
    private Field getIdField(Class<?> clazz) {
        // Try to find the 'id' field in the class or its superclasses
        while (clazz != null) {
            try {
                return clazz.getDeclaredField("id"); // Look for 'id' field
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass(); // Try the superclass if not found
            }
        }
        return null; // Return null if 'id' field is not found in the class hierarchy
    }
}
