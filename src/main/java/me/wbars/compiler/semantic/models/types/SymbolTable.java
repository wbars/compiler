package me.wbars.compiler.semantic.models.types;

import me.wbars.compiler.utils.Registry;

public class SymbolTable extends Registry<Type> {
    private final SymbolTable prev;
    private SymbolTable next;

    public SymbolTable(SymbolTable prev) {
        this.prev = prev;
        if (prev != null) prev.next = this;
    }

    @Override
    public void register(String literal, Type type) {
//        if (lookup(literal) != null) throw new RuntimeException(); todo enable this to prevent overriding things
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
