package me.wbars.semantic.models;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.Collections;
import java.util.List;

public class TypeAliasNode extends ASTNode {
    private ASTNode baseType;
    public TypeAliasNode(String name, ASTNode baseType) {
        super(name);
        this.baseType = baseType;
    }

    public TypeAliasNode(String name) {
        this(name, null);
    }

    public void setBaseType(ASTNode baseType) {
        this.baseType = baseType;
    }

    public ASTNode getBaseType() {
        return baseType;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        if (index != 0) throw new IllegalArgumentException();
        baseType = node;
    }

    @Override
    public List<ASTNode> children() {
        return Collections.singletonList(baseType);
    }
}
