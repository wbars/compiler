package me.wbars.semantic.models;

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
}
