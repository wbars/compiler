package me.wbars.semantic.models;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

import java.util.Collections;
import java.util.List;

public class ConstDefinitionNode extends ASTNode {
    private ASTNode expr;

    public ConstDefinitionNode(String name, ASTNode expr) {
        super(name);
        this.expr = expr;
    }

    public ConstDefinitionNode(String name) {
        this(name, null);
    }

    public void setExpr(ASTNode expr) {
        this.expr = expr;
    }

    public ASTNode getExpr() {
        return expr;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        if (index != 0) throw new IllegalArgumentException();
        expr = node;
    }

    @Override
    public List<ASTNode> children() {
        return Collections.singletonList(expr);
    }
}
