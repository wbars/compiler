package me.wbars.semantic.models;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.Collections;
import java.util.List;

public class PointerTypeNode extends ASTNode {
    private LiteralNode domainType;
    public PointerTypeNode(String name, LiteralNode domainType) {
        super(name);
        this.domainType = domainType;
    }

    public PointerTypeNode(String name) {
        this(name, null);
    }

    public LiteralNode getDomainType() {
        return domainType;
    }

    public void setDomainType(LiteralNode domainType) {
        this.domainType = domainType;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        if (index != 0) throw new UnsupportedOperationException();
        domainType = (LiteralNode) node;
    }

    @Override
    public List<ASTNode> children() {
        return Collections.singletonList(domainType);
    }
}
