package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Id {
    boolean autoIncrement() default true;

    Strategy strategy() default Strategy.IDENTITY;

    enum Strategy {
        IDENTITY, // Auto-increment
        SEQUENCE, // Use sequence
        TABLE // Use separate table
    }
}