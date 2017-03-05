package me.wbars.compiler.semantic.models;

import me.wbars.compiler.generator.JvmBytecodeGenerator;
import me.wbars.compiler.parser.models.Tokens;
import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;

public class LiteralNode extends ASTNode {

    public LiteralNode(String name, Type type) {
        super(name);
        this.type = type;
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
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ASTNode> children() {
        return Collections.emptyList();
    }

    @Override
    public List<Token> tokens() {
        if (type == TypeRegistry.STRING) return singletonList(Token.create(Tokens.STRING_VAR, value));
        if (type == TypeRegistry.INTEGER) return singletonList(Token.create(Tokens.UNSIGNED_INTEGER, value));
        return singletonList(Token.create(type.name().toUpperCase(), value)); //todo workaround
    }
}
