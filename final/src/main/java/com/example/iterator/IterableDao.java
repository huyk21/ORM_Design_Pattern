package com.example.iterator;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.EntityMapper;
import com.example.GenericDaoImpl;
import com.example.connection.DatabaseSession;

public class IterableDao<T> extends GenericDaoImpl<T> {
    public IterableDao(DatabaseSession session, Class<T> clazz) {
        super(session, clazz);
    }

    public LazyIterator<T> iterate(String whereCondition, LazyIteratorFactory<T> factory) throws SQLException, ReflectiveOperationException {
        ResultSet resultSet = session.getConnection().createStatement().executeQuery(buildReadQuery(whereCondition));
        return factory.create(resultSet, new EntityMapper<>(clazz, session));
    }
}
