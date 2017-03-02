package me.wbars.compiler.semantic.models;

import me.wbars.compiler.generator.JvmBytecodeGenerator;
import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.util.ArrayList;
import java.util.List;

public class GetIndexNode extends UnaryOpNode {
    private final List<ASTNode> indexes = new ArrayList<>();

    public GetIndexNode() {
        super("");
    }

    public void addIndex(ASTNode index) {
        indexes.add(index);
    }

    public List<ASTNode> getIndexes() {
        return indexes;
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
    public List<ASTNode> children() {
        return indexes;
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        indexes.set(index, node);
    }
}
