package me.wbars.compiler.semantic.models;

import me.wbars.compiler.generator.JvmBytecodeGenerator;
import me.wbars.compiler.parser.models.Tokens;
import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.scanner.models.TokenFactory;

import java.util.Collections;
import java.util.List;

import static me.wbars.compiler.utils.CollectionsUtils.merge;

public class BinaryArithmeticOpNode extends BinaryOpNode {
    public BinaryArithmeticOpNode(String value, ASTNode left, ASTNode right) {
        super(value, left, right);
    }

    public BinaryArithmeticOpNode(String value) {
        this(value, null, null);
    }

    @Override
    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        return codeGenerator.generate(this);
    }

    @Override
    public List<Token> tokens() {
        return merge(
                tryParens(left),
                Collections.singletonList(Token.create(Tokens.SIGN, value)),
                tryParens(right)
        );
    }

    private List<Token> tryParens(ASTNode node) {
        if (isWithParens()) return merge(
                Collections.singletonList(TokenFactory.openParen()),
                node.tokens(),
                Collections.singletonList(TokenFactory.closeParen()));
        return node.tokens();
    }
}
