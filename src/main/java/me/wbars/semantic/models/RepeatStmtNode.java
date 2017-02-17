package me.wbars.semantic.models;

import java.util.List;

public class RepeatStmtNode extends ASTNode {
    private final List<ASTNode> statements;
    private final ExprNode untilExpression;

    public RepeatStmtNode(List<ASTNode> statements, ExprNode untilExpression) {
        super(untilExpression.getValue());
        this.statements = statements;
        this.untilExpression = untilExpression;
    }

    public List<ASTNode> getStatements() {
        return statements;
    }

    public ExprNode getUntilExpression() {
        return untilExpression;
    }
}
