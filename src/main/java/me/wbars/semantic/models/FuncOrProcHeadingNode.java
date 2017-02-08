package me.wbars.semantic.models;

import java.util.List;

public class FuncOrProcHeadingNode extends ASTNode {
    private final LiteralNode resultType;
    private final List<ASTNode> parameters;

    public FuncOrProcHeadingNode(String name, LiteralNode resultType, List<ASTNode> parameters) {
        super(name);
        this.resultType = resultType;
        this.parameters = parameters;
    }

    public FuncOrProcHeadingNode(String name, List<ASTNode> parameters) {
        this(name, null, parameters);
    }

    public LiteralNode getResultType() {
        return resultType;
    }

    public List<ASTNode> getParameters() {
        return parameters;
    }

    public boolean isProcedure() {
        return resultType == null;
    }
}
