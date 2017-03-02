package me.wbars.compiler.semantic.models;

import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.util.Collections;
import java.util.List;

public class SetTypeNode extends CompoundTypeNode {
    private ASTNode baseType;

    public SetTypeNode(String name, ASTNode baseType, boolean packed) {
        super(name, packed);
        this.baseType = baseType;
    }

    public SetTypeNode(String name, boolean packed) {
        this(name, null, packed);
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
