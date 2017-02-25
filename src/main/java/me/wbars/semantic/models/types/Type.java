package me.wbars.semantic.models.types;

public interface Type {
    String name();
    default int aType() {
        throw new UnsupportedOperationException();
    }
    default String alias() {
        return String.valueOf(name().toUpperCase().charAt(0));
    }
}
