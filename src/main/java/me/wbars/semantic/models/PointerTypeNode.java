package me.wbars.semantic.models;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

public class PointerTypeNode extends ASTNode {
    private final LiteralNode domainType;
    public PointerTypeNode(String name, LiteralNode domainType) {
        super(name);
        this.domainType = domainType;
    }

    public LiteralNode getDomainType() {
        return domainType;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }
}
