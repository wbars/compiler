package me.wbars.compiler.semantic.models;

import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;

public class LiteralParameterNode extends ASTNode {
    private List<LiteralNode> identifiers;
    private LiteralNode nameIdentifier;

    public LiteralParameterNode(List<LiteralNode> identifiers, LiteralNode nameIdentifier) {
        super(nameIdentifier != null ?nameIdentifier.getValue() : "");
        this.identifiers = identifiers;
        this.nameIdentifier = nameIdentifier;
    }
    public LiteralParameterNode() {
        this(null, null);
    }

    public List<LiteralNode> getIdentifiers() {
        return identifiers;
    }

    public LiteralNode getNameIdentifier() {
        return nameIdentifier;
    }

    public void setIdentifiers(List<LiteralNode> identifiers) {
        this.identifiers = identifiers;
    }

    public void setNameIdentifier(LiteralNode nameIdentifier) {
        this.nameIdentifier = nameIdentifier;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        if (index < identifiers.size()) identifiers.set(index, (LiteralNode) node);
        else nameIdentifier = (LiteralNode) node;
    }

    @Override
    public List<ASTNode> children() {
        return Stream.of(identifiers, singletonList(nameIdentifier)).flatMap(Collection::stream).collect(Collectors.toList());
    }
}
