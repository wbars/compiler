package me.wbars.semantic.models;

import java.util.List;

public class FuncOrProcHeadingNode extends ASTNode {
    private final ASTNode resultType;
    private final List<ASTNode> parameters;

    public FuncOrProcHeadingNode(String name, ASTNode resultType, List<ASTNode> parameters) {
        super(name);
        this.resultType = resultType;
        this.parameters = parameters;
    }

    public FuncOrProcHeadingNode(String name, List<ASTNode> parameters) {
        this(name, null, parameters);
    }

    public ASTNode getResultType() {
        return resultType;
    }

    public List<ASTNode> getParameters() {
        return parameters;
    }

    public boolean isProcedure() {
        return resultType == null;
    }
}
