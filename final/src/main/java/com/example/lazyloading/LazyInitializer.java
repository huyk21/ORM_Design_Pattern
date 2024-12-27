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
        throw new IllegalStateException("Lazy loading failed, target object is null!");
    }

    // Invoke the actual method on the target object
    Object result = method.invoke(target, args);
   
    return result;
}


    private void loadEntity() {
        try {
           
            this.target = fetchCallback.call(); // Fetch the actual entity
            this.initialized = true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize lazy entity", e);
        }
    }
}
