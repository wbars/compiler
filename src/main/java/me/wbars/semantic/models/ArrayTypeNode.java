package me.wbars.semantic.models;

import java.util.List;

public class ArrayTypeNode extends CompoundTypeNode {
    private final List<ASTNode> indexes;
    private final ASTNode componentType;

    public ArrayTypeNode(String name, List<ASTNode> indexes, ASTNode componentType, boolean packed) {
        super(name, packed);
        this.indexes = indexes;
        this.componentType = componentType;
    }

    public List<ASTNode> getIndexes() {
        return indexes;
    }

    public ASTNode getComponentType() {
        return componentType;
    }
}
