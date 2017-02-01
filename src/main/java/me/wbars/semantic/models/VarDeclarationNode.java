package me.wbars.semantic.models;

import java.util.List;

public class VarDeclarationNode extends ASTNode {
    private final List<LiteralNode> identifiers;
    private final ASTNode typeDenoter;
    public VarDeclarationNode(List<LiteralNode> identifiers, ASTNode typeDenoter) {
        super("");
        this.identifiers = identifiers;
        this.typeDenoter = typeDenoter;
    }

    public List<LiteralNode> getIdentifiers() {
        return identifiers;
    }

    public ASTNode getTypeDenoter() {
        return typeDenoter;
    }
}
