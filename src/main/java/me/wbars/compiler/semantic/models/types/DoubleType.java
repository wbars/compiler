package me.wbars.compiler.semantic.models.types;

public class DoubleType implements Type {
    @Override
    public String name() {
        return "Double";
    }

    @Override
    public int aType() {
        return 7;
    }
}
