package me.wbars.semantic.models;

import me.wbars.generator.JvmBytecodeGenerator;
import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

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

    @Override
    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        return codeGenerator.generate(this);
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }
}
