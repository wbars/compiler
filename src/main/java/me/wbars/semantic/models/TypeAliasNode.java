package me.wbars.semantic.models;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

public class TypeAliasNode extends ASTNode {
    private ASTNode baseType;
    public TypeAliasNode(String name, ASTNode baseType) {
        super(name);
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
