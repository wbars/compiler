package me.wbars.compiler.semantic.models;

import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.util.Arrays;
import java.util.List;

public class SubrangeTypeNode extends ASTNode {
    private LiteralNode leftBound;
    private LiteralNode rightBound;

    public SubrangeTypeNode(String name, LiteralNode leftBound, LiteralNode rightBound) {
        super(name);
        this.leftBound = leftBound;
        this.rightBound = rightBound;
    }

    public SubrangeTypeNode(String name) {
        this(name, null, null);
    }

    public void setLeftBound(LiteralNode leftBound) {
        this.leftBound = leftBound;
    }

    public void setRightBound(LiteralNode rightBound) {
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

    @Override
    protected void replaceChild(int index, ASTNode node) {
        if (index == 0) leftBound = (LiteralNode) node;
        if (index == 1) rightBound = (LiteralNode) node;
        throw new IllegalArgumentException();
    }

    @Override
    public List<ASTNode> children() {
        return Arrays.asList(leftBound, rightBound);
    }
}
