package io.bclub.view;

public interface AbstractView<T> {
    void bind(T t);
    T get();
}
