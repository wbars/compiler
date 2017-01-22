package me.wbars.scanner.models;

import java.util.HashMap;
import java.util.Map;

public class PartOfSpeech {
    private static final Map<String, PartOfSpeech> cache = new HashMap<>();
    public final String name;
    private static int counter = 0;
    private final int id;

    private PartOfSpeech(String name) {
        this.name = name;
        this.id = counter++;
    }

    public static PartOfSpeech getOrCreate(String name) {
        return cache.computeIfAbsent(name, PartOfSpeech::new);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PartOfSpeech partOfSpeech = (PartOfSpeech) o;

        return name.equals(partOfSpeech.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public int getId() {
        return id;
    }
}
