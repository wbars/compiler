package me.wbars.semantic.models;

import java.util.List;

public class ProcedureStmtNode extends ASTNode {
    private final LiteralNode identifier;
    private final List<ActualParameterNode> params;
    public ProcedureStmtNode(LiteralNode identifier, List<ActualParameterNode> params) {
        super(identifier.getValue());
        this.identifier = identifier;
        this.params = params;
    }

    public LiteralNode getIdentifier() {
        return identifier;
    }

    public List<ActualParameterNode> getParams() {
        return params;
    }
}
