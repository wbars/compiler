package me.wbars.semantic.models;

public class AssignmentStatementNode extends BinaryOpNode {
    public AssignmentStatementNode(ASTNode left, ASTNode right) {
        super("", left, right);
    }
}
