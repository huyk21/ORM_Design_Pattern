package com.example.validator;

import java.lang.reflect.Field;

public class AlphanumericValidator<T> implements Validator<T> {
    @Override
    public boolean validate(T entity, Field field) throws IllegalAccessException {
        Object value = field.get(entity);
        if (value == null) {
            return false;
        }

        String stringValue = value.toString();
        return stringValue.matches("^[a-zA-Z0-9]*$");
    }

    @Override
    public String getErrorMessage() {
        return "Field must contain only alphanumeric characters.";
    }
}

