package me.wbars.semantic.models.types;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final Map<String, Type> identifiersTypes = new HashMap<>();
    private final SymbolTable prev;
    private SymbolTable next;

    public SymbolTable(SymbolTable prev) {
        this.prev = prev;
        if (prev != null) prev.next = this;
    }

    public void register(String literal, Type type) {
        if (identifiersTypes.putIfAbsent(literal, type) != null) throw new RuntimeException();
    }

    public Type get(String literal) {
        Type type = identifiersTypes.get(literal);
        return type != null || prev == null ? type : prev.get(literal);
    }

    public SymbolTable getNext() {
        return next;
    }
}
