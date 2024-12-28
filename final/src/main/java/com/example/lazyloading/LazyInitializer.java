package com.example.lazyloading;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

public class LazyInitializer<T> {
    private T target;
    private final Class<T> entityClass;
    private final Callable<T> fetchCallback;
    private boolean initialized = false;

    public LazyInitializer(Class<T> entityClass, Callable<T> fetchCallback) {
        this.entityClass = entityClass;
        this.fetchCallback = fetchCallback;
    }

    public T createProxy() {
        try {
            return new ByteBuddy()
                    .subclass(entityClass) // Create subclass proxy
                    .method(ElementMatchers.isVirtual() // Match only non-final methods
                            .and(ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class)))) // Exclude Object methods
                    .intercept(MethodDelegation.to(this)) // Delegate method calls to this LazyInitializer
                    .make()
                    .load(entityClass.getClassLoader())
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create proxy", e);
        }
    }

    @net.bytebuddy.implementation.bind.annotation.RuntimeType
    public Object intercept(@net.bytebuddy.implementation.bind.annotation.SuperCall Callable<Object> superCall,
                            @net.bytebuddy.implementation.bind.annotation.Origin Method method,
                            @net.bytebuddy.implementation.bind.annotation.AllArguments Object[] args) throws Exception {
        if (!initialized) {
            loadEntity();
        }

        if (target == null) {
            // Print error and return a default value or null based on the return type
            System.out.println("Lazy loading failed: target object is null for method " + method.getName());
            return getDefaultValue(method.getReturnType());
        }

        // Invoke the actual method on the target object
        return method.invoke(target, args);
    }

    private void loadEntity() {
        try {
            this.target = fetchCallback.call(); // Fetch the actual entity
            this.initialized = true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize lazy entity", e);
        }
    }

    // Helper to provide default values for primitive return types
    private Object getDefaultValue(Class<?> returnType) {
        if (returnType.isPrimitive()) {
            if (returnType == boolean.class) return false;
            if (returnType == char.class) return '\0';
            if (returnType == byte.class || returnType == short.class ||
                returnType == int.class || returnType == long.class) return 0;
            if (returnType == float.class) return 0.0f;
            if (returnType == double.class) return 0.0d;
        }
        return null; // For non-primitive types
    }
}
