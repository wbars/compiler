package me.wbars.compiler.semantic.models;

import me.wbars.compiler.generator.JvmBytecodeGenerator;
import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.util.Collections;
import java.util.List;

public class LiteralNode extends ASTNode {

    public LiteralNode(String name, Type type) {
        super(name);
        this.type = type;
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
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ASTNode> children() {
        return Collections.emptyList();
    }
}
