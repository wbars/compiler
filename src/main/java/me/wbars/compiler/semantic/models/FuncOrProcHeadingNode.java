package me.wbars.compiler.semantic.models;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FuncOrProcHeadingNode extends ASTNode {
    private LiteralNode resultType;
    private List<LiteralParameterNode> parameters;

    public FuncOrProcHeadingNode(String name, LiteralNode resultType, List<LiteralParameterNode> parameters) {
        super(name);
        this.resultType = resultType;
        this.parameters = parameters;
    }

    public FuncOrProcHeadingNode(String name) {
        this(name, null, null);
    }

    public void setResultType(LiteralNode resultType) {
        this.resultType = resultType;
    }

    public void setParameters(List<LiteralParameterNode> parameters) {
        this.parameters = parameters;
    }

    public LiteralNode getResultType() {
        return resultType;
    }

    public List<LiteralParameterNode> getParameters() {
        return parameters;
    }

    public boolean isProcedure() {
        return resultType == null;
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        if (index == 0) resultType = (LiteralNode) node;
        parameters.set(index - 1, (LiteralParameterNode) node);
    }

    @Override
    public List<ASTNode> children() {
        return Stream.of(Collections.singletonList(resultType), parameters).flatMap(Collection::stream).collect(Collectors.toList());
    }
}
