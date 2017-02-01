package me.wbars.semantic.models;

public class SetTypeNode extends CompoundTypeNode {
    private final ASTNode baseType;

    public SetTypeNode(String name, ASTNode baseType, boolean packed) {
        super(name, packed);
        this.baseType = baseType;
    }

    public ASTNode getBaseType() {
        return baseType;
    }
}
