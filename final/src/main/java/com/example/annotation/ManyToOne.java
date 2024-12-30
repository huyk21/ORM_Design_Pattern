package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a Many-to-One relationship between two entities.
 */
@Target(ElementType.FIELD) // This annotation applies to fields only
@Retention(RetentionPolicy.RUNTIME) // Retain at runtime for reflection
public @interface ManyToOne {
}
