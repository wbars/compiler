package me.wbars.semantic.models;

import me.wbars.generator.JvmBytecodeGenerator;
import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProcedureStmtNode extends ASTNode {
    private LiteralNode identifier;
    private List<ActualParameterNode> arguments;
    public ProcedureStmtNode(LiteralNode identifier, List<ActualParameterNode> arguments) {
        super(identifier != null ? identifier.getValue() : null);
        this.identifier = identifier;
        this.arguments = arguments;
    }

    public ProcedureStmtNode() {
        this(null, null);
    }

    public LiteralNode getIdentifier() {
        return identifier;
    }

    public List<ActualParameterNode> getArguments() {
        return arguments;
    }

    public void setIdentifier(LiteralNode identifier) {
        this.identifier = identifier;
    }

    public void setArguments(List<ActualParameterNode> arguments) {
        this.arguments = arguments;
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
        if (index == 0) identifier = (LiteralNode) node;
        else arguments.set(index - 1, (ActualParameterNode) node);
    }

    @Override
    public List<ASTNode> children() {
        return Stream.of(Collections.singletonList(identifier), arguments).flatMap(Collection::stream).collect(Collectors.toList());
    }
}
