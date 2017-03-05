package me.wbars.compiler.semantic.models;

import me.wbars.compiler.parser.models.Tokens;
import me.wbars.compiler.scanner.models.Token;

import java.util.List;

public class GetPointerNode extends UnaryOpNode {

    public GetPointerNode() {
        super(Tokens.UPARROW);
    }

    @Override
    public List<Token> tokens() {
        throw new UnsupportedOperationException();
    }
}
