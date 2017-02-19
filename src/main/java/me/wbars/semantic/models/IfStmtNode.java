package me.wbars.semantic.models;

import me.wbars.generator.JvmBytecodeGenerator;
import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.List;

public class IfStmtNode extends ASTNode {
    private final ExprNode condition;
    private final List<ASTNode> trueBranch;
    private final List<ASTNode> falseBranch;

    public IfStmtNode(ExprNode condition, List<ASTNode> trueBranch, List<ASTNode> falseBranch) {
        super(condition.getValue());
        this.condition = condition;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    public ExprNode getCondition() {
        return condition;
    }

    public List<ASTNode> getTrueBranch() {
        return trueBranch;
    }

    public List<ASTNode> getFalseBranch() {
        return falseBranch;
    }

    @Override
    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        return codeGenerator.generate(this);
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }
}
