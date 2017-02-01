package me.wbars.semantic.models;

public class IfStmtNode extends ASTNode {
    private final ExprNode condition;
    private final ASTNode trueBranch;
    private final ASTNode falseBranch;

    public IfStmtNode(ExprNode condition, ASTNode trueBranch, ASTNode falseBranch) {
        super(condition.getValue());
        this.condition = condition;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    public ExprNode getCondition() {
        return condition;
    }

    public ASTNode getTrueBranch() {
        return trueBranch;
    }

    public ASTNode getFalseBranch() {
        return falseBranch;
    }
}
