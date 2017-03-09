package me.wbars.compiler.utils;

public class MutablePair<T, V> {
    private T first;
    private V second;

    public MutablePair(T first, V second) {
        this.first = first;
        this.second = second;
    }

    public T first() {
        return first;
    }

    public V second() {
        return second;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public void setSecond(V second) {
        this.second = second;
    }
}
