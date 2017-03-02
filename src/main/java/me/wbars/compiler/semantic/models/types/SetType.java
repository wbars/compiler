package me.wbars.compiler.semantic.models.types;

public class SetType implements Type {
    private final Type type;

    SetType(Type type) {
        this.type = type;
    }

    @Override
    public String name() {
        return "Set";
    }

    public Type getType() {
        return type;
    }
}
