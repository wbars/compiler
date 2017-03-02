package me.wbars.compiler.semantic.models;

import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.util.Arrays;
import java.util.List;

public abstract class BinaryOpNode extends ASTNode {
    public BinaryOpNode(String value, ASTNode left, ASTNode right) {
        super(value);
        this.left = left;
        this.right = right;
    }

    protected ASTNode left;
    protected ASTNode right;

    public ASTNode getLeft() {
        return left;
    }

    public ASTNode getRight() {
        return right;
    }

    public void setLeft(ASTNode left) {
        this.left = left;
    }

    public void setRight(ASTNode right) {
        this.right = right;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }

    @Override
    public List<ASTNode> children() {
        return Arrays.asList(left, right);
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        if (index == 0) {
            left = node;
            return;
        }
        if (index == 1) {
            right = node;
            return;
        }
        throw new UnsupportedOperationException();
    }
}
