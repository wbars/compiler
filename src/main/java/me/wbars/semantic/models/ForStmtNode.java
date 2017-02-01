package me.wbars.semantic.models;

public class ForStmtNode extends ASTNode {
    private final LiteralNode controlVar;
    private final ExprNode initialValue;
    private final ExprNode finalValue;
    private final boolean increment;
    private final ASTNode body;

    public ForStmtNode(LiteralNode controlVar, ExprNode initialValue, ExprNode finalValue, boolean increment, ASTNode body) {
        super("");
        this.controlVar = controlVar;
        this.initialValue = initialValue;
        this.finalValue = finalValue;
        this.increment = increment;
        this.body = body;
    }

    public LiteralNode getControlVar() {
        return controlVar;
    }

    public ExprNode getInitialValue() {
        return initialValue;
    }

    public ExprNode getFinalValue() {
        return finalValue;
    }

    public boolean isIncrement() {
        return increment;
    }

    public ASTNode getBody() {
        return body;
    }
}
