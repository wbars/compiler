package me.wbars.semantic.models;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

public class SubrangeTypeNode extends ASTNode {
    private final LiteralNode leftBound;
    private final LiteralNode rightBound;

    public SubrangeTypeNode(String name, LiteralNode leftBound, LiteralNode rightBound) {
        super(name);
        this.leftBound = leftBound;
        this.rightBound = rightBound;
    }

    public LiteralNode getLeftBound() {
        return leftBound;
    }

    public LiteralNode getRightBound() {
        return rightBound;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }
}
