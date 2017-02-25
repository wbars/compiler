package me.wbars.semantic.models.types;

public class StringType implements Type {
    @Override
    public String name() {
        return "String";
    }

    @Override
    public String alias() {
        return "Ljava/lang/String;";
    }
}
