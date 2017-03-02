package me.wbars.compiler.semantic.models.types;


public class LongType implements Type{
    @Override
    public String name() {
        return "Long";
    }

    @Override
    public String alias() {
        return "J";
    }

    @Override
    public int aType() {
        return 11;
    }
}
