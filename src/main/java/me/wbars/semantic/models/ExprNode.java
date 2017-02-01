package me.wbars.semantic.models;

public class ExprNode extends BinaryOpNode {
    public ExprNode(String value, ASTNode left, ASTNode right) {
        super(value, left, right);
    }

    public ExprNode(ASTNode simpleExpr) {
        super(null, simpleExpr, null);
    }
}
