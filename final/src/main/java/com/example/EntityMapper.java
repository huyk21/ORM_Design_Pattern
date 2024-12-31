// File: EntityMapper.java
package com.example;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.annotation.Column;
import com.example.annotation.Id;
import com.example.annotation.JoinColumn;
import com.example.annotation.ManyToOne;
import com.example.annotation.OneToOne;
import com.example.connection.DatabaseSession;
import com.example.entity.Dao;
import com.example.entity.GenericDaoImpl;

/**
 * Responsible for mapping ResultSet to entity instances.
 *
 * @param <T> The type of the entity.
 */
public class EntityMapper<T> {

    private final Class<T> clazz;
    private final DatabaseSession session;

    /**
     * Constructor initializing the mapper with the entity class and database session.
     *
     * @param clazz   The Class type of the entity.
     * @param session The DatabaseSession instance.
     */
    public EntityMapper(Class<T> clazz, DatabaseSession session) {
        this.clazz = clazz;
        this.session = session;
    }

    /**
     * Maps a ResultSet row to an entity instance.
     *
     * @param rs The ResultSet.
     * @return The mapped entity.
     * @throws ReflectiveOperationException If instantiation fails.
     * @throws SQLException                 If ResultSet access fails.
     */
    public T mapResultSetToEntity(ResultSet rs) throws ReflectiveOperationException, SQLException {
        T entity = clazz.getDeclaredConstructor().newInstance();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                String columnName = column.name();
                Object value = rs.getObject(columnName);
                field.set(entity, value);
            }

            if (field.isAnnotationPresent(OneToOne.class)) {
                JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                if (joinColumn != null) {
                    Object foreignKey = rs.getObject(joinColumn.name());
                    if (foreignKey != null) {
                        Dao<?> dao = new GenericDaoImpl<>(session, field.getType());
                        Object relatedEntity = dao.findById(foreignKey).orElse(null);
                        field.set(entity, relatedEntity);
                    }
                }
            }

            if (field.isAnnotationPresent(ManyToOne.class)) {
                JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                if (joinColumn != null) {
                    Object foreignKey = rs.getObject(joinColumn.name());
                    if (foreignKey != null) {
                        Dao<?> dao = new GenericDaoImpl<>(session, field.getType());
                        Object relatedEntity = dao.findById(foreignKey).orElse(null);
                        field.set(entity, relatedEntity);
                    }
                }
            }

            // Note: Handling of OneToMany relationships can be implemented with lazy loading or separate queries
        }

        return entity;
    }

    /**
     * Retrieves the ID field annotated with @Id.
     *
     * @return The ID Field.
     * @throws IllegalStateException If no field is annotated with @Id.
     */
    public Field getIdField() {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }
        throw new IllegalStateException("No field annotated with @Id in class " + clazz.getName());
    }
}
