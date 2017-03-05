package me.wbars.compiler.semantic.models;

import me.wbars.compiler.parser.models.Tokens;
import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.scanner.models.TokenFactory;
import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static me.wbars.compiler.utils.CollectionsUtils.merge;

public class ProcOrFunctionDeclarationNode extends ASTNode {
    private FuncOrProcHeadingNode heading;
    private BlockNode body;

    public ProcOrFunctionDeclarationNode(FuncOrProcHeadingNode heading, BlockNode body) {
        super(heading != null ? heading.getValue() : "");
        this.heading = heading;
        this.body = body;
    }

    public ProcOrFunctionDeclarationNode() {
        this(null, null);
    }

    public FuncOrProcHeadingNode getHeading() {
        return heading;
    }

    public BlockNode getBody() {
        return body;
    }

    public void setHeading(FuncOrProcHeadingNode heading) {
        this.heading = heading;
    }

    public void setBody(BlockNode body) {
        this.body = body;
    }

    @Override
    protected Type getType(TypeRegistry typeRegistry) {
        return typeRegistry.processType(this);
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        if (index == 0) heading = (FuncOrProcHeadingNode) node;
        if (index == 1) body = (BlockNode) node;
        throw new IllegalArgumentException();
    }

    @Override
    public List<ASTNode> children() {
        return Arrays.asList(heading, body);
    }

    @Override
    public List<Token> tokens() {
        return merge(
                heading.tokens(),
                Collections.singletonList(TokenFactory.createSemicolon()),
                body.tokens(),
                Collections.singletonList(Token.keyword(Tokens.END)),
                Collections.singletonList(TokenFactory.createSemicolon())
        );
    }
}
