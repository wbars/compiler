package me.wbars.semantic.models;

public class RelOpNode extends BinaryOpNode {
    public RelOpNode(String operation, ASTNode left, ASTNode right) {
        super(operation, left, right);
    }
}
