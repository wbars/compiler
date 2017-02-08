package me.wbars.semantic.models.types;

public class PointerType implements Type {
    private final Type type;

    PointerType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String name() {
        return "Pointer";
    }
}
