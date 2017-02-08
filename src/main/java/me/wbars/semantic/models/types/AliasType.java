package me.wbars.semantic.models.types;

public class AliasType implements Type {
    private final String name;
    private final Type type;

    AliasType(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String name() {
        return name;
    }
}
