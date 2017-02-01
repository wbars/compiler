package me.wbars.semantic.models;

public class CompoundTypeNode extends ASTNode {
    private final boolean packed;
    public CompoundTypeNode(String value, boolean packed) {
        super(value);
        this.packed = packed;
    }

    public boolean isPacked() {
        return packed;
    }
}
