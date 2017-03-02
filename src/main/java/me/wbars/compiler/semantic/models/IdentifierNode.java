package me.wbars.compiler.semantic.models;

import me.wbars.compiler.generator.JvmBytecodeGenerator;
import me.wbars.compiler.semantic.models.types.Type;

public class IdentifierNode extends LiteralNode {
    public IdentifierNode(String name, Type type) {
        super(name, type);
    }

    @Override
    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        return codeGenerator.generate(this);
    }
}
