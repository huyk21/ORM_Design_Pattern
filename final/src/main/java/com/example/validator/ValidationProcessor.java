package com.example.validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ValidationProcessor<T> {
    private final Map<Class<? extends Annotation>, Validator<T>> validators = new HashMap<>();

    // Register custom validators
    public void registerValidator(Class<? extends Annotation> annotationClass, Validator<T> validator) {
        validators.put(annotationClass, validator);
    }

    // Validate the entity
    public boolean validate(T entity) {
        boolean isValid = true;

        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            for (Map.Entry<Class<? extends Annotation>, Validator<T>> entry : validators.entrySet()) {
                Class<? extends Annotation> annotationClass = entry.getKey();
                Validator<T> validator = entry.getValue();

                if (field.isAnnotationPresent(annotationClass)) {
                    try {
                        if (!validator.validate(entity, field)) {
                            System.err.println(field.getName() + ": " + validator.getErrorMessage());
                            isValid = false;
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return isValid;
    }
}
