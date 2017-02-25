package me.wbars.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Registry<T> {
    private final Map<String, T> elements = new HashMap<>();

    public T lookup(String name) {
        return elements.get(name);
    }

    public void register(String name, T element) {
        elements.put(name, element);
    }

    public int size() {
        return elements.size();
    }

    public Set<String> keys() {
        return elements.keySet();
    }
}
