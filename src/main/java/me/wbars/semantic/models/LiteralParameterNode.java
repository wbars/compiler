package me.wbars.semantic.models;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.List;

public class LiteralParameterNode extends ASTNode {
    private final List<LiteralNode> identifiers;
    private final LiteralNode nameIdentifier;

    public LiteralParameterNode(List<LiteralNode> identifiers, LiteralNode nameIdentifier) {
        super(nameIdentifier.getValue());
        this.identifiers = identifiers;
        this.nameIdentifier = nameIdentifier;
    }

    public List<LiteralNode> getIdentifiers() {
        return identifiers;
    }

    public LiteralNode getNameIdentifier() {
        return nameIdentifier;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }
}
