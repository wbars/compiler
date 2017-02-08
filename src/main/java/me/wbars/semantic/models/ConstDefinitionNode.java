package me.wbars.semantic.models;

import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

public class ConstDefinitionNode extends ASTNode {
    private final ASTNode expr;

    public ConstDefinitionNode(String name, ASTNode expr) {
        super(name);
        this.expr = expr;
    }

    public ASTNode getExpr() {
        return expr;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }
}
