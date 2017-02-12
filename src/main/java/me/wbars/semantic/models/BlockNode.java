package me.wbars.semantic.models;

import me.wbars.generator.JvmBytecodeGenerator;
import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.ArrayList;
import java.util.List;

public class BlockNode extends ASTNode {
    private final List<LiteralNode> labels = new ArrayList<>();
    private final List<VarDeclarationNode> varDeclarations = new ArrayList<>();
    private final List<ASTNode> typeDefinitions = new ArrayList<>();
    private final List<ConstDefinitionNode> constDefinitions = new ArrayList<>();
    private final List<ProcOrFunctionDeclarationNode> procOrFunctionDeclarations = new ArrayList<>();
    private final List<ASTNode> statements = new ArrayList<>();
    public BlockNode() {
        super("block");
    }

    public List<ConstDefinitionNode> getConstDefinitions() {
        return constDefinitions;
    }

    public List<LiteralNode> getLabels() {
        return labels;
    }

    public List<ASTNode> getTypeDefinitions() {
        return typeDefinitions;
    }

    public List<VarDeclarationNode> getVarDeclarations() {
        return varDeclarations;
    }

    public List<ProcOrFunctionDeclarationNode> getProcOrFunctionDeclarations() {
        return procOrFunctionDeclarations;
    }

    public List<ASTNode> getStatements() {
        return statements;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }

    @Override
    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        return codeGenerator.generate(this);
    }
}
