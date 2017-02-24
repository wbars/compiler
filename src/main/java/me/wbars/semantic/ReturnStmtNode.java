package me.wbars.semantic;

import me.wbars.generator.JvmBytecodeGenerator;
import me.wbars.semantic.models.ASTNode;
import me.wbars.semantic.models.ExprNode;
import me.wbars.semantic.models.types.Type;
import me.wbars.semantic.models.types.TypeRegistry;

public class ReturnStmtNode extends ASTNode {
    private final ExprNode expr;

    public ReturnStmtNode(ExprNode expr) {
        super("return");
        this.expr = expr;
    }

    public ExprNode getExpr() {
        return expr;
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
