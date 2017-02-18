package me.wbars.semantic.models;

import me.wbars.generator.JvmBytecodeGenerator;
import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.List;

public class ArrayLiteralNode extends ASTNode {
    private final List<ExprNode> items;
    public ArrayLiteralNode(List<ExprNode> items) {
        super(items.stream().map(ASTNode::getValue).reduce((s, s2) -> s + " " + s2).orElse(""));
        this.items = items;
    }

    public List<ExprNode> getItems() {
        return items;
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
