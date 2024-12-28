package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the foreign key column in the relationship.
 */
@Target(ElementType.FIELD) // This annotation applies to fields only
@Retention(RetentionPolicy.RUNTIME) // Retain at runtime for reflection
public @interface JoinColumn {
    /**
     * Specifies the name of the foreign key column.
     */
    String name();
}
