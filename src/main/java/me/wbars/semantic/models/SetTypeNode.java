package me.wbars.semantic.models;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

public class SetTypeNode extends CompoundTypeNode {
    private final ASTNode baseType;

    public SetTypeNode(String name, ASTNode baseType, boolean packed) {
        super(name, packed);
        this.baseType = baseType;
    }

    public ASTNode getBaseType() {
        return baseType;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }
}
