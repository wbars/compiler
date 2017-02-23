package me.wbars.semantic.models;

import me.wbars.generator.JvmBytecodeGenerator;
import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.List;

public class ForStmtNode extends ASTNode {
    private final LiteralNode controlVar;
    private final ExprNode initialValue;
    private final ExprNode finalValue;
    private final boolean increment;
    private final List<ASTNode> statements;

    public ForStmtNode(LiteralNode controlVar, ExprNode initialValue, ExprNode finalValue, boolean increment, List<ASTNode> statements) {
        super("");
        this.controlVar = controlVar;
        this.initialValue = initialValue;
        this.finalValue = finalValue;
        this.increment = increment;
        this.statements = statements;
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

    public List<ASTNode> getStatements() {
        return statements;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }

    @Override
    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        return codeGenerator.generate(this);
    }
}
