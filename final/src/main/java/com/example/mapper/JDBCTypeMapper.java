package com.example.mapper;

import java.sql.JDBCType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JDBCTypeMapper {
    private static final Map<Class<?>, JDBCType> TYPE_MAP = new HashMap<>();

    static {
        // Primitive types
        TYPE_MAP.put(boolean.class, JDBCType.BOOLEAN);
        TYPE_MAP.put(byte.class, JDBCType.TINYINT);
        TYPE_MAP.put(short.class, JDBCType.SMALLINT);
        TYPE_MAP.put(int.class, JDBCType.INTEGER);
        TYPE_MAP.put(long.class, JDBCType.BIGINT);
        TYPE_MAP.put(float.class, JDBCType.REAL);
        TYPE_MAP.put(double.class, JDBCType.DOUBLE);

        // Wrapper classes
        TYPE_MAP.put(Boolean.class, JDBCType.BOOLEAN);
        TYPE_MAP.put(Byte.class, JDBCType.TINYINT);
        TYPE_MAP.put(Short.class, JDBCType.SMALLINT);
        TYPE_MAP.put(Integer.class, JDBCType.INTEGER);
        TYPE_MAP.put(Long.class, JDBCType.BIGINT);
        TYPE_MAP.put(Float.class, JDBCType.REAL);
        TYPE_MAP.put(Double.class, JDBCType.DOUBLE);

        // String
        TYPE_MAP.put(String.class, JDBCType.VARCHAR);

        // Date and Time
        TYPE_MAP.put(Date.class, JDBCType.TIMESTAMP);
        TYPE_MAP.put(java.sql.Date.class, JDBCType.DATE);
        TYPE_MAP.put(java.sql.Time.class, JDBCType.TIME);
        TYPE_MAP.put(java.sql.Timestamp.class, JDBCType.TIMESTAMP);
        TYPE_MAP.put(LocalDate.class, JDBCType.DATE);
        TYPE_MAP.put(LocalDateTime.class, JDBCType.TIMESTAMP);

        // Other common types
        TYPE_MAP.put(BigDecimal.class, JDBCType.DECIMAL);
        TYPE_MAP.put(byte[].class, JDBCType.BINARY);
    }

    public static JDBCType getJDBCType(Class<?> javaType) {
        return TYPE_MAP.getOrDefault(javaType, JDBCType.VARCHAR);
    }
}
