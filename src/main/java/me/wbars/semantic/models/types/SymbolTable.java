package me.wbars.semantic.models.types;

import me.wbars.utils.Registry;

public class SymbolTable extends Registry<Type> {
    private final SymbolTable prev;
    private SymbolTable next;

    public SymbolTable(SymbolTable prev) {
        this.prev = prev;
        if (prev != null) prev.next = this;
    }

    @Override
    public void register(String literal, Type type) {
        if (lookup(literal) != null) throw new RuntimeException();
        super.register(literal, type);
    }

    @Override
    public Type lookup(String literal) {
        Type type = super.lookup(literal);
        return type != null || prev == null ? type : prev.lookup(literal);
    }

    public SymbolTable getNext() {
        return next;
    }
}
