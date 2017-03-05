package me.wbars.compiler.semantic.models;

import me.wbars.compiler.parser.models.Tokens;
import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.scanner.models.TokenFactory;

import java.util.Collections;
import java.util.List;

import static me.wbars.compiler.utils.CollectionsUtils.merge;

public class GetFieldNode extends UnaryOpNode {
    public GetFieldNode(String fieldName) {
        super(fieldName);
    }

    @Override
    public List<Token> tokens() {
        return merge(
                Collections.singletonList(TokenFactory.createDot()),
                Collections.singletonList(Token.create(Tokens.IDENTIFIER, value)),
                getTarget().tokens()
        );
    }
}
