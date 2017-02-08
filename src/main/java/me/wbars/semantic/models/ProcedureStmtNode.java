package me.wbars.semantic.models;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.List;

public class ProcedureStmtNode extends ASTNode {
    private final LiteralNode identifier;
    private final List<ActualParameterNode> arguments;
    public ProcedureStmtNode(LiteralNode identifier, List<ActualParameterNode> arguments) {
        super(identifier.getValue());
        this.identifier = identifier;
        this.arguments = arguments;
    }

    public LiteralNode getIdentifier() {
        return identifier;
    }

    public List<ActualParameterNode> getArguments() {
        return arguments;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }
}
