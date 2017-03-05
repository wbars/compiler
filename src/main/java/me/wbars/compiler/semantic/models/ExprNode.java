package me.wbars.compiler.semantic.models;

import me.wbars.compiler.generator.JvmBytecodeGenerator;
import me.wbars.compiler.parser.models.Tokens;
import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.scanner.models.TokenFactory;

import java.util.Collections;
import java.util.List;

import static me.wbars.compiler.utils.CollectionsUtils.merge;

public class ExprNode extends BinaryOpNode {
    public ExprNode(String value, ASTNode left, ASTNode right) {
        super(value, left, right);
    }

    public ExprNode() {
        this(null, null, null);
    }

    @Override
    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        return codeGenerator.generate(this);
    }

    @Override
    public List<Token> tokens() {
        return tryParens(
                right != null ? merge(
                        left.tokens(),
                        Collections.singletonList(Token.create(Tokens.RELOP, value)),
                        right.tokens()
                ) : left.tokens()
        );
    }

    private List<Token> tryParens(List<Token> tokens) {
        if (isWithParens()) return merge(
                Collections.singletonList(TokenFactory.openParen()),
                tokens,
                Collections.singletonList(TokenFactory.closeParen()));
        return tokens;
    }
}
