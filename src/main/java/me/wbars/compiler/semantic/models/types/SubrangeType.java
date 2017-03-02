package me.wbars.compiler.semantic.models.types;

public class SubrangeType implements Type {
    private final String left;
    private final String right;

    SubrangeType(String left, String right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String name() {
        return "Subrange";
    }

    public String getLeft() {
        return left;
    }

    public String getRight() {
        return right;
    }
}
