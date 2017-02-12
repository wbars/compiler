package me.wbars.semantic.models;

import me.wbars.generator.JvmBytecodeGenerator;
import me.wbars.semantic.models.types.SymbolTable;
import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

public abstract class ASTNode {
    protected final String value;
    protected Type type;
    protected SymbolTable symbolTable = null;

    public ASTNode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public final Type getType() {
        return type;
    }

    protected Type getType(TypeRegistry typeRegistry) {
        return null;
    }

    public final Type getProcessedType(TypeRegistry typeRegistry) {
        if (type == null) {
            type = getType(typeRegistry);
            symbolTable = typeRegistry.getTable();
        }
        return type;
    }

    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        return -1;
    }
}
