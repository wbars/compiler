package me.wbars.compiler.semantic.models;

import me.wbars.compiler.generator.JvmBytecodeGenerator;

public class ExprNode extends BinaryOpNode {
    public ExprNode(String value, ASTNode left, ASTNode right) {
        super(value, left, right);
    }

    public ExprNode(ASTNode simpleExpr) {
        super(simpleExpr.getValue(), simpleExpr, null);
    }

    public ExprNode() {
        this(null, null, null);
    }



    @Override
    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        return codeGenerator.generate(this);
    }
}
