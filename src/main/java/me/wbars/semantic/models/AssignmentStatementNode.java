package me.wbars.semantic.models;

import me.wbars.generator.JvmBytecodeGenerator;

public class AssignmentStatementNode extends BinaryOpNode {
    public AssignmentStatementNode(ASTNode left, ASTNode right) {
        super("", left, right);
    }

    @Override
    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        return codeGenerator.generate(this);
    }
}
