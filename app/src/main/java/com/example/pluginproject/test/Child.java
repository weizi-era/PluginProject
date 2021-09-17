package com.example.pluginproject.test;

public abstract class Child<T> {

    private T childInstance;

    protected abstract T create();

    public T get() {
        if (childInstance == null) {
            synchronized (Child.class) {
                if (childInstance == null) {
                    childInstance = create();
                }
            }
        }

        return childInstance;
    }
}
