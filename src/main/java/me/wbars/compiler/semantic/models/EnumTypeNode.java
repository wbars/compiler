package me.wbars.compiler.semantic.models;

import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.util.List;
import java.util.stream.Collectors;

public class EnumTypeNode extends ASTNode {
    private List<LiteralNode> identifiers;

    public EnumTypeNode(String name, List<LiteralNode> identifiers) {
        super(name);
        this.identifiers = identifiers;
    }

    public EnumTypeNode(String name) {
        this(name, null);
    }

    public void setIdentifiers(List<LiteralNode> identifiers) {
        this.identifiers = identifiers;
    }

    public List<LiteralNode> getIdentifiers() {
        return identifiers;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        if (index >= identifiers.size()) throw new IllegalArgumentException();
        identifiers.set(index, (LiteralNode) node);
    }

    @Override
    public List<ASTNode> children() {
        return identifiers.stream().map(e -> (ASTNode)e).collect(Collectors.toList());
    }

    @Override
    public List<Token> tokens() {
        throw new UnsupportedOperationException();
    }
}
