package me.wbars.compiler.semantic.models;

import me.wbars.compiler.generator.JvmBytecodeGenerator;
import me.wbars.compiler.parser.models.Tokens;
import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static me.wbars.compiler.utils.CollectionsUtils.merge;

public class IfStmtNode extends ASTNode {
    private ExprNode condition;
    private List<ASTNode> trueBranch;
    private List<ASTNode> falseBranch;

    public IfStmtNode(ExprNode condition, List<ASTNode> trueBranch, List<ASTNode> falseBranch) {
        super(condition != null ? condition.getValue() : "");
        this.condition = condition;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    public IfStmtNode() {
        this(null, null, null);
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

    public void setCondition(ExprNode condition) {
        this.condition = condition;
    }

    public void setTrueBranch(List<ASTNode> trueBranch) {
        this.trueBranch = trueBranch;
    }

    public void setFalseBranch(List<ASTNode> falseBranch) {
        this.falseBranch = falseBranch;
    }

    @Override
    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        return codeGenerator.generate(this);
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        if (index == 0) {
            condition = (ExprNode) node;
            return;
        }
        if (index < trueBranch.size() + 1) {
            trueBranch.set(index - 1, node);
            return;
        }
        falseBranch.set(index - 1 - trueBranch.size(), node);
    }

    @Override
    public List<ASTNode> children() {
        return Stream.of(singletonList(condition), trueBranch, falseBranch).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public List<Token> tokens() {
        return merge(
                singletonList(Token.keyword(Tokens.IF)),
                condition.tokens(),
                singletonList(Token.keyword(Tokens.THEN)),
                branch(trueBranch),
                ifNotEmpty(falseBranch, nodes -> merge(singletonList(Token.keyword(Tokens.ELSE)), branch(falseBranch)))
        );
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }
}
