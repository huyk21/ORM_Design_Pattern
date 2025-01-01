package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.JDBCType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    String name() default "";

    JDBCType type() default JDBCType.VARCHAR;

    boolean nullable() default true;

    boolean unique() default false;

    int length() default 255;

    int precision() default 0;

}
