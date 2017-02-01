package me.wbars.semantic.models;

public class WhileStmtNode extends ASTNode {
    private final ExprNode condition;
    private final ASTNode body;

    public WhileStmtNode(ExprNode condition, ASTNode body) {
        super(condition.getValue());
        this.condition = condition;
        this.body = body;
    }

    public ExprNode getCondition() {
        return condition;
    }

    public ASTNode getBody() {
        return body;
    }
}
