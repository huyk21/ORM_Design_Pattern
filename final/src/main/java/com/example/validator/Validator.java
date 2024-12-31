package com.example.validator;

import java.lang.reflect.Field;

public interface Validator<T> {
    boolean validate(T entity, Field field) throws IllegalAccessException;
    String getErrorMessage();
}