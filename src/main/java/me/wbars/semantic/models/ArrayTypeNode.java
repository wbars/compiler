package me.wbars.semantic.models;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.List;

public class ArrayTypeNode extends CompoundTypeNode {
    private final List<SubrangeTypeNode> indexes;
    private final ASTNode componentType;

    public ArrayTypeNode(String name, List<SubrangeTypeNode> indexes, ASTNode componentType, boolean packed) {
        super(name, packed);
        this.indexes = indexes;
        this.componentType = componentType;
    }

    public List<SubrangeTypeNode> getIndexes() {
        return indexes;
    }

    public ASTNode getComponentType() {
        return componentType;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }
}
