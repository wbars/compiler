package me.wbars.semantic.models;

import me.wbars.generator.JvmBytecodeGenerator;
import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WhileStmtNode extends ASTNode {
    private  ExprNode condition;
    private  List<ASTNode> statements;

    public WhileStmtNode(ExprNode condition, List<ASTNode> statements) {
        super(condition != null ? condition.getValue() : "");
        this.condition = condition;
        this.statements = statements;
    }

    public WhileStmtNode() {
        this(null, null);
    }

    public ExprNode getCondition() {
        return condition;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }

    @Override
    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        return codeGenerator.generate(this);
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        if (index == 0) condition = (ExprNode) node;
        else statements.set(index - 1, node);
    }

    @Override
    public List<ASTNode> children() {
        return Stream.of(Collections.singletonList(condition), statements).flatMap(Collection::stream).collect(Collectors.toList());
    }

    public List<ASTNode> getStatements() {
        return statements;
    }

    public void setCondition(ExprNode condition) {
        this.condition = condition;
    }

    public void setStatements(List<ASTNode> statements) {
        this.statements = statements;
    }
}
