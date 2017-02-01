package me.wbars.semantic.models;

public class PointerTypeNode extends ASTNode {
    private final LiteralNode domainType;
    public PointerTypeNode(String name, LiteralNode domainType) {
        super(name);
        this.domainType = domainType;
    }

    public LiteralNode getDomainType() {
        return domainType;
    }
}
