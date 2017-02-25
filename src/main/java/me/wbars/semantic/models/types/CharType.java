package me.wbars.semantic.models.types;

public class CharType implements Type {
    @Override
    public String name() {
        return "Char";
    }

    @Override
    public int aType() {
        return 5;
    }
}
