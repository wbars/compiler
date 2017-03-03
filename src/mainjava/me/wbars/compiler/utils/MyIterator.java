package me.wbars.compiler.utils;

import java.util.List;

public class MyIterator<T> {
    private final List<T> value;
    private int counter = 0;

    public MyIterator(List<T> value) {
        this.value = value;
    }

    public boolean notFinished() {
        return counter < value.size();
    }

    public T current() {
        return notFinished() ? value.get(counter) : null;
    }

    public void advance() {
        counter++;
    }

    public void descent() {
        counter--;
    }

    // can switch to LL(1)?
    public T lookahead() {
        return counter + 1 < value.size() ? value.get(counter + 1) : null;
    }
}
