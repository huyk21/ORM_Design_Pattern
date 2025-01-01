package com.example.validator;

import java.lang.reflect.Field;

public class NotNullValidator<T> implements Validator<T> {
    @Override
    public boolean validate(T entity, Field field) throws IllegalAccessException {
        Object value = field.get(entity);
        return value != null;
    }

    @Override
    public String getErrorMessage() {
        return "This field must not be null.";
    }

    
}

