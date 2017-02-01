package me.wbars.semantic.models;

public abstract class ASTNode {
    protected final String value;

    public ASTNode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
