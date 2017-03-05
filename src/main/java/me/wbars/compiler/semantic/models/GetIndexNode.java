package me.wbars.compiler.semantic.models;

import me.wbars.compiler.generator.JvmBytecodeGenerator;
import me.wbars.compiler.scanner.models.Token;
import me.wbars.compiler.scanner.models.TokenFactory;
import me.wbars.compiler.semantic.models.types.Type;
import me.wbars.compiler.semantic.models.types.TypeRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.wbars.compiler.utils.CollectionsUtils.merge;

public class GetIndexNode extends UnaryOpNode {
    private final List<ASTNode> indexes = new ArrayList<>();

    public GetIndexNode() {
        super("");
    }

    public void addIndex(ASTNode index) {
        indexes.add(index);
    }

    public List<ASTNode> getIndexes() {
        return indexes;
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
    public List<ASTNode> children() {
        return indexes;
    }

    @Override
    public List<Token> tokens() {
        return merge(
                getTarget().tokens(),
                Collections.singletonList(TokenFactory.openBracket()),
                nestedTokens(indexes, TokenFactory::comma),
                Collections.singletonList(TokenFactory.closeBracket())
        );
    }

    @Override
    protected void replaceChild(int index, ASTNode node) {
        indexes.set(index, node);
    }
}
