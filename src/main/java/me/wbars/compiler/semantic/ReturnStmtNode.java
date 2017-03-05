package me.wbars.compiler.semantic;

import me.wbars.compiler.generator.JvmBytecodeGenerator;
import me.wbars.compiler.parser.models.Tokens;
import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.semantic.models.ASTNode;
import me.wbars.compiler.semantic.models.ExprNode;
import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static me.wbars.compiler.utils.CollectionsUtils.merge;

public class ReturnStmtNode extends ASTNode {
    private ExprNode expr;

    public ReturnStmtNode(ExprNode expr) {
        super("return");
        this.expr = expr;
    }

    public ReturnStmtNode() {
        this(null);
    }

    public ExprNode getExpr() {
        return expr;
    }

    public void setExpr(ExprNode expr) {
        this.expr = expr;
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
        if (index != 0) throw new IllegalArgumentException();
        expr = (ExprNode) node;
    }

    @Override
    public List<ASTNode> children() {
        return null;
    }

    @Override
    public List<Token> tokens() {
        return merge(
                Collections.singletonList(Token.keyword(Tokens.RETURN)),
                (expr != null ? expr.tokens() : emptyList())
        );
    }
}
