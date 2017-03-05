package me.wbars.compiler.semantic.models;

import me.wbars.compiler.generator.JvmBytecodeGenerator;
import me.wbars.compiler.parser.models.Tokens;
import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.scanner.models.TokenFactory;
import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static me.wbars.compiler.utils.CollectionsUtils.merge;

public class ForStmtNode extends ASTNode {
    private IdentifierNode controlVar;
    private ExprNode initialValue;
    private ExprNode finalValue;
    private boolean increment;
    private List<ASTNode> statements;

    public ForStmtNode(IdentifierNode controlVar, ExprNode initialValue, ExprNode finalValue, boolean increment, List<ASTNode> statements) {
        super("");
        this.controlVar = controlVar;
        this.initialValue = initialValue;
        this.finalValue = finalValue;
        this.increment = increment;
        this.statements = statements;
    }

    public ForStmtNode(boolean increment) {
        this(null, null, null, increment, null);
    }

    public IdentifierNode getControlVar() {
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

    public void setControlVar(IdentifierNode controlVar) {
        this.controlVar = controlVar;
    }

    public void setInitialValue(ExprNode initialValue) {
        this.initialValue = initialValue;
    }

    public void setFinalValue(ExprNode finalValue) {
        this.finalValue = finalValue;
    }

    public void setIncrement(boolean increment) {
        this.increment = increment;
    }

    public void setStatements(List<ASTNode> statements) {
        this.statements = statements;
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
        if (index == 0) controlVar = (IdentifierNode) node;
        if (index == 1) initialValue = (ExprNode) node;
        if (index == 2) finalValue = (ExprNode) node;
        statements.set(index - 3, node);
    }

    @Override
    public List<ASTNode> children() {
        return Stream.of(asList(controlVar, initialValue, finalValue), statements)
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public List<Token> tokens() {
        return merge(
                Collections.singletonList(Token.keyword(Tokens.FOR)),
                controlVar.tokens(),
                Collections.singletonList(TokenFactory.createAssignment()),
                initialValue.tokens(),
                Collections.singletonList(TokenFactory.createDirection(increment)),
                finalValue.tokens(),
                Collections.singletonList(Token.keyword(Tokens.DO)),
                branch(statements)
        );
    }
}
