package me.wbars.compiler.semantic.models;

import me.wbars.compiler.generator.JvmBytecodeGenerator;
import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RepeatStmtNode extends ASTNode {
    private List<ASTNode> statements;
    private ExprNode untilExpression;

    public RepeatStmtNode(List<ASTNode> statements, ExprNode untilExpression) {
        super(untilExpression != null ? untilExpression.getValue() : "");
        this.statements = statements;
        this.untilExpression = untilExpression;
    }

    public RepeatStmtNode() {
        this(null, null);
    }

    public List<ASTNode> getStatements() {
        return statements;
    }

    public ExprNode getUntilExpression() {
        return untilExpression;
    }

    public void setStatements(List<ASTNode> statements) {
        this.statements = statements;
    }

    public void setUntilExpression(ExprNode untilExpression) {
        this.untilExpression = untilExpression;
    }

    @Override
    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        return codeGenerator.generate(this);
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        if (index < statements.size()) statements.set(index, (LiteralNode) node);
        else untilExpression = (ExprNode) node;
    }

    @Override
    public List<ASTNode> children() {
        return Stream.of(statements, Collections.singletonList(untilExpression))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }
}
