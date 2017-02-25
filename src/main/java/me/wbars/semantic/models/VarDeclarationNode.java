package me.wbars.semantic.models;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VarDeclarationNode extends ASTNode {
    private List<LiteralNode> identifiers;
    private ASTNode typeDenoter;

    public VarDeclarationNode(List<LiteralNode> identifiers, ASTNode typeDenoter) {
        super("");
        this.identifiers = identifiers;
        this.typeDenoter = typeDenoter;
    }

    public VarDeclarationNode() {
        this(null, null);
    }

    public List<LiteralNode> getIdentifiers() {
        return identifiers;
    }

    public ASTNode getTypeDenoter() {
        return typeDenoter;
    }

    public void setIdentifiers(List<LiteralNode> identifiers) {
        this.identifiers = identifiers;
    }

    public void setTypeDenoter(ASTNode typeDenoter) {
        this.typeDenoter = typeDenoter;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        if (index < identifiers.size()) identifiers.set(index, (LiteralNode) node);
        else typeDenoter = node;
    }

    @Override
    public List<ASTNode> children() {
        return Stream.of(identifiers, Collections.singletonList(typeDenoter)).flatMap(Collection::stream).collect(Collectors.toList());
    }
}
