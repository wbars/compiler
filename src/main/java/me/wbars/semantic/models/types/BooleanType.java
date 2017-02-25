package me.wbars.semantic.models.types;

public class BooleanType implements Type {
    @Override
    public String name() {
        return "Boolean";
    }

    @Override
    public String alias() {
        return "Z";
    }

    @Override
    public int aType() {
        return 4;
    }
}
