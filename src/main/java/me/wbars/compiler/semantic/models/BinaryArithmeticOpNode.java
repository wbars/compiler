package me.wbars.compiler.semantic.models;

import me.wbars.compiler.generator.JvmBytecodeGenerator;

public class BinaryArithmeticOpNode extends BinaryOpNode {
    public BinaryArithmeticOpNode(String value, ASTNode left, ASTNode right) {
        super(value, left, right);
    }

    public BinaryArithmeticOpNode(String value) {
        this(value, null, null);
    }

    @Override
    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        return codeGenerator.generate(this);
    }
}