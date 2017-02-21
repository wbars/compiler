package me.wbars.utils;

import java.util.HashMap;
import java.util.Map;

public class Registry<T> {
    private final Map<String, T> elements = new HashMap<>();

    public T lookup(String name) {
        return elements.get(name);
    }

    public void register(String name, T element) {
        elements.put(name, element);
    }
}
