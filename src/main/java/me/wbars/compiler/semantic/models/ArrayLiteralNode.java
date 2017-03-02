package me.wbars.compiler.semantic.models;

import me.wbars.compiler.generator.JvmBytecodeGenerator;
import me.wbars.compiler.semantic.models.types.ArrayType;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayLiteralNode extends ASTNode {
    private List<ExprNode> items;

    public ArrayLiteralNode(List<ExprNode> items) {
        super(items.stream().map(ASTNode::getValue).reduce((s, s2) -> s + " " + s2).orElse(""));
        this.items = items;
    }

    public ArrayLiteralNode() {
        this(Collections.emptyList());
    }

    public List<ExprNode> getItems() {
        return items;
    }

    public void setItems(List<ExprNode> items) {
        this.items = items;
    }

    @Override
    protected ArrayType getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }

    @Override
    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        return codeGenerator.generate(this);
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        items.set(index, (ExprNode) node);
    }

    @Override
    public List<ASTNode> children() {
        return items.stream().map(e -> (ASTNode) e).collect(Collectors.toList());
    }

    @Override
    public ArrayType getType() {
        return (ArrayType) super.getType();
    }
}
