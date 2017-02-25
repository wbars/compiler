package me.wbars.semantic.models;

import me.wbars.generator.JvmBytecodeGenerator;
import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;

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

    @Override
    protected void replaceChild(int index, ASTNode node) {
        int selectedIndex = index;
        if (index < labels.size()) {
            labels.set(selectedIndex, (LiteralNode) node);
            return;
        }

        selectedIndex -= labels.size();
        if (index < varDeclarations.size()) {
            varDeclarations.set(selectedIndex , (VarDeclarationNode) node);
            return;
        }

        selectedIndex -= varDeclarations.size();
        if (index < typeDefinitions.size()) {
            typeDefinitions.set(selectedIndex, node);
            return;
        }

        selectedIndex -= typeDefinitions.size();
        if (index < constDefinitions.size()) {
            constDefinitions.set(selectedIndex, (ConstDefinitionNode) node);
            return;
        }


        selectedIndex -= constDefinitions.size();
        if (index < procOrFunctionDeclarations.size()) {
            procOrFunctionDeclarations.set(selectedIndex, (ProcOrFunctionDeclarationNode) node);
            return;
        }

        selectedIndex -= procOrFunctionDeclarations.size();
        if (index < statements.size()) {
            statements.set(selectedIndex, node);
            return;
        }

        throw new IllegalArgumentException();
    }

    @Override
    public List<ASTNode> children() {
        return of(labels, varDeclarations, typeDefinitions, constDefinitions, procOrFunctionDeclarations, statements)
                .flatMap(Collection::stream).collect(toList());
    }
}
