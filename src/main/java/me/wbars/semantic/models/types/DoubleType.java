package me.wbars.semantic.models.types;

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
