package me.wbars.semantic.models;

import java.util.List;

public class FuncOrProcHeadingNode extends ASTNode {
    private final LiteralNode resultType;
    private final List<LiteralParameterNode> parameters;

    public FuncOrProcHeadingNode(String name, LiteralNode resultType, List<LiteralParameterNode> parameters) {
        super(name);
        this.resultType = resultType;
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
}
