package me.wbars.compiler.semantic.models;

import me.wbars.compiler.generator.JvmBytecodeGenerator;
import me.wbars.compiler.parser.models.Tokens;
import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.semantic.models.types.Type;

import java.util.List;

import static java.util.Collections.singletonList;

public class IdentifierNode extends LiteralNode {
    public IdentifierNode(String name, Type type) {
        super(name, type);
    }

    @Override
    public int generateCode(JvmBytecodeGenerator codeGenerator) {
        return codeGenerator.generate(this);
    }


    @Override
    public List<Token> tokens() {
        return singletonList(Token.create(Tokens.IDENTIFIER, value));
    }
}
