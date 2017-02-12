package me.wbars.semantic.models;

import me.wbars.generator.JvmBytecodeGenerator;

public class ExprNode extends BinaryOpNode {
    public ExprNode(String value, ASTNode left, ASTNode right) {
        super(value, left, right);
    }

    public ExprNode(ASTNode simpleExpr) {
        super(null, simpleExpr, null);
    }

    @Override
    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        return codeGenerator.generate(this);
    }
}
