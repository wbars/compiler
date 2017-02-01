package me.wbars.semantic.models;

public class ConstDefinitionNode extends ASTNode {
    private final ASTNode expr;

    public ConstDefinitionNode(String name, ASTNode expr) {
        super(name);
        this.expr = expr;
    }

    public ASTNode getExpr() {
        return expr;
    }
}
