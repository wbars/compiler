package me.wbars.semantic.models;

public class BinaryOpNode extends ASTNode {
    public BinaryOpNode(String value, ASTNode left, ASTNode right) {
        super(value);
        this.left = left;
        this.right = right;
    }

    protected final ASTNode left;
    protected final ASTNode right;


    public ASTNode getLeft() {
        return left;
    }

    public ASTNode getRight() {
        return right;
    }
}
