package me.wbars.semantic.models;

import me.wbars.generator.JvmBytecodeGenerator;

public class BinaryArithmeticOpNode extends BinaryOpNode {
    public BinaryArithmeticOpNode(String value, ASTNode left, ASTNode right) {
        super(value, left, right);
    }

    @Override
    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        return codeGenerator.generate(this);
    }
}
