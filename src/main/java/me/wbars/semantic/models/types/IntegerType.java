package me.wbars.semantic.models.types;

public class IntegerType implements Type {
    @Override
    public String name() {
        return "Integer";
    }

    @Override
    public int aType() {
        return 10;
    }
}
