package me.wbars.semantic.models;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

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

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }
}
